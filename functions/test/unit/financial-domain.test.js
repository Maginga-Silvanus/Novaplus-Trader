const test = require("node:test");
const assert = require("node:assert/strict");
const {
  createPendingFinancialRecord,
  financialRequestFingerprint,
  validateFinancialRequest,
} = require("../../financial-domain");
const functionExports = require("../../index");

const base = {
  type: "DEPOSIT",
  idempotencyKey: "deposit-request-123",
  currency: "KES",
  amountMinorUnits: 120_000,
  method: "MPESA",
};

test("validates and normalizes pending deposit, withdrawal, and transfer requests", () => {
  assert.deepEqual(validateFinancialRequest(base), base);
  assert.deepEqual(validateFinancialRequest({
    type: "WITHDRAWAL",
    idempotencyKey: "withdrawal-request-123",
    currency: "USD",
    amountMinorUnits: 500,
    method: "BANK",
    destination: "  acct-123  ",
  }), {
    type: "WITHDRAWAL",
    idempotencyKey: "withdrawal-request-123",
    currency: "USD",
    amountMinorUnits: 500,
    method: "BANK",
    destination: "acct-123",
  });
  assert.deepEqual(validateFinancialRequest({
    type: "TRANSFER",
    idempotencyKey: "transfer-request-123",
    currency: "KES",
    amountMinorUnits: 100,
    recipientId: " recipient-uid ",
  }), {
    type: "TRANSFER",
    idempotencyKey: "transfer-request-123",
    currency: "KES",
    amountMinorUnits: 100,
    recipientId: "recipient-uid",
    note: "",
  });
});

test("rejects invalid values and client-supplied identity, PIN, settlement, or status fields", () => {
  for (const invalid of [
    { ...base, amountMinorUnits: 0 },
    { ...base, amountMinorUnits: 1.5 },
    { ...base, currency: "BTC" },
    { ...base, method: "UNSUPPORTED" },
    { ...base, method: "CRYPTO" },
    { ...base, userId: "victim" },
    { ...base, walletId: "another-wallet" },
    { ...base, status: "COMPLETED" },
    { ...base, securityPin: "1234" },
    { ...base, idempotencyKey: "bad key" },
  ]) {
    assert.throws(() => validateFinancialRequest(invalid));
  }
  assert.throws(() => validateFinancialRequest({
    type: "WITHDRAWAL",
    idempotencyKey: "withdrawal-request-123",
    currency: "USD",
    amountMinorUnits: 10,
    method: "BANK",
    destination: "",
  }));
});

test("fingerprint is stable for retries and changes with request details", () => {
  const normalized = validateFinancialRequest(base);
  assert.equal(financialRequestFingerprint(normalized), financialRequestFingerprint(normalized));
  assert.notEqual(
    financialRequestFingerprint(normalized),
    financialRequestFingerprint(validateFinancialRequest({ ...base, amountMinorUnits: 120_001 }))
  );
});

test("pending request records are owner-scoped and cannot imply settlement", () => {
  const request = validateFinancialRequest(base);
  const record = createPendingFinancialRecord("firebase-user-a", request, "server timestamp");

  assert.equal(record.status, "PENDING");
  assert.equal(record.settlementEnabled, false);
  assert.equal(record.walletId, "wallet-firebase-user-a-kes");
  assert.equal(record.userId, "firebase-user-a");
  assert.equal(record.amountMinorUnits, 120_000);
  assert.equal(record.createdAt, "server timestamp");
  assert.equal("balanceAfterMinorUnits" in record, false);
  assert.equal("securityPin" in record, false);
  assert.notEqual(
    record.walletId,
    createPendingFinancialRecord("firebase-user-b", request, "server timestamp").walletId
  );
});

test("the Firebase Functions module registers the financial request callable", () => {
  assert.equal(typeof functionExports.createFinancialRequest, "function");
});
