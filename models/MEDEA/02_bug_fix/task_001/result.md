# Result

## Status

completed

## Summary

Fixed SAML client creation failure when Client ID contains URL query-string or X.500 DN-special characters such as `=`, `?`, `/`, `:`, `&`.

**Root Cause:**
In `BCCertificateUtilsProvider` and `BCFIPSCertificateUtilsProvider`, the certificate subject DN was constructed via unsafe string concatenation:

```java
X500Name subjectDN = new X500Name("CN=" + subject);
```

BouncyCastle's `X500Name` parser interprets the string as an RFC 2253 DN. If the subject (SAML Client ID) contains characters like `=` (from query strings `?action=sso_id`) or `,`, `+`, `;`, the parser treats them as DN syntax and throws `IllegalArgumentException: badly formatted directory string`. This bubbles up from `CertificateUtils.generateV1SelfSignedCertificate` through `KeycloakModelUtils.generateKeyPairCertificate` called in `SamlProtocolFactory.setupClientDefaults`, causing the entire SAML client creation to fail.

The same bug existed in `ElytronCertificateUtilsProvider.subjectToX500Principle` which used `new X500Principal("CN=" + subject)` without escaping, and would fail for `=` in the CN value.

## Files Changed

- `crypto/default/src/main/java/org/keycloak/crypto/def/BCCertificateUtilsProvider.java`
  - Added imports for `DERUTF8String`, `X500NameBuilder`, `BCStyle`
  - Replaced `new X500Name("CN=" + subject)` with safe builder:
    ```java
    X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
    builder.addRDN(BCStyle.CN, new DERUTF8String(subject));
    return builder.build();
    ```
  - Introduced private helper `createSubjectDN(String)` used in both `generateV3Certificate` and `generateV1SelfSignedCertificate`

- `crypto/fips1402/src/main/java/org/keycloak/crypto/fips/BCFIPSCertificateUtilsProvider.java`
  - Same fix as above: safe `X500NameBuilder` with `DERUTF8String` to handle special chars including leading `#`

- `crypto/elytron/src/main/java/org/keycloak/crypto/elytron/ElytronCertificateUtilsProvider.java`
  - Added import `javax.naming.ldap.Rdn`
  - Rewrote `subjectToX500Principle` to use `Rdn.escapeValue(subject)` for proper RFC 2253 escaping:
    ```java
    String escaped = Rdn.escapeValue(subject).toString();
    return new X500Principal("CN=" + escaped);
    ```
  - Added fallback handling when subject already starts with `CN=` but fails to parse (escapes the value part)

- `core/src/test/java/org/keycloak/util/CertificateUtilsTest.java` (NEW)
  - Abstract test covering SAML-like Client IDs with `=`, `?`, `/`, `:`, `&`, `,`, `+`, `#`, leading/trailing spaces

- `crypto/default/src/test/java/org/keycloak/crypto/def/test/CertificateUtilsTest.java` (NEW)
  - Wrapper for default provider

- `crypto/elytron/src/test/java/org/keycloak/crypto/elytron/test/CertificateUtilsTest.java` (NEW)
  - Wrapper for Elytron provider

- `crypto/fips1402/src/test/java/org/keycloak/crypto/fips/test/CertificateUtilsTest.java` (NEW)
  - Wrapper for FIPS provider

## Approach and Reasoning

1. **Investigation:** Traced call chain `SamlProtocolFactory.setupClientDefaults` → `KeycloakModelUtils.generateKeyPairCertificate` → `CertificateUtils` → provider implementations. Verified via standalone reproducer using `bcprov-jdk18on-1.78.1` that old approach `new X500Name("CN=" + "https://example.com/?action=sso_id")` fails with "badly formatted directory string", while builder succeeds.

2. **Fix Selection:**
   - Preferred safe construction over ad-hoc string replacement per task hints.
   - For BouncyCastle providers: `X500NameBuilder` with `BCStyle.INSTANCE` and `DERUTF8String` ensures proper escaping of `=`, `,`, `+`, `;`, `<`, `>`, `"`, `\`, `#`, leading/trailing spaces. Using `DERUTF8String` bypasses BC's `stringToValue` parsing that misinterprets leading `#` as hex BER.
   - For Elytron: `javax.naming.ldap.Rdn.escapeValue` is JDK standard for RFC 2253 escaping, correctly handles `=` → `\=`, `,` → `\,`, leading `#` → `\#`, leading space → `\ `, etc. Verified via small `TestRdn.java`.

