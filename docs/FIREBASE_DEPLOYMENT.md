# Firebase Deployment Guide — NovaPlus+ Trader

## 1. Firebase Project Creation
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new company-owned Firebase project.
3. Enable **Authentication** (Email/Password, Google).
4. Enable **Cloud Firestore** in production mode.
5. Enable **Cloud Functions** (Node.js 18+ runtime).

## 2. Android App Registration
1. Register your Android app with package name `com.example.novaplustrader` (or your company namespace).
2. Download `google-services.json` and place it in `app/google-services.json`.

## 3. Firestore Security Rules & Indexes
- Deploy security rules (`firestore.rules`) and indexes (`firestore.indexes.json`) using Firebase CLI:
  ```bash
  firebase deploy --only firestore
  ```

## 4. Cloud Functions Deployment
1. Navigate to `functions/`:
   ```bash
   cd functions
   npm install
   ```
2. Configure server-side secrets:
   ```bash
   firebase functions:secrets:set TEST_TRADE_FEE_BPS
   ```
3. Deploy functions:
   ```bash
   firebase deploy --only functions
   ```
