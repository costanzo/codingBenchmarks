# Coding Agent Evaluation Report

## 1. Benchmark Context

| Field | Value |
| --- | --- |
| Repository under test | Keycloak benchmark suite; Task 1 is standalone Java algorithm work |
| Repository URL | https://github.com/keycloak/keycloak |
| Base branch | unknown |
| Base commit | unknown |
| Java target | JDK 17 |
| Evaluation date | 2026-07-14 |
| Evaluator | Codex |
| Models evaluated | `CASSANDRA`, `MEDEA` |
| Pricing included | Yes for CASSANDRA Task 1; unavailable for MEDEA Task 1 because no token log is present |
| Score scale | 1.0 to 5.0, half-point increments |

This report currently summarizes only `01_algorithm/task_001`, as requested. Other benchmark
categories remain pending until their results are evaluated.

## 2. Executive Summary

CASSANDRA clearly outperformed MEDEA on Task 1. CASSANDRA completed the standalone Java algorithm
implementation, produced a detailed result report, added implementation and test files, and reported
substantial validation evidence.

MEDEA failed Task 1. It reportedly thought for approximately 3 minutes and then stopped without
producing implementation output, changed files, tests, or a useful task report. No `session.json` or
equivalent token log is present for MEDEA Task 1, so its token usage and cost cannot be calculated
from the available files.

The main quality gap is reliability and completion. CASSANDRA delivered a working solution with
tests and complexity reasoning; MEDEA produced no task output.

## 3. Overall Leaderboard

Task 1 only:

| Rank | Model | Overall Score | Completed Tasks | Failed / Incomplete Tasks | Total Estimated Cost | Avg Cost / Task | Score / Dollar | High-Level Assessment |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | CASSANDRA | 4.5 / 5 | 1 | 0 | $1.02698425 | $1.02698425 | 4.38 | Completed the algorithm task with implementation, tests, and detailed reporting. |
| 2 | MEDEA | 1.0 / 5 | 0 | 1 | unknown | unknown | unknown | Failed to produce output after approximately 3 minutes. |

## 4. Category Score Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Average |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | pending | pending | pending | pending | pending | 4.5 |
| MEDEA | 1.0 | pending | pending | pending | pending | pending | 1.0 |

## 5. Pricing And Cost Efficiency

### 5.1 Model Rate Cards

Rates are in USD per 1 million tokens. Real model names are not included in this public report.

| Model | Input / MTok | Output / MTok | Cache Write / MTok | Cache Hit / MTok | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | $5.00 | $25.00 | $6.25 | $0.50 | Cache pricing is included when cache token counts are available. |
| MEDEA | $1.25 | $4.25 | n/a | n/a | Cache pricing was not provided; cache fields remain unknown unless a private run log supplies them. |

### 5.2 Cost Formula

```text
cost_usd = (input_tokens / 1_000_000 * input_rate)
         + (output_tokens / 1_000_000 * output_rate)
         + (cache_write_tokens / 1_000_000 * cache_write_rate, if available)
         + (cache_hit_tokens / 1_000_000 * cache_hit_rate, if available)
```

### 5.3 Cost Matrix

Task 1 only:

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Total Cost |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | $1.02698425 | pending | pending | pending | pending | pending | $1.02698425 |
| MEDEA | unknown | pending | pending | pending | pending | pending | unknown |

### 5.4 Cost-Quality Comparison

| Model | Average Score | Total Cost | Completed Tasks | Avg Cost / Completed Task | Score / Dollar | Cost-Adjusted Assessment |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | 4.5 / 5 | $1.02698425 | 1 | $1.02698425 | 4.38 | Strong quality for the completed task; exact cost available from `session.json`. |
| MEDEA | 1.0 / 5 | unknown | 0 | unknown | unknown | Cannot assess cost efficiency because the task failed and no token log is available. |

**Comparison Notes**

