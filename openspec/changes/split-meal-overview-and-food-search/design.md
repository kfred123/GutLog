# Design

## Context

See `proposal.md` for motivation and `specs/food-diary/spec.md` for the contract. Today `StoolLogScreen` renders `MealEntryForm` whenever `MealLogState.draft` is non-null. That single composable holds the date/time text fields in local `remember` state, the search query in local state, search results from `MealLogState.searchResults`, the custom-food action, selected foods with portion selectors, the meal portion, and Save/Cancel. `MealLogState.openDraft()` already triggers an empty-query search so the seeded catalog is available. `FoodDiaryUiTest` drives search, add, portions, save, and cancel through test tags such as `food-search`, `catalog-food-<id>`, `add-custom-food`, `meal-food-<id>`, and `cancel-meal`.

## Goals / Non-Goals

**Goals:**

- Split the form into a meal overview composable and a food search overlay composable in shared UI.
- Keep overlay visibility in testable shared state, not only in composable-local state.
- Keep the overview's unsaved date/time input intact while the overlay is open.

**Non-Goals:**

- No change to `MealEntryRepository`, Room, catalog seeding, or portion rules.
- No new navigation library; the overlay is a layer on the existing screen.
- No iOS-specific work.

## Decisions

### 1. Track overlay visibility in `MealLogState`

Add a `foodSearchOpen: StateFlow<Boolean>` with `openFoodSearch()` and `closeFoodSearch()`. `openFoodSearch()` is a no-op without a draft or while saving; it clears search error and runs `searchFoods("")` so the seeded catalog is shown. `addFood(...)` and `addCustomFood(...)` close the overlay after adding. `cancelDraft()` and a successful save also close it. This keeps the auto-close rule unit-testable with the existing fake repository.

Alternative: keep an `isOpen` flag in composable `remember` state. Rejected because the close-after-add rule would only be testable through UI tests.

### 2. Render the overlay above a still-composed overview

`MealEntryForm` becomes the overview: date/time fields, foods with portion controls, meal portion, an Add button (tag `add-food`), save error, and Cancel/Save. When `foodSearchOpen` is true, a `FoodSearchOverlay` is drawn on top of it as a full-size surface inside the same screen (e.g. a `Box` layer with an opaque background or a Compose `Dialog`), with a title, close action (tag `close-food-search`), search field, results, and custom-food action. Because the overview stays in composition, its `remember`ed date/time text is preserved. The search query is local to the overlay composable, so it starts empty on each open. Existing test tags for search, catalog items, and custom foods move into the overlay unchanged.

Alternative: replace the overview with the search screen while open. Rejected because the overview's unsaved date/time text would be lost unless hoisted into state.

### 3. Dismissal paths

The close action and the system back gesture close the overlay without changing the draft. Back handling uses the platform-available back handler for shared Compose (or the `Dialog`'s `onDismissRequest` if a dialog is used). Save is only reachable from the overview, so saving with the overlay open is not possible.

### 4. Localization and accessibility

Add English and German strings for the overlay title, the Add action, and the close action, plus content descriptions for icon-only controls. Reuse the existing search label and custom-food strings.

## Risks / Trade-offs

- [Existing UI tests assume search is inline] → Update `FoodDiaryUiTest` to tap `add-food` before searching and assert the overlay closes after each selection.
- [Back gesture might exit the meal flow instead of the overlay] → Handle back within the overlay first and cover it in a UI test.
- [A stale search response could populate a reopened overlay] → `openFoodSearch()` starts a new search generation; the existing generation check discards older results.
- [Focus/keyboard may remain after auto-close] → Clear focus when the overlay closes.

## Migration Plan

UI-only change; ship with the shared module. No data migration. Rollback is reverting the UI/state change.
