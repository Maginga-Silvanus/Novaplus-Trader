const { randomBytes } = require("node:crypto");
const admin = require("firebase-admin");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const {
  applyTestWalletOperation,
  authenticatedUid,
  createPortfolio,
  createMainWallet,
  executePaperTrade,
  getInitialTestFunding,
  isEligibleTestAccount,
  requestFingerprint,
  walletOperationFingerprint,
  validateIdempotencyKey,
  validateTradeRequest,
  validateTradeRequestFields,
  PORTFOLIO_ASSETS,
} = require("./trading-domain");

admin.initializeApp();
const db = admin.firestore();
const assetDecimals = { BTC: 8, ETH: 8, SOL: 9, BNB: 8, USDT: 6, USDC: 6 };

function configInteger(name, defaultValue, maximum = Number.MAX_SAFE_INTEGER) {
  const raw = process.env[name];
  if (raw == null || raw === "") return defaultValue;
  if (!/^\d+$/.test(raw)) throw new Error(`${name} must be a nonnegative integer`);
  const value = Number(raw);
  if (!Number.isSafeInteger(value) || value > maximum) throw new Error(`${name} is out of range`);
  return value;
}

const config = Object.freeze({
  initialUsdMinorUnits: configInteger("TEST_INITIAL_USD_MINOR_UNITS", 0),
  initialKesMinorUnits: configInteger("TEST_INITIAL_KES_MINOR_UNITS", 0),
  depositLimitUsdMinorUnits: configInteger("TEST_DEPOSIT_LIMIT_USD_MINOR_UNITS", 100_000),
  depositLimitKesMinorUnits: configInteger("TEST_DEPOSIT_LIMIT_KES_MINOR_UNITS", 10_000_000),
  tradeFeeBps: configInteger("TEST_TRADE_FEE_BPS", 10, 10_000),
  minimumTradeUsdMinorUnits: configInteger("TEST_MIN_TRADE_USD_MINOR_UNITS", 100),
});

function portfolioRef(uid) {
  return db.collection("users").doc(uid).collection("cryptoWallets").doc("spot");
}

function mainWalletRef(uid) {
  return db.collection("users").doc(uid).collection("wallets").doc("main");
}

function holdingRef(uid, asset) {
  return portfolioRef(uid).collection("holdings").doc(asset.toLowerCase());
}

function publicPortfolio(data, holdings) {
  return {
    bitcoinWalletId: data.bitcoinWalletId,
    mode: data.mode,
    paperTradeFeeBps: data.paperTradeFeeBps ?? config.tradeFeeBps,
    minimumTradeUsdMinorUnits: data.minimumTradeUsdMinorUnits ?? config.minimumTradeUsdMinorUnits,
    assetBalancesAtomicUnits: holdings,
  };
}