- Quality premium: CASSANDRA delivered usable work; MEDEA did not produce an answer.
- Cost efficiency: CASSANDRA is the only model with both a score and a known cost for Task 1.
- Token profile: CASSANDRA used 34 input tokens, 23,023 output tokens, 37,819 cache-write tokens, and 429,741 cache-hit tokens.
- Practical recommendation for Task 1: CASSANDRA.

## 6. Category-by-Category Comparison

### 6.1 Algorithm

**Winner:** CASSANDRA

| Model | Score | Correctness | Completeness | Code Quality | Reasoning | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | 5.0 | 5.0 | 4.5 | 4.5 | 4.5 |
| MEDEA | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |

**Comparison Notes**

- Correctness: CASSANDRA implemented the Temporal Dependency Router and reported sample, edge-case, stress, and randomized differential validation. MEDEA produced no implementation to evaluate.
- Complexity and performance: CASSANDRA used a Dijkstra-style label-setting search over `(node, capabilityMask)` states with capability projection and dominance pruning. MEDEA reported no approach.
- Integration: Task 1 is standalone Java rather than Keycloak integration work. CASSANDRA used JDK 17-compatible Java in the expected workspace layout.
- Reasoning quality: CASSANDRA explained state representation, window handling, dominance pruning, complexity, and limitations. MEDEA provided no reasoning.
- Main differentiator: CASSANDRA completed the task; MEDEA failed before producing output.

### 6.2 Bug Fix

Pending.

### 6.3 Frontend UI

Pending.

### 6.4 PR Review

Pending.

### 6.5 Code Analysis

Pending.

### 6.6 Test Generation

Pending.

## 7. Task-Level Evidence

### 7.1 CASSANDRA

#### 01_algorithm / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/CASSANDRA/01_algorithm/task_001` |
| Result file | `models/CASSANDRA/01_algorithm/task_001/result.md` |
| Score file | not present |
| Overall score | 4.5 / 5 |
| Status | completed |
| Input tokens | 34 |
| Output tokens | 23023 |
| Cache write tokens | 37819 |
| Cache hit tokens | 429741 |
| Total tokens | 490617 |
| Estimated cost | $1.02698425 |

**Files Changed**

- `models/CASSANDRA/01_algorithm/task_001/workspace/algorithm/src/main/java/TemporalDependencyRouter.java`: standalone Java implementation.
- `models/CASSANDRA/01_algorithm/task_001/workspace/algorithm/src/test/java/TemporalDependencyRouterTest.java`: standalone test harness.
- `models/CASSANDRA/01_algorithm/task_001/result.md`: task result report.

**Commands / Evidence Reported By Agent**

```text
javac -d out src/main/java/TemporalDependencyRouter.java src/test/java/TemporalDependencyRouterTest.java
java -cp out TemporalDependencyRouterTest
Large stress: N=100000, M=300000, K=12, Q=100000, ~600000 windows, ~0.31 s, ~97 MB RSS
Dense/high-capability stress: N=2000, M=30000, Q=2000, full 12-bit mask, ~1.63 s, ~47 MB RSS
Differential correctness: 500 randomized small inputs matched an independent brute-force solver
```

**Evaluator Notes**

- Strengths: Complete implementation, expected file layout, clear algorithmic explanation, broad validation evidence, exact token and cost data available from `session.json`.
- Weaknesses: The reported theoretical worst case can still stress the `node × 2^P` state space under adversarial dense workloads.
- Risk: Performance appears strong in reported tests, but worst-case search expansion remains the main residual uncertainty.

### 7.2 MEDEA

#### 01_algorithm / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/MEDEA/01_algorithm/task_001` |
| Result file | `models/MEDEA/01_algorithm/task_001/result.md` |
| Score file | not present |
| Overall score | 1.0 / 5 |
| Status | failed |
| Input tokens | unknown |
| Output tokens | unknown |
| Cache write tokens | unknown |
| Cache hit tokens | unknown |
| Total tokens | unknown |
| Estimated cost | unknown |

**Files Changed**

- None.

**Commands / Evidence Reported By Agent**

```text
No commands, tests, build output, implementation, or result report were produced by MEDEA.
The run reportedly stopped after approximately 3 minutes without output.
```

