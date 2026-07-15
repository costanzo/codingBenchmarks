# Coding Agent Evaluation Report

## 1. Benchmark Context

| Field | Value |
| --- | --- |
| Repository under test | Keycloak benchmark suite; Task 1 is standalone Java algorithm work; Task 2 is Keycloak bug fixing |
| Repository URL | https://github.com/keycloak/keycloak |
| Base branch | unknown |
| Base commit | unknown |
| Java target | JDK 17 |
| Evaluation date | 2026-07-15 |
| Evaluator | Codex |
| Models evaluated | `CASSANDRA`, `MEDEA` |
| Pricing included | Yes where `session.json` is available |
| Score scale | 1.0 to 5.0, half-point increments |

This report currently summarizes `01_algorithm/task_001` and `02_bug_fix/task_001` for both models.
Other benchmark categories remain pending until their results are evaluated.

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
winner. MEDEA is more cost-efficient for Task 1 based on session cost, while CASSANDRA delivered
stronger validation against large inputs and better hot-path engineering.

Both models have completed Task 2, the SAML client ID certificate-subject bug fix. Both correctly
identify unsafe X.500 subject construction and replace string-parsed DN creation with safer builder
or escaping APIs. CASSANDRA has stronger round-trip tests and reported negative verification against
the original bug; MEDEA has broader BouncyCastle/FIPS coverage by also updating V3 certificate
subject construction and preserves more Elytron `CN=` compatibility. The Bug Fix category is a tie
on score.

## 3. Overall Leaderboard

Evaluated tasks only:

| Rank | Model | Overall Score | Completed Tasks | Failed / Incomplete Tasks | Total Estimated Cost | Avg Cost / Task | Score / Dollar | High-Level Assessment |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | CASSANDRA | 4.5 / 5 | 2 | 0 | $3.56795650 | $1.78397825 | 2.52 | Best quality so far: stronger Task 1 and tied Task 2. |
| 2 | MEDEA | 4.25 / 5 | 2 | 0 | $1.83209050 | $0.91604525 | 4.64 | Lower cost with a strong Task 2 result. |

## 4. Category Score Matrix

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Average |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | 4.5 | pending | pending | pending | pending | 4.5 |
| MEDEA | 4.0 | 4.5 | pending | pending | pending | pending | 4.25 |

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

Evaluated tasks only:

| Model | Algorithm | Bug Fix | Frontend UI | PR Review | Code Analysis | Test Generation | Total Cost |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | $1.02698425 | $2.54097225 | pending | pending | pending | pending | $3.56795650 |
| MEDEA | $0.26224780 | $1.56984270 | pending | pending | pending | pending | $1.83209050 |

### 5.4 Cost-Quality Comparison

| Model | Average Score | Total Cost | Completed Tasks | Avg Cost / Completed Task | Score / Dollar | Cost-Adjusted Assessment |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| CASSANDRA | 4.5 / 5 | $3.56795650 | 2 | $1.78397825 | 2.52 | Best average quality so far, but higher cost. |
| MEDEA | 4.25 / 5 | $1.83209050 | 2 | $0.91604525 | 4.64 | Strong cost-adjusted result across the two evaluated MEDEA tasks. |

**Comparison Notes**

- Quality premium: CASSANDRA delivered the stronger Task 1 result and tied Task 2 quality.
- Cost efficiency: MEDEA has the higher score-per-dollar across the two evaluated tasks.
- Token profile: CASSANDRA used fewer direct input tokens on Task 1 but substantial cache traffic; MEDEA Task 2 consumed a much larger Keycloak context with 6,577,213 cache-hit tokens.
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

**Winner:** Tie

| Model | Score | Root Cause Analysis | Correctness | Regression Safety | Test Quality | Report Quality |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| CASSANDRA | 4.5 | 5.0 | 4.5 | 4.0 | 4.5 | 4.5 |
| MEDEA | 4.5 | 5.0 | 4.5 | 4.0 | 4.5 | 4.5 |

**Comparison Notes**

- Root cause analysis: Both models correctly traced the failure to unsafe certificate subject DN construction from client-controlled subject text, especially `new X500Name("CN=" + subject)` and Elytron's `new X500Principal("CN=" + subject)` path.
- Patch quality: CASSANDRA uses X.500 builder APIs for the SAML-relevant self-signed path and Elytron helper. MEDEA also fixes BouncyCastle/FIPS V3 certificate subject construction and uses explicit `DERUTF8String` plus `Rdn.escapeValue`.
- Regression risk: CASSANDRA is more focused but changes Elytron behavior for preformatted `CN=` strings. MEDEA is broader and preserves parseable Elytron `CN=` input, but changes more certificate-generation paths.
- Evidence: Both reported provider tests across default, Elytron, and FIPS. I independently reran each default-provider targeted test; both passed.
- Main differentiator: CASSANDRA has stronger exact CN round-trip tests and reported a negative test against the original bug; MEDEA has broader subject-construction coverage.

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

