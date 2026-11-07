# HW2 – C dalis: pardavimų kodo matavimai prieš ir po pertvarkymo

Apimtis – pardavimų kodas backend'e: `SaleService`, iš jo iškelta `SalesAnalyticsService`, `SalesTotalsService`, `SaleController`, `TotalSalesController` ir naujos DTO / išimties klasės.

| Būsena | Versija | Ką reiškia |
|---|---|---|
| **Prieš** | `30459a0` (`hw2/metrics-tooling`) | `src` sutampa su `0df9540` (`hw1-baseline`); pridėti tik B matavimo įrankiai (`git diff --stat 0df9540 30459a0 -- VGTU-Aventory-Backend/src` – tuščias) |
| **Po** | `hw2/sale` | po visų `refactor(sale)` commit'ų ir 3 pertvarkymo atneštų radinių pataisymo (`fix(sale)`) |

Abi būsenos matuotos tais pačiais įrankiais ir konfigūracija (`build.gradle` iš `hw2/metrics-tooling`):

```bash
cd VGTU-Aventory-Backend
./gradlew test complexityMain pmdMain cpdMain spotbugsMain
```

Neapdorotos ataskaitos: `before/` ir `after/` (`pmd/`, `cpd/`, `spotbugs/`, `complexity/pmd-complexity.csv`, `jacoco/jacocoTestReport.xml`).

## Testai ir padengimas (JaCoCo 0.8.14)

| | Prieš | Po |
|---|---|---|
| Testų skaičius (visi praeina) | 51 | 108 |
| `SaleService` eilutės | 29/111 (26 %) | 33/33 (100 %) |
| `SaleService` šakos | 8/60 (13 %) | 10/10 (100 %) |
| `SalesAnalyticsService` eilutės / šakos | – | 65/65, 30/30 (100 %) |
| `SalesTotalsService` eilutės / šakos | – | 36/36, 14/14 (100 %) |
| `TotalSalesController` eilutės / šakos | 62/88, 14/22 | 32/32, 6/6 (100 %) |
| Visas projektas, eilutės | 255/573 (45 %) | 360/539 (67 %) |
| Visas projektas, šakos | 62/218 (28 %) | 106/198 (54 %) |

Tarpinis taškas (`b26e327`, testai parašyti, kodas dar nekeistas): 82 testai, `SaleService` 111/111 eilučių ir 57/60 šakų. Likusios 3 šakos – visada teisingos sąlygos (143 ir 226 eil.), kurių joks testas negali pasiekti; jas pašalinus (`d276a80`) šakų padengimas tapo 100 %.

## Sudėtingumas (PMD `complexityMain`: CC / CogC / NCSS)

| Metodas | Prieš | Po |
|---|---|---|
| `getProductSalesComparison` | 22 / 19 / 39 | 11 / 10 / 23 |
| `getTopSellingProductsByDateRange` | 15 / 15 / 35 | 3 / 3 / 16 |
| `registerSale` | 9 / 7 / 25 | 9 / 7 / 25 (nepakito) |
| `TotalSalesController.getTotalProfitInInterval` | 4 / 3 / 19 | 1 / – / 2 |
| `TotalSalesController.getTotalSalesCountInInterval` | 5 / 5 / 20 | 1 / – / 2 |
| `TotalSalesController.findAllBySaleDateBetween` | 6 / 5 / 22 | 3 / 2 / 5 |
| `TotalSalesController` (klasė, NCSS) | 90 | 29 |

Iškelti metodai: `validateDateRange` 5/2/5, `validateNotBeforeFirstSale` 3/1/4, `findSalesInRange` 1/–/4, `isCountedSale` 1/1/2; `SalesTotalsService.validateInterval` 7/3/8, `totalSalesCount` 4/3/7; `TotalSalesController.withInterval` 3/2/6.

## Statinė analizė

| Įrankis | Prieš: projektas | Prieš: pardavimų kodas | Po: projektas | Po: pardavimų kodas |
|---|---|---|---|---|
| PMD 7.29.0 | 128 | 38 (12 – 1 prioriteto) | 113 | 23 (6 – 1 prioriteto) |
| CPD (≥ 50 leksemų) | 5 dublikatai | 3 | 2 dublikatai | **0** |
| SpotBugs 4.10.5 + FindSecBugs | 49 | 23 | 41 | 15 |

