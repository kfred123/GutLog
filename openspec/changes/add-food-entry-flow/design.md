# Design

## Context

See `proposal.md` for motivation and `specs/food-diary/spec.md` for the user-visible contract. The shared `MealEntry` currently stores one description, event timestamp, and optional note. A `MealEntryRepository` and Room DAO already exist, but `DiaryApp` currently receives only the stool repository and the meal action is disabled. `DiaryDatabase` is at version 2; version 1-to-2 is an explicit migration registered by the Android and iOS database builders.

The app's language preference is English or German. The shared UI and domain code are in `commonMain`; Android creates the database and repositories in `androidMain`. This change is Android-focused and does not include iOS-specific feature wiring or verification.

## Goals / Non-Goals

**Goals:**

- Keep food selection, meal rules, and UI state testable in shared code, with persistence behind a repository contract.
- Support an offline built-in bilingual catalog and reusable user-entered foods.
- Preserve existing meal records and their notes during the schema change.
- Enable the food flow only where the meal repository is supplied, so the existing iOS host can continue to build without platform-specific changes.

**Non-Goals:**

- No nutrition quantities, calories, nutrient estimates, barcode scanning, photo recognition, or symptom analysis.
- No account, remote catalog, or synchronization service.
- No iOS-specific implementation or verification in this change.

## Decisions

### 1. Represent a meal as an aggregate with child foods

Extend the domain meal model to carry its event time, optional note, meal-level portion size, and a list of food selections. A food selection has a stable identity, a display name, an optional catalog reference, and an optional portion override. `small`, `medium`, and `large` are the only portion values. The effective size is computed as the food override when present, otherwise the meal default; changing the default therefore does not modify explicit overrides.

Keep the existing repository boundary and provide operations to observe meals, search catalog entries, and save a complete meal. Saving the meal, its food selections, and newly entered catalog foods is one transaction, preventing a partially saved meal or a custom food from surviving a cancelled/failed meal save.

Alternative: keep concatenating multiple food names into `MealEntry.description`. Rejected because it cannot reliably preserve individual selections or portion overrides.

### 2. Extend Room additively and interpret legacy descriptions

Advance the Room schema from version 2 with an automatically generated 2-to-3 migration: add the meal portion column with a `MEDIUM` database default and create catalog and meal-food tables. Keep the existing meal description column for compatibility. New records can store a readable summary there, while their child rows hold the structured selections.

Legacy meal rows have no child rows after migration. The repository maps each such row to one legacy food using its original description and event time, with `MEDIUM` as the meal default and no inferred food override. It does not rewrite the old description or invent a historical portion. The v1-to-v2 migration remains explicit; Room chains it with the generated 2-to-3 migration. The additive schema migration is declared on the shared database so Android's existing builder registration for v1-to-v2 remains sufficient and the unchanged iOS builder can continue to compile and open upgraded databases.

Alternative: destructively recreate `meal_entries` or rewrite it into a new layout. Rejected because it increases migration risk and is unnecessary for the user-visible behavior.

### 3. Seed stable built-in foods and persist custom foods locally

Keep a curated, version-controlled starter catalog in shared code, with stable IDs, category, German name, and English name. Seed missing built-in rows idempotently through the repository before catalog search; never overwrite user-created rows. Include a practical first set across vegetables, fruit, meat, and legumes, such as tomatoes, carrots, apples, bananas, chicken, beef, lentils, and chickpeas.

Search case-insensitively across both built-in names and each custom food's entered name. Display the built-in name matching the active app language; display custom food names as entered. A custom food is committed to the local catalog only in the same transaction that saves the meal. Repeated custom names are matched case-insensitively to avoid adding duplicate catalog rows.

Alternative: fetch foods from a remote service or add every unsaved draft item immediately. Rejected because remote access breaks offline use and early persistence leaves orphan custom foods after cancellation.

### 4. Keep the editor and list in shared UI, activate from Android

Add a shared meal state holder that owns loading, search results, meal draft, time validation, save status, and errors without depending on Room. The food entry form places the search field and results above the current meal's selected foods, then exposes a meal portion selector and optional per-food overrides. The existing save action closes the form only after the repository confirms success; cancellation discards the draft.

Supply the meal repository from Android's existing database instance and enable the food action when that repository is present. Keep the parameter optional for the existing iOS host, which remains outside this change. Localize all new controls, portion labels, errors, and accessibility text in English and German.

Alternative: call the meal DAO directly from composables or introduce a general navigation/dependency-injection framework. Rejected because direct persistence would bypass the existing boundary, while a larger framework is unnecessary for this flow.

### 5. Validate with shared tests and Android migration/UI coverage

Test portion inheritance and overrides, bilingual search, custom-food reuse, cancellation, save failure, selected time, and meal ordering with shared domain/state tests and a fake repository. Extend the Android Room migration test from a version-2 fixture to verify old meal descriptions, timestamps, and notes survive, default to medium, and remain readable alongside new multi-food meals. Add targeted Compose interaction coverage for search/add, free-text entry, portion inheritance and overrides, save, and cancel.

## Risks / Trade-offs

- [An additive database schema may be incomplete on one builder] -> Declare the 2-to-3 migration on the shared database and test migration from a real version-2 Room fixture; retain the explicit v1-to-v2 path.
- [A legacy meal may be mistaken for a fully structured new meal] -> Map rows without child foods as one legacy food derived from the unchanged description, with no claimed historical override.
- [Catalog seeding may run more than once] -> Use stable built-in IDs and idempotent inserts in a transaction; preserve existing custom entries.
- [Custom spelling variants may clutter search results] -> Match names case-insensitively before creating a new custom catalog row.
- [A meal default change could erase an intentional food size] -> Persist overrides separately and derive effective sizes without rewriting overrides.

## Migration Plan

1. Add the version-3 Room entities and generated additive 2-to-3 migration, while retaining the existing explicit 1-to-2 migration.
2. Verify a fresh database and an existing version-2 database; confirm legacy meals retain timestamp, description, and note and display with medium as the unrecorded default.
3. Implement and seed the local bilingual catalog, then wire the meal repository into the Android app and enable the food entry action.
4. Ship database and UI support together. Do not use destructive migration or delete existing data. Correct migration defects with a forward migration.
