# Bug Fixing Task 001: SAML Client Creation Fails For Client IDs With Query Characters

You are working inside this benchmark task directory.

## Source Issue

GitHub issue: https://github.com/keycloak/keycloak/issues/50177

Issue summary: Creating a SAML client from the Keycloak Admin Console can fail when the Client ID contains URL query-string characters such as `=`. The failure happens while Keycloak creates the default SAML signing certificate for the client.

## Goal

Fix the Keycloak bug described in issue #50177.

All implementation code, test code, sample project code, code snippets, and proposed patches for this task must be written in Java and should target JDK 17.

## Working Area

Use `workspace/keycloak/` as the Keycloak project checkout for this task. Do not modify files outside this task directory except through the Keycloak worktree under `workspace/keycloak/`.

The Keycloak checkout must be a git worktree created from the benchmark run pinned base branch and base commit. Expected branch naming:

```text
bench/<MODEL_CODE>/02_bug_fix/task_001
```

Example:

```text
bench/CASSANDRA/02_bug_fix/task_001
```

## Bug Description

When a user creates a SAML client in the Admin Console, Keycloak initializes SAML client defaults. As part of that flow, Keycloak creates a key pair and a self-signed certificate.

The reported issue occurs when the SAML Client ID is also a SAML entity ID containing URL query-string syntax, for example an entity ID with `?action=...` or another segment containing `=`.

The Client ID is used when constructing the certificate subject distinguished name. If the Client ID is inserted into the subject DN without proper escaping or sanitization, the X.500 name parser can interpret special characters as DN syntax and fail.

The visible result is:

- SAML client creation fails.
- The server returns an uncaught error.
- The client is not created.

This call path is a likely place to investigate:

```text
SamlProtocolFactory.setupClientDefaults
KeycloakModelUtils.generateKeyPairCertificate
CertificateUtils.generateV1SelfSignedCertificate
BCCertificateUtilsProvider.generateV1SelfSignedCertificate
```

The underlying exception comes from BouncyCastle X.500 name parsing when the generated subject string is malformed.

## Reproduction Scenario

Use a Keycloak worktree and investigate the SAML client creation path.

The issue can be reproduced conceptually by creating a SAML client with a Client ID shaped like a URL containing query parameters, such as:

```text
https://example.com/?action=sso_id
```

Expected behavior:

```text
SAML client creation succeeds regardless of Client ID format.
```

Actual behavior:

```text
SAML client creation fails while creating the default certificate.
```

## Requirements

Implement a robust fix in Keycloak.

Your fix should:

- Preserve support for SAML Client IDs that are URLs or entity IDs.
- Prevent malformed X.500 subject distinguished names when Client ID contains special DN characters.
- Avoid broad behavioral changes outside the SAML default-certificate creation path unless clearly justified.
- Include or update Java tests that cover Client IDs containing characters such as `=`, `?`, `/`, `:`, and `&` where appropriate.
- Prefer escaping or constructing X.500 names safely over ad hoc string replacement.
- Keep the change compatible with JDK 17.

## Investigation Hints

Start by inspecting the code around:

```text
org.keycloak.protocol.saml.SamlProtocolFactory
org.keycloak.models.utils.KeycloakModelUtils
org.keycloak.common.util.CertificateUtils
org.keycloak.crypto.def.BCCertificateUtilsProvider
```

Look for where a subject DN or common name is created from the Client ID.

A good solution should make the certificate subject safe without corrupting the client ID stored in Keycloak or changing the SAML entity ID semantics.

## Execution Policy

You may run normal commands inside `workspace/keycloak/` if they help you complete the task, including Java/JDK commands, tests, or project build commands. Use Java targeting JDK 17. This benchmark repo does not provide external grading scripts. Complete the task by editing the Keycloak worktree and writing your final report in `result.md`.

## Completion Requirements

When finished, update `result.md` with:

- final status
- summary of the bug and root cause
- files changed
- approach and reasoning
- tests added or updated
- commands run and outcomes, if any
- remaining risks or limitations
- self-assessed rating if requested
- token usage if available
- notes


