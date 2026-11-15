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

Die Room-Migrations- und Persistenztests benötigen einen laufenden Android-
Emulator oder ein verbundenes Gerät:

```powershell
.\gradlew.bat :androidApp:connectedDebugAndroidTest
```

Die lokale Datenbank verwendet Schema-Version 3. Beim Upgrade von Version 1
oder 2 werden vorhandene Stuhl- und Mahlzeitendaten ohne Zurücksetzen
übernommen; neue Blut- und Dringlichkeitsmerkmale sind für alte Einträge nicht
markiert. Mahlzeiten aus Version 2 behalten Zeit, Beschreibung und Notiz; eine
damals nicht gespeicherte Portionsgröße wird als „mittel“ angezeigt. Der
Food-Katalog und die Lebensmittel einer Mahlzeit werden in zusätzlichen Tabellen
gespeichert.
Ein Downgrade auf eine ältere App-Version nach dem Upgrade wird nicht
automatisch unterstützt.

## Stuhlgang erfassen

Über den Plus-Button unten rechts öffnet sich ein Menü für Stuhlgang, Essen und
Schmerzen. Stuhlgang und Essen können eingetragen werden; Schmerzen ist als
„demnächst“ gekennzeichnet. Das Stuhl-Formular bietet Datum und Uhrzeit für
aktuelle oder zurückliegende Einträge, sieben schematische Bristol-Symbole sowie
unabhängige Merkmale für Blut und Dringlichkeit.
Zukünftige oder durch eine Zeitumstellung nicht eindeutig bestimmbare
Zeitpunkte werden nicht gespeichert. Der Bristol-Typ des zuletzt erfolgreich
gespeicherten Eintrags wird beim nächsten Eintrag vorausgewählt, auch wenn
dessen Ereigniszeit älter ist. Ohne gespeicherten Stuhlgang bleibt die Auswahl
leer. Abbrechen oder ein fehlgeschlagenes Speichern ändert diese Vorgabe nicht.

## Mahlzeiten erfassen

Im Menü „Essen eintragen“ kann eine Mahlzeit mit Datum und Uhrzeit erstellt
werden. Die Lebensmittelsuche funktioniert offline und findet den eingebauten
Katalog auf Deutsch und Englisch unabhängig von der gewählten App-Sprache. Er
enthält häufige Gemüse, Obst, Fleisch und Hülsenfrüchte. Nicht gefundene
Lebensmittel können als Freitext hinzugefügt und nach erfolgreichem Speichern
lokal für spätere Suchen übernommen werden; beim Abbrechen bleibt der Katalog
unverändert.

Für die Mahlzeit wird eine Portionsgröße von klein, mittel oder groß gewählt.
Lebensmittel übernehmen diese Vorgabe, sofern nicht eine abweichende Größe
direkt am Lebensmittel gewählt wird. Wird die Mahlzeit-Portion geändert, bleiben
solche Einzel-Overrides erhalten. Gespeicherte Mahlzeiten erscheinen mit ihrer
lokalen Uhrzeit und den tatsächlich geltenden Portionsgrößen. Eine
Nährwertberechnung, Fotoerkennung oder Analyse von Zusammenhängen ist nicht
enthalten.

Die Einträge dienen der persönlichen Dokumentation. Die App erstellt keine
Diagnosen, Erkennungsvorschläge oder statistischen Analysen.

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
