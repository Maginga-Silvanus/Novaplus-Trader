const test = require("node:test");
const assert = require("node:assert/strict");
const {
  applyIdempotentPaperTrade,
  applyIdempotentTestWalletOperation,
  authenticatedUid,
  calculateFeeMinorUnits,
  createMainWallet,
  createPortfolio,
  executePaperTrade,
  getInitialTestFunding,
  hasValidWallets,
  isEligibleTestAccount,
  requestFingerprint,
  validateTradeRequest,
} = require("../../trading-domain");

const now = Date.now();
const buyRequest = (idempotencyKey, overrides = {}) => ({
  idempotencyKey,
  asset: "BTC",
  pair: "BTC/USDT",
  direction: "BUY",
  amount: 100,
  amountDenomination: "USD",
  quotePriceUsd: 50_000,
  limitPriceUsd: 50_500,
  quoteObservedAtMs: now,
  ...overrides,
});
const sellRequest = (idempotencyKey, overrides = {}) => ({
  ...buyRequest(idempotencyKey),
  direction: "SELL",
  amount: 0.001,
  amountDenomination: "ASSET",
  limitPriceUsd: 49_500,
  ...overrides,
});
const mainWallet = (usdBalanceMinorUnits = 0, kesBalanceMinorUnits = 0) => ({
  ...createMainWallet(),
  usdBalanceMinorUnits,
  kesBalanceMinorUnits,
});
const emptyHoldings = () => createPortfolio("paper", "BTC-test-wallet").assetBalancesAtomicUnits;

test("rejects unauthenticated wallet requests", () => {
  assert.throws(() => authenticatedUid(null), { code: "unauthenticated" });
  assert.throws(() => authenticatedUid({ uid: "" }), { code: "unauthenticated" });
  assert.equal(authenticatedUid({ uid: "user-1" }), "user-1");
});

test("only an explicit Firebase testAccount claim qualifies for test funding", () => {
  assert.equal(isEligibleTestAccount(null), false);
  assert.equal(isEligibleTestAccount({ uid: "user-1" }), false);
  assert.equal(isEligibleTestAccount({ uid: "user-1", testAccount: "true" }), false);
  assert.equal(isEligibleTestAccount({ uid: "user-1", testAccount: true }), true);

  const unapprovedWallet = createMainWallet({
    initialUsdBalanceMinorUnits: 100_000,
    initialKesBalanceMinorUnits: 1_000_000,
    eligibleForTestFunding: false,
  });
  assert.equal(unapprovedWallet.usdBalanceMinorUnits, 0);
  assert.equal(unapprovedWallet.kesBalanceMinorUnits, 0);
  assert.equal(unapprovedWallet.testFundingGrantAppliedAt, null);
});

test("initial test funding grants once, preserves existing cash, and defers unapproved accounts", () => {
  const configured = { usdMinorUnits: 12_500, kesMinorUnits: 2_000_000 };
  const unapproved = getInitialTestFunding({ uid: "regular-user" }, null, configured);
  assert.equal(unapproved.applyGrant, false);
  assert.equal(unapproved.markGrantApplied, false);
  assert.equal(unapproved.usdMinorUnits, 0);
  assert.equal(unapproved.kesMinorUnits, 0);

  const approved = getInitialTestFunding({ uid: "test-user", testAccount: true }, null, configured);
  assert.equal(approved.applyGrant, true);
  assert.equal(approved.markGrantApplied, true);
  assert.equal(approved.usdMinorUnits, 12_500);
  assert.equal(approved.kesMinorUnits, 2_000_000);

  const existingCash = getInitialTestFunding(
    { uid: "test-user", testAccount: true },
    { usdBalanceMinorUnits: 1, kesBalanceMinorUnits: 0 },
    configured
  );
  assert.equal(existingCash.applyGrant, false);
  assert.equal(existingCash.markGrantApplied, true);

  const alreadyGranted = getInitialTestFunding(
    { uid: "test-user", testAccount: true },
    { usdBalanceMinorUnits: 0, kesBalanceMinorUnits: 0, testFundingGrantAppliedAt: new Date() },
    configured
  );
  assert.equal(alreadyGranted.applyGrant, false);
  assert.equal(alreadyGranted.markGrantApplied, false);
});

test("validates supported pairs, direction-specific denomination, amount and fresh quote", () => {
  assert.throws(() => validateTradeRequest(buyRequest("short")));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key", { asset: "DOGE" })));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key", { pair: "BTC/USD" })));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key", { amount: 0 })));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key", { amountDenomination: "BTC" })));
  assert.throws(() => validateTradeRequest(sellRequest("valid-idempotency-key", { amountDenomination: "USD" })));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key", { quoteObservedAtMs: now - 31_000 }), now));
  assert.throws(() => validateTradeRequest(buyRequest("valid-idempotency-key"), now, 100, 60_000));
  assert.doesNotThrow(() => validateTradeRequest(buyRequest("valid-idempotency-key"), now));
});

