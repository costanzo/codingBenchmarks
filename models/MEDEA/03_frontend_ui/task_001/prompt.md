# Frontend UI Task 001: Organization Invitation Management

You are working inside this benchmark task directory.

## Goal

Implement a Keycloak Admin Console UI for managing organization membership invitations.

This task is intentionally about a specific Keycloak business workflow: realm administrators need a usable way to invite users into organizations, see pending invitations, and revoke invitations that should no longer be valid.

Your final deliverable is the completed Keycloak worktree plus a report in `result.md`.

## Product Requirements

Add an Organization Invitation Management experience in the Keycloak Admin Console.

The UI should let an admin:

- view pending organization invitations
- search or filter invitations by email, organization, and status where the available data supports it
- create a new invitation with:
  - recipient email
  - target organization
  - optional expiration or validity period if supported by the underlying model/API
  - optional organization role or membership metadata if supported by the underlying model/API
- revoke or cancel a pending invitation with a confirmation modal
- refresh the invitation list after create or revoke actions
- see clear loading, empty, success, validation-error, and request-error states

Prefer the most natural location in the existing Admin Console information architecture, such as an Invitations tab or section under Organizations. Follow existing Keycloak navigation, page, table, form, modal, notification, and i18n patterns.

## Technical Requirements

Use `workspace/keycloak/` as the Keycloak project checkout for this task.

Keycloak runtime and backend code must target Java/JDK 17. Because this is a frontend UI task, you may also modify the existing Keycloak Admin Console frontend stack, including TypeScript, React, generated admin-client bindings, translations, and frontend tests where appropriate.

Do not build a standalone mock app. Integrate into the real Keycloak Admin Console codebase.

Use existing Keycloak APIs, generated clients, hooks, route conventions, PatternFly components, forms, validation helpers, and i18n conventions where possible. If invitation backend support already exists, wire the UI to it. If a small backend/admin-client gap blocks the UI, implement the smallest compatible integration needed rather than creating fake data.

## Working Area

Use `workspace/keycloak/` as the Keycloak project checkout for this task. Do not modify files outside this task directory except through the Keycloak worktree under `workspace/keycloak/`.

The Keycloak checkout must be a git worktree created from the benchmark run pinned base branch and base commit. Expected branch naming:

```text
bench/<MODEL_CODE>/03_frontend_ui/task_001
```

Example:

```text
bench/CASSANDRA/03_frontend_ui/task_001
```

## Suggested Investigation Areas

Start by inspecting the existing Admin Console organization implementation and nearby patterns. Likely areas include, but are not limited to:

- Admin Console routes and navigation for organizations
- Organization list/detail pages
- organization member management UI
- admin-client organization APIs or generated models
- existing invite, create, delete, confirmation modal, and table patterns
- existing i18n resource files
- existing frontend test conventions

Use repository search rather than assuming exact file names.

## Quality Expectations

A strong solution should:

- feel native to the Keycloak Admin Console
- preserve existing behavior for organization and member management
- avoid mock-only UI or hardcoded sample data
- handle permission/API failures gracefully
- keep state transitions predictable after create and revoke actions
- use accessible labels, table actions, form validation, and confirmation text
- add or update tests where practical
- keep changes scoped to the organization invitation workflow

## Execution Policy

You may run normal commands inside `workspace/keycloak/` if they help you complete the task, including Java/JDK commands, frontend build/test commands, linting, or project search commands. This benchmark repo does not provide external grading scripts.

## Completion Requirements

When finished, update `result.md` with:

- final status
- summary of implemented UI behavior
- files changed
- route or navigation entry points added or modified
- API/client integration notes
- validation, loading, empty, and error states implemented
- tests run or verification performed, if any
- known limitations or follow-up work
- token usage if available