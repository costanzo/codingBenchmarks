# Result

## Status

completed

Review-only task. No production code was modified. A dedicated git worktree
(`bench/CASSANDRA/04_pr_review/task_001`) was created from the pinned base tag
`26.6.0`; all inspection of the PR was performed against an upstream clone that
contains the PR ref.

## PR Summary

**PR #50650 — "Pre-authentication username enumeration via username/delegation
scope types" (Closes #50468).** Author: Martin Bartoš. Head SHA
`ec5bb77fc8b8700856a8d448c0f2e9ba6e5a08ef`, parent
`ef30b1d23b9840d1df24cff23d445bb42d378300`.

### Problem being fixed
Parameterized client scopes (preview feature `PARAMETERIZED_SCOPES`) support a
`username` scope type (and a `delegation` subtype). Before the PR,
`UsernameScopeType.validateParameter()` called `resolveUser()` (a user-store
lookup plus a disabled check) at **request-parse time, before the requesting
user has authenticated**. Because the authorization endpoint / CIBA backchannel
endpoint validate the scope pre-auth, an unauthenticated caller could observe:

- HTTP 200 (login form) when the target username/email exists and is enabled,
- HTTP 400 `invalid_scope` when the target does not exist,
- a distinct "user is disabled" path for disabled accounts,

thereby enumerating usernames/emails and account state without credentials
(low-severity pre-auth information disclosure).

### Fix
1. **`UsernameScopeType.validateParameter()`** — removed the `resolveUser(...)`
   call; it now only rejects a blank parameter. Existence / disabled /
   self-target checks remain in `validateParameterWithUser(...)`, which is only
   invoked once an authenticated user is known. `DelegationScopeType` inherits
   the fixed `validateParameter` (it only overrides `validateParameterWithUser`,
   adding the impersonation permission check).
2. **`AuthorizationContextUtil`** — added `UserModel`-aware overloads of
   `getAuthorizationRequestContextFromScopesWithClient`,
   `getAuthorizationRequestsStreamFromScopesWithClient`, and
   `getClientScopesStreamFromAuthorizationRequestContextWithClient`. Old
   signatures now delegate with `user = null`, preserving pre-auth behavior.
3. **`TokenManager`** — `attachAuthenticationSession(...)` (≈L466) passes
   `userSession.getUser()`, and `verifyConsentStillAvailable(...)` (≈L813) passes
   `user` into the parser so invalid username/delegation scopes are silently
   dropped post-auth (RFC 6749 §3.3) from consent and issued tokens.
4. **`AuthenticationManager.getClientScopeModelStream(...)`** (≈L1290) passes
   `authSession.getAuthenticatedUser()` so the consent screen is built with the
   post-auth user.
5. **`DefaultClientSessionContext.fromClientSessionAndScopeParameter(...)`** —
   extracts the user via
   `Optional.ofNullable(clientSession.getUserSession()).map(UserSessionModel::getUser).orElse(null)`
   and passes it to the parser; this also removes a potential NPE when the
   user session is null.
6. **`BackchannelAuthenticationEndpoint`** (CIBA) — scope is now validated with
   `TokenManager.isValidScope(session, scope, client)` (no user), with a comment
   explaining that the `login_hint` user is only *identified*, not
   authenticated, at that point. The actual username/delegation filtering happens
   later, during CIBA token issuance, where the authenticated user is known.

### How the drop works
`ClientScopeAuthorizationRequestParser.getMatchingClientScope(user, ...)` calls
`validateParameter` when `user == null` (pre-auth) and `validateParameterWithUser`
when `user != null` (post-auth). On `InvalidScopeParameterException` it logs a
WARN and returns `Optional.empty()`, so the offending parameterized scope is
dropped from the request context (and therefore from consent + tokens) instead
of failing the whole request.

### Tests
- `ParameterizedScopesIsolationTest`: the previously-`invalid_scope` case is
  rewritten to expect successful login with the scope dropped; adds
  `usernameScopeTypeDoesNotLeakUserExistence` and
  `cibaUsernameScopeTypeDoesNotLeakUserExistence`; test client/server config
  extended with CIBA (ping mode, notification endpoint, grant enabled, HTTP auth
  channel URI). Existing tests already cover email resolution, email-disallowed,
  and disabled-target-at-refresh.
