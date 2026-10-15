const ASSETS = new Set(["BTC", "ETH", "SOL", "BNB"]);
const PORTFOLIO_ASSETS = ["BTC", "ETH", "SOL", "BNB", "USDT", "USDC"];
const ASSET_SCALES = {
  BTC: 100_000_000,
  ETH: 100_000_000,
  SOL: 1_000_000_000,
  BNB: 100_000_000,
  USDT: 1_000_000,
  USDC: 1_000_000,
};
const MAX_TRADE_USD_MINOR_UNITS = 100_000_000;
const MAX_QUOTE_AGE_MS = 30_000;

function validateIdempotencyKey(key) {
  if (typeof key !== "string" || !/^[A-Za-z0-9_-]{16,128}$/.test(key)) {
    throw new Error("A valid idempotency key is required");
  }
  return key;
}

function requestFingerprint(data) {
  return JSON.stringify([
    data.asset,
    data.pair,
    data.direction,
    data.amount,
    data.amountDenomination,
    data.quotePriceUsd,
    data.limitPriceUsd,
    data.quoteObservedAtMs,
  ]);
}

function walletOperationFingerprint(data) {
  return JSON.stringify([data.operation, data.currency, data.amountMinorUnits]);
}

function validateTradeRequestFields(data) {
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    throw new Error("Invalid trade request");
  }
  const allowedFields = new Set([
    "idempotencyKey",
    "asset",
    "pair",
    "direction",
    "amount",
    "amountDenomination",
    "quotePriceUsd",
    "limitPriceUsd",
    "quoteObservedAtMs",
  ]);
  if (Object.keys(data).some((field) => !allowedFields.has(field))) {
    throw new Error("Trade request contains unsupported fields");
  }
  validateIdempotencyKey(data.idempotencyKey);
  return data;
}

function validateTradeRequest(data, now = Date.now(), minimumOrderMinorUnits = 100, serverPriceUsd = null) {
  validateTradeRequestFields(data);
  if (typeof data.asset !== "string" || !ASSETS.has(data.asset)) throw new Error("Unsupported asset");
  if (data.pair !== `${data.asset}/USDT`) throw new Error("Unsupported trading pair");
  if (data.direction !== "BUY" && data.direction !== "SELL") throw new Error("Unsupported trade direction");
  const expectedDenomination = data.direction === "BUY" ? "USD" : "ASSET";
  if (data.amountDenomination !== expectedDenomination) {
    throw new Error(`Trade amount must be denominated in ${expectedDenomination}`);
  }
  if (!Number.isFinite(data.amount) || data.amount <= 0) throw new Error("Trade amount must be positive");
  if (data.direction === "BUY") {
    if (data.amount > MAX_TRADE_USD_MINOR_UNITS / 100) throw new Error("Trade amount exceeds the supported limit");
    if (Math.abs(data.amount * 100 - Math.round(data.amount * 100)) > 1e-8) {
      throw new Error("Buy amount must be in whole cents");
    }
    if (Math.round(data.amount * 100) < minimumOrderMinorUnits) {
      throw new Error(`Minimum paper order is ${minimumOrderMinorUnits} cents`);
    }
  } else {
    const quantityAtomic = Math.floor(data.amount * ASSET_SCALES[data.asset]);
    if (!Number.isSafeInteger(quantityAtomic) || quantityAtomic <= 0) {
      throw new Error(`Sell quantity is below ${data.asset} precision`);
    }
  }
  if (!Number.isFinite(data.quotePriceUsd) || data.quotePriceUsd <= 0 ||
      !Number.isFinite(data.limitPriceUsd) || data.limitPriceUsd <= 0) {
    throw new Error("A valid quote and limit price are required");
  }
  if (!Number.isSafeInteger(data.quoteObservedAtMs) ||
      data.quoteObservedAtMs > now + 5_000 || now - data.quoteObservedAtMs > MAX_QUOTE_AGE_MS) {
    throw new Error("The market quote is stale");
  }
  const deviationBps = Math.abs(data.limitPriceUsd - data.quotePriceUsd) / data.quotePriceUsd * 10_000;
  if (deviationBps > 500) throw new Error("The limit price exceeds the allowed slippage");
  if (serverPriceUsd != null) {
    if (!Number.isFinite(serverPriceUsd) || serverPriceUsd <= 0) throw new Error("Invalid server market quote");
    const quoteDeviationBps = Math.abs(data.quotePriceUsd - serverPriceUsd) / serverPriceUsd * 10_000;
    const limitDeviationBps = Math.abs(data.limitPriceUsd - serverPriceUsd) / serverPriceUsd * 10_000;
    if (quoteDeviationBps > 150 || limitDeviationBps > 150) {
      throw new Error("Market quote changed beyond the allowed paper-trade slippage");
    }
  }
  return data;
}

