# Coding Agent Evaluation Report

## 1. Benchmark Context

| Field | Value |
| --- | --- |
| Repository under test | Keycloak |
| Repository URL | https://github.com/keycloak/keycloak |
| Base branch | `<base-branch>` |
| Base commit | `<base-commit-sha>` |
| Java target | JDK 17 |
| Evaluation date | `<yyyy-mm-dd>` |
| Evaluator | `<name>` |
| Models evaluated | `CASSANDRA`, `MEDEA` |
| Score scale | 1.0 to 5.0, half-point increments |

## 2. Executive Summary

Summarize the main result in 3-5 paragraphs:

- Which model performed better overall.
- Which categories had the largest quality gap.
- Whether either model showed serious reliability issues.
- Whether results were consistent across tasks or driven by one strong/weak category.
- Any caveats about incomplete runs, failed builds, missing reports, or environmental issues.

Example:

> CASSANDRA performed better overall, primarily because it produced more targeted fixes and stronger explanations in the bug-fixing and code-analysis tasks. MEDEA was competitive on implementation-heavy tasks but missed more risk areas in PR review and gave less precise validation notes.

## 3. Overall Leaderboard

| Rank | Model | Overall Score | Completed Tasks | Failed / Incomplete Tasks | High-Level Assessment |
| ---: | --- | ---: | ---: | ---: | --- |
| 1 | CASSANDRA | `<avg>` / 5 | `<n>` | `<n>` | `<short summary>` |
| 2 | MEDEA | `<avg>` / 5 | `<n>` | `<n>` | `<short summary>` |

## 4. Category Score Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Average |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<avg>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<avg>` |

## 5. Category-by-Category Comparison

### 5.1 Algorithm

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Correctness | Completeness | Code Quality | Reasoning | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Correctness: `<which model handled the core algorithm and edge cases better>`
- Complexity and performance: `<time/space complexity, scalability, unnecessary work>`
- Integration with Keycloak style: `<fit with existing Java style and conventions>`
- Reasoning quality: `<clarity of explanation, assumptions, tradeoffs>`
- Main differentiator: `<one or two concrete reasons for score gap>`

### 5.2 Bug Fix

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Bug Isolation | Minimality | Regression Risk | Test/Validation Quality | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Root cause analysis: `<who identified the real cause more convincingly>`
- Patch quality: `<small, focused fix vs broad/fragile changes>`
- Regression risk: `<areas that could break>`
- Evidence: `<commands run, tests mentioned, diffs reviewed>`
- Main differentiator: `<why one model scored higher>`

### 5.3 Frontend UI

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Requirement Fit | UI Integration | Accessibility | Maintainability | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Requirement coverage: `<which requested UI behavior was completed>`
- Fit with Keycloak UI patterns: `<component/style consistency>`
- Accessibility/responsiveness: `<keyboard, labels, responsive behavior>`
- Risk: `<visual regressions, brittle state handling, missing tests>`
- Main differentiator: `<why the winner won>`

### 5.4 PR Review

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Critical Findings | Accuracy | False Positives | Coverage | Actionability |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Most important issue found by CASSANDRA: `<issue>`
- Most important issue found by MEDEA: `<issue>`
- Missed seeded/expected issues: `<which model missed what>`
- False positives: `<unsubstantiated or incorrect claims>`
- Main differentiator: `<depth, precision, severity judgment>`

### 5.5 Code Analysis

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Architecture Accuracy | Domain Understanding | Diagram Quality | Specificity | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Accuracy: `<whether analysis matches actual Keycloak code>`
- Depth: `<surface summary vs real domain insight>`
- Diagrams: `<sequence/state diagrams usefulness>`
- Specific code references: `<file/class/method grounding>`
- Main differentiator: `<why scores differ>`

### 5.6 Test Generation

**Winner:** `<CASSANDRA | MEDEA | Tie>`

