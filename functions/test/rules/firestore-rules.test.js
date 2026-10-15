const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const { after, before, beforeEach, test } = require("node:test");
const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const { doc, getDoc, runTransaction, serverTimestamp, setDoc, updateDoc } = require("firebase/firestore");

let environment;
const rules = fs.readFileSync(path.join(__dirname, "../../../firestore.rules"), "utf8");

before(async () => {
  environment = await initializeTestEnvironment({
    projectId: "demo-novaplus-trader",
    firestore: { rules },
  });
});

beforeEach(async () => environment.clearFirestore());
after(async () => environment.cleanup());

test("users can read only their own main wallet, crypto holdings, transactions and trade requests", async () => {
  await environment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users/alice/wallets/main"), {
      usdBalanceMinorUnits: 0,
      kesBalanceMinorUnits: 0,
      mode: "testing",
    });
    await setDoc(doc(context.firestore(), "users/alice/wallets/main/transactions/deposit-001"), {
      operation: "DEPOSIT",
      status: "COMPLETED_TEST",
      testOnly: true,
    });
    await setDoc(doc(context.firestore(), "users/alice/walletOperations/deposit-001"), {
      operation: "DEPOSIT",
      status: "COMPLETED_TEST",
      testOnly: true,
    });
    await setDoc(doc(context.firestore(), "users/alice/cryptoWallets/spot"), {
      mode: "disabled",
      bitcoinWalletId: "BTC-abc",
    });
    await setDoc(doc(context.firestore(), "users/alice/cryptoWallets/spot/holdings/btc"), {
      symbol: "BTC",
      quantityAtomicUnits: 0,
      decimals: 8,
    });
    await setDoc(doc(context.firestore(), "users/alice/cryptoWallets/spot/transactions/transaction-001"), {
      status: "FILLED",
    });
    await setDoc(doc(context.firestore(), "users/alice/tradeRequests/request-001"), { status: "FILLED" });
  });
  const alice = environment.authenticatedContext("alice").firestore();
  const bob = environment.authenticatedContext("bob").firestore();
  await assertSucceeds(getDoc(doc(alice, "users/alice/wallets/main")));
  await assertFails(getDoc(doc(bob, "users/alice/wallets/main")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/wallets/main/transactions/deposit-001")));
  await assertFails(getDoc(doc(bob, "users/alice/wallets/main/transactions/deposit-001")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/walletOperations/deposit-001")));
  await assertFails(getDoc(doc(bob, "users/alice/walletOperations/deposit-001")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/cryptoWallets/spot")));
  await assertFails(getDoc(doc(bob, "users/alice/cryptoWallets/spot")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/cryptoWallets/spot/holdings/btc")));
  await assertFails(getDoc(doc(bob, "users/alice/cryptoWallets/spot/holdings/btc")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/cryptoWallets/spot/transactions/transaction-001")));
  await assertFails(getDoc(doc(bob, "users/alice/cryptoWallets/spot/transactions/transaction-001")));
  await assertSucceeds(getDoc(doc(alice, "users/alice/tradeRequests/request-001")));
  await assertFails(getDoc(doc(bob, "users/alice/tradeRequests/request-001")));
});

test("clients cannot write either wallet, holdings, transactions or trade requests", async () => {
  const alice = environment.authenticatedContext("alice").firestore();
  await environment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users/alice/cryptoWallets/spot"), {
      mode: "disabled",
      bitcoinWalletId: "BTC-abc",
    });
    await setDoc(doc(context.firestore(), "users/alice/wallets/main"), {
      usdBalanceMinorUnits: 0,
      kesBalanceMinorUnits: 0,
      mode: "testing",
    });
  });
  await assertFails(setDoc(doc(alice, "users/alice/wallets/main"), {
    usdBalanceMinorUnits: 999_999_999,
    kesBalanceMinorUnits: 999_999_999,
    mode: "testing",
  }));
  await assertFails(updateDoc(doc(alice, "users/alice/wallets/main"), {
    usdBalanceMinorUnits: 999_999_999,
  }));
  await assertFails(setDoc(doc(alice, "users/alice/wallets/main/transactions/fake"), {
    status: "COMPLETED_TEST",
    testOnly: true,
  }));
  await assertFails(setDoc(doc(alice, "users/alice/walletOperations/fake"), {
    operation: "DEPOSIT",
    amountMinorUnits: 999_999_999,
  }));
  await assertFails(setDoc(doc(alice, "users/alice/cryptoWallets/spot"), {
    mode: "disabled",
    bitcoinWalletId: "BTC-abc",
  }));
  await assertFails(updateDoc(doc(alice, "users/alice/cryptoWallets/spot"), {
    bitcoinWalletId: "BTC-forged",
  }));
  await assertFails(setDoc(doc(alice, "users/alice/cryptoWallets/spot/holdings/btc"), {
    symbol: "BTC",
    quantityAtomicUnits: 999_999,
    decimals: 8,
  }));
  await assertFails(setDoc(doc(alice, "users/alice/cryptoWallets/spot/transactions/tx-1"), {
    status: "FILLED",
  }));
  await assertFails(setDoc(doc(alice, "users/alice/tradeRequests/client-fill"), { status: "FILLED" }));
});

test("Firestore transactions serialize competing paper purchases and deduplicate concurrent retries", async () => {
  await environment.withSecurityRulesDisabled(async (context) => {
    const firestore = context.firestore();
    const walletRef = doc(firestore, "users/alice/wallets/main");
    const holdingRef = doc(firestore, "users/alice/cryptoWallets/spot/holdings/btc");
    await setDoc(walletRef, { usdBalanceMinorUnits: 2_500 });
    await setDoc(holdingRef, { quantityAtomicUnits: 0 });

    const submitTestPurchase = (idempotencyKey) => runTransaction(firestore, async (transaction) => {
      const requestRef = doc(firestore, `users/alice/tradeRequests/${idempotencyKey}`);
      const [request, wallet, holding] = await Promise.all([
        transaction.get(requestRef),
        transaction.get(walletRef),
        transaction.get(holdingRef),
      ]);
      if (request.exists()) return false;
      const available = wallet.get("usdBalanceMinorUnits");
      if (available < 1_000) return false;
      transaction.update(walletRef, { usdBalanceMinorUnits: available - 1_000 });
      transaction.update(holdingRef, {
        quantityAtomicUnits: holding.get("quantityAtomicUnits") + 10,
      });
      transaction.set(requestRef, { status: "FILLED_TEST", testOnly: true });
      return true;
    });

    const duplicateResults = await Promise.all([
      submitTestPurchase("same-paper-request-key"),
      submitTestPurchase("same-paper-request-key"),
    ]);
    assert.equal(duplicateResults.filter(Boolean).length, 1);

    const competingResults = await Promise.all([
      submitTestPurchase("paper-request-key-1"),
      submitTestPurchase("paper-request-key-2"),
    ]);
    assert.equal(competingResults.filter(Boolean).length, 1);

    const [wallet, holding] = await Promise.all([getDoc(walletRef), getDoc(holdingRef)]);
    assert.equal(wallet.get("usdBalanceMinorUnits"), 500);
    assert.equal(holding.get("quantityAtomicUnits"), 20);
  });
});

test("users can edit only validated bot settings in their own namespace", async () => {
  const alice = environment.authenticatedContext("alice").firestore();
  const validSettings = {
    strategy: "trend-following",
    timeframe: "1h",
    indicators: ["ema"],
    riskLimitBps: 500,
    tradeSizeMinorUnits: 1_000,
    stopLossBps: 300,
    takeProfitBps: 600,
    updatedAt: serverTimestamp(),
  };
  await assertSucceeds(setDoc(doc(alice, "users/alice/botSettings/trend"), validSettings));
  await assertFails(setDoc(doc(alice, "users/alice/botSettings/unsafe"), {
    ...validSettings,
    status: "RUNNING",
    updatedAt: serverTimestamp(),
  }));
  await assertFails(setDoc(doc(alice, "users/alice/botSettings/invalid-indicator"), {
    ...validSettings,
    indicators: ["unsafe-script"],
    updatedAt: serverTimestamp(),
  }));
  await assertFails(setDoc(doc(alice, "users/bob/botSettings/trend"), validSettings));
  await assertFails(updateDoc(doc(alice, "users/alice/botSettings/trend"), {
    tradeSizeMinorUnits: -1,
    updatedAt: serverTimestamp(),
  }));
});

test("bot commands and unrelated collections remain denied", async () => {
  const alice = environment.authenticatedContext("alice").firestore();
  await assertFails(setDoc(doc(alice, "users/alice/botCommands/start"), { strategy: "momentum" }));
  await assertFails(getDoc(doc(alice, "systemHealth/current")));
});
