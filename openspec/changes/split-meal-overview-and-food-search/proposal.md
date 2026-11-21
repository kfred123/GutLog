# Proposal

## Why

The meal entry form currently stacks date/time fields, the food search field, all search results, the custom-food action, the selected foods with their portion controls, and the meal portion in one long scrolling column. The meal being composed is hard to see, and search results push it out of view. Separating "what is in this meal" from "find a food" makes meal entry faster and clearer.

## What Changes

- The meal entry view becomes a meal overview: event date/time, the list of foods in this meal with their portion controls, the meal portion, and Save/Cancel. It no longer shows the search field or search results inline.
- The meal overview gets an "Add" action that opens a food search overlay on top of the overview.
- The food search overlay contains the search field, catalog results (seeded catalog when the query is empty), and the free-text custom-food action. It can be dismissed without adding anything.
- Selecting a catalog food or confirming a custom food adds it to the meal and closes the overlay automatically, returning to the overview. Each further food is added by opening the overlay again.
- The search query is reset each time the overlay opens.
- New UI labels and accessibility text are localized in English and German.
- No change to meal persistence, catalog data, portion rules, or saved-meal display.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `food-diary`: adds a requirement that meal composition is presented as a meal overview with a separate, dismissible food search overlay that closes after each addition. The capability is introduced by the in-flight `add-food-entry-flow` change and is not yet in `openspec/specs/`; this change builds on it.

## Impact

- Shared UI: `shared/src/commonMain/kotlin/de/gutlog/app/ui/StoolLogScreen.kt` (`MealEntryForm` split into a meal overview and a food search overlay composable).
- Shared state: `MealLogState` gains overlay open/close state and resets search on open; existing add/remove/portion/save operations remain.
- Localization: new string resources (e.g. overlay title, add action, close action) in English and German.
- Tests: `shared/src/commonTest/.../MealLogStateTest.kt` and `androidApp/src/androidTest/.../FoodDiaryUiTest.kt` updated for the new interaction flow.
- No database, repository, or dependency changes. Android-focused; iOS remains out of scope.
