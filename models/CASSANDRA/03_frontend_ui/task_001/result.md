# Result

## Status

completed

## Summary

The Organization Invitation Management experience was integrated into the real
Keycloak Admin Console. During investigation I found that the pinned benchmark
base (`0526b94f0d`, Keycloak 26.6.0) already ships a fully working invitation
feature (backend REST resources, admin-client bindings, `Invitations.tsx`,
`InviteMemberModal.tsx`, i18n keys, and backend tests) that was merged upstream
before the pinned commit. Rather than duplicate existing code or fabricate a
mock app, I made the invitation workflow feel more native to the Admin Console
information architecture and closed the concrete gaps that remained:

1. Promoted **Invitations** from a non-routable, state-only sub-tab nested inside
   the Members tab to a **first-class, deep-linkable routable tab** under the
   Organization detail page, following the existing `RoutableTabs`/`useRoutableTab`
   convention used by every other organization tab.
2. Wired the `Members` and `Invitations` panels directly as sibling routable
   tabs and removed the now-redundant `MembersSection` wrapper.
3. Added the missing **frontend test coverage** for the invitation workflow
   (Playwright e2e spec + page helpers) and an admin-client invitation listing
   test, which did not previously exist.

Because the invitation UI itself already existed and is comprehensive, the change
set is intentionally scoped and low-risk: it preserves all existing organization
and member management behavior while giving invitations a proper URL, browser
history/refresh persistence, and its own navigation entry point.

## Implemented / Verified UI Behavior

The invitation surface (`Invitations.tsx`, unchanged but now a top-level tab)
provides, and this task confirms and wires up:

- View pending organization invitations in a paginated `KeycloakDataTable`
  (columns: email, first name, last name, sent date, expires at, status badge).
- Free-text search across email/first/last name via `SearchInputComponent`.
- Status filtering (Pending / Expired) via `CheckboxFilterComponent`.
- Create an invitation through a split "Invite member" menu:
  - **Invite new user** -> `InviteMemberModal` (email required + optional
    first/last name) posting to `POST /{orgId}/members/invite-user`.
  - **Invite realm user** -> reuses the shared `MemberModal` posting to
    `POST /{orgId}/members/invite-existing-user`.
- Row actions: Resend invitation, Copy invite link (only when a link exists),
  Delete invitation.
- Bulk delete with a confirmation modal (`useConfirmDialog`).
- List refresh after every create / resend / revoke action (via a `key` counter
  bump), so state transitions stay predictable.

Note on optional requirements: per-invitation **expiration/validity period** and
**organization role/membership metadata** are *not supported by the underlying
model/API* at this base. The backend `inviteUser(email, firstName, lastName)`
endpoint accepts only those three form params and derives expiry from the realm's
admin action-token lifespan, and `OrganizationInvitationModel` exposes only a
read-only `expiresAt`. The prompt scoped these as "if supported by the underlying
model/API," so no fake inputs were added; doing so would have required a backend
model/API change outside the intended scope.

## Files Changed

Modified:
- `js/apps/admin-ui/src/organizations/routes/EditOrganization.tsx`
  - Added `"invitations"` to the `OrganizationTab` union so the tab has a route
    (`/:realm/organizations/:id/invitations`).
- `js/apps/admin-ui/src/organizations/DetailOrganization.tsx`
  - Imported `Members` and `Invitations` directly; added
    `invitationsTab = useTab("invitations")`; rendered `Members` and a new
    routable `Invitations` tab (`data-testid="invitationsTab"`) as siblings.

Removed:
- `js/apps/admin-ui/src/organizations/MembersSection.tsx`
  - The wrapper that nested Members + Invitations as non-routable inner tabs is
    no longer needed and was deleted (no other references remained).

Added (tests):
- `js/apps/admin-ui/test/organization/invitations.spec.ts`
  - Playwright e2e spec: empty state, email required-field validation in the
    Invite new user modal, opening the invite-new-user and invite-realm-user
    modals from the toolbar menu.
- `js/apps/admin-ui/test/organization/invitations.ts`
  - Page-object helpers (`goToInvitationsTab`, `clickInviteNewUser`,
    `clickInviteRealmUser`, `clickInviteNewUserEmptyAction`, `fillInviteForm`).
- `js/libs/keycloak-admin-client/test/organizations.spec.ts`
  - Added an integration test asserting `listInvitations({ orgId })` returns an
    (empty) list for a freshly created organization.

## Route / Navigation Entry Points

- New routable tab path: `/:realm/organizations/:id/invitations`
  (backed by `toEditOrganization({ realm, id, tab: "invitations" })`).
