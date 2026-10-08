# Dart Scorer

Een strakke Android-app om darts bij te houden, gebouwd met Kotlin, Jetpack Compose en Material 3
(minSdk 26, targetSdk 35). Engels en Nederlands, licht en donker thema.

## Spellen

| Spel | Kort |
| --- | --- |
| **501 / 301** | Aftellen naar precies nul. Startscore 101/301/501/701, double out, double in en "first to" 1–5 legs. Met uitgooi-advies, gemiddelde per 3 pijlen en bust-detectie. |
| **Cricket** | Sluit 15 t/m 20 en de bull; extra hits scoren zolang een tegenstander het nummer nog open heeft. |
| **Around the Clock** | 1 t/m 20 op volgorde, daarna de bull. Optie: dubbel/triple slaan 2/3 nummers over. |
| **Shanghai** | 7, 10 of 20 rondes; per ronde telt alleen het rondenummer. Een Shanghai (enkel + dubbel + triple) wint direct. |
| **Killer** | Iedereen een eigen nummer. Raak je eigen dubbel om killer te worden, pak dan levens af met hun dubbels. |

## Functies

- **Invoer per pijl** — kies Enkel/Dubbel/Triple en tik het nummer, of 25/Bull en Mis. Na elke pijl springt de
  keuze terug naar Enkel. **Herstel** maakt de laatste pijl ongedaan, ook na een gewonnen spel.
- **1–8 spelers** met namen die onthouden worden, en een knop om de volgorde te husselen.
- **Spel loopt door** — de stand wordt na elke pijl bewaard; sluit Android de app, dan ga je vanaf het
  startscherm verder waar je was.
- **Scherm blijft aan** tijdens een spel (uit te zetten in Instellingen).
- **Instellingen** — thema Systeem/Licht/Donker en taal English/Nederlands (standaard de taal van je telefoon).
- **Ko-fi** — het koffiekopje op het startscherm en de kaart in Instellingen openen eerst een venster, niet
  direct de website (`https://ko-fi.com/scribba`).

## Projectstructuur

```
app/src/main/java/com/thescrib/dartscorer/
├── domain/    Pure Kotlin, geen Android: Dart, spelregels per spel, Match, uitgooi-advies, MatchCodec
├── data/      DataStore-instellingen en MatchController (lopend spel + bewaren)
└── ui/        Compose-schermen (home, setup, game, settings), thema, componenten, navigatie
```

- **Spelregels zijn pure functies**: `GameRules.throwDart(stand, pijl)` geeft de nieuwe stand. Een `Match` bewaart
  alleen de instellingen, spelers en de lijst pijlen; de stand wordt telkens opnieuw afgespeeld. Daardoor is
  "ongedaan maken" simpelweg de laatste pijl weglaten, en is alles makkelijk te testen.
- **Bewaren**: `MatchCodec` zet een partij om naar een paar regels tekst in DataStore.
- **Dependency injection**: handmatig via `AppContainer` in `DartScorerApp`.

## APK downloaden (GitHub Actions)

Bij elke push naar `main` en bij elke pull request bouwt GitHub Actions de app en draait de tests.

1. Ga naar het tabblad **Actions** → **Android build** en open de bovenste (groene) run.
   Je kunt ook een nieuwe build starten via **Run workflow**.
2. Download onder **Artifacts** `dart-scorer-debug-apk` (een zip met `app-debug.apk`).
3. Pak de zip uit op je telefoon en open de APK. Sta "installeren van onbekende apps" toe als daarom gevraagd wordt.

Debug-builds worden gesigneerd met een vaste sleutel (`app/debug.keystore`), zodat een nieuwe APK
gewoon over de oude heen installeert.

**Optioneel: gesigneerde release-builds.** Voeg bij *Settings → Secrets and variables → Actions* deze vier
secrets toe, dan levert elke build ook `dart-scorer-release-apk` en `dart-scorer-release-aab` op. De AAB (App Bundle)
is het bestand dat je in de Play Console uploadt:

| Secret | Waarde |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | uitvoer van `base64 -w0 dart-scorer-release.jks` (macOS: `base64 -i dart-scorer-release.jks`) |
| `RELEASE_STORE_PASSWORD` | wachtwoord van de keystore |
| `RELEASE_KEY_ALIAS` | bijv. `dartscorer` |
| `RELEASE_KEY_PASSWORD` | wachtwoord van de sleutel |

## Bouwen en testen

```bash
./gradlew :app:assembleDebug          # debug-APK in app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest      # unit tests (spelregels, uitgooi-advies, codec) + screenshottests
./gradlew :app:lintDebug
```

De screenshottests (Robolectric) renderen de schermen in licht en donker naar `app/build/screenshots/`,
zodat je design-wijzigingen kunt controleren zonder emulator. In CI staan ze bij de artifacts als `screenshots`.

## Gesigneerde release-build

1. **Maak een keystore** (eenmalig, en bewaar hem goed: zonder deze sleutel kun je geen updates uitbrengen):
   ```bash
   keytool -genkey -v -keystore dart-scorer-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias dartscorer
   ```
2. **Zet de secrets** uit de tabel hierboven in GitHub. De volgende run van **Android build** levert dan
   `dart-scorer-release-aab` op. Of bouw lokaal met `keystore.properties` in de hoofdmap (staat in `.gitignore`):
   ```properties
   storeFile=dart-scorer-release.jks
   storePassword=•••
   keyAlias=dartscorer
   keyPassword=•••
   ```
   ```bash
   ./gradlew :app:bundleRelease     # app/build/outputs/bundle/release/app-release.aab
   ./gradlew :app:assembleRelease   # app/build/outputs/apk/release/app-release.apk
   ```
3. Verhoog `versionCode` (en `versionName`) in `app/build.gradle.kts` bij elke nieuwe release; Google Play
   weigert een AAB met een `versionCode` die al eerder is geüpload.
4. Zet in de Play Console **Play App Signing** aan; jouw keystore wordt dan de *upload key*.

Alle talen zitten altijd in de AAB (geen taal-splits), omdat je de taal in de app zelf kiest.

## Lettertypes

Space Grotesk en Inter (beide SIL Open Font License), meegeleverd in `app/src/main/res/font/`.
