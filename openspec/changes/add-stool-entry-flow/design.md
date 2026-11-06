# Design

## Context

See `proposal.md` for motivation. The existing `DiaryApp` displays only placeholders and receives no repositories. `MainActivity` and `MainViewController` launch it directly. Platform-specific Room builders already exist but are not connected there yet.

`StoolEntry` and `StoolEntryEntity` contain an ID, event time, Bristol type, and optional note. `RoomStoolEntryRepository` maps between them; the DAO supplies a Flow in descending event time order. `DiaryDatabase` is at version 1, has no migration, and also contains meals. `DiaryEntryTest` currently checks only individual domain validations. Shared UI and persistence tests have not been configured.

This change requires a technical design because it affects the data model, migration, and integration on both platforms. Existing specifications do not conflict with the proposal: the current UI must not show simulated data, and real local entries are compatible with that constraint.

## Goals / Non-Goals

**Goals:**

- Use the existing repository boundary end to end for real data and keep state logic testable without Room.
- Share UI, drawings, and entry logic between Android and iOS.
- Support database upgrades without losing stool or meal data.

**Non-Goals:**

- No general navigation framework, DI framework, or redesign of the meal architecture.
- No new medical interpretation of the Bristol scale or checkboxes.
- No guarantee of restoring an unsaved draft after process termination; saved data remains persistent.

## Decisions

### 1. Extend the existing domain and repository contracts

Add `hasBlood: Boolean = false` and `isUrgent: Boolean = false` at the end of the `StoolEntry` parameter list. This preserves compatibility with existing calls, including the optional note. Add corresponding fields to the Room entity and map them explicitly in both directions. Domain and repository contracts remain independent of Room; Bristol validation and the meal model remain unchanged.

Alternative: encode flags in the note. Rejected because this prevents structured values, independent checkboxes, and reliable presentation.

### 2. Room version 2 with a shared explicit migration

A shared migration from version 1 to 2 adds two non-null integer columns with SQL default 0 to `stool_entries`. Entity definitions declare matching database defaults so schema validation and fresh installations agree. Both platform builders register the same migration. No destructive fallback; `meal_entries` and existing columns remain unchanged.

Alternative: recreate the database or add only Kotlin defaults. Rejected because of data loss or the absence of a SQLite migration, respectively.

### 3. Explicit wiring and lightweight shared state logic

Platform entry points create one long-lived database/repository instance per app host, not on each recomposition. The shared app receives the `StoolEntryRepository`; concrete Room types stay outside the UI. Initialization failures are exposed as an error state, not replaced with an empty in-memory data source.

A small shared state holder separates loading, the list, read errors, the draft, and saving in progress. It observes `observeAll()` in a UI-bound coroutine scope and supports reloading after failures. Coroutine cancellation is propagated; save/read failures are deliberately converted into visible states. Draft values survive recomposition. Time and ID generation remain injectable for deterministic tests and use shared Kotlin time/UUID support in production; local date formatting may receive a targeted multiplatform dependency if needed.

Initialize each new draft from the Bristol type of the last successfully saved stool entry. With the current flow assigning event time at save and offering no retrospective time editing, the first entry in the existing descending repository result supplies this default after restart. After confirmed save success, update the in-session default immediately rather than waiting for the observation Flow. Do not change it on selection alone, cancellation, or failure. Keep both checkbox defaults false. Wait for the initial repository load before initializing a new draft; a read failure remains an explicit retryable error rather than being treated as no history. Apply the default only when creating a draft, never overwrite a user's selection when later Flow emissions arrive. No additional database field or preferences store is required for this flow.

Saving captures the Bristol value and checkboxes together, assigns an ID and current UTC timestamp, and permits only one in-flight save. Retrying after failure uses the same entry identity so an already completed upsert is not duplicated. Saving and discarding are disabled during the write; retrying and cancelling become available again after failure. The form closes only after confirmed repository success; the list follows the repository Flow rather than optimistic fabricated data.

Alternative: direct DAO calls from composables or a comprehensive ViewModel/DI framework. Rejected because of an architectural violation or unnecessary infrastructure for a single flow, respectively.

