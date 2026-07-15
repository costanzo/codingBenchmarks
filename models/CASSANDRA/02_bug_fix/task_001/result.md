# Bug Fix Task 001 — SAML Client Creation Fails For Client IDs With Query Characters

## Status

Completed. Root cause identified and fixed in all three crypto providers, with regression tests added and passing. Targeted Maven test runs verify both the fix and that the tests fail against the original (buggy) code.

## Summary

Keycloak issue #50177: creating a SAML client whose Client ID (also used as the SAML entity ID)
contains URL query-string or X.500 DN-special characters — such as `=`, `?`, `/`, `:`, `&`, `,`,
`+` — fails while Keycloak generates the client's default self-signed SAML signing certificate.

Call path:

```
SamlProtocolFactory.setupClientDefaults
  -> KeycloakModelUtils.generateKeyPairCertificate(clientId)
     -> CertificateUtils.generateV1SelfSignedCertificate(keyPair, subject=clientId)
        -> <crypto provider>.generateV1SelfSignedCertificate(...)
```

### Root cause

Each crypto provider built the certificate subject Distinguished Name by concatenating the raw
Client ID directly into a DN string and then re-parsing that string:

- `crypto/default` (BouncyCastle):
  `new X500Name("CN=" + subject)`
- `crypto/fips1402` (BouncyCastle FIPS):
  `new X500Name("CN=" + subject)`
- `crypto/elytron` (WildFly Elytron):
  `new X500Principal("CN=" + subject)` (via `subjectToX500Principle`)

The X.500 / RFC 2253 string parser then interprets DN-special characters in the Client ID as DN
syntax. For a Client ID like `https://example.com/?action=sso_id`, the embedded `=` (and `,`, `+`,
etc.) makes the parser see extra/invalid attribute-value assignments, so parsing throws and the
whole `generateV1SelfSignedCertificate` call fails with `RuntimeException: Error creating
X509v1Certificate`. Because `SamlProtocolFactory.setupClientDefaults` calls this during client
creation, the SAML client is never created and the server returns an uncaught error.

Confirmed reproduction (default provider) — running the new test against the original code produced:

```
java.lang.RuntimeException: Error creating X509v1Certificate.
  at ...BCCertificateUtilsProvider.generateV1SelfSignedCertificate(BCCertificateUtilsProvider.java:183)
Caused by: ... at org.bouncycastle.asn1.x500.style.IETFUtils.rDNsFromString(...)
  at org.bouncycastle.asn1.x500.X500Name.<init>(...)
  at ...BCCertificateUtilsProvider...:173
```

## Approach and Reasoning

Instead of building a DN *string* and parsing it, build the DN *programmatically* so the Client ID
is treated as an opaque attribute value and gets encoded/escaped correctly. This preserves support
for Client IDs that are URLs or entity IDs, does not mutate or corrupt the stored Client ID, and
does not change SAML entity ID semantics. The change is confined to the default-certificate subject
construction path.

- BouncyCastle providers (`default`, `fips1402`): use
  `new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, subject).build()`. The value is added as
  a typed RDN value rather than parsed from a string, so special characters are encoded safely.
- Elytron provider: use
  `new X500PrincipalBuilder().addItem(X500AttributeTypeAndValue.createUtf8(X500.OID_AT_COMMON_NAME, subject)).build()`,
  replacing the string-parsing `X500Principal(String)` constructor.

This satisfies the requirement to "prefer escaping or constructing X.500 names safely over ad hoc
string replacement" — no character stripping or manual escaping is done; the library builders own
the encoding. All builder classes exist in the pinned BouncyCastle / BC-FIPS / wildfly-elytron
dependencies and are JDK 17 compatible.

Note on prior Elytron behavior: the old `subjectToX500Principle` prepended `CN=` only when the
subject did not already start with `CN=`. In practice every caller
(`KeycloakModelUtils.generateKeyPairCertificate`, key providers, docker, JGroups, etc.) passes a
plain value (client ID, realm name, literal like `"test"`) — never a full DN — so treating the
whole subject as the CN attribute value is consistent with the BouncyCastle providers, which always
treated the entire subject as the CN value.

## Files Changed

Production code:

- `crypto/default/src/main/java/org/keycloak/crypto/def/BCCertificateUtilsProvider.java`
  - Added imports `X500NameBuilder`, `BCStyle`; build subject DN via builder.
