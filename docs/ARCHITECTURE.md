# Architecture & Data Flow — NovaPlus+ Trader

## Overview
NovaPlus+ Trader follows **MVVM (Model-View-ViewModel)** architectural principles with **Jetpack Compose** for UI declarations, **Kotlin Coroutines & Flow** for reactive data streams, and **Repository patterns** for clean separation of concerns.

## Core Modules & Packages
- **`ui/navigation/`**: Jetpack Navigation Compose setup (`NovaNavGraph.kt`, `Screen.kt`).
- **`ui/screens/`**: Feature-specific screens and ViewModels (Home, Wallet, Trading, Markets, Betting, Profile, Admin, Auth, Notifications).
- **`data/repository/`**: Repositories abstracting remote/local data sources (`WalletRepository`, `CryptoWalletRepository`, `MarketDataRepository`, `LiveSportsRepository`, `AuthRepository`).
- **`data/remote/`**: WebSocket and REST network integrations (Binance WebSockets, CoinGecko, ExchangeRate-API, Sportmonks abstraction).
- **`domain/model/`**: Immutable domain models representing market ticks, candles, wallets, sports events, and trading orders.

## Backend Responsibilities
- **Firebase Auth**: User authentication, email/password, and Google Sign-In.
- **Firestore**: User documents, wallets, holdings, trade requests, and audit logs protected by robust security rules.
- **Cloud Functions (`functions/index.js`)**: Server-authoritative transaction execution, portfolio creation, idempotency checks, and paper trade settlement.
