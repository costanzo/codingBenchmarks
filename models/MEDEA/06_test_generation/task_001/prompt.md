# Test Generation Task 001: Password Policy Coverage

You are working inside this benchmark task directory.

## Goal

Add meaningful Java tests for Keycloak password policy behavior so the targeted password policy implementation reaches at least 90% line coverage and 90% branch coverage.

This is a test-generation task. Do not change production behavior to make tests pass. Production code changes are allowed only if they are tiny, clearly justified testability fixes that preserve behavior, and they must be explained in `result.md`.

All new test code must be Java and should target JDK 17.

## Coverage Target

The 90% coverage requirement applies to the password policy target area, not the entire Keycloak repository.

Use repository search and existing module structure to identify the relevant password policy implementation classes and existing tests. Likely areas include password policy providers, validators, policy parsing/configuration, and password history or credential interactions.

Aim for both:

- at least 90% line coverage for the targeted password policy classes
- at least 90% branch coverage for the targeted password policy classes

If the local project setup does not make exact coverage measurement practical, still add the tests and document:

- which target classes you intended to cover
- what test command you ran
- what coverage command or report you attempted
- the best available evidence that the target would meet or approach the threshold
- any blocker that prevented exact measurement

## Test Requirements

Add tests that cover meaningful behavior, including:

- valid password policy configurations
- invalid or malformed policy configurations
- minimum and maximum length boundaries where applicable
- digits, lowercase, uppercase, special character, and regex policies where present
- username, email, or personal-data exclusion policies where present
- password history and recently-used-password behavior where present
- hash iteration or hashing-related policy validation where present
- combined policies where multiple validators are active at once
- null, empty, blank, Unicode, long-string, and unusual-character inputs where relevant
- localized or structured error messages if existing tests assert them

Prefer focused unit tests where the password policy code is isolated, and add integration-style tests only where the behavior depends on Keycloak model/session/credential infrastructure.

## Quality Expectations

A strong solution should:

- follow existing Keycloak test style and naming conventions
- avoid brittle tests that depend on unrelated global state
- use existing test helpers, models, factories, and fixtures where available
- verify both passing and failing password-policy cases
- assert specific failures rather than only checking that any error occurred
- avoid duplicating production logic in the test assertions
- keep test scope focused on password policy behavior
- run the smallest relevant Maven test command before broader commands

## Working Area

Use `workspace/keycloak/` as the Keycloak project checkout for this task. Do not modify files outside this task directory except through the Keycloak worktree under `workspace/keycloak/`.

The Keycloak checkout must be a git worktree created from the benchmark run pinned base branch and base commit. Expected branch naming:

```text
bench/<MODEL_CODE>/06_test_generation/task_001
```

Example:

```text
bench/CASSANDRA/06_test_generation/task_001
```

## Execution Policy

You may run normal commands inside `workspace/keycloak/` if they help you complete the task, including Java/JDK commands, Maven test commands, coverage commands, and project search commands. This benchmark repo does not provide external grading scripts.

Use Java targeting JDK 17.

## Completion Requirements

When finished, update `result.md` with:

- final status
- summary of tests added
- password policy target classes identified
- files changed
- test strategy and major cases covered
- commands run and outcomes
- measured line and branch coverage, if available
- explanation if exact 90% coverage could not be measured
- production code changes, if any, and why they preserve behavior
- known limitations or follow-up work
- token usage if available