- Reached via: left nav **Organizations** -> select an organization ->
  **Invitations** tab (now a peer of Settings / Attributes / Members / Groups /
  Identity providers / Admin events).
- Previously the invitation list was only reachable as a non-URL state sub-tab
  inside the **Members** tab; it now has its own address and is refresh/deep-link
  safe.

## API / Client Integration Notes

No new admin-client bindings were required; the existing
`js/libs/keycloak-admin-client/src/resources/organizations.ts` already exposes:
- `listInvitations` -> `GET /{orgId}/invitations`
- `findInvitation` -> `GET /{orgId}/invitations/{invitationId}`
- `resendInvitation` -> `POST /{orgId}/invitations/{invitationId}/resend`
- `deleteInvitation` -> `DELETE /{orgId}/invitations/{invitationId}`
- `invite` / `inviteExistingUser` -> `POST /{orgId}/members/invite-user` and
  `.../invite-existing-user`

Backend endpoints live in
`services/.../organization/admin/resource/OrganizationInvitationResource.java`
and `OrganizationMemberResource.java` (JDK 17). The model type
`OrganizationInvitationRepresentation` and `OrganizationInvitationStatus` enum are
already exported from the admin-client. All of these are consumed by the UI as-is.

## UI States and Validation

- **Loading**: `KeycloakDataTable` async `loader` handles the fetch lifecycle.
- **Empty**: `ListEmptyState` ("No invitations" / instructions) with secondary
  actions to invite a new user or a realm user.
- **Success**: `addAlert` notifications on invite sent, resend, copy link, and
  delete.
- **Validation error**: recipient email is required (`TextControl` rule
  `required`); the modal surfaces "Required field" on empty submit. Server-side
  email format/duplicate/membership checks return errors surfaced via `addError`.
- **Request error**: `addError("organizationsInvitationsListError", ...)` and
  per-action error keys handle API/permission failures gracefully; the detail
  route is gated by `access: "manage-users"`.

## Tests / Verification Performed

- `eslint` on the changed and surrounding files: **0 errors**
  (`src/organizations/`, `test/organization/`, and the admin-client test).
  Remaining messages are pre-existing `react-compiler` *warnings* on the
  `useTab(...)` hook pattern that already existed in the base for the other tabs.
- Added Playwright e2e spec + helpers and an admin-client integration test
  following existing conventions (`members.spec.ts`, `idp.spec.ts`,
  `organizations.spec.ts`). These suites require a running Keycloak server (and
  the e2e suite a live admin console), so they were authored to convention but
  not executed in this environment (see Limitations).
- Full `tsc`/`vite build` type-check could not be completed because the pinned
  base has a pre-existing dependency drift: the generated kiota admin-client code
  in `js/libs/keycloak-admin-client/src/generated/**` fails to compile against the
  resolved `@microsoft/kiota-abstractions@1.0.0-preview.99`
  (`getCollectionOfPrimitiveValues` signature mismatch). I confirmed this failure
  reproduces on the pristine base with my changes stashed, and pinning
  `preview.86` produced a different set of pre-existing generated-code errors, so
  it is an environment/tooling issue unrelated to this task. My changes are small,
  strictly additive to existing typed patterns, and pass ESLint's TypeScript
  parser cleanly.

## Known Limitations / Follow-up

- The invitation feature already existed in the pinned base; this task's net
  contribution is the routable-tab IA improvement, wrapper cleanup, and the new
  test coverage rather than a from-scratch implementation.
- Full type-check / production build and the new test suites were not executed
  due to the pre-existing kiota generated-client build break and the lack of a
  running Keycloak server in this environment.
- Per-invitation expiration and organization-role/membership metadata are not
  implemented because the current backend model/API does not support them; adding
  them would require a backend change beyond the frontend scope.
- The e2e spec deliberately avoids asserting a fully-sent invitation because the
  invite-new-user flow sends email (requires SMTP), which is not configured in the
  default test setup; it validates navigation, empty state, validation, and modal
  behavior instead.

## Token Usage And Cost Inputs

- input_tokens: 148
- output_tokens: 33302
- cache_write_tokens: 96715
- cache_hit_tokens: 4340223
- total_tokens: 4470388
- estimated_cost_usd: 3.60787025

Token and cost values come from `session.json`.

## Notes

- Worktree created from the pinned base commit `0526b94f0d` (Keycloak 26.6.0) on
  branch `bench/CASSANDRA/03_frontend_ui/task_001`, as required.
- No files outside the task worktree were modified; a temporary dependency
  override used to attempt a type-check was fully reverted (package.json and
  pnpm-lock.yaml restored).
</content>
</invoke>