test("main and crypto wallets are distinct and start with zero balances", () => {
  const wallet = createMainWallet();
  const crypto = createPortfolio("paper", "BTC-test-wallet");
  assert.equal(wallet.usdBalanceMinorUnits, 0);
  assert.equal(wallet.kesBalanceMinorUnits, 0);
  assert.equal(wallet.mode, "testing");
  assert.equal(Object.hasOwn(crypto, "usdBalanceMinorUnits"), false);
  assert.ok(Object.values(crypto.assetBalancesAtomicUnits).every((quantity) => quantity === 0));
  assert.equal(hasValidWallets(wallet, crypto.assetBalancesAtomicUnits), true);
});

test("server can initialize configurable test balances only in the main wallet", () => {
  const wallet = createMainWallet({ initialUsdBalanceMinorUnits: 125_000, initialKesBalanceMinorUnits: 2_500_000 });
  const crypto = emptyHoldings();
  assert.equal(wallet.usdBalanceMinorUnits, 125_000);
  assert.equal(wallet.kesBalanceMinorUnits, 2_500_000);
  assert.ok(Object.values(crypto).every((quantity) => quantity === 0));
});

test("buy debits test USD including fee and credits the atomic crypto quantity", () => {
  const result = executePaperTrade(mainWallet(10_100), emptyHoldings(), buyRequest("purchase-request-key-001"), 50_000);
  assert.equal(result.status, "FILLED_TEST");
  assert.equal(result.grossMinorUnits, 10_000);
  assert.equal(result.feeMinorUnits, 10);
  assert.equal(result.mainWallet.usdBalanceMinorUnits, 90);
  assert.equal(result.cryptoBalances.BTC, 200_000);
  assert.match(result.message, /PAPER MONEY/);
});

test("sell debits holdings and credits test USD net of fee", () => {
  const holdings = { ...emptyHoldings(), BTC: 100_000 };
  const result = executePaperTrade(mainWallet(), holdings, sellRequest("sale-request-key-00001"), 50_000);
  assert.equal(result.status, "FILLED_TEST");
  assert.equal(result.quantityAtomicUnits, 100_000);
  assert.equal(result.grossMinorUnits, 5_000);
  assert.equal(result.feeMinorUnits, 5);
  assert.equal(result.mainWallet.usdBalanceMinorUnits, 4_995);
  assert.equal(result.cryptoBalances.BTC, 0);
});

test("buy then sell reconciles cash, holdings and cumulative fees", () => {
  const purchase = executePaperTrade(
    mainWallet(10_100),
    emptyHoldings(),
    buyRequest("reconcile-buy-request-0001"),
    50_000
  );
  const sale = executePaperTrade(
    purchase.mainWallet,
    purchase.cryptoBalances,
    sellRequest("reconcile-sell-request-01", { amount: 0.002 }),
    50_000
  );
  assert.equal(sale.cryptoBalances.BTC, 0);
  assert.equal(sale.mainWallet.usdBalanceMinorUnits, 10_080);
  assert.equal(10_100 - sale.mainWallet.usdBalanceMinorUnits, purchase.feeMinorUnits + sale.feeMinorUnits);
});

test("insufficient cash, holdings, stale limits and minimum values reject without mutations", () => {
  const holdings = emptyHoldings();
  const cash = mainWallet(9_999);
  const insufficient = executePaperTrade(cash, holdings, buyRequest("insufficient-order-key-01"), 50_000);
  assert.equal(insufficient.status, "REJECTED");
  assert.equal(cash.usdBalanceMinorUnits, 9_999);
  assert.equal(holdings.BTC, 0);

  const noCoins = executePaperTrade(mainWallet(), holdings, sellRequest("insufficient-sell-key-01"), 50_000);
  assert.equal(noCoins.status, "REJECTED");
  assert.equal(noCoins.mainWallet, undefined);
  assert.equal(holdings.BTC, 0);

  const movedPrice = executePaperTrade(mainWallet(50_000), holdings, buyRequest("moved-price-order-key-1"), 60_000);
  assert.equal(movedPrice.status, "REJECTED");
  assert.equal(movedPrice.mainWallet, undefined);

  assert.throws(() => validateTradeRequest(buyRequest("minimum-order-key-0001", { amount: 0.5 })));
});

test("fees use integer minor units and round up", () => {
  assert.equal(calculateFeeMinorUnits(10_000, 10), 10);
  assert.equal(calculateFeeMinorUnits(1, 1), 1);
});