- `ParameterizedScopesOAuthGrantTest.consentPageExcludesInvalidUsernameScopeParam`:
  invalid `user-scope:nonexistent-user` is excluded from the consent page and the
  token while a valid `foo-parameter-scope:param1` remains.
- `TokenExchangeDelegationTest`: `delegationNoImpersonation` now expects the
  delegation grant to be **absent** from the consent page and token;
  `cibaDelegationNoImpersonation` now expects backchannel success + dropped
  scope + `may_act` absent, instead of `invalid_scope`. Adds `@AfterEach`
  consent/session cleanup.

## Review Method

**Setup / compare-against note.** The benchmark pinned base is tag `26.6.0`
(`0526b94f0d`). I verified that `26.6.0` **predates the parameterized-scopes
feature entirely** (`UsernameScopeType.java` does not exist at that tag and
`26.6.0` is *not* an ancestor of the PR; ~881 commits separate them). A raw
`git diff 26.6.0...pr` is therefore not meaningful for this change. The
substantive review was done as **PR head vs its direct parent**
(`ef30b1d23b`), which isolates exactly the 9 changed files.

**Worktree**
```
git -C upstream/keycloak worktree add -b bench/CASSANDRA/04_pr_review/task_001 \
    models/CASSANDRA/04_pr_review/task_001/workspace/keycloak 26.6.0
```

**PR inspection (representative commands)**
```
git show pr-50650 --stat
git show pr-50650 -- '*.java' ':!*Test*'          # production diff
git show pr-50650:.../scope/UsernameScopeType.java
git show pr-50650^1:.../scope/UsernameScopeType.java   # parent for comparison
git show pr-50650:.../scope/DelegationScopeType.java
git show pr-50650:.../rar/parsers/ClientScopeAuthorizationRequestParser.java
git show pr-50650:.../TokenManager.java                # isValidScope / getRequestedClientScopes
git show pr-50650:.../util/AuthorizationContextUtil.java
git show pr-50650:.../util/DefaultClientSessionContext.java
git show pr-50650:.../endpoints/AuthorizationEndpointChecker.java
git show pr-50650:.../ciba/endpoints/BackchannelAuthenticationEndpoint.java
git grep -n "fromClientSessionAndScopeParameter" pr-50650 -- services/
git grep -n "getAuthorizationRequestContextFromScopes" pr-50650 -- services/
git cat-file -e 26.6.0:.../UsernameScopeType.java   # confirm feature absent at base
git merge-base --is-ancestor 26.6.0 pr-50650
```
Also traced the pre-auth context-building call sites
(`AuthorizationEndpointRequestParserProcessor` L108,
`ParEndpointRequestParserProcessor` L74) and all callers of
`DefaultClientSessionContext.fromClientSessionAndScopeParameter` and
`getClientScopeModelStream`.

**No build/test execution.** This is a review-only task and the change is a
targeted logic move; static tracing across the parse → validate → drop pipeline
was sufficient. No tiny local experiments were needed.

## Prioritized Findings

### P0 — none
No confirmed critical correctness, data-loss, or security defect was found. The
core fix is sound: existence/disabled/self-target/impersonation checks are
moved from the pre-auth `validateParameter` to the post-auth
`validateParameterWithUser`, and the scope is dropped (not the whole request)
when validation fails post-auth.

### P1 — none confirmed
The most impactful behavior change (below) is intentional and standards-aligned;
I classify it as P2 compatibility rather than P1.

### P2-1 — Post-auth filtering silently depends on a non-null user; a null user reopens the leak / grants unintended delegation
- **Where:** `ClientScopeAuthorizationRequestParser.getMatchingClientScope`
  (`user == null` ⇒ `validateParameter`, else `validateParameterWithUser`);
  consumers `AuthenticationManager.getClientScopeModelStream`
  (`authSession.getAuthenticatedUser()`), `TokenManager.attachAuthenticationSession`
  (`userSession.getUser()`), `verifyConsentStillAvailable`,
  `DefaultClientSessionContext.fromClientSessionAndScopeParameter`.
