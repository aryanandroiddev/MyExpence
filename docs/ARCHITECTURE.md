# Architecture — Phase 1 (Offline-first)

## High-level
MyExpence is an **offline-first Android app** built with **Jetpack Compose** and a **local encrypted Room database**. All ingestion (SMS/CSV) happens **on-device**, and budgets/alerts run via **WorkManager**.

## Layering & packages

- `dev.app.arya.myexpence.data`
  - `db/`: Room entities/DAO + `AppDatabase` (SQLCipher encryption via `SupportFactory`)
  - `repo/`: repositories (`TransactionRepository`, `AnalyticsRepository`, `BudgetRepository`, `SettingsRepository`, `CategoryRepository`)
  - `ingest/`: `SmsImporter` and `CsvImporter` (parsing + dedupe keys)
  - `security/`: encrypted passphrase generation/storage for DB (`DbPassphrase`)
  - `work/`: background workers + notifications (`BudgetAlertWorker`)
- `dev.app.arya.myexpence.domain`
  - domain models used by UI (e.g., `Transaction`, `DashboardSummary`)
- `dev.app.arya.myexpence.ui`
  - `nav/`: Compose navigation + scaffold
  - `screens/`: screens (Dashboard/Transactions/Add/Budgets/Settings)
  - `state/`: ViewModels + ViewModel factory (simple DI)

## Data storage

### Encrypted database
- SQLCipher is used for encryption-at-rest.
- Passphrase is generated once and stored in `EncryptedSharedPreferences` (keyed by Android Keystore via `MasterKey`).

### Dedupe strategy
- Imported transactions (SMS/CSV) generate a deterministic `sourceKey` (SHA-256 hash of stable fields).
- `transactions.sourceKey` has a **unique index**, preventing duplicates on re-import.

## Ingestion flows

### SMS import
- Read from `content://sms/inbox` (requires `READ_SMS` and internal device policy support).
- Parse messages using rules/regex into candidate transactions.
- Insert into DB with `source=SMS` + `rawSourceJson` payload.
- Store last processed timestamp in `ingestion_state`.

### CSV import
- User selects a CSV via Storage Access Framework.
- Minimal Phase 1 importer expects common headers (`date`, `description`/`narration`, `amount` or `debit`/`credit`).
- Inserts with `source=CSV` + `rawSourceJson` payload; dedupe via `sourceKey`.

## Analytics
- Dashboard uses SQL aggregations in `AnalyticsRepository`:
  - income sum, expense sum
  - category totals for the month

## Budgets & alerts
- Budgets are monthly (`monthStartEpochMs`) and can be overall or per-category.
- `BudgetAlertWorker` runs daily (WorkManager) and:
  - computes spend vs budget
  - triggers a notification when thresholds are crossed
  - stores `lastNotifiedThreshold` to prevent repeated alerts

## Local authentication
- Optional app lock gate using biometric/device credential.
- When enabled, app shows a lock screen and requires authentication before showing content.

