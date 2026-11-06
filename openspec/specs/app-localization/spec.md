# app-localization Specification

## Purpose

Defines the supported user-facing languages and localized app identity so people can use the diary in English or German on Android and iOS.

## Requirements

### Requirement: Show localized app names and interface text
The application MUST present its platform app name according to the device locale and all in-app names and user-facing interface text in the active supported language on Android and iOS.

#### Scenario: Use the English app identity
- **WHEN** English is the device locale and the active in-app language
- **THEN** the platform app name and in-app name are “GutLog” and the interface text is English

#### Scenario: Use the German app identity
- **WHEN** German is the device locale and the active in-app language
- **THEN** the platform app name and in-app name are “Bauchlog” and the interface text is German

### Requirement: Select an initial language from the device locale
When no manual language preference has been saved, the application MUST use German for a German device locale and English for other device locales.

#### Scenario: Start with a German device locale
- **WHEN** the app starts without a saved language preference on a device configured for German
- **THEN** the app displays its German name and German interface text

#### Scenario: Start with an unsupported device locale
- **WHEN** the app starts without a saved language preference on a device configured for a language other than German or English
- **THEN** the app displays its English name and English interface text

### Requirement: Allow and retain a manual language choice
The application MUST allow a person to choose English or German in the app and MUST retain that choice locally across restarts without requiring a network connection.

#### Scenario: Change the language manually
- **WHEN** a person chooses English or German in the app
- **THEN** the in-app name and interface text immediately use the chosen language, while the platform app name continues to follow the device locale

#### Scenario: Restore a saved language choice offline
- **WHEN** the app is restarted without a network connection after a manual language choice
- **THEN** the app continues using the previously chosen language
