# diary-foundation Specification

## Purpose

Beschreibt das gemeinsame Android-/iOS-Grundgerüst, die Domänenmodelle für
Stuhl- und Mahlzeiteneinträge sowie den lokalen, Repository-basierten Speicher.

## Requirements

### Requirement: Gemeinsame App-Oberfläche für Android und iOS bereitstellen

Das Projekt MUST eine gemeinsame Compose-Multiplatform-Einstiegsoberfläche
bereitstellen, die sowohl von der Android-App als auch vom iOS-Host gestartet
werden kann.

#### Scenario: Android-App starten

- WHEN die Android-App startet
- THEN zeigt sie die gemeinsame Compose-Tagebuchoberfläche an

#### Scenario: iOS-App starten

- WHEN der iOS-Host auf einem unterstützten Apple-Ziel startet
- THEN zeigt er dieselbe gemeinsame Compose-Tagebuchoberfläche an

### Requirement: Stuhl- und Mahlzeiteneinträge in der gemeinsamen Domäne modellieren

Die gemeinsame Domäne MUST einen Stuhleintrag mit stabiler ID, UTC-Zeitstempel in
Epoch-Millisekunden, Bristol-Skalenwert von 1 bis 7 und optionaler Notiz
definieren. Sie MUST außerdem einen Mahlzeiteneintrag mit stabiler ID,
UTC-Zeitstempel in Epoch-Millisekunden, nicht leerer Freitextbeschreibung und
optionaler Notiz definieren.

#### Scenario: Ungültigen Bristol-Skalenwert ablehnen

- WHEN ein Stuhleintrag mit einem Bristol-Skalenwert außerhalb von 1 bis 7 erstellt
  wird
- THEN lehnt die Domänenvalidierung den Eintrag ab

#### Scenario: Leere Mahlzeitbeschreibung ablehnen

- WHEN ein Mahlzeiteneintrag mit leerer Beschreibung erstellt wird
- THEN lehnt die Domänenvalidierung den Eintrag ab

### Requirement: Lokale Persistenz hinter Repository-Verträgen kapseln

Die gemeinsame Domäne MUST Repository-Verträge zum Beobachten, Speichern und
Löschen von Stuhl- und Mahlzeiteneinträgen bereitstellen. Eine lokale
Room-Implementierung MUST beide Eintragstypen speichern und MUST durch eine
Datenzugriffsgrenze von gemeinsamer Oberfläche und Domänentypen getrennt sein.

#### Scenario: Lokal gespeicherte Einträge beobachten

- WHEN ein Repository Stuhl- oder Mahlzeiteneinträge beobachtet
- THEN liefert es die aktuell lokal gespeicherten Einträge und nachfolgende lokale
  Änderungen

#### Scenario: Eintrag ohne Netzwerk speichern

- WHEN ein gültiger Stuhl- oder Mahlzeiteneintrag offline gespeichert wird
- THEN kann er ohne entfernten Dienst aus dem lokalen Speicher wieder gelesen
  werden

### Requirement: Nicht implementiertes Produktverhalten aus dem Grundgerüst heraushalten

Das Grundgerüst MUST weder Fotoerkennungsergebnisse noch statistische
Zusammenhänge erfinden und MUST nicht den Eindruck erwecken, eine der beiden
zukünftigen Funktionen sei bereits verfügbar.

#### Scenario: Grundgerüst-Anwendung öffnen

- WHEN eine Person die Grundgerüst-Anwendung öffnet
- THEN zeigt sie eine klare Einstiegsoberfläche ohne simulierte Tagebuch-,
  Erkennungs- oder Analyseergebnisse
