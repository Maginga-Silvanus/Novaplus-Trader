# Test wallets and paper crypto trading

## Wallets and configuration

`getOrCreateCryptoPortfolio` derives the user from callable Firebase Authentication and initializes the separate Firestore wallets atomically:

- `users/{uid}/wallets/main` stores the USD and KES **TEST FUNDS** as integer minor units.
- `users/{uid}/wallets/main/transactions/{id}` stores server-authored test deposits, withdrawals, and configured initial funding.
- `users/{uid}/cryptoWallets/spot` stores internal portfolio metadata and paper-trading settings.
- `users/{uid}/cryptoWallets/spot/holdings/{assetId}` stores crypto quantities in integer atomic units, starting at zero.
- `users/{uid}/cryptoWallets/spot/transactions/{id}` stores confirmed simulated purchases and sales only.
- `users/{uid}/tradeRequests/{idempotencyKey}` stores the authoritative outcome for each paper-trade request.
- `users/{uid}/walletOperations/{idempotencyKey}` stores idempotent test wallet funding requests.

The Functions backend is the only writer to wallet balances and transaction records. Signed-in owners and users with an authorized `adminRole` claim can read wallet data; owner clients cannot write authoritative balances or transactions. Initial test balances default to zero and are granted only when the authenticated Firebase ID token has the boolean custom claim `testAccount: true`; regular signed-in users always initialize with zero. Configure balances through Firebase Functions environment variables, never client fields:

| Variable | Default | Purpose |
| --- | ---: | --- |
| `TEST_INITIAL_USD_MINOR_UNITS` | `0` | One-time starting USD cents per user |
| `TEST_INITIAL_KES_MINOR_UNITS` | `0` | One-time starting KES cents per user |
| `TEST_DEPOSIT_LIMIT_USD_MINOR_UNITS` | `100000` | Maximum cumulative simulated USD deposits per user |
| `TEST_DEPOSIT_LIMIT_KES_MINOR_UNITS` | `10000000` | Maximum cumulative simulated KES deposits per user |
| `TEST_TRADE_FEE_BPS` | `10` | Simulated trade fee in basis points (10 = 0.10%) |
| `TEST_MIN_TRADE_USD_MINOR_UNITS` | `100` | Minimum paper order value in USD cents |

For local development, create a private `functions/.env` file with the variables to override. Do not commit that file. Changing the initial balance only affects new wallets and existing zero-balance wallets that have not received their one-time initialization grant; it does not overwrite an established balance. Initial grants are recorded in the main wallet transaction history.

Grant the claim only to controlled development accounts from a trusted Admin SDK environment; do not expose a client callable for this operation. Preserve existing custom claims when granting it:

```js
const user = await admin.auth().getUser(uid);
await admin.auth().setCustomUserClaims(uid, {
  ...(user.customClaims || {}),
  testAccount: true,
});
```

The callable rejects test credits and withdrawals unless the same claim is present. These are capped simulated ledger operations only; no payment or payout is initiated. Remove the claim to revoke future test funding operations. Already-issued Firebase ID tokens may retain claims until refreshed.

Test deposits are capped cumulatively per currency and test withdrawals cannot exceed the matching test balance. Both operations are idempotent and explicitly marked `COMPLETED_TEST`; no payment or payout provider is called.

## Administrator roles

Administrative access is provisioned only by a trusted Firebase Admin SDK environment. The Android app reads the signed Firebase ID token claim `adminRole` and grants portal access only when it exactly matches one of `SUPER_ADMIN`, `ADMIN`, `SUPPORT`, `OPERATIONS`, `COMPLIANCE`, or `READ_ONLY`. Missing, malformed, or unknown values mean no admin access. Firestore Rules independently enforce the same claim allowlist for administrative reads; user profile fields never grant a role. Do not add a client-callable role setter.

Provision a role from a secured operator script or backend using the Admin SDK, preserving unrelated claims:

```js
const user = await admin.auth().getUser(uid);
await admin.auth().setCustomUserClaims(uid, {
  ...(user.customClaims || {}),
  adminRole: "SUPPORT", // use one of the documented role names
});
```

To revoke admin access, remove `adminRole` while preserving other claims, then revoke refresh tokens as appropriate. Force-refresh the operator's ID token after provisioning or revocation. Any future privileged callable must validate `request.auth.token.adminRole` server-side against the role allowlist and enforce action-specific permissions; a role displayed by the client is never authorization.

