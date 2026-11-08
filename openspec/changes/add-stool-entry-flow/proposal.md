# Proposal

## Why

The diary currently provides only a landing screen and local storage contracts; users cannot yet record bowel movements through the app. Quick entry with visual Bristol type selection and blood and urgency flags will make the stool log usable for the first time.

## What Changes

- A persistently accessible plus button at the bottom right opens a popover or dropdown with "Stuhlgang eintragen", "Essen eintragen", and "Schmerzen eintragen". The latter two remain visible but disabled and marked "demnächst".
- The bowel movement form allows the user to select a date and time, including past events, but not a future time.
- The form offers a single selection from the seven Bristol types, each with a schematic visualization and German description, plus independent "Blut" and "Dringend" checkboxes.
- New entries preselect the Bristol type from the last successfully saved entry, including after app restarts. Without a previous entry, no type is selected; both checkboxes always start unchecked. Cancelled or failed entries do not change the default.
- Confirmed entries are saved locally and displayed in the stool log with their timestamp, Bristol type and shape, and selected flags. The shape is red when blood is marked, and urgency is accompanied by a pictogram; cancelling saves nothing.
- The existing stool model and Room persistence gain two boolean flags and durable metadata for the last successfully saved Bristol type. A migration preserves existing data.
- Loading, empty, and error states and protection against duplicate saves are part of the entry flow.
- Out of scope: meal and pain entry, editing or deleting through the UI, an additional note field, photo recognition, analysis, or medical assessment.

## Capabilities

### New Capabilities

- `stool-log`: Recording and displaying local bowel movement entries, including visual Bristol type selection and blood and urgency flags.

### Modified Capabilities

- `diary-foundation`: The shared stool model gains persistently stored blood and urgency flags; existing entries remain compatible. The affected German requirement is explicitly replaced with an English equivalent that preserves its existing validation scenarios.

## Impact

Affected areas are `DiaryApp.kt`, new shared entry/log components and their state logic, `StoolEntry`, `StoolEntryEntity`, the database version and migration, and Android database initialization. The Android entry point must pass the existing Room repository to the shared UI; it currently launches it without a data connection. iOS development and verification are out of scope until the user re-enables them.

The DAO's descending chronological ordering remains intact; the repository contract also needs to expose the last-saved default independently of event time. No network services or accounts are introduced. Tests and the README will be updated for the usable local stool log flow; targeted test dependencies will be added only as needed.