- **Risk:** The entire security guarantee now rests on the *caller* supplying a
  non-null authenticated user in every post-auth path. If any post-auth path
  reaches the parser with `user == null` (e.g. an auth session where
  `getAuthenticatedUser()` is not yet populated, or a client-session whose
  `getUserSession()` is null), the parser silently falls back to the blank-only
  `validateParameter`, so an invalid `username`/`delegation` scope would **not**
  be dropped — reintroducing the enumeration leak on the consent screen or,
  worse for `delegation`, letting a non-impersonatable delegation scope survive
  into a token (the impersonation check lives only in `validateParameterWithUser`).
- **Why it matters in Keycloak:** delegation grants `may_act`/impersonation
  semantics; skipping `validateParameterWithUser` would be an authorization
  bypass, not just information disclosure.
- **Suggested validation:** add a defensive check (log + drop, or assert) when a
  parameterized scope of a user-bound type is parsed with `user == null` in a
  post-auth context, and add a test that drives consent/token issuance with an
  auth/client session that has no authenticated user to prove the scope is still
  excluded.
- **Confidence:** inference. The interactive auth-code, consent, and CIBA
  token flows examined here are post-auth and pass a real user, so the common
  paths are safe; the risk is the fragility of the implicit contract, not a
  demonstrated null path.

### P2-2 — Incomplete null-`userSession` guard within `DefaultClientSessionContext`
- **Where:** `DefaultClientSessionContext.java`. The PR adds
  `Optional.ofNullable(clientSession.getUserSession())...` in
  `fromClientSessionAndScopeParameter` (≈L98), but the same class still
  dereferences `clientSession.getUserSession().getUser()` unguarded in
  `buildScopesStringFromAuthorizationRequest` (≈L228),
  `getAuthorizationRequestContext()` (≈L251),
  `isClientScopePermittedForUser` (≈L262), and the loading helpers
  (≈L305, ≈L331).
- **Risk:** If the null-`userSession` scenario that justified the new guard is
  actually reachable (the guard implies it is — e.g. introspection / userinfo /
  pre-authorized-code / device / CIBA flows where a client session may lack a
  user session), then constructing the context succeeds but a later call to
  `getAuthorizationRequestContext()` / `buildScopesStringFromAuthorizationRequest`
  on the same object still throws NPE. The fix is thus internally inconsistent.
- **Suggested fix:** either extend the same `Optional` guard to the sibling
  methods, or document/assert that `getUserSession()` is guaranteed non-null on
  those paths (in which case the new guard at L98 is unnecessary and should be
  justified with a comment).
- **Confidence:** the inconsistency is confirmed from code; reachability of the
  sibling NPE with `PARAMETERIZED_SCOPES` enabled is an inference.

### P2-3 — Test gap: no test asserts response *indistinguishability*, which is the actual vulnerability
- **Where:** `ParameterizedScopesIsolationTest`.
- **Risk:** The new tests check the *outcome* of each case separately
  (existing-target ⇒ scope kept; nonexistent-target ⇒ login succeeds + scope
  dropped). They do **not** assert that the observable authorization-endpoint
  response (HTTP status / redirect vs error) is identical for an *existing* vs a
  *nonexistent* vs a *disabled* target. The CVE was precisely the observable
  difference (200 vs 400 vs disabled message). A future refactor could
  re-introduce a distinguishing response while these tests still pass.
- **Suggested fix:** add a test that requests the same scope for a valid target
  and a nonexistent/disabled target and asserts both produce the same
  authorization-endpoint status (redirect/login form), i.e. the enumeration
  oracle is closed.
- **Confidence:** confirmed from the test diff.

### P3-1 — Test coverage holes for closely-related paths
- No **PAR endpoint** test, although issue #50468 references PAR. PAR reuses the
  same pre-auth context builder (`ParEndpointRequestParserProcessor` L74, user =
  `null`), so it is covered by construction, but not exercised.
