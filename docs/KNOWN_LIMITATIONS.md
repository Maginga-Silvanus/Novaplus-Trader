# Known Limitations & External Dependencies — NovaPlus+ Trader

## 1. Simulated / Demo-Only Features
- **Sports Betting**: All sports wagers and odds settlements use virtual Demo Credits (`demoBettingBalance`) and are strictly isolated from real fiat/crypto wallets.
- **Paper Trading**: Spot trading uses simulated price execution and virtual balances.

## 2. External Dependencies & Subscriptions Required
- **Live Sports Data**: Enterprise sports feeds (Sportmonks / Sportradar) require a paid commercial subscription and secure backend token configuration.
- **Fiat Payment Gateway**: Production M-Pesa Daraja or credit card acquirer credentials must be configured for real fiat deposits/withdrawals.

## 3. Regulatory & Compliance Notice
- Real-money trading, deposits, withdrawals, and wagering are disabled by design. The purchasing company must complete its required legal, regulatory, and banking compliance before enabling live financial transactions.
- KYC document storage/review, responsible-gaming controls, and automated bot execution require trusted Firebase-backed services that are not deployed in this repository. Those operations report failure rather than saving local-only success; the admin KYC queue is intentionally unavailable until the review backend is configured.
