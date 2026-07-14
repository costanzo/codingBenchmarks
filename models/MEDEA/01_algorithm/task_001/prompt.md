# Algorithm Task 001: Temporal Dependency Router

You are working inside this benchmark task directory.

## Goal

Implement a standalone Java solution for a difficult temporal graph routing problem. This task does not use Keycloak or any other third-party project.

All implementation code, test code, sample code, code snippets, and proposed patches for this task must be written in Java and should target JDK 17.

## Working Area

Use this directory for the standalone algorithm implementation:

```text
workspace/algorithm/
```

Recommended layout:

```text
workspace/algorithm/src/main/java/TemporalDependencyRouter.java
workspace/algorithm/src/test/java/TemporalDependencyRouterTest.java
```

You may run normal workspace commands if they help you complete the task, including Java/JDK commands or tests. Do not modify files outside this task directory.

## Problem Statement

A distributed platform contains `N` services connected by directed dependency calls. A dependency call from service `u` to service `v` is only available during specific time windows. Each available window has its own latency.

Every service also provides zero or more capabilities. A request may require a set of capabilities to have been encountered before it reaches the target service. Capabilities are accumulated from every visited service, including the source and target.

For each query, compute the earliest possible arrival time at the target service, starting at a given time, while satisfying the required capability set. If no valid route exists before the deadline, return `-1`.

## Formal Definition

There are:

- `N` services numbered `1..N`
- `K` capability types numbered `0..K-1`
- `M` directed temporal dependency edges
- `Q` route queries

Each service `i` has a capability bitmask `cap[i]`.

Each directed edge has one or more availability windows. A window is:

```text
openTime closeTime latency
```

You may depart on that edge at time `t` if:

```text
openTime <= t <= closeTime
```

If you arrive at service `u` before `openTime`, you may wait until `openTime` and then take the edge. The arrival time at `v` is:

```text
max(currentTime, openTime) + latency
```

The edge cannot be used from that window if `currentTime > closeTime`.

If multiple windows exist for the same edge, choose the window that produces the earliest valid arrival.

For each query:

```text
source target startTime deadline requiredCapabilityMask
```

A route is valid only if:

```text
arrivalTime <= deadline
(accumulatedCapabilities & requiredCapabilityMask) == requiredCapabilityMask
```

Return the minimum valid arrival time, or `-1` if no such route exists.

## Input Format

```text
N M K Q
cap[1]
cap[2]
...
cap[N]

For each of M edges:
u v W
openTime_1 closeTime_1 latency_1
openTime_2 closeTime_2 latency_2
...
openTime_W closeTime_W latency_W

For each of Q queries:
source target startTime deadline requiredCapabilityMask
```

All masks are non-negative decimal integers.

## Output Format

Print one line per query:

```text
earliestArrivalTime
```

or:

```text
-1
```

## Constraints

The implementation should be designed for large inputs:

```text
1 <= N <= 100000
1 <= M <= 300000
0 <= K <= 12
1 <= Q <= 100000
1 <= total number of edge windows <= 600000
0 <= time values <= 1_000_000_000_000
1 <= latency <= 1_000_000_000
```

Important performance expectations:

- Use `long` for time values.
- Avoid recursion that can overflow the stack.
- Avoid object-heavy per-state allocations in hot loops where practical.
- Use fast input parsing.
- Use priority-queue based search, interval lookup, and dominance pruning where appropriate.

## Required Java API

Implement a public class named:

```java
public final class TemporalDependencyRouter
```

It must support command-line execution from standard input/stdout:

```java
public static void main(String[] args) throws Exception
```

You may add helper classes inside the same file or package-private classes in additional Java files.

## Sample Input

```text
4 4 3 3
1
2
4
0
1 2 2
0 10 5
20 30 1
2 4 1
6 25 4
1 3 1
0 100 3
3 4 1
4 8 10
1 4 0 20 3
1 4 0 20 5
1 4 11 40 3
```

## Sample Output

```text
10
14
25
```

## Sample Explanation

Query 1 requires capabilities `0` and `1`, mask `3`. Start at service `1` at time `0`, already collecting capability `0`. Take `1 -> 2` at time `0`, arrive at `5`, collect capability `1`, then wait until time `6` for `2 -> 4`, arriving at `10`.

Query 2 requires capabilities `0` and `2`, mask `5`. Take `1 -> 3`, arrive at `3`, collect capability `2`, then wait until time `4` for `3 -> 4`, arriving at `14`.

Query 3 starts at time `11`. The first `1 -> 2` window has expired, so wait for the second window at time `20`, arrive at service `2` at time `21`, then take `2 -> 4` and arrive at `25`.

## Edge Cases To Consider

- Source equals target
- Required capabilities already satisfied at source
- Waiting for a future edge window
- Multiple windows per edge
- Multiple edges between the same pair of services
- Expired windows
- Deadline exactly equal to arrival time
- Capability mask `0`
- Unreachable target
- Routes that arrive earlier but with weaker capabilities versus later routes with stronger capabilities

## Completion Requirements

When finished, update `result.md` with:

- final status
- summary of work
- files changed
- approach and reasoning
- algorithmic complexity
- commands run and outcomes, if any
- self-assessed rating if requested
- token usage and estimated cost if available
- notes or limitations

