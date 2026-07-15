# Coding Agent Evaluation Report

## 1. Benchmark Context

| Field | Value |
| --- | --- |
| Repository under test | Keycloak benchmark suite; Task 1 is standalone Java algorithm work |
| Repository URL | https://github.com/keycloak/keycloak |
| Base branch | unknown |
| Base commit | unknown |
| Java target | JDK 17 |
| Evaluation date | 2026-07-15 |
| Evaluator | Codex |
| Models evaluated | `CASSANDRA`, `MEDEA` |
| Pricing included | Yes for Task 1; token and cost values come from each model's `session.json` |
| Score scale | 1.0 to 5.0, half-point increments |

This report currently summarizes only `01_algorithm/task_001`. Other benchmark categories remain
pending until their results are evaluated.

## 2. Executive Summary

CASSANDRA performed better on Task 1 quality. It completed the standalone Java Temporal Dependency
Router with a more performance-oriented implementation, a detailed edge-case test harness, large
stress runs, and differential correctness validation.

MEDEA also completed Task 1 after the rerun. Its solution is functional by its report, passes the
prompt sample and 1000 randomized small-graph checks against a brute-force state search, and provides
a clear explanation of the temporal Dijkstra plus dominance-pruning approach. Its main weaknesses are
scalability and implementation efficiency: it uses object-heavy graph/frontier/PQ structures, leaves
compiled `out/` artifacts in the workspace, and explicitly notes that the per-query state search may
not scale to the largest `Q=100000` workloads.

The quality gap is narrower than the earlier failed-run comparison, but CASSANDRA remains the Task 1
winner. MEDEA is more cost-efficient for this single task based on session cost, while CASSANDRA
delivered stronger validation against large inputs and better hot-path engineering.

## 3. Overall Leaderboard

Task 1 only:

| Rank | Model | Overall Score | Completed Tasks | Failed / Incomplete Tasks | Total Estimated Cost | Avg Cost / Task | Score / Dollar | High-Level Assessment |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | CASSANDRA | 4.5 / 5 | 1 | 0 | $1.02698425 | $1.02698425 | 4.38 | Stronger implementation quality, stress validation, and performance engineering. |
| 2 | MEDEA | 4.0 / 5 | 1 | 0 | $0.26224780 | $0.26224780 | 15.25 | Completed and much cheaper, but less scalable and less optimized. |

## 4. Category Score Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Average |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | pending | pending | pending | pending | pending | 4.5 |
| MEDEA | 4.0 | pending | pending | pending | pending | pending | 4.0 |

## 5. Pricing And Cost Efficiency

### 5.1 Model Rate Cards

Rates are in USD per 1 million tokens. Real model names are not included in this public report.

| Model | Input / MTok | Output / MTok | Cache Write / MTok | Cache Hit / MTok | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | $5.00 | $25.00 | $6.25 | $0.50 | Cache pricing is included when cache token counts are available. |
| MEDEA | $1.25 | $4.25 | n/a | n/a | Session cost is available; public rate card does not provide separate cache pricing. |

### 5.2 Cost Formula

```text
cost_usd = (input_tokens / 1_000_000 * input_rate)
         + (output_tokens / 1_000_000 * output_rate)
         + (cache_write_tokens / 1_000_000 * cache_write_rate, if available)
         + (cache_hit_tokens / 1_000_000 * cache_hit_rate, if available)
```

When `session.json` provides an explicit total cost, this report uses that value as the task cost.

### 5.3 Cost Matrix

Task 1 only:

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Total Cost |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | $1.02698425 | pending | pending | pending | pending | pending | $1.02698425 |
| MEDEA | $0.26224780 | pending | pending | pending | pending | pending | $0.26224780 |

### 5.4 Cost-Quality Comparison

