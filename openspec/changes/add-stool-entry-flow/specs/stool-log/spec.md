# Spec Delta

## Purpose

Defines local recording and display of bowel movement entries with visual Bristol type selection and blood and urgency flags. This change targets Android; iOS development and verification are out of scope until re-enabled by the user.

## ADDED Requirements

### Requirement: Open entry actions from the bottom of the screen

The application MUST provide an accessible plus button at the bottom right of the log screen that opens a dropdown or popover. It MUST remain accessible regardless of scroll position. The menu MUST show "Stuhlgang eintragen", "Essen eintragen", and "Schmerzen eintragen" in German. The stool action MUST open a new bowel movement form; the meal and pain actions MUST be disabled and visibly marked "demnächst". User-facing and accessibility text MUST also be available in English.

#### Scenario: Open the entry form

- **WHEN** the user opens the plus menu and selects the stool action
- **THEN** a new entry form opens using the Bristol default defined below and the "Blut" and "Dringend" checkboxes unchecked

#### Scenario: Show future actions without implying availability

- **WHEN** the user opens the plus menu
- **THEN** meal and pain actions are visible, disabled, and marked as coming soon

#### Scenario: Dismiss the menu

- **WHEN** the user dismisses the menu without selecting the stool action
- **THEN** no form opens and no entry is created

#### Scenario: Display a long log

- **WHEN** the user scrolls through a long stool log
- **THEN** the add button remains usable at the bottom of the screen without obscuring the last entry or system controls

### Requirement: Select an event date and time

The form MUST allow selecting a local date and time, initially set to the current local date and time. It MUST accept valid current or past instants and MUST NOT save future instants. The application MUST store the selected instant as a UTC epoch timestamp and MUST show a validation message for future, invalid, or ambiguous local times instead of silently changing the selection.

#### Scenario: Record a past event

- **WHEN** the user selects a valid past local date and time and saves the form
- **THEN** the saved timestamp represents that selected instant

#### Scenario: Reject a future event

- **WHEN** the selected instant is in the future at the time of saving
- **THEN** the entry is not saved and an understandable validation message is shown

#### Scenario: Handle a clock change

- **WHEN** the selected local date and time does not identify a unique instant because of a daylight-saving transition
- **THEN** the application requests a valid unambiguous selection and does not silently alter it

### Requirement: Select a Bristol type visually and unambiguously

The form MUST offer Bristol types 1 through 7, each with its own schematic visualization, type number, and short German shape description. At most one type MUST be selected. Selection state and labels MUST be understandable without color perception and with a screen reader. Saving MUST be disabled without a selection.

#### Scenario: Change the selection

- **WHEN** the user selects type 2 and then type 6
- **THEN** only type 6 is marked as selected and is used when saving

#### Scenario: Start without a Bristol selection

- **WHEN** the entry form has no Bristol type selected
- **THEN** all seven visualized options are accessible and saving is disabled

#### Scenario: Use a small display

- **WHEN** the form is displayed on a small screen or with enlarged text
- **THEN** all seven types, both checkboxes, and save and cancel actions remain accessible through scrollable content where necessary

### Requirement: Preselect the last successfully saved Bristol type

A new entry MUST preselect the Bristol type of the last successfully saved stool entry, including after app restarts. Without a previous saved entry, no type MUST be selected. The user MUST be able to change the preselection. Cancelled drafts and failed saves MUST NOT change the default. "Blut" and "Dringend" MUST start unchecked regardless of previous flags.

#### Scenario: Reuse the last saved type

- **WHEN** the last successfully saved entry has Bristol type 4 and the user opens a new entry
- **THEN** type 4 is selected, saving is enabled, and both checkboxes are unchecked

#### Scenario: Reuse the type after restart

- **WHEN** the last successfully saved entry has Bristol type 6 and the app is restarted before opening a new entry
- **THEN** type 6 is preselected

#### Scenario: Save an older event last

- **WHEN** the user saves a backdated type 5 entry after a chronologically newer type 3 entry and restarts the app
- **THEN** the next new entry preselects type 5 even though the type 3 entry remains first in the chronological log

#### Scenario: Upgrade data without recorded save order

