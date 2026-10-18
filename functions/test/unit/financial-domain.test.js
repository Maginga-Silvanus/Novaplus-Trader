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
    amountMinorUnits: 5_000,
    method: "BANK",
    destination: "  acct-123  ",
  }), {
    type: "WITHDRAWAL",
    idempotencyKey: "withdrawal-request-123",
    currency: "USD",
    amountMinorUnits: 5_000,
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
    { ...base, providerReference: "QBH123456X" },
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
  assert.throws(() => validateFinancialRequest({
    type: "WITHDRAWAL",
    idempotencyKey: "withdrawal-request-minimum",
    currency: "USD",
    amountMinorUnits: 999,
    method: "BANK",
    destination: "account-1",
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

test("Paybill deposits require a unique-format KES M-Pesa reference and start as submitted", () => {
  const request = validateFinancialRequest({
    ...base,
    currency: "KES",
    method: "PAYBILL",
    providerReference: " qbh123456x ",
  });
  assert.equal(request.providerReference, "QBH123456X");
  const record = createPendingFinancialRecord("firebase-user-a", request, "server timestamp");
  assert.equal(record.status, "SUBMITTED");
  assert.equal(record.providerReference, "QBH123456X");
  assert.equal(record.settlementEnabled, false);

  assert.throws(() => validateFinancialRequest({
    ...base,
    currency: "USD",
    method: "PAYBILL",
    providerReference: "QBH123456X",
  }));
  assert.throws(() => validateFinancialRequest({
    ...base,
    currency: "KES",
    method: "PAYBILL",
    providerReference: "invalid code",
  }));
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
  assert.equal(typeof functionExports.lookupTransferRecipient, "function");
  assert.equal(typeof functionExports.advancePaybillDepositStatus, "function");
  assert.equal(typeof functionExports.approvePaybillDeposit, "function");
});
