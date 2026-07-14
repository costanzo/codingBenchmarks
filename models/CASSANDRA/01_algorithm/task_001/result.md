# Algorithm Task 001: Temporal Dependency Router — Result

## Final Status

**COMPLETE.** A standalone JDK 17 Java solution is implemented, tested against the provided
sample, an extensive edge-case suite, a large performance stress, and validated for correctness
against an independent brute-force solver over 500 randomized inputs (all matched).

## Summary of Work

Implemented `TemporalDependencyRouter`, a command-line program that reads the specified input
format from stdin and prints one earliest-arrival time (or `-1`) per query to stdout. The core is a
per-query, time-ordered label-setting search (Dijkstra variant) over states `(node, capabilityMask)`
with capability projection and superset-dominance pruning.

## Files Changed / Added

- `workspace/algorithm/src/main/java/TemporalDependencyRouter.java` — main implementation
  (public final class, `main`, plus package-private `Solver` and `FastReader` helpers in the same
  file).
- `workspace/algorithm/src/test/java/TemporalDependencyRouterTest.java` — standalone test harness
  (no external framework); redirects stdin/stdout into `main` and compares output. Exits non-zero on
  any failure.

No files outside this task directory were modified.

## Approach and Reasoning

- **State space.** A route's validity depends on accumulated capabilities intersected with the
  query's `requiredCapabilityMask`. Only bits present in the required mask can ever matter, so
  masks are projected onto the required mask. This collapses the state space to
  `node × 2^popcount(requiredMask)` and makes `requiredMask` satisfaction equivalent to reaching
  `mask == requiredMask`.
- **Search.** A Dijkstra-style search ordered by arrival time. The first time a label
  `(target, requiredMask)` is popped, it is the earliest valid arrival (times are monotonic non-
  decreasing as popped). If the smallest time in the frontier ever exceeds the deadline, the search
  stops early and returns `-1`.
- **Edge windows.** For departing at current time `t`, the earliest arrival over an edge is the
  minimum over its windows of `max(t, openTime) + latency`, restricted to windows with
  `t <= closeTime`. Waiting for a future window is handled naturally by `max(t, openTime)`.
  Multiple windows per edge and multiple parallel edges between the same pair are handled by simply
  taking the minimum arrival across all of them.
- **Dominance pruning.** For each node a small list of `(mask, time)` labels is kept. A new label is
  discarded if the node already holds a superset-mask label with time `≤` the new time; conversely,
  inserting a new label removes any stored labels it dominates. This aggressively bounds the number
  of live labels and is what keeps the dense/high-capability worst case tractable.
- **Performance engineering.** Hand-rolled byte-level long parser; CSR adjacency; a manual binary
  heap over primitive `long[]` arrays (no per-state object allocation in the hot loop); per-query
  scratch structures reset in O(1) via a generation stamp so large arrays are not re-zeroed each
  query. All time arithmetic uses `long`. The search is iterative (no recursion), so there is no
  stack-overflow risk.

### Edge cases explicitly handled and tested

Source equals target (already satisfied, and requiring a round-trip to collect capabilities),
required capabilities already satisfied at source, waiting for a future window, multiple windows per
edge, multiple/parallel edges, expired windows, deadline exactly equal to arrival (accepted),
deadline just below arrival (rejected), capability mask `0`, unreachable target, `startTime`
exceeding deadline, and the "weaker-but-earlier vs stronger-but-later" tradeoff.

## Algorithmic Complexity

Let `R` be a query's required mask and `P = popcount(R)`. Per query the state space is
`O(N · 2^P)` and each state relaxes its outgoing edges (each with its windows). With dominance
pruning the practical work is far below the theoretical bound. Worst-case time per query is
`O((N·2^P + E·2^P·Wavg) · log(states))`; space is `O(states + windows)`. Because `K ≤ 12`,
`2^P ≤ 4096`; most realistic queries carry small required masks and terminate quickly, and the
early deadline cutoff prunes further. Input parsing and graph construction are `O(N + M + totalWindows)`.

## Commands Run and Outcomes

- `javac -d out src/main/java/TemporalDependencyRouter.java src/test/java/TemporalDependencyRouterTest.java`
  — compiled cleanly (JDK 17).
- `java -cp out TemporalDependencyRouterTest` — **12/12 tests passed**, including the exact sample
  (`10 / 14 / 25`).
- Large stress: generated `N=100000, M=300000, K=12, Q=100000, ~600000 windows`
  (constraint-max). Ran in **~0.31 s**, peak RSS **~97 MB**, exit 0, 100000 output lines.
- Dense/high-capability stress: `N=2000, M=30000, Q=2000`, every query using the full 12-bit mask
  `4095` with generous deadlines. Ran in **~1.63 s**, peak RSS **~47 MB**, exit 0.
- Differential correctness: an independent `HashMap`-based brute-force Dijkstra was run against the
  main solver on **500 randomized small inputs**; **all outputs matched**.

## Self-Assessed Rating

Overall Score: 4.5 / 5

### Score Breakdown

- Correctness: 5.0 / 5
- Completeness: 5.0 / 5
- Code Quality: 4.5 / 5
- Reasoning: 4.5 / 5
- Report Quality: 4.5 / 5

Correctness is strongly validated (sample + broad edge cases + 500-case differential fuzzing) and
performance comfortably meets the stated large-input expectations in the tested workloads. The small
deduction reflects the theoretical worst case: an adversarially constructed instance with very dense
connectivity, maximal `popcount(R)=12` masks, and long feasible deadlines across all 100000 queries
could still stress the `node × 2^P` state space beyond the tested envelope. Dominance pruning
mitigates this but does not asymptotically eliminate it.

## Token Usage And Cost Inputs

- input_tokens: 34
- output_tokens: 23023
- cache_write_tokens: 37819
- cache_hit_tokens: 429741
- total_tokens: 490617
- estimated_cost_usd: 1.02698425

Cost uses the CASSANDRA rate card in `models/CASSANDRA/model.yaml`: $5.00 / MTok input, $25.00 /
MTok output, $6.25 / MTok cache write, and $0.50 / MTok cache hit. Token and cost values come from
`session.json`.

## Notes / Limitations

- Output uses `\n` line endings and a single buffered write for throughput.
- The reader parses signed decimal integers; the format specifies non-negative values, which is
  fully supported (and negatives would also parse).
- No third-party libraries or build tools are required; the code compiles and runs with plain
  `javac`/`java` on JDK 17. The test harness is intentionally framework-free.
- The dominance-pruning worst case noted in the rating is the main theoretical limitation; all
  tested realistic and near-max-constraint workloads completed well within a second or two.
