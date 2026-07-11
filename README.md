# codingBenchmarks

This project evaluates coding agents with anonymized model codes from Greek myth.

The benchmark is file-based and worktree-based. Each task keeps benchmark artifacts under `models/<MODEL_CODE>/<CATEGORY>/<TASK_ID>/`, while the real project under test lives inside that task's `workspace/keycloak/` directory as a git worktree.

## Repository Under Test

The benchmark uses the Keycloak project from GitHub. All models should work from the same pinned Keycloak base branch and base commit for a given benchmark run.

Required language/runtime:

- Java
- JDK 17
- Keycloak worktree per model per task

The coding agent may run normal commands inside its task worktree if useful, including Java/JDK commands, tests, or project build commands. The benchmark repo itself does not provide external grading scripts; the final report must be written to each task's `result.md`.

## Model Codes

Initial model codes:

- `CASSANDRA`
- `MEDEA`

Do not put real model names in public reports. Keep any real model mapping private.

## Directory Structure

```text
codingBenchmarks/
  upstream/
    keycloak/
      # canonical Keycloak clone used to create worktrees

  tasks/
    01_algorithm/task_001/
      prompt.md
      metadata.yaml
    ...

  models/
    CASSANDRA/
      01_algorithm/task_001/
        prompt.md
        metadata.yaml
        result.md
        artifacts/
        workspace/
          keycloak/
            # git worktree branch: bench/CASSANDRA/01_algorithm/task_001
      02_bug_fix/task_001/
        workspace/keycloak/
      ...

    MEDEA/
      01_algorithm/task_001/
        workspace/keycloak/
      ...

  reports/
```

## Worktree Rule

For fairness, every model/task worktree must start from the same pinned Keycloak base branch and base commit.

Recommended branch naming:

```text
bench/<MODEL_CODE>/<CATEGORY>/<TASK_ID>
```

Examples:

```text
bench/CASSANDRA/01_algorithm/task_001
bench/MEDEA/01_algorithm/task_001
```

Recommended worktree locations:

```text
models/CASSANDRA/01_algorithm/task_001/workspace/keycloak
models/MEDEA/01_algorithm/task_001/workspace/keycloak
```

## Workflow

Open the coding agent in a model/task worktree, for example:

```bash
cd models/CASSANDRA/01_algorithm/task_001/workspace/keycloak
```

Then prompt it with something like:

```text
Read ../../prompt.md and complete the task in this Keycloak worktree using Java targeting JDK 17. You may run normal workspace commands if useful. Put your final report in ../../result.md.
```

Review `result.md` manually and assign a score using the 1.0 to 5.0 scale.

## Score Scale

Scores use a 1.0 to 5.0 scale in half-point increments only:

```text
1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
```

Meaning:

- `5.0`: excellent, near production-quality
- `4.0`: good, minor issues
- `3.0`: acceptable, notable gaps
- `2.0`: poor, major issues but some useful progress
- `1.0`: failed or mostly unusable

Suggested dimensions:

- Correctness
- Completeness
- Code Quality
- Reasoning
- Report Quality

## Benchmark Areas

1. `01_algorithm`: complex Java algorithm implementation in Keycloak context
2. `02_bug_fix`: fix a described issue in Keycloak
3. `03_frontend_ui`: implement a UI behavior in Keycloak
4. `04_pr_review`: review a complex Keycloak PR and identify problems
5. `05_code_analysis`: analyze Keycloak and produce project/domain analysis
6. `06_test_generation`: add Java tests for Keycloak code