**Evaluator Notes**

- Strengths: None observed for Task 1.
- Weaknesses: Failed to produce implementation, tests, reasoning, report, or token log.
- Risk: This is a hard task failure and should be counted as incomplete.

## 8. Direct Model-vs-Model Findings

### 8.1 Strengths Comparison

| Area | CASSANDRA | MEDEA | Better |
| --- | --- | --- | --- |
| Code navigation | Found and used the standalone task workspace. | No output. | CASSANDRA |
| Java implementation quality | Produced a JDK 17 standalone solution and test harness. | No implementation. | CASSANDRA |
| Keycloak-specific understanding | Not applicable for standalone Task 1. | Not applicable. | Tie |
| Testing instinct | Reported unit, stress, and differential testing. | No tests. | CASSANDRA |
| Debugging/root cause analysis | Explained algorithm choices and limitations. | No reasoning. | CASSANDRA |
| Report clarity | Detailed `result.md` with evidence and limitations. | No useful report until evaluator update. | CASSANDRA |
| Command usage discipline | Reported targeted compile and test commands. | No commands reported. | CASSANDRA |

### 8.2 Failure Mode Comparison

| Failure Mode | CASSANDRA | MEDEA | Notes |
| --- | --- | --- | --- |
| Over-broad changes | No | No evidence | CASSANDRA stayed within the task workspace. |
| Missed edge cases | Low evidence | Unknown | CASSANDRA explicitly tested many edge cases; MEDEA produced no code. |
| Hallucinated APIs/classes | No evidence | No evidence | Task 1 was standalone Java. |
| Weak validation | No | Yes | CASSANDRA reported validation; MEDEA had none. |
| Poor report detail | No | Yes | MEDEA did not produce a substantive report. |
| Incomplete task | No | Yes | MEDEA stopped without output. |

### 8.3 Reliability Assessment

| Model | Reliability Rating | Evidence |
| --- | ---: | --- |
| CASSANDRA | 4.5 / 5 | Completed Task 1 with code, tests, result report, and token log. |
| MEDEA | 1.0 / 5 | Failed Task 1 after approximately 3 minutes with no output. |

## 9. Score Rationale

### 9.1 Scoring Scale

| Score | Meaning |
| ---: | --- |
| 5.0 | Excellent, near production-quality |
| 4.0 | Good, minor issues |
| 3.0 | Acceptable, notable gaps |
| 2.0 | Poor, major issues but some useful progress |
| 1.0 | Failed or mostly unusable |

### 9.2 Score Dimensions

| Dimension | Weight | What It Measures |
| --- | ---: | --- |
| Correctness | 40% | Whether the answer solves the requested problem and avoids breaking existing behavior |
| Completeness | 20% | Whether all requested requirements are addressed |
| Code Quality | 20% | Fit with task conventions, maintainability, simplicity, and Java quality |
| Reasoning | 10% | Explanation quality, tradeoffs, root cause analysis, architecture understanding |
| Report Quality | 10% | Clear `result.md`, useful validation notes, honest limitations |

## 10. Final Recommendation

Task 1 only:

| Use Case | Recommended Model | Reason |
| --- | --- | --- |
| Complex implementation | CASSANDRA | Completed the standalone temporal graph routing implementation with validation. |
| Bug fixing | pending | Not evaluated in this report update. |
| UI work | pending | Not evaluated in this report update. |
| PR review | pending | Not evaluated in this report update. |
| Codebase analysis | pending | Not evaluated in this report update. |
| Test generation | pending | Not evaluated in this report update. |
| Overall coding agent | CASSANDRA for Task 1 | Only CASSANDRA completed the evaluated task. |

## 11. Caveats And Follow-Ups

- This report currently covers only `01_algorithm/task_001`.
- MEDEA Task 1 has no `session.json`, so token usage and cost are unknown.
- CASSANDRA Task 1 token and cost data come from `models/CASSANDRA/01_algorithm/task_001/session.json`.
- Remaining categories should be evaluated before drawing an overall benchmark conclusion across all six tasks.