#### 02_bug_fix / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/CASSANDRA/02_bug_fix/task_001` |
| Result file | `models/CASSANDRA/02_bug_fix/task_001/result.md` |
| Score file | `models/CASSANDRA/02_bug_fix/task_001/score.md` |
| Overall score | 4.5 / 5 |
| Status | completed |
| Input tokens | 112 |
| Output tokens | 24813 |
| Reasoning tokens | 0 |
| Cache write tokens | 109445 |
| Cache hit tokens | 2472112 |
| Total tokens | 2606482 |
| Estimated cost | $2.54097225 |

**Files Changed**

- `crypto/default/src/main/java/org/keycloak/crypto/def/BCCertificateUtilsProvider.java`: builds self-signed certificate subject DN with `X500NameBuilder`.
- `crypto/fips1402/src/main/java/org/keycloak/crypto/fips/BCFIPSCertificateUtilsProvider.java`: applies the same self-signed BouncyCastle FIPS provider fix.
- `crypto/elytron/src/main/java/org/keycloak/crypto/elytron/ElytronCertificateUtilsProvider.java`: builds subject principals with `X500PrincipalBuilder`.
- `crypto/default/src/test/java/org/keycloak/crypto/def/test/DefaultCryptoCertificateSubjectTest.java`: default provider round-trip regression test.
- `crypto/fips1402/src/test/java/org/keycloak/crypto/fips/test/FIPS1402CertificateSubjectTest.java`: FIPS provider round-trip regression test.
- `crypto/elytron/src/test/java/org/keycloak/crypto/elytron/test/ElytronCertificateSubjectTest.java`: Elytron provider round-trip regression test.

**Commands / Evidence Reported By Agent**

```text
mvn -o -pl crypto/default -am test -Dtest=DefaultCryptoCertificateSubjectTest
mvn -o -pl crypto/fips1402 -am test -Dtest=FIPS1402CertificateSubjectTest
mvn -o -pl crypto/elytron -am test -Dtest=ElytronCertificateSubjectTest
Regression checks for certificate identity extractor / RSA verifier tests
Negative verification: default-provider test fails against original buggy code
```

**Evaluator Verification**

```text
./mvnw test -pl crypto/default -am -Dtest=DefaultCryptoCertificateSubjectTest -Denforcer.skip=true -DskipTests=false
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Evaluator Notes**

- Strengths: Correct root cause, focused builder-based fix, exact CN round-trip tests, negative verification against the original bug, default-provider test independently verified.
- Weaknesses: No SAML integration test; BouncyCastle/FIPS V3 certificate generation still uses string-parsed subject construction.
- Risk: Low-to-moderate. The SAML default-certificate path is addressed, but Elytron preformatted-DN compatibility and non-SAML V3 certificate subject handling deserve review.

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

#### 02_bug_fix / task_001

| Field | Value |
| --- | --- |
| Worktree branch | `bench/MEDEA/02_bug_fix/task_001` |
| Result file | `models/MEDEA/02_bug_fix/task_001/result.md` |
| Score file | `models/MEDEA/02_bug_fix/task_001/score.md` |
| Overall score | 4.5 / 5 |
| Status | completed |
| Input tokens | 369899 |
| Output tokens | 18835 |
| Reasoning tokens | 9609 |
| Cache write tokens | 0 |
| Cache hit tokens | 6577213 |
| Total tokens | 6975556 |
| Estimated cost | $1.56984270 |

**Files Changed**

- `crypto/default/src/main/java/org/keycloak/crypto/def/BCCertificateUtilsProvider.java`: replaces unsafe string-parsed CN construction with `X500NameBuilder`.
- `crypto/fips1402/src/main/java/org/keycloak/crypto/fips/BCFIPSCertificateUtilsProvider.java`: applies the same BouncyCastle FIPS provider fix.
- `crypto/elytron/src/main/java/org/keycloak/crypto/elytron/ElytronCertificateUtilsProvider.java`: escapes CN values before `X500Principal` construction.
- `core/src/test/java/org/keycloak/util/CertificateUtilsTest.java`: shared certificate-subject special-character tests.
- `crypto/default/src/test/java/org/keycloak/crypto/def/test/CertificateUtilsTest.java`: default provider test wrapper.
- `crypto/elytron/src/test/java/org/keycloak/crypto/elytron/test/CertificateUtilsTest.java`: Elytron provider test wrapper.
- `crypto/fips1402/src/test/java/org/keycloak/crypto/fips/test/CertificateUtilsTest.java`: FIPS provider test wrapper.

**Commands / Evidence Reported By Agent**

```text
mvn test -pl crypto/default -am -Dtest=org.keycloak.crypto.def.test.CertificateUtilsTest -Denforcer.skip=true
mvn test -pl crypto/elytron -am -Dtest=org.keycloak.crypto.elytron.test.CertificateUtilsTest -Denforcer.skip=true
mvn test -pl crypto/fips1402 -am -Dtest=org.keycloak.crypto.fips.test.CertificateUtilsTest -Denforcer.skip=true
mvn test -pl crypto/default -am -Dtest=org.keycloak.crypto.def.test.PemUtilsBCTest -Denforcer.skip=true -o
```

**Evaluator Verification**

```text
./mvnw test -pl crypto/default -am -Dtest=org.keycloak.crypto.def.test.CertificateUtilsTest -Denforcer.skip=true -DskipTests=false
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Evaluator Notes**