### PMD – kas dingo

| Taisyklė | Prieš | Po | Kodėl |
|---|---|---|---|
| `AvoidThrowingRawExceptionTypes` (p1) | 12 | 6 | datų patikros iškeltos į vieną metodą; „Not enough stock“ – `InsufficientStockException` |
| `CyclomaticComplexity` | 2 | 1 | `getTopSellingProductsByDateRange` CC 15 → 3 |
| `CognitiveComplexity` | 2 | 0 | abu analitikos metodai po slenksčiu |
| `DataClass` | 3 | 0 | DTO tapo `record` / Lombok klase `dto` pakete |
| `AvoidInstantiatingObjectsInLoops` | 3 | 1 | sumos kaupiamos `Map.merge`, nebekuriami tarpiniai atsakymo objektai; liko 1 – palyginimo rezultatų sąrašo kūrimas |
| `UnnecessaryFullyQualifiedName` | 1 | 0 | `java.time.LocalDateTime.now()` → `LocalDateTime.now(clock)` |

Nepakito: `UseConcurrentHashMap` (5), `UnnecessaryAnnotationValueElement` (4), `AvoidCatchingGenericException` (3, `SaleController` try/catch), `OnDemandImport` (2), `AvoidLiteralsInIfCondition` (1, „5 produktai“).
Pertvarkymas buvo atnešęs `MissingSerialVersionUID` (`InsufficientStockException`, `e4484de`) – ištaisyta pridėjus `serialVersionUID`.

### CPD – visi 3 pardavimų kodo dublikatai pašalinti

| Prieš | Kaip pašalinta |
|---|---|
| `SaleService` 96–119 ir 176–199 (24 eil., 112 leksemų) | Extract Method: `validateDateRange`, `validateNotBeforeFirstSale`, `findSalesInRange` |
| `TotalSalesController` 105–118 ir 133–150 (14 eil., 91 leksema) | logika perkelta į `SalesTotalsService`, bendras `withInterval` |
| `TotalSalesController` 82–90, 110–118, 138–150 (9 eil., 62 leksemos, 3 kopijos) | tas pats `withInterval` |

### SpotBugs

| Tipas | Prieš | Po |
|---|---|---|
| `THROWS_METHOD_THROWS_RUNTIMEEXCEPTION` | 12 | 6 |
| `UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD`, `URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD` | 2 | 0 (vieši DTO laukai pašalinti) |
| `SPRING_ENDPOINT` (informacinis FindSecBugs) | 8 | 8 |
| `ENTITY_LEAK` (`createSale` grąžina JPA `Sale`) | 1 | 1 |
| `EI_EXPOSE_REP`, `EI_EXPOSE_REP2` | 0 | 0 – pertvarkymas buvo atnešęs 2 (`ProductSalesComparisonRequest` saugojo ir grąžino kintamą `List`, `ba71d10`); ištaisyta: įrašas saugo nekeičiamą kopiją |

## Kas nepagerėjo ar pablogėjo

- `registerSale` (CC 9) nekeistas: pertvarkymas buvo skirtas analitikai. Kandidatas tolesniam darbui – Extract Method likučio pakeitimui ir pardavimo sukūrimui.
- Klasės lygio suminis CC beveik nepakito: logika ne dingo, o pasiskirstė per mažesnius metodus ir klases (SRP).
- Pertvarkymas buvo atnešęs 3 naujus radinius (`MissingSerialVersionUID`, du `EI_EXPOSE_REP*`). Jie ištaisyti atskiru commit'u, o `ProductSalesComparisonRequest` elgsena užfiksuota 4 testais (`ProductSalesComparisonRequestTest`). Taisant pirmas variantas (`? null :`) davė naują PMD `NullAssignment`, antras (`if`) – SpotBugs vėl rodė `EI_EXPOSE_REP`; galutinis variantas trūkstamą sąrašą paverčia tuščiu (servisas null ir tuščią sąrašą atmeta ta pačia klaida, tad HTTP atsakymas nepakito).
- Liko `RuntimeException` analitikos patikrose ir `catch (RuntimeException)` controller'yje – juos pakeisti specifinėmis išimtimis ir bendru `@RestControllerAdvice` reikštų keisti klaidų atsakymų formatą (paprastas tekstas / `{"error": ...}`), kurį naudoja klientai.
