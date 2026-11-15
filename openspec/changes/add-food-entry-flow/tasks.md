# Tasks

## 1. Meal domain and catalog behavior

- [x] 1.1 Model meal portions and multiple food selections, including optional per-food overrides, and verify default inheritance and override behavior with shared unit tests.
- [x] 1.2 Define the bilingual built-in catalog and local catalog search contract, and verify category coverage, German/English matching, display-name selection, and case-insensitive custom-food reuse with shared unit tests.
- [x] 1.3 Extend the meal repository contract and shared meal state holder for search, editable time, draft add/remove, save, cancellation, and errors; verify lifecycle and validation behavior with fake-repository tests.

## 2. Local persistence and migration

- [x] 2.1 Add Room entities and DAO operations for catalog foods and meal-food selections, seed built-in foods idempotently, and verify fresh-database catalog search and meal persistence with Room tests.
- [x] 2.2 Upgrade the Room schema from version 2 to 3 without destructive migration, preserve legacy descriptions/timestamps/notes, and verify migration defaults legacy portions to medium using a real version-2 database fixture.
- [x] 2.3 Verify transactional meal saves persist custom catalog foods only with a saved meal, preserve per-food overrides, and leave neither partial meals nor orphan custom foods after failure.
- [x] 2.4 Update the README's database version and migration description, and verify it documents the version-2-to-3 upgrade and legacy medium-size fallback accurately.

## 3. Android food-entry experience

- [x] 3.1 Add the shared Compose food picker and meal editor with bilingual search results, custom free-text entry, editable date/time, meal portion selection, and per-food overrides; verify these interactions with Compose UI tests.
- [x] 3.2 Add German and English labels, validation/error messages, and accessibility text for food search, selected foods, portions, and save/cancel actions; verify required resource coverage in both locale files.
- [x] 3.3 Wire the meal repository from the Android database, enable the food action only when the repository is available, and display saved meals with event time and effective portions; verify the Android flow and retained list with UI tests.
- [x] 3.4 Update the README with the Android food-entry flow and offline catalog behavior, and verify the described user behavior matches the implemented screens.

## 4. Integration verification

- [x] 4.1 Run `.\gradlew.bat :shared:testAndroidHostTest` and confirm the shared domain, catalog, and state tests pass.
- [x] 4.2 Run `.\gradlew.bat :androidApp:connectedDebugAndroidTest` on an emulator or device and confirm database migration, persistence, and food-entry UI tests pass.
- [x] 4.3 Run `.\gradlew.bat :androidApp:assembleDebug` and confirm the Android app builds with the food flow enabled.