- Strengths: Correct root cause, robust escaping/construction strategy, tests cover required URL/query and DN-special characters, default-provider test independently verified.
- Weaknesses: No SAML admin-client integration test; provider-level behavior change is broader than the SAML setup path.
- Risk: Low-to-moderate. The broader provider-level change is justified, but full-DN compatibility for BouncyCastle provider callers deserves review.

## 8. Direct Model-vs-Model Findings

### 8.1 Strengths Comparison

| Area | CASSANDRA | MEDEA | Better |
| --- | --- | --- | --- |
| Code navigation | Used expected workspaces for Tasks 1 and 2. | Used expected workspaces for Tasks 1 and 2. | Tie |
| Java implementation quality | Strong Task 1 and focused Task 2 fix. | Good Task 1, broad Task 2 provider-level fix. | CASSANDRA |
| Keycloak-specific understanding | Strong Task 2 understanding of Keycloak certificate providers. | Strong Task 2 understanding of Keycloak certificate providers. | Tie |
| Testing instinct | Strong Task 1 validation. | Good Task 1 validation and strong Task 2 provider tests. | Tie |
| Debugging/root cause analysis | Strong Task 1 reasoning. | Strong Task 2 root cause analysis. | Tie |
| Report clarity | Detailed Task 1 and Task 2 reports after token updates. | Detailed Task 1 and Task 2 reports after token updates. | Tie |
| Command usage discipline | Targeted Task 1 commands. | Targeted Task 2 Maven tests; Task 1 had minor artifact cleanliness issue. | Tie |

### 8.2 Failure Mode Comparison

| Failure Mode | CASSANDRA | MEDEA | Notes |
| --- | --- | --- | --- |
| Over-broad changes | Minor compatibility/scope caveat | Minor artifact/scope issue | CASSANDRA changes Elytron full-DN behavior; MEDEA Task 1 reported compiled `out/` and Task 2 changes more cert paths. |
| Missed edge cases | Low evidence | Low-to-moderate evidence | Both tested edge cases; CASSANDRA's explicit edge suite is stronger. |
| Hallucinated APIs/classes | No evidence | No evidence | No unsupported APIs/classes observed in evaluated task outputs. |
| Weak validation | No for Task 1 | No for Task 2, partial for Task 1 | MEDEA Task 2 validation is solid; Task 1 lacks CASSANDRA-scale stress. |
| Poor report detail | No | No | Both final reports are usable. |
| Incomplete task | No for evaluated Tasks 1 and 2 | No for evaluated Tasks 1 and 2 | Both completed the two currently evaluated tasks. |

### 8.3 Reliability Assessment

| Model | Reliability Rating | Evidence |
| --- | ---: | --- |
| CASSANDRA | 4.5 / 5 | Completed Tasks 1 and 2 with code, tests, result reports, and token logs; strongest current evidence is Task 1 plus strong Task 2 tests. |
| MEDEA | 4.25 / 5 | Completed Tasks 1 and 2 with code, tests, result reports, and token logs; strongest current evidence is the Task 2 bug fix. |

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

Evaluated tasks:

| Use Case | Recommended Model | Reason |
| --- | --- | --- |
| Complex implementation | CASSANDRA | Stronger large-input validation and lower-allocation implementation. |
| Cost-sensitive algorithm runs | MEDEA | Completed the task at lower reported session cost. |
| Bug fixing | Tie | Both completed Task 2 with robust certificate-subject fixes and passing targeted provider tests. |
| UI work | pending | Not evaluated in this report update. |
| PR review | pending | Not evaluated in this report update. |
| Codebase analysis | pending | Not evaluated in this report update. |
| Test generation | pending | Not evaluated in this report update. |
| Overall coding agent | CASSANDRA so far | Higher average score across the two currently evaluated tasks. |

## 11. Caveats And Follow-Ups

- This report currently covers `01_algorithm/task_001` and `02_bug_fix/task_001` for both models.
- MEDEA's `session.json` includes 18,919 reasoning tokens. The public model rate card does not specify separate reasoning or cache pricing, so the report uses the total session cost directly.
- MEDEA Task 2's `session.json` includes 9,609 reasoning tokens and 6,577,213 cache-hit tokens. The report uses the total session cost directly.
- CASSANDRA Task 2's `session.json` includes 109,445 cache-write tokens and 2,472,112 cache-hit tokens.
- Remaining categories should be evaluated before drawing an overall benchmark conclusion across all six tasks.
