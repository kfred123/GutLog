# Design

## Context

The Android launcher and shared Compose screen currently hard-code “Tagebuch” and German copy. The shared module uses Kotlin Multiplatform and Compose Multiplatform 1.10.3 but has no Compose resource catalog. Android uses `de.diary.app`; the shared namespace is `de.diary.shared`, the framework is `DiaryShared`, and the iOS host, target, and bundle settings still use Diary identifiers. See `proposal.md` for motivation and `specs/app-localization/spec.md` for behavior.

## Goals / Non-Goals

**Goals:**
- Establish GutLog as the internal project and app identity, with German-facing branding “Bauchlog”.
- Localize the existing shared interface and native platform app names for English and German.
- Allow an offline, persistent in-app language override while using the device locale by default.

**Non-Goals:**
- Add food-photo recognition, correlation analysis, accounts, or network services.
- Transfer data between installations under the old and new application identifiers.
- Rename domain concepts such as diary entries that still describe the product's behavior.

## Decisions

### Use shared Compose resources for in-app copy

Move every existing visible string from `DiaryApp` into Compose Multiplatform string resources for English and German. Set the shared resource locale from the resolved in-app language. This keeps both platforms' Compose UI in parity and uses the existing Compose Multiplatform stack rather than maintaining a custom string lookup layer.

For platform launcher names, use Android localized string resources and iOS localized `InfoPlist.strings`. These labels follow the device locale because they are provided by the operating system; an in-app override changes the name and copy inside the app, not the launcher label.

### Resolve and persist the in-app language in shared application state

Use the device locale on first launch, selecting German for German locales and English for all others. A manual selection takes precedence at subsequent launches. Persist only the language override in platform-local preferences through a small shared-facing platform abstraction, implemented with Android preferences and iOS `NSUserDefaults`; no account, Room schema change, or network dependency is needed. Keep the resolved language as a small, explicit value so locale resolution can be unit tested.

Alternatives considered: relying only on native resource selection would not support an in-app override; storing the preference in Room would couple a UI preference to the diary database.

### Apply a consistent reverse-domain GutLog identifier set

Replace the old `Diary` app and framework names and the `de.diary.*` packages with `GutLog` / `GutLogShared` and `de.gutlog.*`. Use `de.gutlog.app` for Android and `de.gutlog.app.ios` for the iOS bundle, following the current `de.diary.*` naming pattern. Update Kotlin source package declarations and paths, Swift imports and app/target symbols, Gradle project name, Xcode products and settings, manifests, and documentation together. Keep the generic Gradle module paths `:shared` and `:androidApp`; they do not contain the old product identity and renaming them would add churn without changing the app identity.

Alternatives considered: retaining package and bundle identifiers would preserve upgrade identity but conflicts with the user's explicit decision to rename technical identifiers.

## Risks / Trade-offs

- [Changing application identifiers makes existing installations separate apps and does not migrate their private data] → Document the breaking identity change; retain the old installation's data untouched rather than attempting an unrequested cross-identity migration.
- [Renaming packages and Xcode/Gradle references can leave stale build or source paths] → Update references as one coordinated operation and verify Android compilation plus the iOS project/framework configuration on macOS.
- [The launcher label may differ from the manually selected in-app language] → Keep this platform-standard distinction explicit: launcher labels follow the device locale; the app's own name and interface follow the in-app language.

## Migration Plan

1. Change project/package/framework and native app identifiers together, then update all dependent references and documentation.
2. Add English and German shared strings and platform launcher-name resources; initialize the shared locale from the system locale and apply any saved override.
3. Verify the Android build and shared tests locally. On macOS, build and launch the iOS target in both supported system locales.
4. Release under the new platform identifiers as a separate app identity. Do not remove or modify data belonging to an installation under the old identifiers.

Rollback means reverting the identifier and localization changes before distribution under the new IDs. After distribution, switching identifiers back would again create a distinct app identity; no automatic data transfer is part of this change.