- No **self-target** username-scope test (`validateParameterWithUser` throws
  "User cannot target themselves"); no test proving that scope is dropped.
- No test for a **disabled existing user at the authorization endpoint**
  (pre-auth); only nonexistent-at-authorization and disabled-at-refresh are
  covered.
- No unit test directly exercising `UsernameScopeType`/`DelegationScopeType`
  `validateParameter` vs `validateParameterWithUser`.
- **Confidence:** confirmed from the diff.

### P3-2 — WARN log on every invalid parameterized scope is attacker-triggerable and reveals user state
- **Where:** `ClientScopeAuthorizationRequestParser.getMatchingClientScope`:
  `logger.warnf("Invalid scope parameter for '%s': %s", requestScope, e.getMessage())`.
- **Risk:** For user-bound scope types the message text differentiates
  "User '…' not found" vs "User '…' is disabled". This is server-side log only
  (not returned to the client, so it does **not** re-open the HTTP oracle), but:
  (a) an unauthenticated caller can now trigger this WARN at will by spamming the
  authorization/CIBA endpoint with bogus `username:` scopes → log-flooding /
  minor DoS-on-logs, and (b) usernames/emails and account state land in logs at
  WARN. Consider DEBUG level and/or omitting the raw parameter value.
- **Confidence:** confirmed from code.

### P3-3 — `login_hint` enumeration in CIBA is untouched (noted for completeness, not a regression)
- **Where:** `BackchannelAuthenticationEndpoint.authorizeClient` resolves the
  `login_hint` user (`resolveUser(...)`) before scope validation.
- **Note:** This PR only closes the *scope-based* enumeration vector. The
  `login_hint` resolution is pre-existing and inherent to CIBA (a user must be
  identified to be notified); it is not made worse by this PR. Flagged only so
  reviewers do not assume all CIBA username-enumeration vectors are closed.
- **Confidence:** confirmed from code; explicitly out of scope for this PR.

## Test Gap Analysis

Strengths: the PR adds/updates focused integration tests across auth-code, CIBA,
consent-screen, and token-exchange delegation flows, and existing tests already
cover email resolution, email-not-allowed, and disabled-at-refresh. The changed
behavior for the primary flows is well exercised.

Gaps (see P2-3, P3-1):
1. No assertion of *indistinguishable responses* between existing / nonexistent /
   disabled targets at the authorization endpoint (the core CVE property).
2. No PAR-endpoint test despite the issue mentioning PAR.
3. No self-target drop test.
4. No disabled-target-at-authorization-endpoint (pre-auth) test.
5. No test for the null-authenticated-user post-auth edge (P2-1) or the
   unguarded `DefaultClientSessionContext` methods (P2-2).
6. No provider-level unit tests for `UsernameScopeType`/`DelegationScopeType`.

## Compatibility and Regression Analysis

- **Backward compatibility (intended behavior change):** username/delegation
  scopes that reference a nonexistent/disabled/self/non-impersonatable user no
  longer cause a `400 invalid_scope` at the authorization or CIBA backchannel
  endpoint. Instead the request proceeds and the offending scope is silently
  omitted from consent and tokens (RFC 6749 §3.3 permits ignoring invalid
  scopes). Any integration that *relied on* the 400 to detect a bad target will
  see a behavior change. Impact is limited because `PARAMETERIZED_SCOPES` is a
  preview/experimental feature and the username/delegation types are new.
- **API/signature compatibility:** `AuthorizationContextUtil` keeps all original
  method signatures (they delegate with `user = null`), so external callers are
  source/binary compatible. New overloads are additive.
- **NPE regression risk:** the guard added in
  `DefaultClientSessionContext.fromClientSessionAndScopeParameter` removes a
  latent NPE for null user sessions; sibling methods remain unguarded (P2-2).
- **CIBA:** the backchannel endpoint now validates scope without the login_hint
  user; genuinely malformed/unknown scopes are still rejected with
  `invalid_scope`, while user-bound scopes defer to post-auth filtering. The new
  CIBA tests confirm both username and delegation cases succeed and drop the
  scope.
