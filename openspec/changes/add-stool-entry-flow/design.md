# Design

## Context

See `proposal.md` for motivation. The existing `DiaryApp` displays only placeholders and receives no repositories. Android's `MainActivity` launches it directly. The Android Room builder already exists but is not connected there yet.

`StoolEntry` and `StoolEntryEntity` contain an ID, event time, Bristol type, and optional note. `RoomStoolEntryRepository` maps between them; the DAO supplies a Flow in descending event time order. `DiaryDatabase` is at version 1, has no migration, and also contains meals. `DiaryEntryTest` currently checks only individual domain validations. Shared UI and persistence tests have not been configured.

This change requires a technical design because it affects the data model, migration, and Android integration. Existing specifications do not conflict with the proposal: the current UI must not show simulated data, and real local entries are compatible with that constraint. iOS development and verification are out of scope until the user re-enables them.

## Goals / Non-Goals

**Goals:**

- Use the repository boundary end to end for real data and keep state logic testable without Room.
- Share UI, drawings, and entry logic in common code, with Android as the only platform target in this change.
- Support database upgrades without losing stool or meal data.

**Non-Goals:**

- No general navigation framework, DI framework, or redesign of the meal architecture.
- No new medical interpretation of the Bristol scale or checkboxes.
- No guarantee of restoring an unsaved draft after process termination; saved data remains persistent.

## Decisions

### 1. Extend the existing domain and repository contracts

Add `hasBlood: Boolean = false` and `isUrgent: Boolean = false` at the end of the `StoolEntry` parameter list. This preserves compatibility with existing calls, including the optional note. Add corresponding fields to the Room entity and map them explicitly in both directions. Extend the repository contract to read the last successfully saved Bristol type separately from the chronologically ordered log. Domain and repository contracts remain independent of Room; Bristol validation and the meal model remain unchanged.

Alternative: encode flags in the note. Rejected because this prevents structured values, independent checkboxes, and reliable presentation.

### 2. Room version 2 with a shared explicit migration

A shared migration from version 1 to 2 adds two non-null integer columns with SQL default 0 to `stool_entries` and creates a singleton metadata table for the last-saved Bristol type. Entity definitions declare matching database defaults so schema validation and fresh installations agree. The Android database builder registers the migration. No destructive fallback; `meal_entries` and existing columns remain unchanged.

The entry write and metadata update occur in one database transaction. Since version 1 did not record save order, initialize the metadata from the legacy entry with the latest event timestamp, using its ID as a deterministic tie-breaker; leave it unset if no entry exists. This is a migration fallback, not a claim that the historical save order is known.

Alternative: recreate the database or add only Kotlin defaults. Rejected because of data loss or the absence of a SQLite migration, respectively.

### 3. Explicit wiring and lightweight shared state logic

Platform entry points create one long-lived database/repository instance per app host, not on each recomposition. The shared app receives the `StoolEntryRepository`; concrete Room types stay outside the UI. Initialization failures are exposed as an error state, not replaced with an empty in-memory data source.

A small shared state holder separates loading, the list, read errors, the draft, and saving in progress. It observes `observeAll()` in a UI-bound coroutine scope and supports reloading after failures. Coroutine cancellation is propagated; save/read failures are deliberately converted into visible states. Draft values survive recomposition. Time and ID generation remain injectable for deterministic tests and use shared Kotlin time/UUID support in production; local date formatting may receive a targeted multiplatform dependency if needed.

Initialize each new draft from the durably stored Bristol type of the last successfully saved stool entry, independently of event-time ordering. After confirmed save success, update the in-session default immediately rather than waiting for an observation Flow. Do not change it on selection alone, cancellation, or failure. Keep both checkbox defaults false. Wait for the initial repository/default load before initializing a new draft; a read failure remains an explicit retryable error rather than being treated as no history. Apply the default only when creating a draft, never overwrite a user's selection when later Flow emissions arrive.

Saving captures the selected local date and time, Bristol value, and checkboxes together, converts the selected time to a UTC timestamp, assigns an ID, and permits only one in-flight save. Initialize the date and time to the current local time when opening the form. Reject future instants when saving, including if the clock changes while editing, and surface invalid or ambiguous local times rather than silently changing the selected value. Retrying after failure uses the same entry identity so an already completed upsert is not duplicated. Saving and discarding are disabled during the write; retrying and cancelling become available again after failure. The form closes only after confirmed repository success; the list follows the repository Flow rather than optimistic fabricated data.

Alternative: direct DAO calls from composables or a comprehensive ViewModel/DI framework. Rejected because of an architectural violation or unnecessary infrastructure for a single flow, respectively.

### 4. Bottom action area and dedicated entry form

The log screen receives a fixed plus button at the bottom right. It opens a dropdown or popover with "Stuhlgang eintragen", "Essen eintragen", and "Schmerzen eintragen". Only the stool action is enabled; the other two are visibly disabled and marked "demnächst". The menu can be dismissed without creating an entry. Insets and list spacing separate the action from system navigation and the last entry. Replace the existing stool placeholder and notice about future entry forms; retain the nutrition section without adding functionality. All new visible and accessibility strings support German and English.

