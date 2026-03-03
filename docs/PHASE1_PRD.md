# Phase 1 PRD — MyExpence (Offline-first)

## Goal
Build an **offline-first** expense tracker that can capture transactions from **SMS**, **CSV bank statements**, and **manual entry**, provide a **dashboard with analytics**, support **income tracking**, create **budgets with alerts**, and protect data with **local authentication**.

## Target users
- People who want a single place to track spending and income without cloud sync.
- Internal/enterprise distribution environments where SMS reading is permitted by device policy.

## Non-goals (Phase 1)
- No cloud accounts, no cross-device sync, no multi-user sharing.
- No third-party bank integrations (Plaid, etc.).
- No receipt OCR/scanning.
- No ML-based categorization (rules-based only).

## Core concepts & definitions
- **Transaction**: A single income or expense record.
- **Candidate/Suggestion**: A parsed transaction from SMS/CSV that the user can confirm/edit.
- **Source**: One of `MANUAL`, `SMS`, `CSV`.
- **Budget**: A monthly spending limit for a category or overall spending.
- **Alert thresholds**: Spending ratio thresholds such as 80% and 100%.

## User stories (Phase 1)

### Manual entry
- As a user, I can add an **expense** with amount, date, category, and optional merchant/notes.
- As a user, I can add an **income** entry similarly.
- As a user, I can **edit** or **delete** a transaction later.

### SMS ingestion
- As a user, I can enable SMS import and grant permission.
- As a user, I can view **SMS-derived suggestions** and confirm/edit them before saving.
- As a user, I can avoid duplicates if the same SMS is processed again.

### CSV upload
- As a user, I can select a CSV file (bank statement) and import transactions.
- As a user, I can map CSV columns (date/description/amount or debit/credit).
- As a user, I can preview rows before importing.
- As a user, I can avoid duplicates across repeated imports.

### Categorization
- As a user, the app auto-categorizes transactions via rules (merchant keywords / sender keywords).
- As a user, I can override a category per transaction.

### Dashboard & analytics
- As a user, I can see monthly totals: expenses, income, net.
- As a user, I can see category breakdown for a selected month.
- As a user, I can see a trend of daily spending for recent days.

### Budgets & alerts
- As a user, I can set a monthly budget overall and/or per category.
- As a user, I receive an alert when spending crosses configured thresholds.

### Local authentication & security
- As a user, I can enable an **app lock** (biometric/PIN) to protect access.
- As a user, my data is encrypted at rest (device-local).

## Functional requirements
- Transactions must support: amount, currency, timestamp, type (income/expense), category, merchant, notes, tags (optional), source, and raw source payload when imported.
- Imports must be idempotent via **dedupe keys**.
- Analytics must compute quickly from local storage; results should be consistent and reproducible.
- Budget alerts must not spam repeated notifications for the same threshold crossing.

## Acceptance criteria (Phase 1)
- Manual add/edit/delete works and persists across restarts.
- SMS import can parse common messages into suggestions and allows confirmation.
- CSV import supports column mapping and imports into transactions; duplicates are prevented on re-import.
- Dashboard shows monthly totals and category breakdown from real stored data.
- Budgets can be created; alerts fire when thresholds are crossed.
- App lock can be enabled and blocks app access after background timeout.

## Metrics (local-only)
- Import success rate (count imported vs parsed candidates).
- Dedupe rate (duplicates avoided per import).
- Time to add a manual transaction (seconds).