- **WHEN** an existing dataset without save-order metadata is upgraded
- **THEN** the default is initialized from its latest event timestamp, breaking timestamp ties deterministically by ID, or remains unset if no stool entry exists

#### Scenario: First entry

- **WHEN** no stool entry has previously been saved
- **THEN** no Bristol type is selected and saving remains disabled until the user selects one

#### Scenario: Ignore cancelled or failed changes

- **WHEN** the last successfully saved entry has type 3 and a draft changed to type 7 is cancelled or its save fails
- **THEN** the default for the next new entry remains type 3

#### Scenario: Update the default after success

- **WHEN** the user changes a preselected type 3 to type 5 and successfully saves the entry
- **THEN** the next new entry preselects type 5

### Requirement: Record blood and urgency independently

The form MUST provide checkboxes with the visible labels "Blut" and "Dringend". Both MUST be independently toggleable and MUST be preserved unchanged as flags on the saved entry.

#### Scenario: Combine flags

- **WHEN** the user saves a valid Bristol type and any of the four combinations of both checkboxes
- **THEN** the entry contains exactly that combination and changing one checkbox does not change the other

### Requirement: Save entries only after confirmation and exclusively locally

The application MUST persist a valid entry locally only through "Speichern", assigning a stable ID and the UTC timestamp corresponding to the selected date and time. The entry and the last-saved Bristol default MUST be updated atomically. It MUST prevent a second save while saving is in progress. After success, the form MUST close and the log MUST display the entry. Cancelling or navigating back before saving MUST discard the draft without persistence.

#### Scenario: Save offline

- **WHEN** the user saves a valid entry without a network connection
- **THEN** exactly one new entry with the Bristol type and both flag values appears in the log

#### Scenario: Activate save repeatedly

- **WHEN** the user activates save again while a save operation is in progress
- **THEN** this does not create an additional entry

#### Scenario: Discard an entry

- **WHEN** the user cancels an unsaved entry or leaves it through back navigation
- **THEN** the saved log remains unchanged and the next new entry uses the last successfully saved Bristol type, or no selection if none exists, with both checkboxes unchecked

#### Scenario: Saving fails

- **WHEN** local saving fails
- **THEN** the draft is retained, an understandable error message is displayed, and retrying is possible without falsely indicating success

### Requirement: Display the saved stool log

The application MUST display locally saved bowel movement entries in descending event time order, newest first. Each entry MUST show its date and time in the local time zone, Bristol type number, and enabled flags as "Blut" and "Dringend" respectively. Disabled flags MUST NOT receive positive indicators. Entries MUST persist across app restarts.

#### Scenario: Multiple entries and restart

- **WHEN** multiple entries with different timestamps have been saved and the app is restarted
- **THEN** the saved values appear in full, in descending event time order, without fabricated entries

### Requirement: Visualize Bristol shape and urgency in the saved stool log

Each saved entry MUST show the schematic Bristol shape corresponding to its type. When blood is enabled, the shape MUST be red, while the blood label remains visible so the meaning is not conveyed by color alone. When urgency is enabled, the entry MUST show an additional urgency pictogram alongside its text label. Entries without urgency MUST NOT show the urgency pictogram. The shape description MUST be accessible.

#### Scenario: Visualize the saved stool type and flags

- **WHEN** an entry with Bristol type 4 is displayed with blood and urgency enabled
- **THEN** its type-4 stool shape is shown in red, its blood and urgency labels are shown, and an additional urgency pictogram is visible

#### Scenario: Do not show disabled flag pictograms

- **WHEN** an entry has blood and urgency disabled
- **THEN** its stool shape uses the normal color and no urgency pictogram is shown

#### Scenario: Match drawing to Bristol type

- **WHEN** an entry with Bristol type 2 appears in the log
- **THEN** the log shows the schematic drawing for type 2 and provides its type in the accessible description

#### Scenario: Empty log

- **WHEN** loading has completed successfully and no bowel movement entries exist
- **THEN** the application displays an understandable empty state and the add button

#### Scenario: Loading or read failure

- **WHEN** the log is still loading or local reading fails
- **THEN** the application displays a loading state or an understandable error message with a retry action respectively, rather than a misleading empty state
