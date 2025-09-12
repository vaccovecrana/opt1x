## Features not yet implemented

1. **Ticket: Implement Versioning and Change Tracking for Configurations**
  - **Description**: Add a new OtConfigVersion table to snapshot OtNode trees on save. Extend OtConfigService to store versions. Add UI "History" tab in OtConfig route for viewing timelines and diffs. Include basic audit logging in a new OtAuditLog table with API endpoint (/api/v1/audit).
  - **Priority**: High
  - **Estimated Effort**: Medium (schema updates, service logic, UI component).
  - **Dependencies**: None.

3. **Ticket: Introduce Global Search Across Entities**
  - **Description**: Add /api/v1/search endpoint querying names/paths/values. Implement global search bar in UiMenu, rendering results in new OtSearch route.
  - **Priority**: High
  - **Estimated Effort**: Medium (SQL queries, UI search component).
  - **Dependencies**: None.

4. **Ticket: Build Basic Dashboard for Overview Metrics**
  - **Description**: Flesh out default UI route with recent changes, totals (configs/namespaces), and active users. Add /api/v1/stats endpoint.
  - **Priority**: Medium-High
  - **Estimated Effort**: Low (UI charts with Chart.js, simple backend aggregates).
  - **Dependencies**: None.

5. **Ticket: Add Notifications and Webhooks for Changes**
  - **Description**: Create OtWebhook table. Trigger async notifications in services (e.g., post-save). Add UI section in OtConfig for managing webhooks.
  - **Priority**: Medium
  - **Estimated Effort**: Medium (table, async logic, UI form).
  - **Dependencies**: None.

6. **Ticket: Support Environment Profiles for Configs**
  - **Description**: Add "env" field to OtNamespace/OtConfig. Extend rendering to merge/override by env (?env=prod). Add env dropdowns in UI editors.
  - **Priority**: Medium
  - **Estimated Effort**: Medium (schema, render logic, UI updates).
  - **Dependencies**: None.

7. **Ticket: Implement Backup/Export and Restore Functionality**
  - **Description**: Add /api/v1/backup for JSON dump and POST for import/validation. UI buttons in new "Admin" menu.
  - **Priority**: Medium
  - **Estimated Effort**: Medium (JSON serialization, validation).
  - **Dependencies**: None.

9. **Ticket: Enhance Secrets Management with Rotation and TTL**
  - **Description**: Add cron for key rotation in OtSealService. UI alerts for expiring secrets.
  - **Priority**: Low-Medium
  - **Estimated Effort**: Low (cron job, UI notifications).
  - **Dependencies**: None.

10. **Ticket: Associate Identity Information with API Keys**
  - **Description**: Add fields to OtApiKey (externalId, userEmail, expirationUtcMs). Update createApiKey to accept identity params. Add revocation method that cascades to children.
  - **Priority**: High (builds on discussion)
  - **Estimated Effort**: Low (schema updates, service methods).
  - **Dependencies**: None.

11. **Ticket: Implement Generic OIDC Interface (Opt-In)**
  - **Description**: Add OtOidcProvider table. Implement Authorization Code Flow with Nimbus lib (or JDK). Add /oidc/login and /oidc/callback routes. Make opt-in via config toggle. Map IDP claims to OtApiKey on callback.
  - **Priority**: Medium-High
  - **Estimated Effort**: Medium (OIDC logic, UI button in OtLogin).
  - **Dependencies**: Ticket 10 (identity association).

12. **Ticket: Add Automated Termination Handling via Webhooks/SCIM**
  - **Description**: Implement /api/v1/webhook/idp for IDP events (e.g., user.deactivated → revoke linked keys). Optional SCIM endpoints for full sync.
  - **Priority**: Medium
  - **Estimated Effort**: Medium (endpoint, parsing, revocation trigger).
  - **Dependencies**: Tickets 10 and 11 (identity and OIDC setup).
