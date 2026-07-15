# Result

## Status

completed

## PR Summary

**PR #50650 – Pre-authentication username enumeration via username/delegation scope types (Closes #50468)**

- **Type:** Security weakness fix (CVSS 3.7 Low), not GA feature but pre-auth enumeration.
- **Root Cause:** `UsernameScopeType.validateParameter()` called `resolveUser()` (user store lookup + disabled check) before the requesting user authenticated. `AuthorizationEndpointChecker` / `isValidScope` returned 400 `invalid_scope` for nonexistent/disabled target users vs 200 login form for valid users, allowing unauthenticated enumeration. `DelegationScopeType` inherits same issue, plus CIBA endpoint validated scope with `login_hint` user.
- **Fix:**
  - `UsernameScopeType.validateParameter()` now only checks blank, removing store lookup. Existence/disabled/self-target checks remain in `validateParameterWithUser()` for post-auth filtering.
  - `AuthorizationContextUtil` adds overloads accepting `UserModel` (`getAuthorizationRequestContextFromScopesWithClient(...,user,scope)`, `getAuthorizationRequestsStream...`, `getClientScopesStream...`). Old methods delegate to new with `null` to preserve pre-auth behavior.
  - `TokenManager.attachAuthenticationSession()` and `verifyConsentStillAvailable()` now pass `userSession.getUser()` / `user` into parsing, so invalid username/delegation scopes are silently dropped post-auth (per RFC 6749 §3.3) from consent and tokens.
  - `AuthenticationManager.getClientScopeModelStream()` passes `authSession.getAuthenticatedUser()`.
  - `DefaultClientSessionContext.fromClientSessionAndScopeParameter()` extracts user via `Optional.ofNullable(clientSession.getUserSession()).map(UserSessionModel::getUser).orElse(null)` and passes it to parsing; also fixes potential NPE when `userSession` null.
  - `BackchannelAuthenticationEndpoint` validates scope without user (`isValidScope(session,scope,client)`) to prevent CIBA enumeration, with comment explaining pre-auth.
- **Tests:**
  - `ParameterizedScopesIsolationTest`: changes existing invalid username test from expecting `invalid_scope` error to successful login + scope dropped; adds `usernameScopeTypeDoesNotLeakUserExistence` and `cibaUsernameScopeTypeDoesNotLeakUserExistence` covering auth-code and CIBA flows.
  - `ParameterizedScopesOAuthGrantTest.consentPageExcludesInvalidUsernameScopeParam`: verifies consent page excludes invalid `user-scope:nonexistent-user` while showing valid `foo-parameter-scope:param1`, and token scope verifies drop.
  - `TokenExchangeDelegationTest`: `delegationNoImpersonation` now expects consent page *not* to show delegation grant when user cannot impersonate, and scope dropped; `cibaDelegationNoImpersonation` now expects success + scope dropped instead of `invalid_scope`.
  - Server config for isolation test extended with CIBA attributes.

Observed head: `ec5bb77fc8b8700856a8d448c0f2e9ba6e5a08ef`.

## Review Method

**Setup:**
- Pinned base is `26.6.0` (0526b94f0d) which predates parameterized scopes, so direct diff against base shows no files. Used canonical upstream clone at `/home/ubuntu/codingBenchmarks/upstream/keycloak`.
- Created worktree `/home/ubuntu/codingBenchmarks/models/MEDEA/04_pr_review/task_001/workspace/keycloak` from tag `26.6.0` via `git worktree add -b bench/MEDEA/04_pr_review/task_001 ... 26.6.0`.
- Fetched PR ref: `git fetch origin pull/50650/head:pr-50650` in upstream clone.
- Inspected commits: `git log --oneline pr-50650 -1`, `git show pr-50650 --stat`, `git show ec5bb77...` full diff.
- Compared parent vs PR: `git show pr-50650^1:services/...` vs `pr-50650:services/...` for all 6 service files.