test("deposit limit and withdrawal balance are enforced for both currencies", () => {
  const initial = mainWallet(0, 2_000);
  const deposited = applyIdempotentTestWalletOperation(initial, {
    idempotencyKey: "test-deposit-key-0001",
    operation: "DEPOSIT",
    currency: "USD",
    amountMinorUnits: 5_000,
  }, null, { depositLimitMinorUnits: 5_000 });
  assert.equal(deposited.status, "COMPLETED_TEST");
  assert.equal(deposited.mainWallet.usdBalanceMinorUnits, 5_000);

  const limitReached = applyIdempotentTestWalletOperation(deposited.mainWallet, {
    idempotencyKey: "test-deposit-key-0002",
    operation: "DEPOSIT",
    currency: "USD",
    amountMinorUnits: 1,
  }, null, { depositLimitMinorUnits: 5_000 });
  assert.equal(limitReached.status, "REJECTED");

  const withdrawn = applyIdempotentTestWalletOperation(deposited.mainWallet, {
    idempotencyKey: "test-withdraw-key-0001",
    operation: "WITHDRAWAL",
    currency: "KES",
    amountMinorUnits: 1_000,
  }, null, { depositLimitMinorUnits: 5_000 });
  assert.equal(withdrawn.mainWallet.kesBalanceMinorUnits, 1_000);
  const tooMuch = applyIdempotentTestWalletOperation(withdrawn.mainWallet, {
    idempotencyKey: "test-withdraw-key-0002",
    operation: "WITHDRAWAL",
    currency: "KES",
    amountMinorUnits: 1_001,
  }, null, { depositLimitMinorUnits: 5_000 });
  assert.equal(tooMuch.status, "REJECTED");
  assert.equal(withdrawn.mainWallet.kesBalanceMinorUnits, 1_000);
});

test("repeated operation idempotency keys do not apply credits or debits twice", () => {
  const request = {
    idempotencyKey: "test-operation-idempotent-1",
    operation: "DEPOSIT",
    currency: "USD",
    amountMinorUnits: 500,
  };
  const first = applyIdempotentTestWalletOperation(mainWallet(), request, null, { depositLimitMinorUnits: 1_000 });
  const repeated = applyIdempotentTestWalletOperation(first.mainWallet, request, first, { depositLimitMinorUnits: 1_000 });
  assert.equal(repeated.duplicate, true);
  assert.equal(repeated.mainWallet.usdBalanceMinorUnits, 500);
  assert.throws(() => applyIdempotentTestWalletOperation(first.mainWallet, {
    ...request,
    amountMinorUnits: 600,
  }, first, { depositLimitMinorUnits: 1_000 }));
});

test("a repeated trade key cannot debit twice or be reused for another trade", () => {
  const request = buyRequest("paper-trade-idempotency-001");
  const first = applyIdempotentPaperTrade(
    mainWallet(20_000),
    emptyHoldings(),
    request,
    50_000,
    null
  );
  const repeated = applyIdempotentPaperTrade(
    first.mainWallet,
    first.cryptoBalances,
    request,
    50_000,
    first
  );
  assert.equal(first.status, "FILLED_TEST");
  assert.equal(repeated.duplicate, true);
  assert.equal(repeated.mainWallet.usdBalanceMinorUnits, first.mainWallet.usdBalanceMinorUnits);
  assert.equal(repeated.cryptoBalances.BTC, first.cryptoBalances.BTC);
  assert.throws(() => applyIdempotentPaperTrade(
    first.mainWallet,
    first.cryptoBalances,
    buyRequest(request.idempotencyKey, { amount: 200 }),
    50_000,
    first
  ));
  assert.notEqual(requestFingerprint(request), requestFingerprint(buyRequest(request.idempotencyKey, { amount: 200 })));
});

test("concurrent competing buy operations serialize without overspending", async () => {
  let wallet = mainWallet(10_100);
  let balances = emptyHoldings();
  let transactionQueue = Promise.resolve();
  const submitAtomically = (key) => {
    const operation = transactionQueue.then(() => {
      const result = executePaperTrade(wallet, balances, buyRequest(key), 50_000);
      if (result.status === "FILLED_TEST") {
        wallet = result.mainWallet;
        balances = result.cryptoBalances;
      }
      return result;
    });
    transactionQueue = operation.then(() => undefined);
    return operation;
  };
  const outcomes = await Promise.all([
    submitAtomically("concurrent-paper-order-key-01"),
    submitAtomically("concurrent-paper-order-key-02"),
  ]);
  assert.equal(outcomes.filter((outcome) => outcome.status === "FILLED_TEST").length, 1);
  assert.equal(wallet.usdBalanceMinorUnits, 90);
  assert.equal(balances.BTC, 200_000);
});

test("market quote changes alone do not modify either wallet", () => {
  const wallet = mainWallet(5_000, 6_000);
  const balances = { ...emptyHoldings(), BTC: 123 };
  validateTradeRequest(buyRequest("market-price-update-key-1", { quotePriceUsd: 50_100, limitPriceUsd: 50_601 }));
  assert.equal(wallet.usdBalanceMinorUnits, 5_000);
  assert.equal(wallet.kesBalanceMinorUnits, 6_000);
  assert.equal(balances.BTC, 123);
});
