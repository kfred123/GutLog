# Tasks

## 1. Overlay state in `MealLogState`

- [x] 1.1 Add `foodSearchOpen` state with `openFoodSearch()` (no-op without a draft or while saving; resets search error and runs an empty-query search) and `closeFoodSearch()`; verify with new `MealLogStateTest` cases for open, no-op without draft, and reopen listing the seeded catalog.
- [x] 1.2 Close the overlay in `addFood`, `addCustomFood`, `cancelDraft`, and after a successful save; verify `MealLogStateTest` asserts the overlay is closed after adding a catalog food and a custom food, that the food was added exactly once, and that closing without selection leaves the draft unchanged.

## 2. Split the meal UI

- [x] 2.1 Add English and German strings for the food search overlay title, the Add action, and the close action in `shared/src/commonMain/composeResources/values/strings.xml` and `values-de/strings.xml`; verify the shared module compiles with the generated resource accessors.
- [x] 2.2 Reduce `MealEntryForm` in `StoolLogScreen.kt` to the meal overview (date/time, added foods with remove and portion controls, meal portion, `add-food` button, save error, Cancel/Save) with no inline search field or results; verify by building the Android app and the updated UI test in 2.4.
- [x] 2.3 Add a `FoodSearchOverlay` composable rendered above the still-composed overview while `foodSearchOpen` is true, containing a title, `close-food-search` action, the `food-search` field (query local to the overlay, empty on open), `catalog-food-<id>` results, loading/error text, and `add-custom-food`; handle system back as close and clear focus on close; verify date/time edits in the overview survive opening and closing the overlay in the UI test in 2.4.
- [x] 2.4 Update `androidApp/src/androidTest/kotlin/de/gutlog/app/FoodDiaryUiTest.kt` so flows tap `add-food` before searching, assert the overlay closes after selecting a catalog food and after adding a custom food, add two foods via two overlay openings, dismiss via close and via back without changing the meal, and confirm the search field is absent from the overview; verify the instrumented tests pass.

## 3. Integration check

- [x] 3.1 Run the shared unit tests and the Android build (`./gradlew :shared:allTests :androidApp:assembleDebug` or the project's equivalent) and manually confirm on a device or emulator that a meal with several foods can be composed via the overlay and saved; verify all pass and the saved meal appears in the diary.