**Code inspection:**
- Read `UsernameScopeType`, `DelegationScopeType` (via `ls-tree` and `show`), `ClientScopeAuthorizationRequestParser` (core parsing logic that returns `Optional.empty()` on `InvalidScopeParameterException`), `TokenManager.isValidScope` (622-770), `AuthorizationContextUtil`, `AuthenticationManager.getClientScopeModelStream`, `DefaultClientSessionContext`, `BackchannelAuthenticationEndpoint`, `AuthorizationEndpointChecker`, `OAuth2GrantTypeBase.getRequestedScopes`, `StandardTokenExchangeProvider.getRequestedScope`.
- Searched for other `isValidScope` usages: `AuthorizationEndpointChecker`, `OAuth2GrantTypeBase`, `StandardTokenExchangeProvider`, `BackchannelAuthenticationEndpoint`.
- Checked mappers `ParameterizedScopeMapper`, `OIDCLoginProtocolFactory.DELEGATION_SCOPE`, may_act handling.

**Web:**
- Attempted `webfetch` of `https://github.com/keycloak/keycloak/pull/50650` and issue `#50468` – GitHub HTML partially blocked but extracted PR description, reviewer comments (msdaly200 requested CIBA tests, rmartinc approved), and issue reproduction steps confirming enumeration via 200 vs 400 and distinct disabled message, plus PAR/CIBA note.

**No build/test execution** beyond static analysis (review-only task). Tiny local experiments not needed.

**Commands run (representative):**
```bash
git -C /home/.../upstream/keycloak worktree add -b bench/MEDEA/04_pr_review/task_001 .../workspace/keycloak 26.6.0
git -C .../upstream/keycloak fetch origin pull/50650/head:pr-50650
git -C .../upstream/keycloak log --oneline pr-50650 -5
git -C .../upstream/keycloak show pr-50650 --stat
git -C .../upstream/keycloak show ec5bb77fc8b8700856a8d448c0f2e9ba6e5a08ef
git -C .../upstream/keycloak show pr-50650^1:services/.../UsernameScopeType.java
git -C .../upstream/keycloak show pr-50650:services/.../UsernameScopeType.java
git -C .../upstream/keycloak show pr-50650^1:services/.../AuthorizationContextUtil.java
git -C .../upstream/keycloak show pr-50650:services/.../AuthorizationContextUtil.java
git -C .../upstream/keycloak show pr-50650^1:services/.../TokenManager.java | sed -n '440,490p'
git -C .../upstream/keycloak grep -n "isValidScope" -- services/src/main/java/org/keycloak/protocol/oidc/endpoints/AuthorizationEndpointChecker.java
```

## Prioritized Findings

### P0 – Critical correctness/security/data-loss
- **None introduced by PR.** PR fixes pre-existing P0-ish enumeration weakness (weakness type, CVSS 3.7). No new data-loss or auth bypass introduced.

### P1 – Serious regression or production risk

**[P1] Client-credentials / null-userSession path now includes invalid username/delegation scopes instead of dropping**

- **Files:** `services/src/main/java/org/keycloak/services/util/DefaultClientSessionContext.java:101-109`, `services/src/main/java/org/keycloak/services/util/AuthorizationContextUtil.java:83-120`, `services/src/main/java/org/keycloak/protocol/oidc/scope/UsernameScopeType.java:40-44`
- **What:** Before fix, `getMatchingClientScope` with `user==null` called `validateParameter()` which called `resolveUser()` → nonexistent users caused `Optional.empty()` → scope dropped. After fix, `validateParameter()` only checks blank, so even `nonexistent-user` is considered valid and remains in `AuthorizationRequestContext`. New `fromClientSessionAndScopeParameter` passes `user=null` when `clientSession.getUserSession()==null` (client credentials, transient). Thus tokens could include `username:nosuch` / `delegation:nosuch` with parameter values that never existed, and for delegation, without `canImpersonate` check.
- **Why matters:** Client credentials grant (or any flow where `userSession` null) could request `delegation:arbitraryUser` and get token containing that scope + `may_act` claim (mapper reads param from context) without ever checking target existence or permission. Pre-fix also missed permission check for null-user case, but at least dropped nonexistent users; now it doesn't even drop. For regular auth code flow with user, later filtering via `attachAuthenticationSession` fixes it, but for pure client-credentials it's permanent.
- **Suggested fix / validation:** Ensure `DefaultClientSessionContext` when `user==null` either drops username/delegation scopes or explicitly validates they are not allowed for that grant. Alternatively, make `validateParameter` still reject obviously invalid patterns? But per security goal, best is to keep dropping invalid when no user context? Need documented policy: what should `username` scope mean for client credentials? Probably should be rejected or dropped. Add check: if `user==null` and scope type is `username`/`delegation`, treat as invalid (return empty). Or pass through but ensure mapper handles missing target user.
- **Confirmed from code:** Yes – parser behavior change + new null handling.

