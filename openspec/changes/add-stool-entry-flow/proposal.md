# Proposal

## Why

The diary currently provides only a landing screen and local storage contracts; users cannot yet record bowel movements through the app. Quick entry with visual Bristol type selection and blood and urgency flags will make the stool log usable for the first time.

## What Changes

- A persistently accessible button with a toilet icon at the bottom of the screen opens the bowel movement entry form.
- The form offers a single selection from the seven Bristol types, each with a schematic visualization and German description, plus independent "Blut" and "Dringend" checkboxes.
- New entries preselect the Bristol type from the last successfully saved entry, including after app restarts. Without a previous entry, no type is selected; both checkboxes always start unchecked. Cancelled or failed entries do not change the default.
- Confirmed entries are saved locally and displayed in the stool log with their timestamp, Bristol type, and selected flags; cancelling saves nothing.
- The existing stool model and Room persistence gain two boolean flags. A migration preserves existing data.
- Loading, empty, and error states and protection against duplicate saves are part of the entry flow.
- Out of scope: meal entry, editing or deleting through the UI, retrospective time selection, an additional note field, photo recognition, analysis, or medical assessment.

## Capabilities

### New Capabilities

- `stool-log`: Recording and displaying local bowel movement entries, including visual Bristol type selection and blood and urgency flags.

### Modified Capabilities

- `diary-foundation`: The shared stool model gains persistently stored blood and urgency flags; existing entries remain compatible. The affected German requirement is explicitly replaced with an English equivalent that preserves its existing validation scenarios.

## Impact

Affected areas are `DiaryApp.kt`, new shared entry/log components and their state logic, `StoolEntry`, `StoolEntryEntity`, the database version and migration, and database initialization on both platforms. Android and iOS entry points must pass the existing Room repository to the shared UI; they currently launch it without a data connection.

Repository contracts and the DAO's descending chronological ordering remain intact. No network services or accounts are introduced. Tests and the README will be updated for the usable local stool log flow; targeted test dependencies will be added only as needed.
