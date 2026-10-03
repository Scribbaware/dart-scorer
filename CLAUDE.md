# CLAUDE.md

Richtlijnen voor Claude bij het werken aan deze repository.

## Communicatie

- **Taal:** standaard Nederlands. Schrijft de eigenaar in het Engels, antwoord dan in het Engels; schakelt die
  terug naar Nederlands, ga dan ook weer mee. Volg gewoon het laatste bericht en vraag niet steeds welke taal.
- De eigenaar is beginner: leg keuzes kort en begrijpelijk uit, zonder jargon waar dat kan. Geef bij handelingen op
  GitHub of de telefoon concrete stappen (waar klikken, wat kiezen).
- Houd berichten kort: wat er gedaan is, wat de eigenaar eventueel moet doen, en links naar de PR of de build.
- UI-teksten in de app zijn Engels, Nederlands, Duits, Frans en Spaans (alle vijf bijhouden). Code, identifiers en commitberichten zijn Engels; uitleggende codecommentaar
  en PR-beschrijvingen zijn Nederlands.

## Werkwijze: alles automatisch

De eigenaar heeft blijvend toestemming gegeven om wijzigingen zelfstandig van begin tot eind af te handelen.
De eigenaar hoeft alleen te zeggen wát er moet veranderen en daarna de nieuwe APK te downloaden.

1. **Branch:** werk nooit direct op `main`. Begin elke nieuwe taak vanaf de nieuwste `main`. Is de vorige PR van
   dezelfde branch al gemerged, zet de branch dan opnieuw op `origin/main` en voeg de oude remote-branch eraan toe
   met een gewone merge (`git merge origin/<branch>`). De inhoud is al via squash op `main` gekomen, dus dat geeft geen
   wijzigingen. Zo is geen force-push nodig.
2. **Lokaal testen:** `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`. Bij UI-wijzigingen:
   bekijk ook de screenshots (zie Design).
3. **Pull request** naar `main`, met een Nederlandse beschrijving: Samenvatting (wat en waarom) en Testen (checklist).
4. **Wachten op de build:** de workflow **Android build** draait op elke PR (±5–10 min). Volg de PR en plan zelf een
   controle in, zodat je terugkomt als de build klaar is. Is hij rood: zoek de oorzaak in de job-logs, los het op,
   test lokaal en push opnieuw. Nooit tests uitzetten of overslaan om groen te krijgen.
5. **Zelf mergen** (squash) zodra de build groen is en er geen conflicten zijn. Ruim daarna geplande controles op.
6. **Melden** dat de nieuwe APK klaarstaat: **Actions → Android build → bovenste run op `main` → Artifacts →
   `dart-scorer-debug-apk`**. Bij alleen documentatiewijzigingen: zeg dat de app zelf niet veranderd is.

Nooit doen:
- force-pushen of geschiedenis herschrijven (rebase/amend op gepushte commits); gebruik een merge-commit;
- direct naar `main` pushen;
- secrets, wachtwoorden of `keystore.properties` committen.

Vraag eerst om akkoord als een wijziging:
- de werking van de app flink verandert of een keuze vraagt die bij de eigenaar hoort;
- het formaat van een bewaard spel (`MatchCodec`) verandert: oude data moet leesbaar blijven of netjes worden genegeerd;
- de debug-sleutel (`app/debug.keystore`) of de `applicationId` wijzigt, want dan kan de nieuwe APK niet over de oude heen worden geïnstalleerd.

## Repository en GitHub

- De repository verhuist naar een ander GitHub-account van de eigenaar. Ga niet uit van een vaste accountnaam: haal
  eigenaar en repo-naam uit `git remote get-url origin`.
- De package-naam en `applicationId` `com.thescrib.dartscorer` blijven ongewijzigd, anders installeert de nieuwe APK
  niet over de oude heen.
- Kan Claude de repo niet bereiken (klonen, pushen of PR mislukt): laat de eigenaar zijn GitHub-account verbinden via
  https://claude.ai/connect-github en daar de Claude GitHub-app op de repo installeren. Start daarna een nieuwe sessie
  met de repo geselecteerd.
- CI: `.github/workflows/android.yml` draait tests, lint en de debug-build op elke push naar `main`, elke PR en via
  **Run workflow**. Debug-APK's zijn gesigneerd met de vaste `app/debug.keystore`. Een gesigneerde release-APK komt er
  alleen als de `RELEASE_*`-secrets zijn ingesteld (zie README).

## Project

Android-app om darts te scoren: Kotlin, Jetpack Compose, DataStore en Material 3 (minSdk 26, targetSdk 35).
Spellen: 501/301 (X01), Cricket, Around the Clock, Shanghai en Killer. Zie de README voor functies en keuzes.

```
app/src/main/java/com/thescrib/dartscorer/
├── domain/    Pure Kotlin, geen Android-imports: Dart, GameRules per spel, Match, checkout(), MatchCodec
├── data/      DataStore-instellingen, MatchController (lopend spel, bewaren na elke pijl)
└── ui/        Compose-schermen, thema, componenten, navigatie
```

- Houd `domain/` vrij van Android-afhankelijkheden, zodat het met gewone JUnit-tests te testen is.
- Spelregels zijn pure functies (`GameRules.throwDart`); een `Match` is instellingen + spelers + pijlen, en de stand
  wordt opnieuw afgespeeld. Undo = laatste pijl weglaten. Voeg een nieuw spel toe als `GameConfig` + `GameRules`,
  met een `GamePreset` voor het startscherm, een scorebord in `Scoreboards.kt` en een regel in `MatchCodec`.
- Elke tekst staat in `values/strings.xml` (Engels) en in `values-nl`, `values-de`, `values-fr` en `values-es`. Een nieuwe taal: map `values-xx`, een waarde in `AppLanguage` en een `Locale` in `Localization.kt`.
- Dependency injection gebeurt handmatig via `AppContainer` in `DartScorerApp`.

## Design

- De app moet strak ogen, in zowel het lichte als het donkere thema. Gebruik de kleuren uit `ui/theme/Color.kt` en
  `MaterialTheme.colorScheme`, geen losse hex-waarden in schermen.
- Dartbordkleuren: groen voor dubbel, rood voor triple. Ko-fi gebruikt het warme verloop `KofiGradient`.
- Ko-fi: koffiekopje links in de balk van het startscherm en een kaart onder "Steunen" in Instellingen; beide openen
  eerst `KofiDialog`, nooit direct de website.
- Lettertypes: Space Grotesk voor koppen en cijfers, Inter voor tekst. Gebruik `TABULAR` voor scores, zodat cijfers niet verspringen.
- Controleer UI-wijzigingen met de screenshottests. Voeg een nieuw scherm toe aan `ScreenshotTest` en bekijk de PNG's:
  `./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*'` → `app/build/screenshots/`

## Commando's

```bash
./gradlew :app:testDebugUnitTest   # unit tests + screenshottests (Robolectric)
./gradlew :app:lintDebug           # lint; moet zonder fouten zijn
./gradlew :app:assembleDebug       # debug-APK
```

In een cloudsessie zonder Android SDK: installeer de command-line tools in `/opt/android-sdk` met
`platforms;android-35`, `build-tools;35.0.0` en `platform-tools`, en zet `sdk.dir=/opt/android-sdk` in
`local.properties` (dat bestand staat niet in git).
