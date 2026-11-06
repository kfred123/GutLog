# Proposal

## Why

The project and its user-facing app still use the provisional Diary/Tagebuch identity, and the shared interface has no localization mechanism. Establishing GutLog as the stable internal identity while presenting a natural name in each supported language makes the app consistent across platforms and usable in English and German.

## What Changes

- Rename the project and its internal Android, iOS, Kotlin package, and shared-framework identifiers to GutLog.
- **BREAKING** Replace the Android application ID and iOS bundle identifier with GutLog identifiers; existing installs will be treated as a different app identity.
- Localize the app name and all user-facing interface text in English and German. The English name is “GutLog”; the German name is “Bauchlog”.
- Select the initial language from the device locale, allow a manual in-app language choice, and retain that choice locally.
- Keep “GutLog” as the internal project name regardless of the displayed language.

## Capabilities

### New Capabilities
- `app-localization`: Provide English and German user-facing text, localized app names, and device-locale selection with a persistent manual override.

### Modified Capabilities

## Impact

The shared Compose UI and its resources, Android application metadata and identifiers, iOS Xcode project and bundle settings, Kotlin package and framework names, project/module names, and project documentation are affected. The identifier change creates a new install identity on both platforms; existing local data is not automatically transferred between the old and new app identities.