- `crypto/fips1402/src/main/java/org/keycloak/crypto/fips/BCFIPSCertificateUtilsProvider.java`
  - Added imports `X500NameBuilder`, `BCStyle`; build subject DN via builder.
- `crypto/elytron/src/main/java/org/keycloak/crypto/elytron/ElytronCertificateUtilsProvider.java`
  - Added imports `X500AttributeTypeAndValue`, `X500PrincipalBuilder`; rewrote
    `subjectToX500Principle` to build the principal via the builder.

Tests (new):

- `crypto/default/src/test/java/org/keycloak/crypto/def/test/DefaultCryptoCertificateSubjectTest.java`
- `crypto/fips1402/src/test/java/org/keycloak/crypto/fips/test/FIPS1402CertificateSubjectTest.java`
- `crypto/elytron/src/test/java/org/keycloak/crypto/elytron/test/ElytronCertificateSubjectTest.java`

## Tests

Each new test generates a self-signed certificate for a set of subjects containing special
characters and asserts that (a) generation succeeds and (b) the certificate's subject CN
round-trips the exact original value (parsed back with `javax.naming.ldap.LdapName`). Subjects
covered:

- `https://example.com/?action=sso_id` (the exact issue scenario)
- `https://example.com/app?a=1&b=2` (`?`, `=`, `&`)
- `client=with=equals` (`=`)
- `path/with/slashes` (`/`)
- `scheme:with:colons` (`:`)
- `a,b+c` (`,`, `+` — the classic DN separators)
- `plain-client-id` (control / no regression)

Commands run (JDK 17, Maven 3.6.3, offline):

- `mvn -o -pl crypto/default  -am test -Dtest=DefaultCryptoCertificateSubjectTest`
  → Tests run: 1, Failures: 0, Errors: 0
- `mvn -o -pl crypto/fips1402 -am test -Dtest=FIPS1402CertificateSubjectTest`
  → Tests run: 1, Failures: 0, Errors: 0
- `mvn -o -pl crypto/elytron  -am test -Dtest=ElytronCertificateSubjectTest`
  → Tests run: 1, Failures: 0, Errors: 0
- Regression check (existing DN-consuming tests still green):
  - `DefaultCertificateIdentityExtractorTest` → 5 pass; `DefaultCryptoRSAVerifierTest` → 9 pass
  - `FIPS1402CertificateIdentityExtractorTest` → 5 (skipped in this env, unchanged behavior)
  - `ElytronCertificateIdentityExtractorTest` → 5 pass
- Negative verification: temporarily stashing the `BCCertificateUtilsProvider.java` fix and running
  `DefaultCryptoCertificateSubjectTest` reproduced the original failure
  (`RuntimeException: Error creating X509v1Certificate` from BouncyCastle DN string parsing),
  confirming the test genuinely guards the bug. Fix was restored afterward.

## Remaining Risks or Limitations

- Did not build/run the full server or Admin Console end-to-end flow; verification is at the crypto
  provider unit level, which is exactly where the failure originated. The `SamlProtocolFactory` and
  `KeycloakModelUtils` layers are unchanged and simply pass the Client ID through.
- The FIPS identity-extractor regression test is skipped in this environment (BC-FIPS approved-mode
  gating), not a result of this change; the FIPS subject test itself runs and passes.
- Behavior is now uniform across providers: the entire subject is treated as the CN value. This is
  consistent with the previous BouncyCastle behavior; only a theoretical caller passing a fully
  pre-formatted DN string would see a difference, and no such caller exists in the codebase.

## Rating

Self-assessed: 4.5 / 5.0. Correct, minimal, library-safe fix applied consistently across all three
crypto providers; tests reproduce the exact issue scenario, verify round-trip correctness, and were
proven to fail on the old code. Not a full 5.0 because end-to-end Admin Console verification and the
full crypto test suites were not exhaustively run in this environment.

## Token Usage And Cost Inputs

- input_tokens: 112
- output_tokens: 24813
- cache_write_tokens: 109445
- cache_hit_tokens: 2472112
- total_tokens: 2606482
- estimated_cost_usd: 2.5409722500000007

Token and cost values come from `session.json`.

## Notes

- Branch: `bench/CASSANDRA/02_bug_fix/task_001`, created as a git worktree from base commit
  `0526b94f0d` ("Set version to 26.6.0").
- The worktree started empty (`.gitkeep` only); it was populated by adding the worktree from the
  shared `upstream/keycloak` repository at the pinned base commit.
- No files outside the task worktree were modified.