**[P1] Incomplete user propagation in token-exchange and generic grant validation**

- **Files:** `services/src/main/java/org/keycloak/protocol/oidc/grants/OAuth2GrantTypeBase.java:265-275`, `services/src/main/java/org/keycloak/protocol/oidc/tokenexchange/StandardTokenExchangeProvider.java:221-231`, `services/src/main/java/org/keycloak/protocol/oidc/endpoints/AuthorizationEndpointChecker.java:234-236`
- **What:** These still call `TokenManager.isValidScope(session,scope,client)` (null user) even when `targetUser` is available (token exchange). They rely on later `attachAuthenticationSession(...,user)` to filter. If later filtering fails (e.g., transient session not filtered), scope could bypass impersonation check.
- **Why matters:** Defense-in-depth fragility. If future change removes filtering in `attachAuthenticationSession`, enumeration or impersonation bypass could reappear.
- **Suggestion:** Audit call sites: pass `targetUser` where available, but ensure pre-auth paths remain null to avoid enumeration. For token exchange, consider passing `targetUser` to `isValidScope` overload that accepts user (exists: `isValidScope(session,scopes,client,user)`). Add comment explaining null vs user distinction.
- **Confirmed:** Code inspection.

### P2 – Moderate bug, missing important test, maintainability risk

**[P2] Missing explicit coverage for disabled-user and email-as-username enumeration**

- **Files:** `tests/base/src/test/java/org/keycloak/tests/oauth/ParameterizedScopesIsolationTest.java`, issue #50468 steps.
- **What:** Tests cover `nonexistent-user` but not disabled user (`getUserByUsername` returns user with `isEnabled()==false` → distinct message pre-fix). Also no test for email lookup (`realm.isLoginWithEmailAllowed() && param contains "@"`).
- **Why matters:** Issue specifically mentions disabled account distinct message. Fix handles it (resolveUser disabled check moved to post-auth), but no test ensures disabled user no longer leaks.
- **Suggestion:** Add test creating disabled user and requesting `username:disabledUser` and `username:disabled@email` verifying 200 login form and scope dropped.

**[P2] No PAR-specific test**

- **File:** `ParameterizedScopesIsolationTest`, `AuthorizationEndpointChecker`.
- **What:** Issue notes PAR entrypoint may also be affected. Fix via `AuthorizationEndpointChecker` (used by PAR) should prevent enumeration, but no PAR test exists. Reviewer msdaly200 flagged CIBA gap (fixed), but PAR still not explicitly tested.
- **Suggestion:** Add PAR test similar to CIBA – push auth request with `username:nonexistent` and verify 200/302 success.

**[P2] Silent-drop behavior change not documented in code/migration**

- **Files:** `UsernameScopeType`, `TokenExchangeDelegationTest`.
- **What:** Changing from `invalid_scope` error to silent drop per RFC 6749 §3.3 is breaking for clients expecting hard error. PR description notes question about hard-error vs ignore. No migration guide or log at info level.
- **Why matters:** Existing deployments using delegation may see tokens missing delegation without clear error, harder to debug.
- **Suggestion:** Add release notes, maybe debug log when scope dropped post-auth, and document in `docs/documentation/server_admin/topics/clients/con-parameterized-client-scopes.adoc`.

**[P2] Logging side-channel retains detailed message**

- **File:** `services/src/main/java/org/keycloak/protocol/oidc/rar/parsers/ClientScopeAuthorizationRequestParser.java:150-154`
- **What:** On `InvalidScopeParameterException`, logs `warnf("Invalid scope parameter for '%s': %s", requestScope, e.getMessage())` where message contains `User 'x' not found` or `is disabled`. After fix, pre-auth path no longer logs (since no exception), but post-auth path with user still logs detailed message that includes existence. If logs are accessible, could still be side-channel for authenticated users probing others? Low.
- **Suggestion:** Log generic message without parameter value, or at debug level.

