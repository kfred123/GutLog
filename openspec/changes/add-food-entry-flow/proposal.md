# Proposal

## Why

The app already has a local meal-entry model and repository, but its food diary is still a placeholder. People need a quick way to record a meal by choosing common foods or entering foods that are not in the catalog, with enough portion detail to make each entry useful.

## What Changes

- Replace the food diary placeholder with a meal-entry flow that supports a user-entered time and multiple foods per meal.
- Provide a built-in catalog of common vegetables, fruits, meats, and legumes with German and English names, searchable in both languages regardless of the selected app language.
- Allow free-text foods that are not in the built-in catalog and retain them in the local catalog for later searches.
- Add a meal-level portion size of small, medium, or large. Foods inherit this default unless the user assigns an individual override; changing the meal default affects only foods without overrides.
- Save confirmed meals and catalog additions locally, offline, using the existing repository boundary.

## Capabilities

### New Capabilities

- `food-diary`: Search and add catalog or free-text foods to a meal, set meal and per-food portions, and save meals locally.

### Modified Capabilities

None.

## Impact

- Shared meal domain models and repository contracts will need to represent a meal with multiple foods and portion choices.
- Room persistence and database migration will need to preserve existing meal entries while adding meal-food and user-catalog data.
- The shared Compose UI and Android app wiring will need to expose the new food-entry flow; user-facing text must support German and English. This change targets Android; iOS-specific work and verification are out of scope.
- The built-in catalog and custom foods must be available offline. Photo recognition and food-trigger analysis remain out of scope.
