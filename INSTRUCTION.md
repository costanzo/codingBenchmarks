# OpenCode Task Instructions

Use this file as the copy-paste guide for running each benchmark task with OpenCode.

Replace `<MODEL_CODE>` with the anonymized model code you are evaluating, for example `CASSANDRA` or `MEDEA`. Do not put real model names in public reports.

Before starting a task, make sure its workspace is prepared:

- `01_algorithm/task_001` uses `workspace/algorithm/` and does not need Keycloak.
- `02_bug_fix` through `06_test_generation` use `workspace/keycloak/` and must be git worktrees created from the correct pinned Keycloak base commit.
- For `02_bug_fix/task_001`, use an affected Keycloak base such as `26.6.0`, not latest `main`, because the upstream issue is closed.

Every OpenCode prompt should tell the agent to update `../../result.md` before finishing.

## Task 1: Algorithm

Workspace:

```bash
cd models/<MODEL_CODE>/01_algorithm/task_001/workspace/algorithm
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the algorithm task in this standalone Java workspace using JDK 17.

Implement the required solution in Java. You may run normal workspace commands if useful. Keep all task files inside this workspace unless the prompt explicitly says otherwise.

When finished, write the final report to ../../result.md, including status, summary, files changed, commands run, validation results, limitations, token usage, and estimated cost if available.
```

## Task 2: Bug Fix

Task: Keycloak issue #50177, SAML client creation fails when the Client ID contains URL query-string or X.500 DN-special characters.

Important: this task should use an affected Keycloak base, such as `26.6.0`, not latest `main`.

Workspace:

```bash
cd models/<MODEL_CODE>/02_bug_fix/task_001/workspace/keycloak
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the bug-fix task in this Keycloak worktree using Java targeting JDK 17.

This task is based on Keycloak issue #50177. Investigate why SAML client creation fails when the Client ID contains URL query-string or X.500 DN-special characters such as "=", "?", "/", ":", and "&".

Implement a robust fix, add or update relevant Java tests, and run targeted commands if useful. Do not modify files outside this task worktree.

When finished, write the final report to ../../result.md, including status, root cause, files changed, tests added or updated, commands run, remaining limitations, token usage, and estimated cost if available.
```

## Task 3: Frontend UI

Task: Organization Invitation Management UI in the Keycloak Admin Console.

Workspace:

```bash
cd models/<MODEL_CODE>/03_frontend_ui/task_001/workspace/keycloak
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the frontend UI task in this Keycloak worktree.

Implement the Organization Invitation Management experience in the real Keycloak Admin Console. Use Java/JDK 17 for backend/runtime code if needed, and use the existing Admin Console TypeScript/React/PatternFly stack for frontend work.

Do not build a standalone mock app. Follow existing Keycloak navigation, API/client, form, table, modal, i18n, accessibility, and test conventions. You may run normal workspace commands if useful.

When finished, write the final report to ../../result.md, including status, implemented UI behavior, files changed, navigation or route entry points, API/client integration notes, UI states and validation, tests or verification, limitations, token usage, and estimated cost if available.
```

## Task 4: PR Review

Task: Review Keycloak PR #50650.

Workspace:

```bash
cd models/<MODEL_CODE>/04_pr_review/task_001/workspace/keycloak
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the PR review task in this Keycloak worktree.

Review Keycloak PR #50650. This is a review-only task: do not implement fixes unless you need tiny local experiments to understand behavior.

Fetch and inspect the PR if needed, compare it against the pinned benchmark base, and produce a code-review style report with prioritized P0-P3 findings. Focus on concrete correctness, regression, security, compatibility, maintainability, and test-coverage risks. Avoid speculative findings unless clearly labeled.

When finished, write the final report to ../../result.md, including status, PR summary, review method and commands run, prioritized findings, test-gap analysis, compatibility/regression analysis, files inspected, risk assessment, token usage, and estimated cost if available.
```

Suggested PR fetch commands inside the worktree if needed:

```bash
git fetch origin pull/50650/head:pr-50650
git diff main...pr-50650 --stat
git diff main...pr-50650
```

If the benchmark uses a pinned base other than `main`, compare against that pinned base instead.

## Task 5: Code Analysis

Task: Browser authentication flow and required actions analysis.

Workspace:

```bash
cd models/<MODEL_CODE>/05_code_analysis/task_001/workspace/keycloak
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the code-analysis task in this Keycloak worktree.

Analyze Keycloak's browser authentication flow and required-action lifecycle. This is an analysis task: do not implement product changes or bug fixes.

Use the Java source code to produce a source-grounded report. Include Mermaid diagrams for the browser authentication sequence and required-action state machine, key class/interface responsibilities, execution path with concrete class and method references, extension points, session model relationships, security checkpoints, edge cases, files inspected, and commands run if any.

When finished, write the final report to ../../result.md, including limitations, token usage, and estimated cost if available.
```

## Task 6: Test Generation

Task: Password policy test coverage.

Workspace:

```bash
cd models/<MODEL_CODE>/06_test_generation/task_001/workspace/keycloak
opencode
```

Paste into OpenCode:

```text
Read ../../prompt.md and complete the test-generation task in this Keycloak worktree using Java targeting JDK 17.

Add meaningful Java tests for Keycloak password policy behavior so the targeted password policy implementation reaches at least 90% line coverage and 90% branch coverage. The coverage target applies to the password policy target classes, not the whole Keycloak repository.

Do not change production behavior to make tests pass. Follow existing Keycloak test conventions, add focused tests for valid and invalid policies, boundary cases, combined policies, password history, malformed inputs, and security-relevant failures. Run targeted Maven test or coverage commands if useful.

When finished, write the final report to ../../result.md, including status, summary of tests added, target classes, files changed, test strategy, commands run, coverage results if available, production code changes if any, limitations, token usage, and estimated cost if available.
```

## After Each Task

From the repository root, inspect the result and worktree diff. Example for CASSANDRA task 2:

```bash
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak status --short
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak diff --stat
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak diff
cat models/CASSANDRA/02_bug_fix/task_001/result.md
```

Then manually assign a score using the 1.0 to 5.0 half-point scale.