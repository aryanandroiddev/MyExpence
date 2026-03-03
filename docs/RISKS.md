# Phase 1 Risk Matrix & Mitigations

## Product / UX risks

### SMS parsing accuracy
- **Risk**: Bank SMS formats vary by bank/region; regex rules may mis-detect amount/type/merchant.
- **Impact**: Incorrect transactions, user distrust.
- **Mitigation**:
  - Treat imports as **suggestions** (confirm/edit) in next iteration.
  - Store `rawSourceJson` for traceability and debugging.
  - Add unit tests per bank template as you encounter them.
  - Add allowlist of senders and keywords.

### SMS permission & policy constraints
- **Risk**: Even in internal distribution, device policy/user settings may block SMS reading.
- **Impact**: SMS feature unusable for some deployments.
- **Mitigation**:
  - Permission-gated toggle and clear user messaging.
  - Provide CSV/manual as complete alternatives.

### Budget alerts becoming noisy
- **Risk**: Notifications may spam or trigger too often.
- **Impact**: User disables notifications or uninstalls.
- **Mitigation**:
  - Track `lastNotifiedThreshold` per budget.
  - Add settings for alert thresholds + frequency later.

## Data / correctness risks

### Duplicate imports
- **Risk**: Re-importing the same SMS/CSV creates duplicates.
- **Impact**: Inflated totals.
- **Mitigation**:
  - Deterministic `sourceKey` + unique index on `transactions.sourceKey`.
  - Keep stable sourceKey inputs (timestamp/amount/sender/desc).

### CSV format variability
- **Risk**: Different banks use different headers and date formats.
- **Impact**: Import fails or misses rows.
- **Mitigation**:
  - Phase 1 supports common headers; add mapping wizard + date-format parser variants in Phase 1.1/2.
  - Show summary counts and allow re-try.

## Security risks

### Encryption implementation details
- **Risk**: Storing passphrase incorrectly or losing it can break access to data.
- **Impact**: Data loss or weaker security.
- **Mitigation**:
  - Generate passphrase once and store in `EncryptedSharedPreferences`.
  - Avoid rotation in Phase 1; document recovery expectations.

### Rooted device threat model
- **Risk**: A rooted attacker can bypass many local protections.
- **Impact**: Data compromise.
- **Mitigation**:
  - Phase 1 threat model is “casual access prevention”; clarify in docs.

## Engineering / delivery risks

### Kotlin 2.0 + KAPT fallback
- **Risk**: KAPT falls back to language 1.9 stubs and may break with future features.
- **Impact**: Build instability.
- **Mitigation**:
  - Keep Room entities/DAO simple.
  - Move to KSP when plugin resolution is stable in your environment.

### Schema migrations
- **Risk**: Room schema changes may require migrations.
- **Impact**: Upgrades can crash or wipe data.
- **Mitigation**:
  - Add explicit migrations and schema export once the schema stabilizes.
  - Avoid `fallbackToDestructiveMigration()` for production builds later.

### Notification permissions (Android 13+)
- **Risk**: Alerts won’t show without `POST_NOTIFICATIONS` runtime grant.
- **Impact**: Budgets appear “broken.”
- **Mitigation**:
  - Add an in-app prompt and status indicator in Settings.