| Model | Average Score | Total Cost | Completed Tasks | Avg Cost / Completed Task | Score / Dollar | Cost-Adjusted Assessment |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | 4.5 / 5 | $1.02698425 | 1 | $1.02698425 | 4.38 | Best quality for Task 1, but higher cost. |
| MEDEA | 4.0 / 5 | $0.26224780 | 1 | $0.26224780 | 15.25 | Lower quality than CASSANDRA, but better score per dollar on this task. |

**Comparison Notes**

- Quality premium: CASSANDRA delivered stronger validation and a more optimized implementation.
- Cost efficiency: MEDEA produced a completed Task 1 result at roughly one quarter of CASSANDRA's cost.
- Token profile: CASSANDRA used fewer direct input tokens but substantial cache traffic; MEDEA used more direct input and reasoning tokens plus high cache-hit volume.
- Practical recommendation for Task 1: CASSANDRA for quality, MEDEA for cost-sensitive runs where moderate scalability risk is acceptable.

## 6. Category-by-Category Comparison

### 6.1 Algorithm

**Winner:** CASSANDRA

| Model | Score | Correctness | Completeness | Code Quality | Reasoning | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | 5.0 | 5.0 | 4.5 | 4.5 | 4.5 |
| MEDEA | 4.0 | 4.5 | 4.0 | 3.5 | 4.0 | 4.0 |

**Comparison Notes**

- Correctness: Both models implemented temporal Dijkstra over capability-augmented states with dominance pruning. CASSANDRA reported 12 focused edge tests, large stress tests, and 500 randomized differential checks. MEDEA reported the sample, manual edge tests, and 1000 randomized small-graph comparisons.
- Complexity and performance: CASSANDRA used CSR adjacency, primitive arrays, a manual binary heap, and generation-stamped scratch structures. MEDEA used `ArrayList`, object states, `PriorityQueue`, and per-state objects, and it explicitly noted risk for `100000` queries.
- Requirement fit: Both used Java/JDK 17 and produced the required `TemporalDependencyRouter` class. MEDEA left compiled `workspace/algorithm/out/` artifacts, which is less clean for a benchmark submission.
- Reasoning quality: Both explained the algorithm well. CASSANDRA's report was more concrete about performance validation and limitations.
- Main differentiator: CASSANDRA appears more robust under large-input expectations; MEDEA is complete but less optimized.

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
| Reasoning tokens | 0 |
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

- Strengths: Complete implementation, expected source layout, clear algorithmic explanation, broad validation evidence, exact token and cost data available from `session.json`.
- Weaknesses: The theoretical worst case can still stress the `node × 2^P` state space under adversarial dense workloads.
- Risk: Performance appears strong in reported tests, but worst-case search expansion remains the main residual uncertainty.

### 7.2 MEDEA

#### 01_algorithm / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/MEDEA/01_algorithm/task_001` |
| Result file | `models/MEDEA/01_algorithm/task_001/result.md` |
| Score file | not present |
| Overall score | 4.0 / 5 |
| Status | completed |
| Input tokens | 46793 |
| Output tokens | 10154 |
| Reasoning tokens | 18919 |
| Cache write tokens | 0 |
| Cache hit tokens | 534642 |
| Total tokens | 610508 |
| Estimated cost | $0.26224780 |

**Files Changed**

- `models/MEDEA/01_algorithm/task_001/workspace/algorithm/src/main/java/TemporalDependencyRouter.java`: standalone Java implementation.
- `models/MEDEA/01_algorithm/task_001/workspace/algorithm/src/test/java/StressTest.java`: random differential test harness.
- `models/MEDEA/01_algorithm/task_001/workspace/algorithm/out/`: compiled bytecode artifact reported by MEDEA.
- `models/MEDEA/01_algorithm/task_001/result.md`: task result report.
- `models/MEDEA/01_algorithm/task_001/session.json`: token and cost log.

**Commands / Evidence Reported By Agent**

```text
javac -d out src/main/java/TemporalDependencyRouter.java
sample input from prompt -> 10 / 14 / 25
javac -cp src/main/java -d out src/main/java/TemporalDependencyRouter.java src/test/java/StressTest.java
java -cp out StressTest
All 1000 random tests passed
```

