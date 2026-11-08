# HW2 – testai, pertvarkymas ir matavimai

Apimtis – **backend** (`VGTU-Aventory-Backend`, Java 21, Spring Boot 4, Gradle 9.3.1).
Darbo šaka `hw2/main`, kiekvieno nario šaka `hw2/<sritis>` (A – `hw2/auth`, B – `hw2/product`, C – `hw2/sale`).

## Aplankų struktūra

| Aplankas | Turinys | Atsakingas |
|---|---|---|
| `metrics/hw2-start/` | pradinės metrikos (žyma `hw2-start`) | B |
| `a-auth/after/` | AuthService, LoginAttemptService, AuthController matavimai po pertvarkymo | A |
| `b-product/after/` | ProductController, ProductService matavimai po pertvarkymo | B |

Kiekviename `after/` aplanke: `jacoco.xml`, `pmd-complexity.csv`, `pmd.xml`, `spotbugs.xml`, `cpd.xml`, `gradle.log` ir `santrauka.txt` (testų skaičius, radinių sumos, klasių eilutės).

## Paleidimas

Visos komandos leidžiamos iš `VGTU-Aventory-Backend` aplanko. Reikia JDK 21 (`JAVA_HOME` turi rodyti į JDK 21).

### Testai

```bash
./gradlew test
```

Rezultatai: `build/test-results/test/*.xml`, HTML – `build/reports/tests/test/index.html`.
Vienos klasės testai: `./gradlew test --tests "org.example.vgtuaventory.service.AuthServiceTest"`.

### Testų padengimas (JaCoCo 0.8.14)

```bash
./gradlew test jacocoTestReport
```

`test` automatiškai paleidžia `jacocoTestReport`. Ataskaitos: `build/reports/jacoco/test/jacocoTestReport.xml` ir `build/reports/jacoco/test/html/index.html`.

### Sudėtingumas (PMD CC, CogC, NCSS)

```bash
./gradlew complexityMain
```

CSV: `build/reports/complexity/pmd-complexity.csv`. Taisyklės – `quality-hw1/analysis/static/config/complexity-ruleset.xml` (slenkstis 1, todėl įrašomas kiekvienas metodas).

### Statinė analizė (PMD, CPD, SpotBugs)

```bash
./gradlew pmdMain cpdMain spotbugsMain
```

Ataskaitos: `build/reports/pmd/main.xml`, `build/reports/cpd/main.xml`, `build/reports/spotbugs/main.xml`.

### Visi matavimai vienu kartu

```bash
./gradlew clean test jacocoTestReport complexityMain pmdMain spotbugsMain cpdMain
```

Po to ataskaitos kopijuojamos į atitinkamą `quality-hw2/<narys>/after/` aplanką.

## Commit'ų taisyklės

- Vienas commit'as – vienas žingsnis: testai (`test(...)`) atskirai nuo pertvarkymo (`refactor(...)`), matavimai – `docs(hw2): ...`.
- Prieš kiekvieną `refactor` commit'ą visi testai turi praeiti; elgsena fiksuojama testais anksčiau, nei keičiamas kodas.
- Pakeitimai į `hw2/main` patenka per pull request'ą su komandos nario peržiūra.