function authenticatedUid(auth) {
  if (!auth || typeof auth.uid !== "string" || auth.uid.length === 0) {
    const error = new Error("Authentication is required");
    error.code = "unauthenticated";
    throw error;
  }
  return auth.uid;
}

function isEligibleTestAccount(auth) {
  return auth?.testAccount === true;
}

function getInitialTestFunding(auth, existingWallet, configuredBalances) {
  const eligibleForTestFunding = isEligibleTestAccount(auth);
  const grantAlreadyApplied = Boolean(existingWallet?.testFundingGrantAppliedAt);
  const hasExistingCash = existingWallet != null &&
    ((existingWallet.usdBalanceMinorUnits || 0) > 0 ||
      (existingWallet.kesBalanceMinorUnits || 0) > 0);
  const applyGrant = eligibleForTestFunding && !grantAlreadyApplied && !hasExistingCash;
  return {
    eligibleForTestFunding,
    applyGrant,
    markGrantApplied: !grantAlreadyApplied && (eligibleForTestFunding || hasExistingCash),
    usdMinorUnits: applyGrant ? configuredBalances.usdMinorUnits : 0,
    kesMinorUnits: applyGrant ? configuredBalances.kesMinorUnits : 0,
  };
}

function createPortfolio(mode, bitcoinWalletId) {
  return {
    bitcoinWalletId,
    mode,
    assetBalancesAtomicUnits: Object.fromEntries(PORTFOLIO_ASSETS.map((asset) => [asset, 0])),
  };
}

function createMainWallet({
  initialUsdBalanceMinorUnits = 0,
  initialKesBalanceMinorUnits = 0,
  eligibleForTestFunding = true,
  now = new Date(),
} = {}) {
  return {
    usdBalanceMinorUnits: eligibleForTestFunding ? initialUsdBalanceMinorUnits : 0,
    kesBalanceMinorUnits: eligibleForTestFunding ? initialKesBalanceMinorUnits : 0,
    testDepositedUsdMinorUnits: 0,
    testDepositedKesMinorUnits: 0,
    mode: "testing",
    testFundingGrantAppliedAt: eligibleForTestFunding ? now : null,
  };
}

function hasValidWallets(mainWallet, cryptoBalances) {
  return mainWallet?.mode === "testing"
    && Number.isSafeInteger(mainWallet.usdBalanceMinorUnits)
    && mainWallet.usdBalanceMinorUnits >= 0
    && Number.isSafeInteger(mainWallet.kesBalanceMinorUnits)
    && mainWallet.kesBalanceMinorUnits >= 0
    && PORTFOLIO_ASSETS.every((asset) => Number.isSafeInteger(cryptoBalances?.[asset])
      && cryptoBalances[asset] >= 0);
}

function calculateFeeMinorUnits(amountMinorUnits, feeBps) {
  if (!Number.isSafeInteger(amountMinorUnits) || amountMinorUnits < 0 ||
      !Number.isSafeInteger(feeBps) || feeBps < 0 || feeBps > 10_000) {
    throw new Error("Invalid test trade fee");
  }
  return Math.ceil(amountMinorUnits * feeBps / 10_000);
}

