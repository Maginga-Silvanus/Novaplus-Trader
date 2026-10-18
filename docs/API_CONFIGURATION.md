# API Configuration Guide — NovaPlus+ Trader

## External Integrations & Providers

| Provider | Purpose | Type | Configuration / Status |
| :--- | :--- | :--- | :--- |
| **Binance API / WebSocket** | Live spot market ticks & OHLC candlestick charts | Public REST & WSS | Connected (Public endpoint `wss://stream.binance.com:9443/ws`) |
| **CoinGecko API** | Global cryptocurrency metadata & market cap rankings | Public REST API | Connected (Public endpoint with 15-min TTL cache) |
| **ExchangeRate-API** | USD/KES foreign exchange conversion | Public REST API | Connected (Public endpoint with 1-hour TTL cache) |
| **Sportmonks / Sportradar** | Live sports fixtures, scores, event statuses, and odds | Enterprise Payout API | Abstracted via `LiveSportsRepository` (Requires paid subscription & secret token) |

## Credential Management
- Never embed private API keys in Android source code, `BuildConfig`, or version control.
- Store sensitive tokens in Firebase Cloud Functions environment secrets (`process.env`).
