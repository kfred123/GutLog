# GutLog

GutLog ist ein Kotlin-Multiplatform-Offline-Tagebuch für Android und iOS. Die
App heißt auf Deutsch „Bauchlog“ und auf Englisch „GutLog“.
Das gemeinsame Modul enthält die Compose-Oberfläche, Domänenverträge und eine
lokale Room-Persistenz. Konto und Backend sind nicht erforderlich. Der Wechsel
auf neue Android- und iOS-App-IDs erzeugt eine separate App-Identität; bestehende
Installationen und deren lokale Daten werden nicht automatisch übernommen.
Die Oberfläche unterstützt Deutsch und Englisch, verwendet zunächst die
Gerätesprache und bietet eine dauerhaft gespeicherte manuelle Sprachauswahl.

## Voraussetzungen

- JDK 21
- Android SDK 36
- Der enthaltene Gradle-Wrapper
- Android Studio mit Kotlin-Multiplatform-Plugin für IDE-Unterstützung
- macOS und Xcode zum Bauen oder Starten der iOS-App

## Android

Projekt in Android Studio öffnen oder das Debug-APK im Projektverzeichnis bauen:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

Domänen- und gemeinsame Tests auf dem Android-Host ausführen. Dieser gezielte Task
vermeidet Apple-native Test-Tasks, die macOS benötigen:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

Das APK liegt anschließend unter
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

## iOS

iOS-Builds erfordern macOS mit installiertem Xcode und den Xcode-Command-Line-
Tools. `iosApp/iosApp.xcodeproj` in Xcode öffnen, das Schema `GutLogIOS` und einen
iOS-Simulator oder ein Gerät auswählen und die App bauen/starten. Eine
Xcode-Build-Phase baut und verknüpft das passende Kotlin-Framework über den
Gradle-Wrapper.

Das Projekt wurde unter Windows eingerichtet. Ein iOS-Build lässt sich hier
nicht prüfen, weil Xcode und die Apple-Toolchain macOS voraussetzen.

## Projektstruktur

- `shared/src/commonMain/.../domain`: validierte Einträge und Repository-Verträge
- `shared/src/commonMain/.../data/local`: Room-Entitäten, DAOs und Adapter
- `shared/src/commonMain/.../ui`: gemeinsame Compose-Einstiegsoberfläche
- `androidApp`: Android-Launcher
- `iosApp`: SwiftUI-Host und Xcode-Projekt
- `openspec`: Produktkontext, Anforderungen und Change-Verlauf

## Spezifikationsworkflow

OpenSpec ist mit GitHub-Copilot-Prompts und -Skills in `.github/` eingerichtet;
Projektkontext und Spezifikationen liegen in `openspec/`. Neue Änderungen starten
mit `/opsx-propose` und werden vor der Implementierung geprüft. Die hier
abgestimmten Fotoerkennung und statistische Musteranalyse sind Zukunftsarbeit:
Dieses Grundgerüst erzeugt weder Erkennungsergebnisse noch Analysen.
