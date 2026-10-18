const { createHash } = require("node:crypto");

const depositMethods = new Set(["MPESA", "PAYBILL", "BANK", "CARD"]);
const withdrawalMethods = new Set(["MPESA", "BANK", "CARD"]);
const currencies = new Set(["USD", "KES"]);

function validateFinancialRequest(data) {
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    throw new Error("Provide a financial request.");
  }
  const allowedFields = new Set([
    "type", "idempotencyKey", "currency", "amountMinorUnits", "method",
    "destination", "recipientId", "note", "providerReference",
  ]);
  if (Object.keys(data).some((key) => !allowedFields.has(key))) {
    throw new Error("Financial request contains unsupported fields.");
  }
  if (typeof data.idempotencyKey !== "string" ||
      !/^[A-Za-z0-9_-]{8,128}$/.test(data.idempotencyKey)) {
    throw new Error("Provide a valid idempotency key.");
  }
  if (!["DEPOSIT", "WITHDRAWAL", "TRANSFER"].includes(data.type)) {
    throw new Error("Unsupported financial request type.");
  }
  if (!currencies.has(data.currency)) {
    throw new Error("Only USD and KES requests are supported.");
  }
  if (!Number.isSafeInteger(data.amountMinorUnits) ||
      data.amountMinorUnits < 1 || data.amountMinorUnits > 100_000_000) {
    throw new Error("Amount must be a positive integer within the request limit.");
  }

  const request = {
    type: data.type,
    idempotencyKey: data.idempotencyKey,
    currency: data.currency,
    amountMinorUnits: data.amountMinorUnits,
  };
  if (data.type === "DEPOSIT") {
    if (!depositMethods.has(data.method)) throw new Error("Unsupported deposit method.");
    request.method = data.method;
    if (data.method === "PAYBILL") {
      if (data.currency !== "KES") throw new Error("Paybill deposits must be submitted in KES.");
      if (typeof data.providerReference !== "string" ||
          !/^[A-Z0-9]{6,12}$/.test(data.providerReference.trim().toUpperCase())) {
        throw new Error("Provide a valid M-Pesa transaction code.");
      }
      request.providerReference = data.providerReference.trim().toUpperCase();
    } else if (data.providerReference) {
      throw new Error("A payment reference is only valid for a Paybill deposit.");
    }
  } else if (data.type === "WITHDRAWAL") {
    if (!withdrawalMethods.has(data.method)) throw new Error("Unsupported withdrawal method.");
    if (data.currency === "USD" && data.amountMinorUnits < 1_000) {
      throw new Error("USD withdrawals must be at least $10.00.");
    }
    if (typeof data.destination !== "string" ||
        data.destination.trim().length < 3 || data.destination.length > 256) {
      throw new Error("Provide a valid withdrawal destination.");
    }
    request.method = data.method;
    request.destination = data.destination.trim();
  } else {
    if (typeof data.recipientId !== "string" ||
        data.recipientId.trim().length < 1 || data.recipientId.length > 128) {
      throw new Error("Provide a valid recipient user ID.");
    }
    request.recipientId = data.recipientId.trim();
    if (data.note != null) {
      if (typeof data.note !== "string" || data.note.length > 200) {
        throw new Error("Transfer note must be 200 characters or fewer.");
      }
      request.note = data.note.trim();
    } else {
      request.note = "";
    }
  }
  return request;
}

function financialRequestFingerprint(request) {
  return createHash("sha256")
    .update(JSON.stringify(request))
    .digest("hex");
}

function createPendingFinancialRecord(uid, request, createdAt) {
  const descriptions = {
    DEPOSIT: `Pending ${request.method} deposit request`,
    WITHDRAWAL: `Pending ${request.method} withdrawal request`,
    TRANSFER: "Pending internal transfer request",
  };
  return {
    transactionId: request.idempotencyKey,
    idempotencyKey: request.idempotencyKey,
    userId: uid,
    walletId: `wallet-${uid}-${request.currency.toLowerCase()}`,
    type: request.type,
    operation: request.type,
    currency: request.currency,
    amountMinorUnits: request.amountMinorUnits,
    feeMinorUnits: 0,
    netAmountMinorUnits: request.amountMinorUnits,
    status: request.method === "PAYBILL" ? "SUBMITTED" : "PENDING",
    reference: request.idempotencyKey,
    description: descriptions[request.type],
    ...(request.method ? { method: request.method } : {}),
    ...(request.providerReference ? { providerReference: request.providerReference } : {}),
    ...(request.destination ? { destination: request.destination } : {}),
    ...(request.recipientId ? { recipientId: request.recipientId } : {}),
    ...(request.note ? { note: request.note } : {}),
    settlementEnabled: false,
    createdAt,
    updatedAt: createdAt,
  };
}

module.exports = {
  createPendingFinancialRecord,
  financialRequestFingerprint,
  validateFinancialRequest,
};