### 4. Bottom action area and dedicated entry form

The log screen receives a fixed bottom action area with a toilet icon and the accessible name "Stuhlgang hinzufügen". Insets and list spacing separate it from system navigation and the last entry. Replace the existing stool placeholder and notice about future entry forms; retain the nutrition section without adding functionality.

Present the form as a dedicated shared screen within the app, with scrollable content and explicit save/cancel actions. Android system back discards an unsaved draft; iOS receives a visible back/cancel action. A small screen state is sufficient without a new navigation framework.

Defaults for this initial entry flow: preselect the last successfully saved Bristol type, or leave it unselected when no entry exists; both checkboxes are false. Event time is assigned when saving, with no time picker or note field. Existing notes are nevertheless preserved unchanged in storage and migration. The log shows date/time in the local time zone, the type number, and only enabled flags; no automatic warnings or diagnoses.

Alternative: a compact bottom sheet. Rejected in favor of sufficient room for seven visualizations, enlarged text, and small displays.

### 5. Bristol visualization without external image sources

Seven reusable selection rows contain original Compose Canvas/vector drawings, a type number, a short German description, and selection semantics. Shapes distinguish separate hard pellets (1), a lumpy sausage (2), a sausage with cracks (3), a smooth soft sausage (4), separate soft pieces (5), mushy pieces with ragged edges (6), and watery consistency (7). The drawings provide schematic guidance, not a diagnosis.

Text and selection state are available to screen readers; decorative drawings do not produce duplicate announcements. Selection is not indicated by color alone. The toilet icon is also an original shared vector. Do not substitute downloaded scale images, image licensing dependencies, or platform emojis for the seven visualizations.

Alternative: a numeric dropdown or external images. The former does not satisfy visual selection; the latter introduces unnecessary licensing/asset dependencies.

### 6. Verify behavior at multiple levels

Shared tests extend `DiaryEntryTest` to cover both defaults, all four flag combinations, and Bristol boundaries. Mapping and state tests use a deterministic clock/IDs and a fake repository to cover selection changes, cancellation, errors/retries, duplicate confirmation, and Flow updates. Default-selection tests cover no history, successful save, cancelled and failed changes, restart from persisted history, initial loading/read failure, and preserving an active draft selection across later Flow emissions.

A real SQLite/Room test checks fresh installation and migration of a version-1 dataset, including meals, notes, schema, and subsequent reading/writing of all flag combinations. Add the smallest suitable host or Android instrumentation test configuration; a mock alone does not replace migration testing. Preserve schema exports for versions 1 and 2.

Compose interaction tests check seven selection options, saving disabled without a type, independent checkboxes, cancellation, and successful log display; add only necessary test dependencies. An Android device/emulator and iOS simulator supplement testing of insets, system back, screen readers, enlarged text, actual drawings, and restarts. iOS cannot run on Windows; perform this check on macOS rather than claiming success.

## Risks / Trade-offs

- [Data loss or mismatched Room schemas] -> Explicit migration, matching SQL defaults, and a real version-1 upgrade test before release.
- [Duplicate entries during delayed saves] -> In-flight lock and stable entry identity for repeated upserts.
- [Ambiguous drawings] -> Manually review all seven shapes side by side; the number and short description remain visible.
- [Different platform navigation and insets] -> Shared screen with narrow platform integration and manual Android/iOS checks.
- [No existing UI/database test infrastructure] -> Add only targeted test configuration; do not rely solely on domain tests for migration and critical interactions.
- [Flags were never collected for legacy data] -> `false` means only "not marked", not medically confirmed absence; the UI makes no positive or diagnostic claims about it.

## Migration Plan

1. Preserve the version-1 schema from the previous model, then add the new fields and version-2 schema.
2. Implement the shared migration, register it in Android and iOS builders, and verify it against existing stool/meal data.
3. Release repository wiring and UI together; fresh installations and upgrades use the same version-2 contract.
4. Update the README with the entry flow and targeted test/migration checks.
5. Rolling back to the old version is not transparent after the database upgrade. Do not delete data automatically or use a downgrade fallback; fix issues before release or provide a corrected forward migration.
