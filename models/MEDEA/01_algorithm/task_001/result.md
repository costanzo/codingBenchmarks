# Result

## Status

completed

## Summary

Implemented a standalone Java solution for the Temporal Dependency Router problem in `workspace/algorithm/src/main/java/TemporalDependencyRouter.java`. The solution supports fast input parsing, time-dependent edge departure computation (choosing best availability window), and per-query temporal Dijkstra. For queries with zero required capabilities it runs a standard earliest-arrival time-dependent Dijkstra (FIFO property). For queries requiring capabilities (up to K=12) it runs a state-expanded Dijkstra over (node, capabilityMaskSubset) where mask = accumulated & requiredMask, enhanced with Pareto dominance pruning: a state (mask_sup, time_sup) dominates (mask_sub, time_sub) if mask_sup superset mask_sub and time_sup <= time_sub. Dominated states are discarded, and new states that dominate existing frontier entries cause removal of those entries. This keeps frontier size small while guaranteeing optimality. Source==target and deadline checks, as well as impossible required masks (bits outside K), are handled explicitly. Sample tests and 1000 random stress tests against a brute-force non-pruned search pass.

## Files Changed

- `workspace/algorithm/src/main/java/TemporalDependencyRouter.java` — main implementation (~450 lines). Contains FastScanner, Edge definition with window arrays, dominance helpers, and two query solvers.
- `workspace/algorithm/src/test/java/StressTest.java` — auxiliary random testing harness that compares dominance-pruned search against full state brute-force Dijkstra for small graphs (used during development, not required for final run).
- `workspace/algorithm/out/` — compiled bytecode directory (build artifact, not part of source logic).

## Approach and Reasoning

### Problem Modeling
- Time-dependent FIFO edge: earliest arrival for departure t is min_w { max(t, open_w)+lat_w if t <= close_w }. FIFO property holds (non-decreasing arrival vs departure) → Dijkstra works.
- Capability accumulation is monotonic OR. Only bits inside requiredMask matter → state mask = cap[node] & reqMask OR accumulation, limited to 2^{popcount(req)} ≤ 4096.

### Core Algorithms

**Edge departure:**
Scan all windows of edge (average 2, total 600k) to compute minimal feasible arrival. Correct for multiple windows and waiting.

**Zero-capability queries:**
Standard Dijkstra:
dist[s]=start, PQ ordered by arrival time, relax edges using earliestArrival(). Stop when PQ empty or minimal time > deadline. Early exit when target popped.

**Capability queries:**
State = (node, mask). Initial mask = cap[s] & req. Frontier per node maintains Pareto-optimal entries.
PQ ordered by time. Pop minimal; discard if dominated (removed or superseded). If node==target and mask==req, return time (minimal due to PQ ordering).
For each outgoing edge compute next arrival and nextMask = curMask | (cap[v] & req). If frontier of v already has a superset with ≤ time → skip. Else remove entries dominated by new (subset & time ≥ new) and insert.

Dominance correctness: If A superset B and time_A ≤ time_B, any future extension from B can be done from A no later and with at least same capabilities, so B never leads to better solution.

**Versioned arrays:** To avoid O(N) reset per query, maintain int version arrays for dist and frontiers. New list allocated lazily when version mismatches.

### Edge Cases Handled
- Source equals target: immediate answer if capabilities satisfied, otherwise search allows leaving and returning.
- Required mask 0: fast path without state expansion.
- Waiting for future windows, expired windows, multiple windows per edge, multiple edges between same pair.
- Deadline exactly equal arrival, unreachable target, impossible capability (bit outside K or never provided).
- K=0.

## Algorithmic Complexity

Let W_e = number of windows for edge e, total W = Σ W_e ≤ 600k.

Per edge relaxation: O(W_e) scan (average ≈2).

**Zero-capability query:**
O((N + M) log N + total window scans) in worst case = O((M * avgW + N) log N). Early exit often reduces.

**Capability query:**
Let k = popcount(req) ≤12, S = 2^k ≤4096. In worst case states = N*S (409M) infeasible, but dominance pruning dramatically reduces. Without pruning, worst-case O(N*S log(N*S) + M*S*avgW). With pruning per node frontier size f_n ≤ S but typically small (often <10). Practical per query: O(visitedStates log visitedStates + visitedEdges * f_eval). For Q large (100k) worst-case infeasible; solution targets correctness and reasonable scalability for moderate inputs, and would need additional batching (grouping by source) for full 100k×100k worst case which is beyond single-machine sequential processing.

Memory: adjacency O(N+M+W), frontiers O(visited nodes * avg frontier), dist O(N), plus PQ.

## Commands Run and Outcomes

```bash
mkdir -p workspace/algorithm/src/main/java workspace/algorithm/src/test/java
javac -d out src/main/java/TemporalDependencyRouter.java
echo "compiled"
cat sample.txt | java -cp out TemporalDependencyRouter
# Sample input from prompt -> output:
# 10
# 14
# 25  (matches expected)

# additional manual edge tests:
# 2 nodes 1 edge -> queries with/without caps, source==target

javac -cp src/main/java -d out src/main/java/TemporalDependencyRouter.java src/test/java/StressTest.java
java -cp out StressTest
# -> All 1000 random tests passed
```

All sample and custom edge cases passed. Stress test comparing dominance-pruned search vs brute-force full (node,mask) Dijkstra on 1000 random small graphs passed.

## Rating

Overall Score: 4.0 / 5

### Score Breakdown
- Correctness: 4.5 / 5 — sample and brute-force verified, dominance logic proven, handles all listed edge cases.
- Scalability: 3.0 / 5 — FIFO Dijkstra + window scan is optimal; capability state search uses dominance pruning but per query worst-case still exponential in K and could be heavy for Q=100k × N=100k. Further improvements (source grouping, A*, bidirectional, pre-processing total capability OR) could help.
- Code Quality: 4.0 / 5 — modular, fast I/O, no recursion, long for times, clear dominance helpers, versioned arrays to avoid O(NQ) resets.
- Edge Cases: 4.5 / 5 — covers source==target, waiting, expired windows, multiple windows, deadline equality, mask 0, impossible masks, return-to-source collection.
- Report Quality: 4.0 / 5

## Token Usage And Cost Inputs

- input_tokens: 46793
- output_tokens: 10154
- cache_write_tokens: 0
- cache_hit_tokens: 534642
- total_tokens: 610508
- estimated_cost_usd: 0.26224780000000003

Token and cost values come from `session.json`. Total tokens include 18,919 reasoning tokens in
addition to input, output, cache-write, and cache-hit tokens.

## Notes

- JRE: JDK 17 used as required.
- Implementation avoids object-heavy allocations in hot loop for edge departure (primitive long[] arrays), but still allocates FrontierEntry objects per state; could be further optimized with primitive pools.
- Dominance pruning is crucial for K=12. Without it, per query would explore up to N*4096 states.
- For extremely large Q (100k) the sequential per-query Dijkstra may exceed time limits; potential future work: index queries by source, reuse frontier across same source with different startTimes using incremental approaches, or offline precomputation of time-independent reachability for capability coverage.
- No external libraries used.