function storedPortfolio(data) {
  return {
    bitcoinWalletId: data.bitcoinWalletId,
    mode: data.mode,
    paperTradeFeeBps: config.tradeFeeBps,
    minimumTradeUsdMinorUnits: config.minimumTradeUsdMinorUnits,
    assetSymbols: PORTFOLIO_ASSETS,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

function publicMainWallet(data) {
  return {
    usdBalanceMinorUnits: data.usdBalanceMinorUnits,
    kesBalanceMinorUnits: data.kesBalanceMinorUnits,
    mode: data.mode,
  };
}

function requireAuth(request) {
  try {
    return authenticatedUid(request.auth);
  } catch (error) {
    throw new HttpsError("unauthenticated", "Sign in to access your wallet.");
  }
}

function validateWalletOperation(data) {
  if (!data || typeof data !== "object" || Array.isArray(data) ||
      Object.keys(data).some((key) =>
        !["idempotencyKey", "operation", "currency", "amountMinorUnits"].includes(key))) {
    throw new HttpsError("invalid-argument", "Invalid test wallet operation.");
  }
  try {
    validateIdempotencyKey(data.idempotencyKey);
  } catch (error) {
    throw new HttpsError("invalid-argument", error.message);
  }
  if (!["DEPOSIT", "WITHDRAWAL"].includes(data.operation) ||
      !["USD", "KES"].includes(data.currency) ||
      !Number.isSafeInteger(data.amountMinorUnits) || data.amountMinorUnits <= 0) {
    throw new HttpsError("invalid-argument", "Provide a positive USD or KES test wallet amount.");
  }
  return data;
}

async function fetchBinancePriceUsd(pair) {
  const symbol = pair.replace("/", "");
  let response;
  try {
    response = await fetch(`https://api.binance.com/api/v3/ticker/price?symbol=${encodeURIComponent(symbol)}`, {
      signal: AbortSignal.timeout(5_000),
    });
  } catch (error) {
    throw new HttpsError("unavailable", "Binance market quote is currently unavailable.");
  }
  if (!response.ok) throw new HttpsError("unavailable", "Binance did not return a market quote.");
  const body = await response.json();
  const price = Number(body.price);
  if (body.symbol !== symbol || !Number.isFinite(price) || price <= 0) {
    throw new HttpsError("failed-precondition", "Binance returned an invalid market quote.");
  }
  return price;
}

exports.getOrCreateCryptoPortfolio = onCall(async (request) => {
  const uid = requireAuth(request);
  const configuredBalances = {
    usdMinorUnits: config.initialUsdMinorUnits,
    kesMinorUnits: config.initialKesMinorUnits,
  };
  const ref = portfolioRef(uid);
  const mainRef = mainWalletRef(uid);
  const id = `BTC-${randomBytes(16).toString("hex").toUpperCase()}`;
  const result = await db.runTransaction(async (transaction) => {
    const holdingsRefs = PORTFOLIO_ASSETS.map((asset) => holdingRef(uid, asset));
    const snapshots = await Promise.all([
      transaction.get(ref),
      transaction.get(mainRef),
      ...holdingsRefs.map((holding) => transaction.get(holding)),
    ]);
    const existingPortfolio = snapshots[0];
    const existingMainWallet = snapshots[1];
    const portfolio = existingPortfolio.exists
      ? existingPortfolio.data()
      : createPortfolio("paper", id);
    if (!existingPortfolio.exists) transaction.create(ref, storedPortfolio(portfolio));
    else {
      transaction.update(ref, {
        mode: "paper",
        paperTradeFeeBps: config.tradeFeeBps,
        minimumTradeUsdMinorUnits: config.minimumTradeUsdMinorUnits,
      });
      portfolio.mode = "paper";
      portfolio.paperTradeFeeBps = config.tradeFeeBps;
      portfolio.minimumTradeUsdMinorUnits = config.minimumTradeUsdMinorUnits;
    }

    let mainWallet;
    let initialTestFunding = null;
    if (!existingMainWallet.exists) {
      const initialFunding = getInitialTestFunding(request.auth.token, null, configuredBalances);
      mainWallet = createMainWallet({
        initialUsdBalanceMinorUnits: initialFunding.usdMinorUnits,
        initialKesBalanceMinorUnits: initialFunding.kesMinorUnits,
        eligibleForTestFunding: initialFunding.eligibleForTestFunding,
      });
      if (initialFunding.applyGrant) {
        initialTestFunding = {
          usdMinorUnits: initialFunding.usdMinorUnits,
          kesMinorUnits: initialFunding.kesMinorUnits,
        };
      }
      transaction.create(mainRef, {
        ...mainWallet,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    } else {
      mainWallet = existingMainWallet.data();
      const initialFunding = getInitialTestFunding(
        request.auth.token,
        mainWallet,
        configuredBalances
      );
      const needsWalletUpdate = initialFunding.applyGrant ||
        initialFunding.markGrantApplied ||
        mainWallet.mode !== "testing" ||
        mainWallet.testDepositedUsdMinorUnits == null ||
        mainWallet.testDepositedKesMinorUnits == null;
      if (needsWalletUpdate) {
        mainWallet = {
          ...mainWallet,
          ...(initialFunding.applyGrant ? {
            usdBalanceMinorUnits: initialFunding.usdMinorUnits,
            kesBalanceMinorUnits: initialFunding.kesMinorUnits,
          } : {}),
          testDepositedUsdMinorUnits: mainWallet.testDepositedUsdMinorUnits || 0,
          testDepositedKesMinorUnits: mainWallet.testDepositedKesMinorUnits || 0,
          mode: "testing",
          ...(initialFunding.markGrantApplied ? { testFundingGrantAppliedAt: new Date() } : {}),
        };
        if (initialFunding.applyGrant) {
          initialTestFunding = {
            usdMinorUnits: initialFunding.usdMinorUnits,
            kesMinorUnits: initialFunding.kesMinorUnits,
          };
        }
        transaction.update(mainRef, {
          ...mainWallet,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    }
    for (const [currency, amountMinorUnits] of [
      ["USD", initialTestFunding?.usdMinorUnits || 0],
      ["KES", initialTestFunding?.kesMinorUnits || 0],
    ]) {
      if (amountMinorUnits > 0) {
        const grantRef = mainRef.collection("transactions").doc(`initial-test-grant-${currency.toLowerCase()}-v1`);
        transaction.set(grantRef, {
          operation: "DEPOSIT",
          currency,
          amountMinorUnits,
          status: "COMPLETED_TEST",
          testOnly: true,
          kind: "INITIAL_CONFIGURED_TEST_GRANT",
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    }

    const holdings = {};
    PORTFOLIO_ASSETS.forEach((asset, index) => {
      const existingHolding = snapshots[index + 2];
      holdings[asset] = existingHolding.exists
        ? existingHolding.get("quantityAtomicUnits")
        : 0;
      if (!existingHolding.exists) {
        transaction.create(holdingsRefs[index], {
          symbol: asset,
          quantityAtomicUnits: 0,
          decimals: assetDecimals[asset],
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    });
    return { portfolio, mainWallet, holdings };
  });
  return {
    cryptoWallet: publicPortfolio(result.portfolio, result.holdings),
    mainWallet: publicMainWallet(result.mainWallet),
  };
});

exports.simulateTestWalletOperation = onCall(async (call) => {
  const uid = requireAuth(call);
  if (!isEligibleTestAccount(call.auth.token)) {
    throw new HttpsError("permission-denied", "Simulated wallet funding is restricted to authorized test accounts.");
  }
  const data = validateWalletOperation(call.data);
  const fingerprint = walletOperationFingerprint(data);
  const mainRef = mainWalletRef(uid);
  const operationRef = db.collection("users").doc(uid).collection("walletOperations").doc(data.idempotencyKey);
  const transactionRef = mainRef.collection("transactions").doc(data.idempotencyKey);
  const result = await db.runTransaction(async (transaction) => {
    const [operationSnapshot, walletSnapshot] = await Promise.all([
      transaction.get(operationRef),
      transaction.get(mainRef),
    ]);
    if (operationSnapshot.exists) {
      const existing = operationSnapshot.data();
      if (existing.requestFingerprint !== fingerprint) {
        throw new HttpsError("already-exists", "This idempotency key was already used for another wallet operation.");
      }
      return { ...existing, duplicate: true };
    }
    if (!walletSnapshot.exists) throw new HttpsError("failed-precondition", "Initialize the test wallet before using it.");

    const wallet = walletSnapshot.data();
    const depositLimitMinorUnits = data.currency === "USD"
      ? config.depositLimitUsdMinorUnits
      : config.depositLimitKesMinorUnits;
    let outcome;
    try {
      outcome = applyTestWalletOperation(
        wallet,
        data.operation,
        data.currency,
        data.amountMinorUnits,
        { depositLimitMinorUnits }
      );
    } catch (error) {
      throw new HttpsError("failed-precondition", error.message);
    }
    const record = {
      idempotencyKey: data.idempotencyKey,
      requestFingerprint: fingerprint,
      operation: data.operation,
      currency: data.currency,
      amountMinorUnits: data.amountMinorUnits,
      status: outcome.status,
      message: outcome.message,
      testOnly: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };
    if (outcome.status === "COMPLETED_TEST") {
      transaction.update(mainRef, {
        ...outcome.mainWallet,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
    transaction.create(operationRef, record);
    transaction.create(transactionRef, record);
    return record;
  });
  return {
    status: result.status,
    message: result.message,
    duplicate: result.duplicate === true,
  };
});

exports.submitSpotTradeRequest = onCall(async (call) => {
  const uid = requireAuth(call);
  const data = call.data || {};
  try {
    validateTradeRequestFields(data);
  } catch (error) {
    throw new HttpsError("invalid-argument", error.message);
  }
  const fingerprint = requestFingerprint(data);
  const userRef = db.collection("users").doc(uid);
  const requestRef = userRef.collection("tradeRequests").doc(data.idempotencyKey);
  const existingRequest = await requestRef.get();
  if (existingRequest.exists) {
    const existing = existingRequest.data();
    if (existing.requestFingerprint !== fingerprint) {
      throw new HttpsError("already-exists", "This idempotency key was already used for a different order.");
    }
    return { status: existing.status, message: existing.message, duplicate: true };
  }

  let paperRequest;
  try {
    paperRequest = validateTradeRequest(data, Date.now(), config.minimumTradeUsdMinorUnits);
  } catch (error) {
    throw new HttpsError("invalid-argument", error.message);
  }
  const executionPriceUsd = await fetchBinancePriceUsd(paperRequest.pair);
  try {
    validateTradeRequest(data, Date.now(), config.minimumTradeUsdMinorUnits, executionPriceUsd);
  } catch (error) {
    throw new HttpsError("failed-precondition", error.message);
  }
  const mainRef = mainWalletRef(uid);
  const portfolio = portfolioRef(uid);
  const holdingsRefs = Object.fromEntries(
    PORTFOLIO_ASSETS.map((asset) => [asset, holdingRef(uid, asset)])
  );
  const transactionRef = portfolio.collection("transactions").doc(data.idempotencyKey);
  const createdAtMs = Date.now();

  const result = await db.runTransaction(async (transaction) => {
    const [requestSnapshot, mainSnapshot, portfolioSnapshot, ...holdingSnapshots] = await Promise.all([
      transaction.get(requestRef),
      transaction.get(mainRef),
      transaction.get(portfolio),
      ...PORTFOLIO_ASSETS.map((asset) => transaction.get(holdingsRefs[asset])),
    ]);
    if (requestSnapshot.exists) {
      const existing = requestSnapshot.data();
      if (existing.requestFingerprint !== fingerprint) {
        throw new HttpsError("already-exists", "This idempotency key was already used for a different order.");
      }
      return { ...existing, duplicate: true };
    }
    if (!mainSnapshot.exists || !portfolioSnapshot.exists || holdingSnapshots.some((snapshot) => !snapshot.exists)) {
      throw new HttpsError("failed-precondition", "Initialize both test wallets before trading.");
    }
    const mainWallet = mainSnapshot.data();
    const cryptoWallet = portfolioSnapshot.data();
    if (cryptoWallet.mode !== "paper") {
      throw new HttpsError("failed-precondition", "Paper crypto trading is not enabled for this wallet.");
    }
    const holdings = Object.fromEntries(PORTFOLIO_ASSETS.map((asset) => [asset, 0]));
    PORTFOLIO_ASSETS.forEach((asset, index) => {
      const quantity = holdingSnapshots[index].get("quantityAtomicUnits");
      if (!Number.isSafeInteger(quantity) || quantity < 0) {
        throw new HttpsError("failed-precondition", `${asset} holding contains an invalid quantity.`);
      }
      holdings[asset] = quantity;
    });
    const outcome = executePaperTrade(mainWallet, holdings, paperRequest, executionPriceUsd, {
      feeBps: config.tradeFeeBps,
      minimumOrderMinorUnits: config.minimumTradeUsdMinorUnits,
    });
    const status = outcome.status === "FILLED_TEST" ? "FILLED_TEST" : "REJECTED";
    const record = {
      idempotencyKey: data.idempotencyKey,
      requestFingerprint: fingerprint,
      asset: paperRequest.asset,
      pair: paperRequest.pair,
      direction: paperRequest.direction,
      amount: paperRequest.amount,
      amountDenomination: paperRequest.amountDenomination,
      quotePriceUsd: paperRequest.quotePriceUsd,
      executionPriceUsd,
      limitPriceUsd: paperRequest.limitPriceUsd,
      quantityAtomicUnits: outcome.quantityAtomicUnits || 0,
      grossAmountMinorUnits: outcome.grossMinorUnits || 0,
      feeMinorUnits: outcome.feeMinorUnits || 0,
      netAmountMinorUnits: outcome.netMinorUnits || 0,
      feeBps: config.tradeFeeBps,
      status,
      message: outcome.message,
      testOnly: true,
      priceSource: "Binance public spot ticker; USDT treated as USD for test valuation",
      createdAtMs,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };
    if (status === "FILLED_TEST") {
      transaction.update(mainRef, {
        ...outcome.mainWallet,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      transaction.update(holdingsRefs[paperRequest.asset], {
        quantityAtomicUnits: outcome.cryptoBalances[paperRequest.asset],
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      transaction.create(transactionRef, record);
      transaction.create(mainRef.collection("transactions").doc(`trade-${data.idempotencyKey}`), {
        operation: paperRequest.direction === "BUY" ? "CRYPTO_BUY" : "CRYPTO_SELL",
        currency: "USD",
        amountMinorUnits: outcome.netMinorUnits,
        grossAmountMinorUnits: outcome.grossMinorUnits,
        feeMinorUnits: outcome.feeMinorUnits,
        asset: paperRequest.asset,
        quantityAtomicUnits: outcome.quantityAtomicUnits,
        executionPriceUsd,
        status: "COMPLETED_TEST",
        testOnly: true,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
    transaction.create(requestRef, record);
    return record;
  });
  return {
    status: result.status,
    message: result.message,
    duplicate: result.duplicate === true,
    receipt: result.status === "FILLED_TEST" ? {
      idempotencyKey: result.idempotencyKey,
      asset: result.asset,
      direction: result.direction,
      quantityAtomicUnits: result.quantityAtomicUnits,
      executionPriceUsd: result.executionPriceUsd,
      grossAmountMinorUnits: result.grossAmountMinorUnits,
      feeMinorUnits: result.feeMinorUnits,
      netAmountMinorUnits: result.netAmountMinorUnits,
      createdAtMs: result.createdAtMs,
      status: result.status,
    } : null,
  };
});
