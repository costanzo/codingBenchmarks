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
| Pricing included | Yes; see model rate cards and cost-efficiency section |
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

| Rank | Model | Overall Score | Completed Tasks | Failed / Incomplete Tasks | Total Estimated Cost | Avg Cost / Task | Score / Dollar | High-Level Assessment |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | CASSANDRA | `<avg>` / 5 | `<n>` | `<n>` | `$<cost>` | `$<cost>` | `<score_per_dollar>` | `<short summary>` |
| 2 | MEDEA | `<avg>` / 5 | `<n>` | `<n>` | `$<cost>` | `$<cost>` | `<score_per_dollar>` | `<short summary>` |

## 4. Category Score Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Average |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<avg>` |
| MEDEA | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<score>` | `<avg>` |

## 5. Pricing And Cost Efficiency

### 5.1 Model Rate Cards

Rates are in USD per 1 million tokens. Keep real model names private; only use model codes in this public report.

| Model | Input / MTok | Output / MTok | Cache Write / MTok | Cache Hit / MTok | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | $5.00 | $25.00 | $6.25 | $0.50 | Cache pricing is included when cache token counts are available. |
| MEDEA | $1.25 | $4.25 | `n/a` | `n/a` | Cache pricing was not provided; treat cache fields as unknown unless a private run log supplies them. |

### 5.2 Cost Formula

For each model/task, estimate cost with:

```text
cost_usd = (input_tokens / 1_000_000 * input_rate)
         + (output_tokens / 1_000_000 * output_rate)
         + (cache_write_tokens / 1_000_000 * cache_write_rate, if available)
         + (cache_hit_tokens / 1_000_000 * cache_hit_rate, if available)
```

If cache usage is unavailable, calculate the known non-cache cost and mark cache cost as `unknown`, not zero.

### 5.3 Cost Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Total Cost |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<total>` |
| MEDEA | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<cost>` | `$<total>` |

### 5.4 Cost-Quality Comparison

| Model | Average Score | Total Cost | Completed Tasks | Avg Cost / Completed Task | Score / Dollar | Cost-Adjusted Assessment |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | `<avg>` / 5 | `$<cost>` | `<n>` | `$<cost>` | `<score_per_dollar>` | `<quality vs cost notes>` |
| MEDEA | `<avg>` / 5 | `$<cost>` | `<n>` | `$<cost>` | `<score_per_dollar>` | `<quality vs cost notes>` |

**Comparison Notes**

- Quality premium: `<whether a higher-cost model delivered enough quality to justify the price>`
- Cost efficiency: `<which model produced better score per dollar>`
- Token profile: `<which tasks consumed the most input, output, or cache tokens>`
- Practical recommendation: `<best model when optimizing for quality, cost, or balanced value>`

## 6. Category-by-Category Comparison

### 6.1 Algorithm

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

### 6.2 Bug Fix

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

### 6.3 Frontend UI

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

### 6.4 PR Review

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

### 6.5 Code Analysis

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

### 6.6 Test Generation

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

## 7. Task-Level Evidence

Use this section to record concrete evidence from each task. Link or reference the task folder, result report, branch, and notable files changed.

### 7.1 CASSANDRA

#### 01_algorithm / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/CASSANDRA/01_algorithm/task_001` |
| Result file | `models/CASSANDRA/01_algorithm/task_001/result.md` |
| Score file | `models/CASSANDRA/01_algorithm/task_001/score.md` |
| Overall score | `<score>` / 5 |
| Status | `<completed | partial | failed>` |
| Input tokens | `<n | unknown>` |
| Output tokens | `<n | unknown>` |
| Cache write tokens | `<n | unknown>` |
| Cache hit tokens | `<n | unknown>` |
| Estimated cost | `$<cost | unknown>` |

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

### 7.2 MEDEA

Repeat the same structure for MEDEA.

## 8. Direct Model-vs-Model Findings

### 8.1 Strengths Comparison

| Area | CASSANDRA | MEDEA | Better |
| --- | --- | --- | --- |
| Code navigation | `<notes>` | `<notes>` | `<model>` |
| Java implementation quality | `<notes>` | `<notes>` | `<model>` |
| Keycloak-specific understanding | `<notes>` | `<notes>` | `<model>` |
| Testing instinct | `<notes>` | `<notes>` | `<model>` |
| Debugging/root cause analysis | `<notes>` | `<notes>` | `<model>` |
| Report clarity | `<notes>` | `<notes>` | `<model>` |
| Command usage discipline | `<notes>` | `<notes>` | `<model>` |

### 8.2 Failure Mode Comparison

| Failure Mode | CASSANDRA | MEDEA | Notes |
| --- | --- | --- | --- |
| Over-broad changes | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Missed edge cases | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Hallucinated APIs/classes | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Weak validation | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Poor report detail | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |
| Incomplete task | `<yes/no/examples>` | `<yes/no/examples>` | `<notes>` |

### 8.3 Reliability Assessment

| Model | Reliability Rating | Evidence |
| --- | ---: | --- |
| CASSANDRA | `<score>` / 5 | `<consistency across tasks, build/test behavior, report honesty>` |
| MEDEA | `<score>` / 5 | `<consistency across tasks, build/test behavior, report honesty>` |

## 9. Score Rationale

### 9.1 Scoring Scale

| Score | Meaning |
| ---: | --- |
| 5.0 | Excellent, near production-quality |
| 4.0 | Good, minor issues |
| 3.0 | Acceptable, notable gaps |
| 2.0 | Poor, major issues but some useful progress |
| 1.0 | Failed or mostly unusable |

Half-points indicate performance between two adjacent levels.

### 9.2 Score Dimensions

| Dimension | Weight | What It Measures |
| --- | ---: | --- |
| Correctness | 40% | Whether the answer solves the requested problem and avoids breaking existing behavior |
| Completeness | 20% | Whether all requested requirements are addressed |
| Code Quality | 20% | Fit with Keycloak conventions, maintainability, simplicity, and Java quality |
| Reasoning | 10% | Explanation quality, tradeoffs, root cause analysis, architecture understanding |
| Report Quality | 10% | Clear `result.md`, useful validation notes, honest limitations |

## 10. Final Recommendation

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

## 11. Caveats And Follow-Ups

- `<Any tasks that should be rerun>`
- `<Any environmental issue that affected fairness>`
- `<Any missing logs, incomplete result files, or suspicious outputs>`
- `<Suggested benchmark improvements>`
- `<Any missing token or cache usage data that limits cost comparison>`