function executePaperTrade(mainWallet, cryptoBalances, request, executionPriceUsd, {
  feeBps = 10,
  minimumOrderMinorUnits = 100,
} = {}) {
  if (!hasValidWallets(mainWallet, cryptoBalances)) {
    throw new Error("The test wallets are not valid for paper execution");
  }
  if (!Number.isFinite(executionPriceUsd) || executionPriceUsd <= 0) {
    throw new Error("Invalid server market quote");
  }

  const priceWithinLimit = request.direction === "BUY"
    ? executionPriceUsd <= request.limitPriceUsd
    : executionPriceUsd >= request.limitPriceUsd;
  const quoteDeviationBps = Math.abs(executionPriceUsd - request.quotePriceUsd) /
    request.quotePriceUsd * 10_000;
  if (!priceWithinLimit || quoteDeviationBps > 500) {
    return { status: "REJECTED", message: "The Binance market price moved beyond your order limit" };
  }

  const scale = ASSET_SCALES[request.asset];
  const quantityAtomicUnits = request.direction === "BUY"
    ? Math.floor(request.amount / executionPriceUsd * scale)
    : Math.floor(request.amount * scale);
  if (!Number.isSafeInteger(quantityAtomicUnits) || quantityAtomicUnits <= 0) {
    return { status: "REJECTED", message: "Order quantity is below supported asset precision" };
  }

  const grossMinorUnits = request.direction === "BUY"
    ? Math.round(request.amount * 100)
    : Math.round(quantityAtomicUnits / scale * executionPriceUsd * 100);
  if (!Number.isSafeInteger(grossMinorUnits) || grossMinorUnits < minimumOrderMinorUnits) {
    return { status: "REJECTED", message: "Order is below the configured minimum value" };
  }
  if (grossMinorUnits <= 0) {
    return { status: "REJECTED", message: "Order value exceeds supported test ledger precision" };
  }
  const feeMinorUnits = calculateFeeMinorUnits(grossMinorUnits, feeBps);
  const balances = { ...cryptoBalances };
  const nextWallet = { ...mainWallet };
  if (request.direction === "BUY") {
    const totalDebitMinorUnits = grossMinorUnits + feeMinorUnits;
    if (totalDebitMinorUnits > mainWallet.usdBalanceMinorUnits) {
      return { status: "REJECTED", message: "Insufficient TEST USD funds, including the simulated fee" };
    }
    if (!Number.isSafeInteger(totalDebitMinorUnits) ||
        !Number.isSafeInteger(mainWallet.usdBalanceMinorUnits - totalDebitMinorUnits) ||
        !Number.isSafeInteger(cryptoBalances[request.asset] + quantityAtomicUnits)) {
      return { status: "REJECTED", message: "Order exceeds supported test ledger precision" };
    }
    nextWallet.usdBalanceMinorUnits -= totalDebitMinorUnits;
    balances[request.asset] += quantityAtomicUnits;
  } else {
    if (quantityAtomicUnits > cryptoBalances[request.asset]) {
      return { status: "REJECTED", message: `Insufficient ${request.asset} paper holdings` };
    }
    const netCreditMinorUnits = grossMinorUnits - feeMinorUnits;
    if (netCreditMinorUnits < 0) return { status: "REJECTED", message: "Fee exceeds sale proceeds" };
    if (!Number.isSafeInteger(mainWallet.usdBalanceMinorUnits + netCreditMinorUnits)) {
      return { status: "REJECTED", message: "Sale exceeds supported test cash precision" };
    }
    balances[request.asset] -= quantityAtomicUnits;
    nextWallet.usdBalanceMinorUnits += netCreditMinorUnits;
  }

  return {
    status: "FILLED_TEST",
    message: "PAPER MONEY transaction recorded. No exchange order or on-chain transfer occurred.",
    mainWallet: nextWallet,
    cryptoBalances: balances,
    grossMinorUnits,
    feeMinorUnits,
    netMinorUnits: request.direction === "BUY"
      ? grossMinorUnits + feeMinorUnits
      : grossMinorUnits - feeMinorUnits,
    quantityAtomicUnits,
    executionPriceUsd,
  };
}