The role policy is enforced in Android navigation for usability and independently by Firestore Rules for data access:

| Claim | Permitted admin area |
| --- | --- |
| `SUPER_ADMIN`, `ADMIN` | All admin areas |
| `SUPPORT` | Support tickets and messages |
| `OPERATIONS` | Wallets, transactions, paper trading, and bot views |
| `COMPLIANCE` | KYC and responsible-gaming compliance views |
| `READ_ONLY` | Read-only access to all admin areas |

Never use a profile document role as authority. Changes remain blocked unless a trusted backend explicitly implements and authorizes them.

## Pending financial requests

`createFinancialRequest` accepts authenticated deposit, withdrawal, and internal transfer requests. The callable derives the sender UID from the verified Firebase Auth token, validates the request type, currency, amount, method/destination/recipient, and idempotency key, and atomically writes an owner-scoped request and a `PENDING` main-wallet transaction record. A retry with the same key and identical payload returns the original record; reusing a key for different fields is rejected. For transfers the callable resolves the target to a Firebase Auth user before recording it. It never updates wallet balances, reserves or debits funds, credits a recipient, contacts payment providers, or claims settlement. A pending record is only an accepted request, not proof that payment or transfer occurred. Refunds, reversals, and fee estimates remain unavailable until trusted processing is configured.

The app reads owner transaction history from `users/{uid}/wallets/main/transactions`. Clients cannot create or modify these records or `users/{uid}/financialRequests`; only Admin SDK-backed Functions can write them. The request callable does not accept a client-provided UID, wallet ID, PIN, status, balance, provider reference, or settlement state. Do not pass payment card credentials or security PINs to this endpoint.

## Paper crypto trading

The Android app reads live public market quotes from its existing Binance adapter. The callable independently fetches the supported Binance `ASSETUSDT` ticker and checks it against the submitted quote and limit before executing a paper order. USDT is treated as USD for this test-only valuation; the displayed result is not an exchange execution price guarantee.

`submitSpotTradeRequest` accepts supported BTC, ETH, SOL, BNB, XRP, ADA, DOGE, and LTC USDT pairs. Buys spend main-wallet TEST USD and sells specify the asset quantity. The callable validates quote freshness, supported pair, amount denomination, minimum order, available test cash or holdings, limit price, and fee. It atomically updates the main test USD balance, asset holding, idempotency request, main-wallet transaction history, and crypto transaction record using integer minor/atomic units. Successful responses include a server-priced SIMULATED TRADE receipt. `FILLED_TEST` means only that the simulated transaction was recorded. It is not a real exchange order, cryptocurrency custody, or an on-chain transfer.

Market updates are valuation data only and never mutate holdings or balances. No exchange credentials, real-money balance, payment execution, or custody provider is configured.

## Android market-data endpoints

The Android market screens use Binance public Spot REST and WebSocket market data directly; they do not use these wallet callables for chart or portfolio updates.

- `GET /api/v3/exchangeInfo?symbols=...` verifies that the supported BTCUSDT, ETHUSDT, SOLUSDT, BNBUSDT, XRPUSDT, ADAUSDT, DOGEUSDT, and LTCUSDT pairs are currently spot-tradable.
- `GET /api/v3/ticker/24hr?symbols=...` loads 24-hour price/change and base/quote volume.
- `GET /api/v3/ticker/24hr?symbol=...` seeds the selected pair's ticker.
- `GET /api/v3/klines?symbol=...&interval=1m|5m|15m|1h|4h|1d&limit=100` loads OHLCV history.
- `wss://stream.binance.com:9443/stream?streams=...@ticker/...@ticker` updates market ranking rows.
- `wss://stream.binance.com:9443/ws/<symbol>@ticker` updates the selected pair's ticker.
- `wss://stream.binance.com:9443/ws/<symbol>@kline_<interval>` updates the forming candle. Reconnects reload REST history to recover missed intervals.

Ticker and chart prices/volumes are denominated in USDT; the app does not represent them as verified USD or KES conversions. USD/KES conversion is unavailable until a separate trusted FX provider is configured. Binance market prices are display/valuation data only; paper orders are independently repriced and checked by the Functions backend.

## Security and tests

Firestore rules permit owners to read their own wallet and history documents, deny client writes to authoritative balances/transactions/requests, retain validated bot-setting edits, and deny unrelated paths. Review the full rules impact before deploying them to an existing Firebase project.

Run domain tests with `npm test`. Run Firestore Security Rules tests with `npm run test:rules`.
