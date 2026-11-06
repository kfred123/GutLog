# Tasks

## 1. Rename the project and technical app identities

- [x] 1.1 Set the Gradle root project name, Android application ID and namespaces, Kotlin package declarations and source paths, and shared framework base name to GutLog identifiers (`de.gutlog.app`, `de.gutlog.shared`, `GutLogShared`); verify `.\gradlew.bat :androidApp:assembleDebug` and `.\gradlew.bat :shared:testAndroidHostTest` pass.
- [ ] 1.2 Rename the iOS app/target/product and Swift entry-point identifiers, update the shared framework import and bundle identifier to `de.gutlog.app.ios`, and verify the Xcode project references and framework build phase consistently use the new names on macOS.
- [x] 1.3 Update README setup, project-structure, and build references to GutLog and document that the new platform identifiers are separate app identities; verify each documented path and command matches the renamed project.

## 2. Localize shared application content and language choice

- [x] 2.1 Configure Compose Multiplatform resources and add English and German translations for every current shared UI string, including the in-app product name “GutLog” / “Bauchlog”; verify both resource catalogs contain matching keys and the shared UI has no remaining hard-coded user-facing copy.
- [x] 2.2 Resolve the initial in-app language from the device locale with English fallback, expose a shared-facing preference abstraction, and persist manual language overrides in Android preferences and iOS `NSUserDefaults`; add unit tests for locale resolution and run `.\gradlew.bat :shared:testAndroidHostTest`.
- [x] 2.3 Add an in-app language control that applies the selected Compose resource locale immediately and restores the saved selection on restart; verify an Android run changes the visible copy immediately and retains the selection after an offline restart.

## 3. Localize native platform app names

- [x] 3.1 Replace the Android manifest's hard-coded label with localized launcher resources for English “GutLog” and German “Bauchlog”; verify both resource values and build with `.\gradlew.bat :androidApp:assembleDebug`.
- [ ] 3.2 Add English and German iOS `InfoPlist.strings` app-name localizations and register their regions/resources in the Xcode project; verify each system locale displays its corresponding launcher name in an iOS build on macOS.

## 4. Verify cross-platform localization behavior

- [ ] 4.1 Verify on Android and iOS that a German system locale selects German by default, an unsupported system locale falls back to English, and a manual in-app override persists offline while the platform launcher name follows the system locale; record any platform-specific verification limitation in the implementation handoff.