function applyTestWalletOperation(mainWallet, operation, currency, amountMinorUnits, {
  depositLimitMinorUnits = 0,
} = {}) {
  if (mainWallet?.mode !== "testing") throw new Error("Test wallet is not enabled");
  if (!["DEPOSIT", "WITHDRAWAL"].includes(operation)) throw new Error("Unsupported test wallet operation");
  if (!["USD", "KES"].includes(currency)) throw new Error("Unsupported test wallet currency");
  if (!Number.isSafeInteger(amountMinorUnits) || amountMinorUnits <= 0) {
    throw new Error("Amount must be a positive whole minor-unit value");
  }

  const balanceKey = currency === "USD" ? "usdBalanceMinorUnits" : "kesBalanceMinorUnits";
  const depositKey = currency === "USD" ? "testDepositedUsdMinorUnits" : "testDepositedKesMinorUnits";
  const balance = mainWallet[balanceKey];
  if (!Number.isSafeInteger(balance) || balance < 0) {
    throw new Error(`TEST ${currency} balance is invalid`);
  }
  const next = { ...mainWallet };
  if (operation === "DEPOSIT") {
    const deposited = mainWallet[depositKey] || 0;
    if (!Number.isSafeInteger(deposited) || deposited < 0 ||
        deposited + amountMinorUnits > depositLimitMinorUnits) {
      return { status: "REJECTED", message: "Configured cumulative TEST deposit limit reached" };
    }
    if (!Number.isSafeInteger(balance + amountMinorUnits)) {
      return { status: "REJECTED", message: "TEST balance exceeds supported ledger precision" };
    }
    next[balanceKey] += amountMinorUnits;
    next[depositKey] = deposited + amountMinorUnits;
  } else {
    if (amountMinorUnits > balance) {
      return { status: "REJECTED", message: `Insufficient TEST ${currency} balance` };
    }
    next[balanceKey] -= amountMinorUnits;
  }
  return {
    status: "COMPLETED_TEST",
    message: "PAPER MONEY transaction recorded. No payment or payout was sent.",
    mainWallet: next,
  };
}

function applyIdempotentTestWalletOperation(mainWallet, data, existingRequest, options) {
  const fingerprint = walletOperationFingerprint(data);
  if (existingRequest) {
    if (existingRequest.requestFingerprint !== fingerprint) {
      throw new Error("This idempotency key was already used for another wallet operation");
    }
    return { ...existingRequest, mainWallet, duplicate: true };
  }
  return {
    ...applyTestWalletOperation(mainWallet, data.operation, data.currency, data.amountMinorUnits, options),
    requestFingerprint: fingerprint,
    duplicate: false,
  };
}

function applyIdempotentPaperTrade(mainWallet, cryptoBalances, request, executionPriceUsd, existingRequest, options) {
  const fingerprint = requestFingerprint(request);
  if (existingRequest) {
    if (existingRequest.requestFingerprint !== fingerprint) {
      throw new Error("This idempotency key was already used for another trade");
    }
    return { ...existingRequest, mainWallet, cryptoBalances, duplicate: true };
  }
  const outcome = executePaperTrade(mainWallet, cryptoBalances, request, executionPriceUsd, options);
  return { ...outcome, requestFingerprint: fingerprint, duplicate: false };
}

module.exports = {
  ASSETS,
  ASSET_SCALES,
  PORTFOLIO_ASSETS,
  applyTestWalletOperation,
  applyIdempotentTestWalletOperation,
  applyIdempotentPaperTrade,
  authenticatedUid,
  calculateFeeMinorUnits,
  createPortfolio,
  createMainWallet,
  executePaperTrade,
  getInitialTestFunding,
  hasValidWallets,
  isEligibleTestAccount,
  requestFingerprint,
  walletOperationFingerprint,
  validateIdempotencyKey,
  validateTradeRequest,
  validateTradeRequestFields,
};
