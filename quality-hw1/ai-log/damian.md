# AI naudojimo žurnalas – Damian (narys C)

| Data | Įrankis | Tikslas | Ką AI pasiūlė / padarė | Kaip patikrinta |
|---|---|---|---|---|
| 2026-11-02 | Claude Code (Claude Opus 5.5) | C dalies plano pritaikymas projekto stack'ui | Žingsnių planą 6 ir 8 užduotims, įrankių rinkinį (PMD, SpotBugs + FindSecBugs) | Plano punktai sutikrinti su užduoties dokumentu |
| 2026-11-02 | Claude Code (Claude Opus 5.5) | Statinės analizės įrankių konfigūravimas backend'ui | PMD, CPD ir SpotBugs + FindSecBugs konfigūraciją `build.gradle`, `pmd-ruleset.xml`, `spotbugs-exclude.xml`, `staticAnalysis` užduotį | Įrankiai paleisti su `./gradlew`, ataskaitos sugeneruotos; radinių skaičiai imami tik iš įrankių ataskaitų ant `hw1-baseline` |
| 2026-11-02 | Claude Code (Claude Opus 5.5) | Tolesnių žingsnių planas po B darbo | Baseline = commit `0df9540` (tą patį naudoja B PR #26), README paleidimo komandų pataisymas, galutinių ataskaitų generavimas | `git diff 0df9540 -- VGTU-Aventory-Backend/src` tuščias; skaičiai (PMD 128, SpotBugs 49, CPD 5) paimti iš sugeneruotų XML ataskaitų |