### P3 – Minor issue or cleanup

**[P3] Code duplication / missing Javadoc in AuthorizationContextUtil**

- **File:** `services/src/main/java/org/keycloak/services/util/AuthorizationContextUtil.java:83-120`
- **What:** New overloads duplicate logic; old methods now delegate to new with null. Could add `@Deprecated` or Javadoc explaining null means pre-auth, non-null means post-auth with validation. Also `getClientScopesStreamFromAuthorizationRequestContextWithClient` previously filtered by `SCOPE` source; new overload preserves but duplicates.
- **Suggestion:** Add Javadoc.

**[P3] DefaultClientSessionContext uses Optional for null-safe but getUserSession().getUser() chain**

- **File:** `DefaultClientSessionContext.java:101`
- **What:** `Optional.ofNullable(clientSession.getUserSession()).map(UserSessionModel::getUser).orElse(null)` is correct but inconsistent with other places using direct null checks. Minor style.
- **Suggestion:** Keep consistent, maybe add explicit null check with comment why.

**[P3] BackchannelAuthenticationEndpoint comment could mention RFC and PAR**

- **File:** `BackchannelAuthenticationEndpoint.java:185`
- **What:** Comment says user only identified, not authenticated – good. Could also reference issue #50468.
- **Suggestion:** Minor.

**[P3] Test cleanup added in TokenExchangeDelegationTest.afterEach**

- **File:** `TokenExchangeDelegationTest.java:96-102`
- **What:** New cleanup logs out and revokes consents. Good, but modifies test harness behavior compared to other tests; ensure no hidden dependency on order.
- **Confirmed from code.**

## Test Gap Analysis

**Covered:**
- Auth code flow enumeration prevention (nonexistent username dropped)
- CIBA enumeration prevention (new CIBA test)
- Consent page excludes invalid username scope
- Delegation no-impersonation now drops scope from consent and token (both OIDC and CIBA)

**Gaps:**
- Disabled user enumeration not explicitly tested.
- Email-as-username enumeration (realm with login with email) not tested.
- PAR endpoint not explicitly tested (should be covered via AuthorizationEndpointChecker but no test).
- Token exchange enumeration (client authenticated but probing target user via delegation scope) – no test verifying invalid delegation scoped token dropped vs error.
- Client credentials grant with username/delegation scopes – undefined behavior after change, no test.
- No negative test for self-targeting (`User cannot target themselves`) – ensure still enforced post-auth.

**Recommendation:** Add 2-3 more tests:
- `username:disabledUser` and `delegation:disabledUser` for auth code flow expecting 200 login and no scope.
- PAR test using `ParEndpoint` with `username:nonexistent`.
- Email test with `loginWithEmailAllowed=true`.

## Compatibility and Regression Analysis

- **Breaking change (justified):** Pre-fix returned `invalid_scope` 400 for nonexistent/disabled target; post-fix returns 200 login form and silently drops scope from consents/tokens. Clients relying on error will see success + missing scope. Per RFC 6749 §3.3 silent drop is allowed but should be documented. Impact: Clients that previously blocked login on invalid delegation will now allow login but token lacks `may_act`. That matches PR's intent and reviewer note "worse in the experience [but better for security]".
- **Consent page:** Previously showed delegation grant even when user cannot impersonate; now hides it. Slight UX change but more correct.
- **CIBA:** Previously CIBA backchannel auth request with invalid delegation scope returned 400 `invalid_scope`; now returns success + `auth_req_id` and later token without delegation. Change aligns with auth code flow but may break CIBA clients expecting error.
- **Token response scope parameter:** Clients must check `scope` in token response to detect dropped scopes (RFC requirement). Existing tests assert `assertScopeNotContains`.
- **No DB migration, no config defaults change, feature flag `PARAMETERIZED_SCOPES` still required.**
- **Backward compatibility for API:** New overloads added, old ones preserved delegating with null – no binary break.
- **Risk of regression for client credentials:** As noted P1, null-user path now more permissive (includes invalid scopes) where previously dropped. Could affect tokens issued for service accounts if they use parameterized scopes. Likelihood low because delegation scope is non-repeatable and requires user permission, but should be verified.