| Model | Score | Test Relevance | Edge Cases | Assertions | Maintainability | Regression Value |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` |

**Comparison Notes**

- Meaningfulness: `<tests that would catch real bugs vs superficial coverage>`
- Integration with existing Keycloak tests: `<style, utilities, modules>`
- Edge cases: `<important scenarios covered/missed>`
- Maintainability: `<readability, brittleness, test data setup>`
- Main differentiator: `<why one model’s tests are more valuable>`

## 6. Task-Level Evidence

Use this section to record concrete evidence from each task. Link or reference the task folder, result report, branch, and notable files changed.

### 6.1 CASSANDRA

#### 01_algorithm / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/CASSANDRA/01_algorithm/task_001` |
| Result file | `models/CASSANDRA/01_algorithm/task_001/result.md` |
| Score file | `models/CASSANDRA/01_algorithm/task_001/score.md` |
| Overall score | `<score>` / 5 |
| Status | `<completed | partial | failed>` |

**Files Changed**

- `<path/to/file.java>`: `<purpose of change>`

**Commands / Evidence Reported By Agent**

```text
<commands, tests, build output summary, or 'not reported'>
```

**Evaluator Notes**

- Strengths: `<specific positives>`
- Weaknesses: `<specific concerns>`
- Risk: `<possible regressions or uncertainty>`

Repeat this subsection for each task.

### 6.2 MEDEA

Repeat the same structure for MEDEA.

## 7. Direct Model-vs-Model Findings

### 7.1 Strengths Comparison

| Area | CASSANDRA | MEDEA | Better |
| --- | --- | --- | --- |
| Code navigation | `<notes>` | `<notes>` | `<model>` |
| Java implementation quality | `<notes>` | `<notes>` | `<model>` |
| Keycloak-specific understanding | `<notes>` | `<notes>` | `<model>` |
| Testing instinct | `<notes>` | `<notes>` | `<model>` |
| Debugging/root cause analysis | `<notes>` | `<notes>` | `<model>` |
| Report clarity | `<notes>` | `<notes>` | `<model>` |
| Command usage discipline | `<notes>` | `<notes>` | `<model>` |

### 7.2 Failure Mode Comparison

| Failure Mode | CASSANDRA | MEDEA | Notes |
| --- | --- | --- | --- |
| Over-broad changes | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Missed edge cases | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Hallucinated APIs/classes | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Weak validation | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Poor report detail | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Incomplete task | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |

### 7.3 Reliability Assessment

| Model | Reliability Rating | Evidence |
| --- | ---: | --- |
| CASSANDRA | `<score>` / 5 | `<consistency across tasks, build/test behavior, report honesty>` |
| MEDEA | `<score>` / 5 | `<consistency across tasks, build/test behavior, report honesty>` |

## 8. Score Rationale

### 8.1 Scoring Scale

| Score | Meaning |
| ---: | --- |
| 5.0 | Excellent, near production-quality |
| 4.0 | Good, minor issues |
| 3.0 | Acceptable, notable gaps |
| 2.0 | Poor, major issues but some useful progress |
| 1.0 | Failed or mostly unusable |

Half-points indicate performance between two adjacent levels.

### 8.2 Score Dimensions

| Dimension | Weight | What It Measures |
| --- | ---: | --- |
| Correctness | 40% | Whether the answer solves the requested problem and avoids breaking existing behavior |
| Completeness | 20% | Whether all requested requirements are addressed |
| Code Quality | 20% | Fit with Keycloak conventions, maintainability, simplicity, and Java quality |
| Reasoning | 10% | Explanation quality, tradeoffs, root cause analysis, architecture understanding |
| Report Quality | 10% | Clear `result.md`, useful validation notes, honest limitations |

## 9. Final Recommendation

State which model is better for each use case:

| Use Case | Recommended Model | Reason |
| --- | --- | --- |
| Complex implementation | `<model>` | `<reason>` |
| Bug fixing | `<model>` | `<reason>` |
| UI work | `<model>` | `<reason>` |
| PR review | `<model>` | `<reason>` |
| Codebase analysis | `<model>` | `<reason>` |
| Test generation | `<model>` | `<reason>` |
| Overall coding agent | `<model>` | `<reason>` |

## 10. Caveats And Follow-Ups

- `<Any tasks that should be rerun>`
- `<Any environmental issue that affected fairness>`
- `<Any missing logs, incomplete result files, or suspicious outputs>`
- `<Suggested benchmark improvements>`
