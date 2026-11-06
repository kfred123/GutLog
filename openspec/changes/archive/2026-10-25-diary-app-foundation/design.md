## Context

Dies ist ein neues, leeres Projektverzeichnis. Die erste Implementierung muss mit
der verfügbaren Windows-/Android-Toolchain bauen und zugleich ein iOS-Ziel
enthalten, das auf macOS mit Xcode gebaut werden kann.

## Goals / Non-Goals

**Goals**

- Das anfängliche Projekt klein und verständlich halten.
- Compose-Oberfläche sowie Domänen-/Persistenzcode zwischen Android und iOS teilen.
- Room-Entitäten und DAOs in einem lokalen Datenpaket halten; anderen Bereichen
  nur Domänen-Repository-Schnittstellen bereitstellen.
- Domäneninvarianten validieren und die Android-App lokal bauen.

**Non-Goals**

- Eingabeformulare, Konto/Synchronisierung, Netzwerk, Fotoerkennung oder Analyse.
- Einen iOS-Build unter Windows als geprüft ausgeben.

## Decisions

- Ein Kotlin-Multiplatform-Modul `shared` für gemeinsame Oberfläche, Domäne und
  gekapseltes lokales Datenpaket verwenden, ergänzt um ein kleines Android-App-
  Modul und einen nativen SwiftUI-/Xcode-iOS-Host. So wird vorzeitige Modul-
  Indirektion vermieden und die Persistenzgrenze trotzdem explizit gehalten.
- `Long`-UTC-Epoch-Millisekunden für Zeitstempel, von Aufrufern gelieferte
  `String`-IDs, einen erforderlichen Bristol-Skalenwert von 1–7 und
  Freitextbeschreibungen für Mahlzeiten verwenden.
- Room 2.8.4 mit gebündeltem SQLite-Treiber und KSP verwenden. Die offizielle
  Room-Migrationsdokumentation bestätigt ausdrücklich KMP-Unterstützung; so wird
  die neuere Room-3-Alpha-Version für ein stabiles Grundgerüst vermieden.
- Stabile Versionen verwenden, die mit dem installierten Android-API-36-SDK
  kompatibel sind: Kotlin 2.4.21, Compose Multiplatform 1.10.3, Android Gradle
  Plugin 9.0.0 und Gradle-Wrapper 9.2.1. Compose Multiplatform 1.10.3 unterstützt
  AGP 9.0.0; laut Kotlin-Kompatibilitätsleitfaden sind Gradle 9.2.1 und AGP 9.0.0
  für Kotlin 2.4.21 zulässig. Das installierte JDK 21 genügt. Compose-Bibliotheken
  werden direkt über den Versionskatalog referenziert, nicht über veraltete
  Plugin-Aliase.
- Erkennung und Analyse ausschließlich als dokumentierte Zukunftsvorgaben
  behandeln; keine Platzhalterdienste oder Beispielvorhersagen hinzufügen.
- macOS und Xcode zum Verknüpfen/Ausführen der Apple-Frameworks voraussetzen;
  gemeinsame Metadaten und Android-Builds auf diesem Windows-Host prüfen.

## Risks / Trade-offs

- Das KMP-/Compose-/AGP-Ökosystem entwickelt sich weiter. Fixierte Versionen und
  Gradle-Wrapper machen das Projekt reproduzierbar; künftige Aktualisierungen
  sollten dem offiziellen Kompatibilitätsleitfaden folgen.
- Room-Schema und plattformspezifische Pfade müssen bei Schemaänderungen gemeinsam
  angepasst werden.
- Das Xcode-Projekt kann auf diesem Windows-Host nicht ausgeführt oder geprüft
  werden.

## References

- Kotlin-Multiplatform-Kompatibilität:
  https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html
- Compose-Multiplatform-Kompatibilität:
  https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html
- Room-KMP-Migration und Room 2.8:
  https://developer.android.com/training/data-storage/room/migration-2-to-3
- Android-KMP-Plugin:
  https://developer.android.com/kotlin/multiplatform/plugin