## Files Inspected

- `services/src/main/java/org/keycloak/protocol/oidc/scope/UsernameScopeType.java` (post-fix version from PR branch)
- `services/src/main/java/org/keycloak/protocol/oidc/scope/DelegationScopeType.java`
- `services/src/main/java/org/keycloak/services/util/AuthorizationContextUtil.java` (parent and PR)
- `services/src/main/java/org/keycloak/protocol/oidc/TokenManager.java` (diff sections)
- `services/src/main/java/org/keycloak/services/managers/AuthenticationManager.java` (diff)
- `services/src/main/java/org/keycloak/services/util/DefaultClientSessionContext.java` (diff)
- `services/src/main/java/org/keycloak/protocol/oidc/grants/ciba/endpoints/BackchannelAuthenticationEndpoint.java` (diff)
- `services/src/main/java/org/keycloak/protocol/oidc/endpoints/AuthorizationEndpointChecker.java` (via grep)
- `services/src/main/java/org/keycloak/protocol/oidc/grants/OAuth2GrantTypeBase.java`
- `services/src/main/java/org/keycloak/protocol/oidc/tokenexchange/StandardTokenExchangeProvider.java`
- `services/src/main/java/org/keycloak/protocol/oidc/rar/parsers/ClientScopeAuthorizationRequestParser.java`
- `services/src/main/java/org/keycloak/protocol/oidc/mappers/ParameterizedScopeMapper.java`
- `tests/base/src/test/java/org/keycloak/tests/oauth/ParameterizedScopesIsolationTest.java` (full diff)
- `tests/base/src/test/java/org/keycloak/tests/oauth/ParameterizedScopesOAuthGrantTest.java`
- `tests/base/src/test/java/org/keycloak/tests/oauth/TokenExchangeDelegationTest.java`
- Upstream issue/PR metadata via webfetch.

## Risk Assessment

- **Overall Risk:** Low-Medium. Fix correctly addresses pre-auth enumeration (security weakness) with minimal code change, adding user propagation where needed. CIBA fix complete. No obvious auth bypass introduced.
- **Security:** Improves security, eliminates enumeration via username/delegation scopes at `/authorize` and CIBA. Remaining logging side-channel low risk.
- **Regression Risk:** Medium-Low for auth code and CIBA flows (covered by tests). Slightly higher for edge grants (client credentials, PAR, token exchange) where null-user handling more permissive; needs additional review/testing but unlikely to cause production outage.
- **Maintainability:** Good – new overloads preserve backward compat, comments added, tests added. Minor duplication.
- **Recommendation:** Approve with minor follow-ups: add disabled/email/PAR tests, audit null-user paths for client credentials, consider logging generic message, document breaking change.

## Token Usage And Cost Inputs

- input_tokens: 201514
- output_tokens: 13961
- reasoning_tokens: 12448
- cache_write_tokens: 0
- cache_hit_tokens: 2847096
- total_tokens: 3075019
- estimated_cost_usd: 0.79119515

Pricing per `models/MEDEA/model.yaml`: input $1.25 / MTok, output $4.25 / MTok, cache n/a. The final cost uses the explicit `session.json` session cost because MEDEA's public rate card does not provide separate cache pricing.

## Notes

- Initial task worktree `workspace/keycloak` was empty (only `.gitkeep`). Created worktree from pinned base `26.6.0` using `git -C .../upstream/keycloak worktree add -b bench/MEDEA/04_pr_review/task_001 <path> 26.6.0`.
- Pinned base does not contain parameterized scopes feature, so comparison against base is vacuous; review compared PR head against its parent (`pr-50650^1`) and against current `main` (0f8222eb1c) which contains the feature.
- WebFetch of GitHub PR/issue partially blocked, but sufficient info via git diff and issue description.
- Review-only task – no code modifications applied to worktree aside from worktree creation.
- Limitations: No live Keycloak server to run integration tests; static analysis only. Concurrency/cache invalidation not observed to be affected.
