# Code Analysis Task 001: Browser Authentication Flow and Required Actions

You are working inside this benchmark task directory.

## Goal

Analyze Keycloak's browser authentication flow and required-action lifecycle, then produce a detailed project analysis in `result.md`.

This is a code-analysis task. Do not implement product changes or bug fixes. You may create temporary notes or artifacts inside this task directory if helpful, but the final deliverable is the analysis report.

## Analysis Scope

Explain how Keycloak moves a user through the browser login path and required actions, including:

1. a browser login request entering the authentication session
2. browser flow executions running, including forms, authenticators, conditional executions, and subflows
3. user identification and credential validation
4. required actions being discovered, queued, challenged, completed, skipped, or failed
5. the flow resuming after each required action
6. the authentication session becoming an authenticated user/client session and redirecting back to the client

Your analysis should be grounded in the Java source code in the Keycloak worktree. Avoid generic descriptions that could apply to any identity system.

## Required Deliverables in result.md

Your `result.md` report must include:

- final status
- executive summary
- key class and interface map with responsibilities
- browser authentication sequence diagram in Mermaid
- required-action lifecycle state machine in Mermaid
- step-by-step execution path with concrete Java class and method references
- explanation of how custom authenticators plug into the browser flow
- explanation of how custom required actions plug into the required-action lifecycle
- discussion of authentication session, root authentication session, user session, and client session relationships
- security-sensitive checkpoints and why they matter
- edge-case analysis
- files inspected
- commands run, if any
- limitations or uncertain areas
- token usage and estimated cost if available

## Required Topics

Cover these domain objects and extension points where they are relevant:

- `AuthenticationSessionModel`
- `RootAuthenticationSessionModel`
- `UserSessionModel`
- `ClientSessionContext`
- `AuthenticationProcessor`
- `Authenticator`
- `AuthenticatorFactory`
- `RequiredActionProvider`
- `RequiredActionFactory`
- browser flow executions and subflows
- conditional authenticators
- required action challenge, process, success, and failure behavior
- redirects back to the OIDC/SAML client after successful authentication

Use repository search to find exact package names and method locations rather than assuming every name is in one module.

## Edge Cases To Analyze

Include concrete notes on these cases where the code supports them:

- expired or missing authentication session
- required action cancelled or failed
- duplicate browser tabs using the same authentication session
- user missing, disabled, or changed during the flow
- brokered identity login that leads into required actions
- reset-credentials or action-token paths that overlap with required actions
- stale, replayed, or invalid action links/tokens
- required actions that should not be bypassable before issuing tokens

If a case is only partially supported by the files you inspected, label it clearly as an inference and explain what evidence would confirm it.

## Diagram Expectations

Use Mermaid diagrams in `result.md`.

The sequence diagram should show the main actors/components, such as browser, endpoint/resource class, authentication processor, authenticator, authentication session, required action provider, user session, and client redirect.

The state machine should focus on required-action lifecycle states and transitions, including queued, challenged, processing, completed, failed/cancelled, and resumed authentication.

## Working Area

Use `workspace/keycloak/` as the Keycloak project checkout for this task. Do not modify files outside this task directory except through the Keycloak worktree under `workspace/keycloak/`.

The Keycloak checkout must be a git worktree created from the benchmark run pinned base branch and base commit. Expected branch naming:

```text
bench/<MODEL_CODE>/05_code_analysis/task_001
```

Example:

```text
bench/CASSANDRA/05_code_analysis/task_001
```

## Execution Policy

You may run normal commands inside `workspace/keycloak/` if they help you complete the task, including Java/JDK commands, tests, project search commands, or documentation generation commands. This benchmark repo does not provide external grading scripts.

Do not make source-code changes for this task unless you need tiny temporary experiments to understand behavior. If you do create temporary files, keep them inside this task directory and mention them in `result.md`.

## Quality Expectations

A strong analysis should:

- cite specific Java classes and methods instead of vague package summaries
- distinguish confirmed behavior from inference
- explain control flow and state transitions clearly
- identify where extension providers enter the flow
- explain how security invariants are preserved
- avoid overclaiming when a path was not fully inspected
- be readable by an engineer who knows Keycloak concepts but has not recently read this code