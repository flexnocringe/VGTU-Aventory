# HW1 – kodo kokybės matavimai

Matuojama tik žymos `hw1-baseline` versija, apimtis – **backend** (`VGTU-Aventory-Backend`, Java 21, Spring Boot 4).
Pataisymai daromi atskirose šakose (`hw1-fix/...`).

## Aplankų struktūra

| Aplankas | Turinys | Atsakingas |
|---|---|---|
| `metrics/complexity/` | sudėtingumo eksportai | A |
| `metrics/maintainability/`, `metrics/structure/` | MI, OO metrikos, priklausomybių grafas | B |
| `analysis/static/` | statinės analizės ataskaitos ir konfigūracija | C |
| `review/` | nuorodos į kodo peržiūros pull request'us | visi |
| `ai-log/` | kiekvieno nario AI naudojimo žurnalas | visi |

## Statinė analizė (C)

### Įrankiai ir versijos

| Įrankis | Versija | Tipas | Konfigūracija |
|---|---|---|---|
| PMD | 7.29.0 | stilius, kodo kvapai (code smells), dizainas, klaidų šablonai | `analysis/static/config/pmd-ruleset.xml` |
| PMD CPD | 7.29.0 | pasikartojantis kodas (copy-paste), ≥ 50 leksemų | `cpdMain` užduotis `build.gradle` |
| SpotBugs | 4.10.5 (Gradle įskiepis 6.5.13) | klaidos baitkode (bytecode) | `effort = MAX`, `reportLevel = LOW`, `analysis/static/config/spotbugs-exclude.xml` |
| FindSecBugs | 1.14.0 | saugumas (SpotBugs įskiepis) | kartu su SpotBugs |

Analizė vykdoma su JDK 21 (Gradle toolchain), Gradle 9.3.1.

Konfigūracijos sprendimai:
- PMD: visos Java kategorijos, išskyrus `documentation`. Išjungtos taisyklės, kurios yra tik stiliaus pasirinkimas ir pažymėtų beveik kiekvieną metodą (priežastys – komentaruose `pmd-ruleset.xml`). `LocalVariableDeclarationShouldBeAtStartOfBlock` išjungta po pirmo paleidimo: 184 radiniai, o taisyklė prieštarauja rekomendacijai deklaruoti kintamąjį ten, kur jis pirmą kartą naudojamas (Effective Java, 57 punktas).
- PMD ir SpotBugs neanalizuoja DGS codegen sugeneruoto kodo (`org.example.vgtuaventory.codegen`).
- Testų kodas neanalizuojamas (`pmdTest`, `spotbugsTest` išjungti).
- `ignoreFailures = true` – radiniai nestabdo build'o ir CI.

### Paleidimas

```bash
git checkout hw1-baseline
cd VGTU-Aventory-Backend
./gradlew clean staticAnalysis
```

`staticAnalysis` paleidžia `pmdMain`, `cpdMain` ir `spotbugsMain`, o ataskaitas nukopijuoja į `quality-hw1/analysis/static/{pmd,cpd,spotbugs}/`.
HTML ataskaitos: `pmd/main.html`, `spotbugs/main.html`; CPD – tik XML (`cpd/main.xml`).

Atskirai (pvz. gynime):

```bash
./gradlew pmdMain
./gradlew cpdMain
./gradlew spotbugsMain
```