**Evaluator Notes**

- Strengths: Completed implementation, clear algorithm explanation, random differential testing, lower task cost.
- Weaknesses: Uses object-heavy lists and priority-queue state objects; leaves compiled build output in the workspace; scalability concerns for the largest query count are acknowledged in its own report.
- Risk: Likely correct for normal cases, but less convincing than CASSANDRA for maximum-scale workloads.

## 8. Direct Model-vs-Model Findings

### 8.1 Strengths Comparison

| Area | CASSANDRA | MEDEA | Better |
| --- | --- | --- | --- |
| Code navigation | Used the expected standalone workspace and source layout. | Used the expected standalone workspace and source layout. | Tie |
| Java implementation quality | Primitive-heavy CSR implementation with manual heap and fewer hot-path allocations. | Clear but object-heavy implementation using `ArrayList`, `PriorityQueue`, and per-state objects. | CASSANDRA |
| Keycloak-specific understanding | Not applicable for standalone Task 1. | Not applicable for standalone Task 1. | Tie |
| Testing instinct | Focused edge-case harness plus large stress and 500-case differential testing. | Sample/manual tests plus 1000 random small-graph differential tests. | CASSANDRA |
| Debugging/root cause analysis | Strong explanation of state projection, windows, dominance, complexity, and limits. | Good explanation, with honest scalability caveat. | CASSANDRA |
| Report clarity | Detailed report with validation and exact cost/token update. | Good report after rerun; initially token fields were stale until session data was available. | CASSANDRA |
| Command usage discipline | Targeted compile/test commands and no reported build artifacts in files changed. | Targeted commands, but compiled `out/` artifact remained in the workspace. | CASSANDRA |

### 8.2 Failure Mode Comparison

| Failure Mode | CASSANDRA | MEDEA | Notes |
| --- | --- | --- | --- |
| Over-broad changes | No evidence | Minor artifact issue | MEDEA reported compiled `out/` output as changed. |
| Missed edge cases | Low evidence | Low-to-moderate evidence | Both tested edge cases; CASSANDRA's explicit edge suite is stronger. |
| Hallucinated APIs/classes | No evidence | No evidence | Task 1 was standalone Java. |
| Weak validation | No | Partial | MEDEA validated correctness on random small graphs but did not report large max-constraint stress comparable to CASSANDRA. |
| Poor report detail | No | No | Both final reports are usable. |
| Incomplete task | No | No | Both completed after MEDEA rerun. |

### 8.3 Reliability Assessment

| Model | Reliability Rating | Evidence |
| --- | ---: | --- |
| CASSANDRA | 4.5 / 5 | Completed Task 1 with code, tests, result report, large-input validation, and token log. |
| MEDEA | 4.0 / 5 | Completed Task 1 with code, tests, result report, and token log, but with scalability and artifact-cleanliness concerns. |

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
| Complex implementation | CASSANDRA | Stronger large-input validation and lower-allocation implementation. |
| Cost-sensitive algorithm runs | MEDEA | Completed the task at lower reported session cost. |
| Bug fixing | pending | Not evaluated in this report update. |
| UI work | pending | Not evaluated in this report update. |
| PR review | pending | Not evaluated in this report update. |
| Codebase analysis | pending | Not evaluated in this report update. |
| Test generation | pending | Not evaluated in this report update. |
| Overall coding agent | CASSANDRA for Task 1 | Higher quality score despite MEDEA's better score per dollar. |

## 11. Caveats And Follow-Ups

- This report currently covers only `01_algorithm/task_001`.
- MEDEA's `session.json` includes 18,919 reasoning tokens. The public model rate card does not specify separate reasoning or cache pricing, so the report uses the total session cost directly.
- CASSANDRA and MEDEA costs come from their respective `session.json` files.
- Remaining categories should be evaluated before drawing an overall benchmark conclusion across all six tasks.
