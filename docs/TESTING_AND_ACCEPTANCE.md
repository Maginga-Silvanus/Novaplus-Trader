# Testing & Acceptance — NovaPlus+ Trader

## Automated Test Suite
- **Unit Tests**:
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
  *Current Status*: **59 tests passed, 0 failed**.

## Covered Test Suites
- Authentication and user name resolution (`AuthRepositoryImpl`).
- Wallet transactions, idempotency, and ledger consistency (`FinancialTransactionTest`, `TwoWalletArchitectureTest`).
- Binance market parser and OHLC candlestick aggregation (`BinanceMarketParserTest`).
- Global crypto metadata parsing, rankings, and FX rates (`GlobalCryptoAndFxTest`).
- Live sports feed parsing and strict wallet-isolation regression checks (`LiveSportsAndWalletIsolationTest`).

## Company Acceptance Checklist
- [ ] Verify Firebase project ownership transfer.
- [ ] Run unit tests and build release candidate.
- [ ] Configure company-supplied API subscriptions and payment gateway credentials.
- [ ] Execute end-to-end device testing on staging build variant.
