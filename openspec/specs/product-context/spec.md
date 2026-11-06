## Purpose

Hält Produktziele und Architekturgrenzen fest, die für aktuelle und zukünftige
Änderungen am Offline-Tagebuch gelten.

## Requirements

### Requirement: Tagebuchdaten lokal und offline verfügbar halten

Die Anwendung MUST Tagebucheinträge lokal speichern und MUST für deren Erstellung
und Anzeige weder ein Konto noch ein Backend oder eine Netzwerkverbindung
voraussetzen.

#### Scenario: Tagebuch ohne Netzwerkverbindung verwenden

- WHEN eine Person ohne Netzwerkverbindung einen Eintrag erfasst oder ansieht
- THEN kann die Anwendung diesen Vorgang ausschließlich mit lokalem Speicher
  ausführen

### Requirement: Zukünftige Erkennungsvorschläge bestätigen lassen

Wenn eine foto-basierte Lebensmittelerkennung eingeführt wird, MUST die Anwendung
erkannte Lebensmittel als bearbeitbare Vorschläge anzeigen und MUST sie erst nach
Bestätigung als gespeicherte Mahlzeit übernehmen.

#### Scenario: Zukünftigen Erkennungsvorschlag korrigieren

- WHEN ein Erkennungsdienst Vorschläge zu einem Foto liefert
- THEN kann die Person die Vorschläge prüfen und korrigieren, bevor eine Mahlzeit
  gespeichert wird

### Requirement: Zukünftige Analyse nicht diagnostisch darstellen

Wenn eine Analyse von Mahlzeiten und Beschwerden eingeführt wird, MUST die
Anwendung Ergebnisse als beobachtete Muster statt als Diagnosen oder
Kausalitätsbelege darstellen und MUST auf unzureichende Beobachtungen hinweisen.

#### Scenario: Zu wenige Beobachtungen für ein Muster

- WHEN zu wenige Beobachtungen für einen aussagekräftigen Vergleich vorliegen
- THEN weist die Anwendung auf diese Einschränkung hin, statt ein Ergebnis zu
  erfinden

## Architekturleitlinien

- Kotlin Multiplatform teilt Domänenregeln und Anwendungslogik.
- Compose Multiplatform stellt die gemeinsame Oberfläche bereit.
- Domänenmodelle und Repository-Verträge sind unabhängig von der Persistenz.
- Room stellt die lokale SQLite-Implementierung hinter diesen Verträgen bereit.
- Plattformcode bleibt auf den App-Start und plattformspezifische Dateipfade
  beschränkt.

## Zukünftiger Umfang

Foto-basierte Lebensmittelerkennung ist geplant, aber Modell und Anbieter sind
noch nicht ausgewählt. Bei der späteren Einführung MUST die Erkennung über eine
austauschbare plattformspezifische Schnittstelle erfolgen. Ihre Ergebnisse sind
Vorschläge, die Nutzer vor dem Speichern prüfen und korrigieren können. Die
Anwendung MUST keine simulierten Erkennungsergebnisse als echt darstellen.

Eine lokale Analyse möglicher Zusammenhänge zwischen Mahlzeiten und Beschwerden
ist ebenfalls Zukunftsarbeit. Sie MUST deskriptive Statistik verwenden, bei
unzureichenden Beobachtungen darauf hinweisen und MUST weder Diagnosen noch
Kausalitätsbehauptungen aus Korrelationen ableiten. Keine der beiden Funktionen
ist Teil des Grundgerüsts.
