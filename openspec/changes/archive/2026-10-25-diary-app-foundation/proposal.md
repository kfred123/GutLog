## Why

Das Projekt braucht ein baubares, verständliches Kotlin-Grundgerüst, bevor
Tagebuchfunktionen hinzukommen. Es schafft einen gemeinsamen Android-/iOS-Einstieg,
validierte Domänentypen für Stuhl- und Mahlzeiteneinträge sowie eine lokale
Persistenzgrenze, ohne eine spätere Erkennung oder Analyse vorzutäuschen.

## What Changes

- Ein Kotlin-Multiplatform-Projekt mit gemeinsamer Compose-Oberfläche, Android-
  Launcher und nativem iOS-Host ergänzen.
- Stuhl- und Mahlzeiteneinträge sowie Repository-Verträge in der gemeinsamen
  Domäne definieren.
- Eine lokale Room-Implementierung hinter diesen Verträgen ergänzen.
- Einrichtung, gezielte Prüfungen und die macOS-/Xcode-Voraussetzung für iOS
  dokumentieren.

## Capabilities

### New Capabilities

- `diary-foundation`: Gemeinsamer App-Einstieg, Stuhl- und Mahlzeitendomänentypen
  sowie lokale, Repository-basierte Persistenz.

### Modified Capabilities

- Keine.

## Impact

- Ergänzt Gradle-Projektdateien, gemeinsame Kotlin-Quellen, ein Android-App-Modul
  und ein Xcode-Projekt mit SwiftUI-Host.
- Ergänzt OpenSpec-Workflows für GitHub Copilot und Projektspezifikationen.
- Ergänzt weder Konto noch Server, Fotoerkennung oder statistische Analyse.
