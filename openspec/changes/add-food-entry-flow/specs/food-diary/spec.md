# Spec Delta

## Purpose

The food diary lets people record meals from a searchable bilingual food catalog or by adding their own foods. It keeps entries and reusable custom foods on the device and supports meal-level portions with optional per-food overrides.

## ADDED Requirements

### Requirement: Search a bilingual food catalog

The application MUST provide a searchable catalog seeded with common vegetables, fruits, meats, and legumes, with German and English names for built-in foods. Searches MUST match either language regardless of the active app language, and results MUST show the name for the active language.

#### Scenario: Find a food in either language

- **WHEN** a person searches for a built-in food using its German or English name
- **THEN** the matching food is shown even when the app is using the other language

#### Scenario: Browse common food categories

- **WHEN** a person opens the food picker without entering a search query
- **THEN** the seeded catalog offers common vegetables, fruits, meats, and legumes

### Requirement: Add built-in and custom foods to a meal

The application MUST let a person add one or more catalog foods to a meal and MUST let them enter a food not found in the catalog as free text. A saved custom food MUST remain available in the local catalog for later searches.

#### Scenario: Add a catalog food

- **WHEN** a person selects a food from search results
- **THEN** that food is added to the current meal

#### Scenario: Add and reuse a custom food

- **WHEN** a person enters a food that is not in the catalog and saves the meal
- **THEN** the meal contains that food and it can be found in later catalog searches

#### Scenario: Discard an unsaved custom food

- **WHEN** a person cancels a meal containing a newly entered custom food
- **THEN** neither the meal nor that custom food is saved

### Requirement: Set meal and food portions

Each meal MUST have a portion size of small, medium, or large. Each food in the meal MUST inherit that size unless the person explicitly sets a different size for that food.

#### Scenario: Inherit the meal portion size

- **WHEN** a food is added to a meal with a selected portion size
- **THEN** the food uses the meal's portion size unless it has an individual override

#### Scenario: Change the meal portion size

- **WHEN** a person changes a meal's portion size
- **THEN** foods without individual overrides use the new size and foods with overrides keep their selected sizes

#### Scenario: Override a food's portion size

- **WHEN** a person assigns a portion size to an individual food
- **THEN** that food uses the chosen size without changing the meal's default or other foods

### Requirement: Save meals with an editable event time

The application MUST allow a person to enter a meal's local date and time and MUST save the selected instant and all its foods and portion sizes locally when the person confirms with Save. Saving MUST require at least one food, and cancelling MUST discard the unsaved meal.

#### Scenario: Save a meal

- **WHEN** a person saves a meal with at least one food and a valid event time
- **THEN** the meal, its selected time, foods, portion sizes, and note if present are persisted locally

#### Scenario: Require a food before saving

- **WHEN** a meal has no foods
- **THEN** saving is unavailable and no meal is persisted

#### Scenario: Cancel a meal

- **WHEN** a person cancels before saving
- **THEN** the meal draft is discarded and is absent from the saved diary

### Requirement: Display saved meals from local storage

The application MUST show saved meals in descending event-time order, with each meal's local date and time, foods, and effective portion sizes. Saved meals MUST remain available after an app restart and without a network connection.

#### Scenario: Review saved meals offline

- **WHEN** a person opens the food diary without a network connection
- **THEN** previously saved meals appear newest first with their foods and effective portion sizes

#### Scenario: Show inherited and overridden sizes

- **WHEN** a saved meal contains foods with inherited and overridden portion sizes
- **THEN** each food displays the size that applies to it

### Requirement: Preserve existing meal records

When the local database is upgraded to support foods and portions, the application MUST preserve existing meal timestamps, descriptions, and notes without inventing historical portion choices.

#### Scenario: Upgrade an existing meal diary

- **WHEN** a person opens a database containing meals from the previous schema
- **THEN** each previous meal remains available with its original timestamp, description, and note, and its unavailable historical portion is represented using the standard medium size
