# Project Setup & Prerequisites — NovaPlus+ Trader

## Prerequisites
- **Android Studio**: Arctic Fox or newer (Koala / Ladybug recommended).
- **Android SDK**: Compile SDK 35, Min SDK 24, Target SDK 35.
- **Java Development Kit (JDK)**: JDK 11 or JDK 17.
- **Node.js & Firebase CLI**: Required for backend Cloud Functions deployment.

## Repository Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/company/novaplus-trader.git
   cd NovaPlusTrader
   ```
2. Open the project folder in Android Studio.
3. Sync project with Gradle files.

## Build Instructions
- **Debug Build**:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- **Run Unit Tests**:
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
