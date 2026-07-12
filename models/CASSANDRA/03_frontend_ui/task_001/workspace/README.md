# Workspace

Place the task-specific Keycloak git worktree in this directory as:

```text
workspace/keycloak/
```

The Keycloak worktree should be created from the benchmark run's pinned base branch and base commit.

Expected branch naming:

```text
bench/<MODEL_CODE>/<CATEGORY>/<TASK_ID>
```

Example:

```text
bench/CASSANDRA/03_frontend_ui/task_001
```

The coding agent should work inside `workspace/keycloak/`, may run normal workspace commands if useful, and must write the final report to the sibling `result.md` file in the task directory.

