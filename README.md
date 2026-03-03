# MyExpence 💰

**MyExpence** is a secure, privacy-focused expense management application built with Modern Android Development (MAD) practices. It ensures that your financial data remains strictly local and protected by industry-standard encryption.

## ✨ Key Features
- **Modern UI**: 100% **Jetpack Compose** with Material 3 design.
- **Privacy First**: Integrated **Biometric Authentication** (Fingerprint/Face unlock) for app access.
- **Military-Grade Security**: Local SQLite database fully encrypted using **SQLCipher** (AES-256).
- **Offline Storage**: Powered by **Room Database** and **Preferences DataStore** for settings.
- **Background Tasks**: Automated data management or reminders using **WorkManager**.
- **Edge-Ready**: Fully optimized for **Android 15 (Target SDK 36)**.

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room + SQLCipher (Encrypted Persistence)
- **Architecture**: MVVM with Navigation Compose
- **Security**: Jetpack Security Crypto + Biometric API
- **Concurrency**: Kotlin Coroutines & Flow
- **Dependency Management**: Version Catalogs (libs.versions.toml)

## 🚀 Getting Started
### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35/36

### Installation
1. Clone the repository:
2. Open the project in Android Studio.
3. Sync the Project with Gradle Files.
4. Run the app on a device or emulator (API 24+).

## 🛡 Security Implementation
Unlike standard expense trackers, MyExpence prioritizes data integrity:
- **Database Encryption**: Uses a custom `SupportFactory` to initialize Room with an encrypted SQLCipher layer.
- **Key Management**: Sensitive data is handled using the `androidx.security:security-crypto` library.

## 📜 License
   
