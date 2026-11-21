# Spec Delta

## ADDED Requirements

### Requirement: Show the meal being composed as an overview

While a person composes a meal, the application MUST show a meal overview containing the event date and time, the foods already added with their portion controls, the meal portion size, an Add action, and Save and Cancel. The overview MUST NOT show the food search field or search results.

#### Scenario: Open a new meal

- **WHEN** a person starts a new meal
- **THEN** the meal overview is shown with no foods, the Add action available, Save unavailable, and no search field or search results visible

#### Scenario: Review added foods

- **WHEN** a meal contains one or more foods
- **THEN** the overview lists each food with its effective portion size and lets the person remove it or change its portion

### Requirement: Search for foods in a separate overlay

The Add action on the meal overview MUST open a food search overlay above the overview. The overlay MUST contain the search field, catalog results, and the option to add the entered text as a custom food. Each time it opens, the search query MUST be empty and the seeded catalog MUST be offered.

#### Scenario: Open the food search

- **WHEN** a person selects Add on the meal overview
- **THEN** a food search overlay opens with an empty search field and the seeded catalog foods listed

#### Scenario: Search within the overlay

- **WHEN** a person enters a query in the food search overlay
- **THEN** matching catalog foods are listed and, if no result matches the query exactly, an option to add the entered text as a custom food is offered

#### Scenario: Reopen after a previous search

- **WHEN** a person reopens the food search overlay after an earlier search
- **THEN** the search field is empty and the seeded catalog is listed again

### Requirement: Return to the meal overview after each food selection

Selecting a catalog food or confirming a custom food in the food search overlay MUST add exactly that food to the meal, close the overlay, and return to the meal overview. Dismissing the overlay without a selection MUST return to the overview without changing the meal.

#### Scenario: Add a catalog food from the overlay

- **WHEN** a person selects a food from the overlay's results
- **THEN** the overlay closes and the meal overview lists that food with the meal's portion size

#### Scenario: Add a custom food from the overlay

- **WHEN** a person confirms the entered text as a custom food
- **THEN** the overlay closes and the meal overview lists that custom food

#### Scenario: Add several foods

- **WHEN** a person adds a food, then selects Add again and chooses another food
- **THEN** the meal overview lists both foods

#### Scenario: Dismiss the overlay without adding

- **WHEN** a person closes the food search overlay without selecting a food
- **THEN** the meal overview is shown and its foods, portions, and time are unchanged