- **Non-`PARAMETERIZED_SCOPES` deployments:** unaffected — all new logic is behind
  `Profile.Feature.PARAMETERIZED_SCOPES`; the only non-feature-gated edit is the
  null-safe user extraction in `DefaultClientSessionContext`, which is strictly
  more defensive.
- **Clustering / storage / migration:** no schema, cache, or persisted-state
  changes; nothing cluster- or migration-sensitive.

## Files Inspected

Changed by the PR (all reviewed against parent `ef30b1d23b`):
- `services/.../protocol/oidc/TokenManager.java`
- `services/.../protocol/oidc/grants/ciba/endpoints/BackchannelAuthenticationEndpoint.java`
- `services/.../protocol/oidc/scope/UsernameScopeType.java`
- `services/.../services/managers/AuthenticationManager.java`
- `services/.../services/util/AuthorizationContextUtil.java`
- `services/.../services/util/DefaultClientSessionContext.java`
- `tests/base/.../oauth/ParameterizedScopesIsolationTest.java`
- `tests/base/.../oauth/ParameterizedScopesOAuthGrantTest.java`
- `tests/base/.../oauth/TokenExchangeDelegationTest.java`

Additional context read (unchanged, to trace the pipeline):
- `services/.../protocol/oidc/scope/DelegationScopeType.java`
- `server-spi-private/.../protocol/oidc/scope/ParameterizedScopeTypeProvider.java`
- `services/.../protocol/oidc/rar/parsers/ClientScopeAuthorizationRequestParser.java`
- `services/.../protocol/oidc/endpoints/AuthorizationEndpointChecker.java`
- `services/.../protocol/oidc/endpoints/request/AuthorizationEndpointRequestParserProcessor.java`
- `services/.../protocol/oidc/par/endpoints/request/ParEndpointRequestParserProcessor.java`
- `server-spi/.../models/ClientModel.java` (`getParameterizedClientScope`)

## Risk Assessment

Overall risk: **low-to-moderate**. The change is small, well-scoped, gated behind
a preview feature, and directly addresses the reported enumeration vector; the
primary interactive flows are covered by new/updated tests. No P0/P1 defect was
confirmed.

Residual concerns worth addressing before/at merge:
1. The security property now depends on an implicit "user is non-null in every
   post-auth path" contract (P2-1) — highest-value item to shore up, ideally
   with a defensive drop for user-bound scopes when the user is unexpectedly null
   plus a regression test.
2. The inconsistent null-`userSession` guarding inside
   `DefaultClientSessionContext` (P2-2) should be made consistent.
3. Add an indistinguishability test (P2-3) so the CVE can't silently regress.
4. Consider lowering the WARN log and dropping PII from it (P3-2).

## Token Usage And Cost Inputs

Token counts and cost below are from `session.json`.

- input_tokens: 92
- output_tokens: 33252
- reasoning_tokens: 0
- cache_write_tokens: 97168
- cache_hit_tokens: 2266013
- total_tokens: 2396525
- estimated_cost_usd: 2.57206650

## Notes

- Review-only: no production files were modified. A worktree at
  `workspace/keycloak` was created from pinned base `26.6.0`
  (branch `bench/CASSANDRA/04_pr_review/task_001`); PR content was inspected from
  the upstream clone that holds ref `pull/50650/head`
  (`ec5bb77fc8b8700856a8d448c0f2e9ba6e5a08ef`).
- The benchmark "compare-against pinned base" is `26.6.0`, but that tag predates
  the parameterized-scopes feature and is not an ancestor of the PR, so the
  meaningful diff was computed against the PR's parent commit `ef30b1d23b`. This
  is documented in Review Method and does not affect the findings.
- No build or test run was performed (heavy repo, review-only task, change is a
  targeted logic relocation). Findings labeled "inference" would be confirmed by
  the targeted tests suggested under P2-1/P2-2/P2-3.
- Token/cost numbers were updated from `session.json` during evaluation.
