# Release Signing Guide — NovaPlus+ Trader

## Release Build Configuration
- Release build type is configured in `app/build.gradle.kts`:
  ```kotlin
  buildTypes {
      release {
          isMinifyEnabled = true
          proguardFiles(
              getDefaultProguardFile("proguard-android-optimize.txt"),
              "proguard-rules.pro"
          )
      }
  }
  ```

## Signing Setup (Company Handover)
1. Generate a production keystore securely:
   ```bash
   keytool -genkey -v -keystore novaplus-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias novaplus
   ```
2. Store keystore credentials securely in local environment variables (`signingProps`) or CI/CD secrets:
   - `STORE_FILE`
   - `STORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
3. **Never** commit `.jks` files or signing passwords to source control.