3. **Scope:** Fixed at lowest level (`CertificateUtilsProvider` implementations) rather than only SAML factory, because same bug affects other callers (e.g., `ClientAttributeCertificateResource`, realm name as cert subject). This is broader but justified as correct fix for all usages.

4. **Preservation of Semantics:** Client ID stored in Keycloak remains unchanged; only the X.509 certificate's CN is safely encoded. The certificate still represents the client, but with escaped DN.

## Tests

### New Tests
- `core/src/test/java/org/keycloak/util/CertificateUtilsTest.java`:
  - `testCertificateWithUrlQueryString`: single case `https://example.com/?action=sso_id`
  - `testCertificateWithSpecialChars`: array covering `=`, `?`, `/`, `:`, `&`, `,`, `+`, leading/trailing spaces, `#hashStart`, and complex `CN=already, but tricky...`
  - `testCertificateWithAllRequiredChars`: `https://example.com/test?param1=value1&param2=value2:8080/path`

- Wrappers in three crypto modules ensure fix works across all providers.

### Commands Run

- Verified old vs new behavior with standalone Java:
  ```bash
  curl -L -o bcprov.jar https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.78.1/bcprov-jdk18on-1.78.1.jar
  javac -cp bcprov.jar TestBC.java && java -cp .:bcprov.jar TestBC
  # OLD FAIL for https://example.com/?action=sso_id -> badly formatted directory string
  # NEW OK for https://example.com/?action=sso_id -> CN=https://example.com/?action\=sso_id
  ```

- Verified RDN escaping for Elytron:
  ```bash
  javac TestRdn.java && java TestRdn
  # escaped: https://example.com/?action\=sso_id, parsed OK
  ```

- Ran Maven tests:
  ```bash
  mvn test -pl crypto/default -am -Dtest=org.keycloak.crypto.def.test.CertificateUtilsTest -Denforcer.skip=true
  # Tests run: 3, Failures: 0

  mvn test -pl crypto/elytron -am -Dtest=org.keycloak.crypto.elytron.test.CertificateUtilsTest -Denforcer.skip=true
  # Tests run: 3, Failures: 0

  mvn test -pl crypto/fips1402 -am -Dtest=org.keycloak.crypto.fips.test.CertificateUtilsTest -Denforcer.skip=true
  # Tests run: 3, Failures: 0

  mvn test -pl crypto/default -am -Dtest=org.keycloak.crypto.def.test.PemUtilsBCTest -Denforcer.skip=true -o
  # Tests run: 6, Failures: 0
  ```

## Rating

Overall Score: 4.5 / 5

### Score Breakdown

- **Root Cause Analysis:** 5 / 5 – Correctly identified unsafe `X500Name("CN="+subject)` concatenation across 3 providers.
- **Correctness:** 5 / 5 – Uses `X500NameBuilder` + `DERUTF8String` and `Rdn.escapeValue`, which properly escape all DN-special characters, verified via unit tests.
- **Regression Safety:** 4 / 5 – Fix is minimal and focused on certificate creation; no SAML semantics changed. Existing `PemUtilsBCTest` still passes. Potential edge: old realm names with `#` now handled via DERUTF8String (improvement, not regression).
- **Test Quality:** 4 / 5 – Added covering tests for all required characters across all three crypto providers; integration SAML test not added due to Arquillian heaviness, but unit coverage is solid.
- **Report Quality:** 4.5 / 5 – Detailed root cause, fix reasoning, commands, and validation.

## Token Usage And Cost Inputs

- input_tokens: 369899
- output_tokens: 18835
- cache_write_tokens: 0
- cache_hit_tokens: 6577213
- total_tokens: 6975556
- estimated_cost_usd: 1.5698426999999995

Token and cost values come from `session.json`. Total tokens include 9,609 reasoning tokens in
addition to input, output, cache-write, and cache-hit tokens.

## Notes

- The fix also handles additional DN-special characters beyond the issue's examples: `,`, `+`, `;`, `"`, `\`, `<`, `>`, leading `#` and spaces, which could also appear in SAML entity IDs.
- For Elytron, the fix retains backward compatibility for full DN strings starting with `CN=` by attempting direct parse first, falling back to escaped value if parsing fails.
- Remaining limitation: If a realm name or client ID is extremely long (>64 bytes for CN) or contains non-UTF8, certificate generation may still be limited by X.509 constraints, but that's out of scope.
- No changes outside task worktree; worktree branch `bench/MEDEA/02_bug_fix/task_001` contains all edits.
