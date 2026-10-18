const { createHash, randomBytes } = require("node:crypto");
const admin = require("firebase-admin");
const { getAuth } = require("firebase-admin/auth");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
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
const {
  createPendingFinancialRecord,
  financialRequestFingerprint,
  validateFinancialRequest,
} = require("./financial-domain");

admin.initializeApp();
const db = getFirestore();
const assetDecimals = {
  BTC: 8, ETH: 8, SOL: 9, BNB: 8, XRP: 6, ADA: 6, DOGE: 6, LTC: 8, USDT: 6, USDC: 6,
};

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

function financialRequestRef(uid, idempotencyKey) {
  return db.collection("users").doc(uid).collection("financialRequests").doc(idempotencyKey);
}

function financialTransactionRef(uid, idempotencyKey) {
  return mainWalletRef(uid).collection("transactions").doc(idempotencyKey);
}

function financialRequestReceiptRef(providerReference) {
  const key = createHash("sha256").update(providerReference).digest("hex");
  return db.collection("paybillReceipts").doc(key);
}

async function resolveInternalRecipient(recipientId, senderUid) {
  try {
    let recipient;
    const value = recipientId.trim().replace(/^#/, "");
    if (value.startsWith("NV-")) {
      recipient = await getAuth().getUser(value.slice(3));
    } else if (value.includes("@")) {
      recipient = await getAuth().getUserByEmail(value);
    } else if (value.startsWith("+")) {
      recipient = await getAuth().getUserByPhoneNumber(value);
    } else if (/^0\d{9}$/.test(value)) {
      recipient = await getAuth().getUserByPhoneNumber(`+254${value.slice(1)}`);
    } else if (/^254\d{9}$/.test(value)) {
      recipient = await getAuth().getUserByPhoneNumber(`+${value}`);
    } else {
      recipient = await getAuth().getUser(value);
    }
    if (!recipient.uid || recipient.uid === senderUid) {
      throw new Error("self transfer");
    }
    return recipient.uid;
  } catch (_) {
    throw new HttpsError("invalid-argument", "The internal transfer recipient could not be verified.");
  }
}

exports.lookupTransferRecipient = onCall(async (call) => {
  const senderUid = requireAuth(call);
  const identifier = typeof call.data?.identifier === "string"
    ? call.data.identifier.trim().replace(/^#/, "")
    : "";
  const method = call.data?.method;
  const targetWallet = call.data?.targetWallet;
  if (!identifier || identifier.length > 256 ||
      !["EMAIL", "WALLET_CODE", "PHONE_KYC"].includes(method) ||
      !["USER_WALLET", "BITCOIN_WALLET"].includes(targetWallet)) {
    throw new HttpsError("invalid-argument", "Enter a supported recipient and wallet type.");
  }

  try {
    let recipient;
    if (method === "EMAIL") {
      recipient = await getAuth().getUserByEmail(identifier);
    } else if (method === "PHONE_KYC") {
      const phoneNumber = identifier.startsWith("+")
        ? identifier
        : /^0\d{9}$/.test(identifier)
          ? `+254${identifier.slice(1)}`
          : /^254\d{9}$/.test(identifier)
            ? `+${identifier}`
            : "";
      if (!phoneNumber) {
        return {
          exists: false,
          walletAvailable: false,
          message: "Enter a valid phone number",
        };
      }
      recipient = await getAuth().getUserByPhoneNumber(phoneNumber);
    } else {
      const isNovaWalletCode = identifier.toUpperCase().startsWith("NV-");
      if (isNovaWalletCode) {
        recipient = await getAuth().getUser(identifier.slice(3));
      } else {
        const matches = await db.collectionGroup("cryptoWallets")
          .where("bitcoinWalletId", "==", identifier)
          .limit(2)
          .get();
        const match = matches.docs.find((document) => document.id === "spot");
        if (!match) {
          return {
            exists: false,
            walletAvailable: false,
            message: "Recipient wallet was not found",
          };
        }
        const ownerUid = match.ref.parent.parent?.id;
        if (!ownerUid) throw new HttpsError("internal", "The wallet record has no owner.");
        recipient = await getAuth().getUser(ownerUid);
      }
    }
    if (recipient.uid === senderUid) {
      return {
        exists: false,
        walletAvailable: false,
        message: "You cannot send money to your own wallet",
      };
    }
    const destinationRef = targetWallet === "USER_WALLET"
      ? mainWalletRef(recipient.uid)
      : portfolioRef(recipient.uid);
    const wallet = await destinationRef.get();
    return {
      exists: wallet.exists,
      walletAvailable: wallet.exists,
      message: wallet.exists ? "Recipient wallet found" : "Recipient does not have this wallet",
    };
  } catch (error) {
    if (["auth/user-not-found", "auth/invalid-email", "auth/invalid-phone-number"].includes(error.code)) {
      return {
        exists: false,
        walletAvailable: false,
        message: "Recipient or requested wallet was not found",
      };
    }
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("unavailable", "Recipient verification is currently unavailable.");
  }
});

function publicPortfolio(data, holdings) {
  return {
    bitcoinWalletId: data.bitcoinWalletId,
    marketingWalletId: data.marketingWalletId,
    tradingWalletId: data.tradingWalletId,
    marketingBalanceMinorUnits: data.marketingBalanceMinorUnits || 0,
    tradingBalanceMinorUnits: data.tradingBalanceMinorUnits || 0,
    mode: data.mode,
    paperTradeFeeBps: data.paperTradeFeeBps ?? config.tradeFeeBps,
    minimumTradeUsdMinorUnits: data.minimumTradeUsdMinorUnits ?? config.minimumTradeUsdMinorUnits,
    assetBalancesAtomicUnits: holdings,
  };
}

function storedPortfolio(data) {
  return {
    bitcoinWalletId: data.bitcoinWalletId,
    marketingWalletId: data.marketingWalletId,
    tradingWalletId: data.tradingWalletId,
    marketingBalanceMinorUnits: data.marketingBalanceMinorUnits || 0,
    tradingBalanceMinorUnits: data.tradingBalanceMinorUnits || 0,
    mode: data.mode,
    paperTradeFeeBps: config.tradeFeeBps,
    minimumTradeUsdMinorUnits: config.minimumTradeUsdMinorUnits,
    assetSymbols: PORTFOLIO_ASSETS,
    createdAt: FieldValue.serverTimestamp(),
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
  const id = `BTC-${uid}-${randomBytes(16).toString("hex").toUpperCase()}`;
  const marketingWalletId = `MKT-${uid}`;
  const tradingWalletId = `TRD-${uid}`;
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
      : {
        ...createPortfolio("paper", id),
        marketingWalletId,
        tradingWalletId,
        marketingBalanceMinorUnits: 0,
        tradingBalanceMinorUnits: 0,
      };
    if (!existingPortfolio.exists) transaction.create(ref, storedPortfolio(portfolio));
    else {
      transaction.update(ref, {
        mode: "paper",
        paperTradeFeeBps: config.tradeFeeBps,
        minimumTradeUsdMinorUnits: config.minimumTradeUsdMinorUnits,
        marketingWalletId: portfolio.marketingWalletId || marketingWalletId,
        tradingWalletId: portfolio.tradingWalletId || tradingWalletId,
        marketingBalanceMinorUnits: portfolio.marketingBalanceMinorUnits || 0,
        tradingBalanceMinorUnits: portfolio.tradingBalanceMinorUnits || 0,
      });
      portfolio.mode = "paper";
      portfolio.paperTradeFeeBps = config.tradeFeeBps;
      portfolio.minimumTradeUsdMinorUnits = config.minimumTradeUsdMinorUnits;
      portfolio.marketingWalletId = portfolio.marketingWalletId || marketingWalletId;
      portfolio.tradingWalletId = portfolio.tradingWalletId || tradingWalletId;
      portfolio.marketingBalanceMinorUnits = portfolio.marketingBalanceMinorUnits || 0;
      portfolio.tradingBalanceMinorUnits = portfolio.tradingBalanceMinorUnits || 0;
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
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
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
          updatedAt: FieldValue.serverTimestamp(),
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
          createdAt: FieldValue.serverTimestamp(),
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
          createdAt: FieldValue.serverTimestamp(),
          updatedAt: FieldValue.serverTimestamp(),
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
      createdAt: FieldValue.serverTimestamp(),
    };
    if (outcome.status === "COMPLETED_TEST") {
      transaction.update(mainRef, {
        ...outcome.mainWallet,
        updatedAt: FieldValue.serverTimestamp(),
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

exports.createFinancialRequest = onCall(async (call) => {
  const uid = requireAuth(call);
  let request;
  try {
    request = validateFinancialRequest(call.data);
  } catch (error) {
    throw new HttpsError("invalid-argument", error.message);
  }
  if (request.type === "TRANSFER") {
    request.recipientId = await resolveInternalRecipient(request.recipientId, uid);
  }
  const receiptRef = request.method === "PAYBILL"
    ? financialRequestReceiptRef(request.providerReference)
    : null;
  const fingerprint = financialRequestFingerprint(request);
  const requestRef = financialRequestRef(uid, request.idempotencyKey);
  const transactionRef = financialTransactionRef(uid, request.idempotencyKey);
  const result = await db.runTransaction(async (transaction) => {
    const reads = [
      transaction.get(requestRef),
      transaction.get(transactionRef),
    ];
    if (receiptRef) reads.push(transaction.get(receiptRef));
    const [existingRequest, existingTransaction, receiptSnapshot] = await Promise.all(reads);
    if (existingRequest.exists || existingTransaction.exists) {
      const existing = existingRequest.exists
        ? existingRequest.data()
        : existingTransaction.data();
      if (existing.requestFingerprint !== fingerprint) {
        throw new HttpsError(
          "already-exists",
          "This idempotency key was already used for another financial request."
        );
      }
      return {
        ...existing,
        createdAtMs: existing.createdAt?.toMillis?.() ?? Date.now(),
        duplicate: true,
      };
    }
    if (receiptSnapshot?.exists) {
      throw new HttpsError("already-exists", "This M-Pesa transaction code has already been submitted.");
    }

    const createdAt = FieldValue.serverTimestamp();
    const record = {
      ...createPendingFinancialRecord(uid, request, createdAt),
      requestFingerprint: fingerprint,
    };
    transaction.create(requestRef, record);
    transaction.create(transactionRef, record);
    if (receiptRef) {
      transaction.create(receiptRef, {
        userId: uid,
        transactionId: request.idempotencyKey,
        providerReference: request.providerReference,
        createdAt,
      });
    }
    return {
      ...record,
      createdAtMs: Date.now(),
      duplicate: false,
    };
  });
  const { createdAt, updatedAt, ...response } = result;
  return { ...response, createdAtMs: result.createdAtMs || Date.now() };
});

exports.advancePaybillDepositStatus = onSchedule("every 1 minutes", async () => {
  const cutoff = Date.now() - 60_000;
  const submitted = await db.collectionGroup("transactions")
    .where("status", "==", "SUBMITTED")
    .limit(500)
    .get();

  for (const document of submitted.docs) {
    const createdAt = document.get("createdAt");
    if (document.get("type") !== "DEPOSIT" ||
        document.get("method") !== "PAYBILL" ||
        !createdAt?.toMillis ||
        createdAt.toMillis() > cutoff) {
      continue;
    }

    const userId = document.ref.parent.parent?.parent.parent?.id;
    if (!userId) continue;
    const requestRef = financialRequestRef(userId, document.id);
    await db.runTransaction(async (transaction) => {
      const [current, request] = await Promise.all([
        transaction.get(document.ref),
        transaction.get(requestRef),
      ]);
      if (!current.exists || current.get("status") !== "SUBMITTED") return;
      transaction.update(document.ref, {
        status: "PENDING",
        updatedAt: FieldValue.serverTimestamp(),
      });
      if (request.exists) {
        transaction.update(requestRef, {
          status: "PENDING",
          updatedAt: FieldValue.serverTimestamp(),
        });
      }
    });
  }
});

exports.approvePaybillDeposit = onCall(async (call) => {
  const uid = requireAuth(call);
  const role = call.auth.token.adminRole;
  if (!["SUPER_ADMIN", "ADMIN", "OPERATIONS"].includes(role)) {
    throw new HttpsError("permission-denied", "An authorized operations administrator is required.");
  }
  const userId = typeof call.data?.userId === "string" ? call.data.userId.trim() : "";
  const transactionId = typeof call.data?.transactionId === "string"
    ? call.data.transactionId.trim()
    : "";
  if (!userId || !transactionId) {
    throw new HttpsError("invalid-argument", "A user and deposit transaction are required.");
  }

  const walletRef = mainWalletRef(userId);
  const transactionRef = financialTransactionRef(userId, transactionId);
  const requestRef = financialRequestRef(userId, transactionId);
  const notificationRef = db.collection("users").doc(userId).collection("notifications").doc();
  const result = await db.runTransaction(async (transaction) => {
    const [deposit, wallet, request] = await Promise.all([
      transaction.get(transactionRef),
      transaction.get(walletRef),
      transaction.get(requestRef),
    ]);
    if (!deposit.exists ||
        deposit.get("userId") !== userId ||
        deposit.get("type") !== "DEPOSIT" ||
        deposit.get("method") !== "PAYBILL") {
      throw new HttpsError("not-found", "Paybill deposit request was not found.");
    }
    if (deposit.get("status") === "APPROVED") return { duplicate: true };
    if (deposit.get("status") !== "PENDING") {
      throw new HttpsError("failed-precondition", "The deposit must be pending before approval.");
    }
    const amountMinorUnits = deposit.get("amountMinorUnits");
    const currency = deposit.get("currency");
    const balanceField = currency === "KES" ? "kesBalanceMinorUnits" : "usdBalanceMinorUnits";
    if (!Number.isSafeInteger(amountMinorUnits) || amountMinorUnits <= 0 ||
        !["USD", "KES"].includes(currency)) {
      throw new HttpsError("failed-precondition", "Deposit has an invalid amount or currency.");
    }
    const currentBalance = wallet.exists ? wallet.get(balanceField) || 0 : 0;
    if (!Number.isSafeInteger(currentBalance) || currentBalance < 0) {
      throw new HttpsError("failed-precondition", "The destination wallet contains an invalid balance.");
    }
    const nextBalance = currentBalance + amountMinorUnits;
    if (!Number.isSafeInteger(nextBalance)) {
      throw new HttpsError("failed-precondition", "Wallet balance would exceed the supported limit.");
    }

    if (wallet.exists) {
      transaction.update(walletRef, {
        [balanceField]: nextBalance,
        updatedAt: FieldValue.serverTimestamp(),
      });
    } else {
      transaction.create(walletRef, {
        usdBalanceMinorUnits: currency === "USD" ? amountMinorUnits : 0,
        kesBalanceMinorUnits: currency === "KES" ? amountMinorUnits : 0,
        mode: "testing",
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      });
    }
    const approvedFields = {
      status: "APPROVED",
      approvedBy: uid,
      completedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    };
    transaction.update(transactionRef, approvedFields);
    if (request.exists) {
      transaction.update(requestRef, approvedFields);
    }
    transaction.create(notificationRef, {
      userId,
      title: "Paybill deposit approved",
      body: "Your M-Pesa Paybill deposit has been approved and added to your wallet.",
      category: "WALLET",
      timestamp: "Just now",
      isRead: false,
      deepLink: "transaction_history",
      createdAt: FieldValue.serverTimestamp(),
    });
    return { duplicate: false };
  });
  return { status: "APPROVED", duplicate: result.duplicate };
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
      createdAt: FieldValue.serverTimestamp(),
    };
    if (status === "FILLED_TEST") {
      transaction.update(mainRef, {
        ...outcome.mainWallet,
        updatedAt: FieldValue.serverTimestamp(),
      });
      transaction.update(holdingsRefs[paperRequest.asset], {
        quantityAtomicUnits: outcome.cryptoBalances[paperRequest.asset],
        updatedAt: FieldValue.serverTimestamp(),
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
        createdAt: FieldValue.serverTimestamp(),
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