Present the form as a dedicated shared screen within the app, with scrollable content and explicit save/cancel actions. Android system back discards an unsaved draft. A small screen state is sufficient without a new navigation framework.

Defaults for this initial entry flow: preselect the last successfully saved Bristol type, or leave it unselected when no entry exists; both checkboxes are false. Date and time start at the current local time and can be changed to a valid non-future time. There is no note field. Existing notes are nevertheless preserved unchanged in storage and migration. The log shows date/time in the local time zone, the type number, and only enabled flags; no automatic warnings or diagnoses.

Alternative: a compact bottom sheet. Rejected in favor of sufficient room for seven visualizations, enlarged text, and small displays.

### 5. Bristol visualization without external image sources

Seven reusable selection rows contain original Compose Canvas/vector drawings, a type number, a short German description, and selection semantics. Shapes distinguish separate hard pellets (1), a lumpy sausage (2), a sausage with cracks (3), a smooth soft sausage (4), separate soft pieces (5), mushy pieces with ragged edges (6), and watery consistency (7). The drawings provide schematic guidance, not a diagnosis.

Text and selection state are available to screen readers; decorative drawings do not produce duplicate announcements. Selection is not indicated by color alone. Do not substitute downloaded scale images, image licensing dependencies, or platform emojis for the seven visualizations.

Alternative: a numeric dropdown or external images. The former does not satisfy visual selection; the latter introduces unnecessary licensing/asset dependencies.

### 6. Visualize entry details in the log

Reuse the Bristol drawing from the entry selector in each saved log row, next to its type number. Render the drawing in red when the entry's blood flag is true; keep the explicit blood label so the meaning is not conveyed by color alone. Show a small, original exclamation-in-circle pictogram next to the urgency label only when urgency is true. Keep both indicators within the entry row and retain text labels for screen-reader access.

### 7. Verify behavior at multiple levels

Shared tests extend `DiaryEntryTest` to cover both defaults, all four flag combinations, and Bristol boundaries. Mapping and state tests use a deterministic clock/IDs and a fake repository to cover selection changes, cancellation, errors/retries, duplicate confirmation, and Flow updates. Default-selection tests cover no history, successful save of a backdated entry, cancelled and failed changes, restart from persisted metadata, initial loading/read failure, and preserving an active draft selection across later Flow emissions. Time tests cover future rejection, local-to-UTC conversion, and daylight-saving gaps and overlaps.

A real SQLite/Room test checks fresh installation and migration of a version-1 dataset, including meals, notes, schema, deterministic legacy default, and subsequent reading/writing of all flag combinations. Verify the entry and default update atomically and that a backdated save remains the default after reopening. Add the smallest suitable host or Android instrumentation test configuration; a mock alone does not replace migration testing. Preserve schema exports for versions 1 and 2.

Compose interaction tests check the plus menu and disabled future actions, date/time editing and future rejection, seven selection options, saving disabled without a type, independent checkboxes, cancellation, and successful log display. They also verify that saved Bristol drawings match their type, blood turns the shape red, and urgency displays its pictogram only when enabled; add only necessary test dependencies. An Android device/emulator supplements testing of insets, system back, screen readers, enlarged text, actual drawings, and restarts.

## Risks / Trade-offs

- [Data loss or mismatched Room schemas] -> Explicit migration, matching SQL defaults, and a real version-1 upgrade test before release.
- [Duplicate entries during delayed saves] -> In-flight lock and stable entry identity for repeated upserts.
- [Ambiguous drawings] -> Manually review all seven shapes side by side; the number and short description remain visible.
- [Platform navigation and insets] -> Shared screen with narrow Android integration and manual Android checks.
- [No existing UI/database test infrastructure] -> Add only targeted test configuration; do not rely solely on domain tests for migration and critical interactions.
- [Flags were never collected for legacy data] -> `false` means only "not marked", not medically confirmed absence; the UI makes no positive or diagnostic claims about it.
- [Legacy save order is unknown] -> Seed the default deterministically from the latest event; subsequent saves update it transactionally, even for backdated entries.
- [Local time can be invalid or ambiguous during clock changes] -> Require an unambiguous valid instant and show an actionable validation message rather than silently changing it.

## Migration Plan

1. Preserve the version-1 schema from the previous model, then add the new fields and version-2 schema.
2. Implement the shared migration, register it in the Android builder, and verify it against existing stool/meal data.
3. Release repository wiring and UI together; fresh installations and upgrades use the same version-2 contract.
4. Update the README with the entry flow and targeted test/migration checks.
5. Rolling back to the old version is not transparent after the database upgrade. Do not delete data automatically or use a downgrade fallback; fix issues before release or provide a corrected forward migration.
