# Spec Delta

## REMOVED Requirements

### Requirement: Stuhl- und Mahlzeiteneinträge in der gemeinsamen Domäne modellieren

**Reason**: Replace the German requirement and scenario titles with English equivalents while extending the stool model with blood and urgency flags.

**Migration**: Use "Model stool and meal entries in the shared domain" below. It preserves both original validation scenarios in English and all existing model fields, adding only the planned flags. This is a specification-language replacement, not removal of domain behavior.

## ADDED Requirements

### Requirement: Model stool and meal entries in the shared domain

The shared domain MUST define a stool entry with a stable ID, UTC timestamp in
epoch milliseconds, Bristol scale value from 1 to 7, optional note, and independent
boolean blood and urgency flags. Both flags MUST default to false. It MUST also
define a meal entry with a stable ID, UTC timestamp in epoch milliseconds,
non-empty free-text description, and optional note.

#### Scenario: Reject an invalid Bristol scale value

- **WHEN** a stool entry is created with a Bristol scale value outside 1 through 7
- **THEN** domain validation rejects the entry

#### Scenario: Reject an empty meal description

- **WHEN** a meal entry is created with an empty description
- **THEN** domain validation rejects the entry

#### Scenario: Model independent stool flags

- **WHEN** a valid stool entry is created with any combination of blood and urgency
- **THEN** both values are retained independently

#### Scenario: Use default values

- **WHEN** a valid stool entry is created without explicitly specifying either flag
- **THEN** blood and urgency are both false

### Requirement: Persist new stool flags without data loss

Local persistence MUST save and read back each stool entry's blood and urgency flags unchanged. When upgrading an existing local dataset, it MUST set both flags on existing stool entries to false and preserve all previous stool and meal data unchanged. Destructive resets MUST be excluded.

#### Scenario: Read flags again

- **WHEN** a stool entry with any of the four combinations of blood and urgency is saved and later read again
- **THEN** both flags, the ID, timestamp, Bristol type, and optional note match the saved entry

#### Scenario: Upgrade existing data

- **WHEN** the new app version first opens a dataset without the two flags
- **THEN** all previous stool and meal entries retain their previous values and existing stool entries receive false for both new flags
