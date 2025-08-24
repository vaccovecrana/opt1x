Test Case Specification: Steve’s Team A and DevOps Collaboration in Opt1x
Test Case ID: TC-OPT1X-001
Title: Validate Hierarchical Namespace and API Key Management with Secret Rotation and Webhook Notifications
Version: 1.0
Date: April 23, 2025
Author: Grok 3 (xAI), in collaboration with the user
Status: Draft

1. Test Objective
Verify that the Opt1x secrets management system correctly handles:

    Hierarchical namespace creation and management by Steve (Team A lead).
    Hierarchical API key management, including permission delegation.
    Secret storage and retrieval with inheritance and overrides.
    Automatic secret rotation with webhook notifications.
    Access control and isolation between team and shared namespaces.
    Logging of access and rotation events.

The test case ensures Steve can manage Team A’s resources autonomously while DevOps maintains control over shared namespaces, aligning with Opt1x’s goal of team autonomy and minimal DevOps involvement.

2. Test Scope
In Scope

    Namespace management (team-a, team-a/dev, team-a/dev/team-a-nats-local-dev, shared/dev).
    API key management (team-a-root-key, team-a-steve-key, team-a-linda-key, team-a-key-3).
    Secret storage and retrieval (nats_url in shared/dev and team-a/dev/team-a-nats-local-dev).
    Secret rotation with webhook notifications for team-a/dev/rds_password.
    Access logging and rotation logging.
    Isolation of production namespaces (team-a/prod, shared/prod).

Out of Scope

    Dynamic secrets or direct integration with external systems (e.g., AWS RDS).
    CLI or REST API exposure (assumes internal SecretService method calls).
    Performance testing or scalability validation.

3. Test Environment

    Database: HSQLDB (in-memory for testing).
        JDBC URI: jdbc:hsqldb:mem:opt1x;default_autocommit=true
        Schema: PUBLIC (default HSQLDB schema).
        Autocommit: Enabled on every query.
    Dependencies:
        HSQLDB (org.hsqldb:hsqldb:2.7.3).
        Java standard library for HTTP requests (webhooks).
    Mock Webhook Server: A mock HTTP server (e.g., using com.sun.net.httpserver.HttpServer) to receive webhook notifications.
        URL: http://localhost:8080/update-credentials
        Expected to verify Authorization header and payload.

4. Prerequisites

    HSQLDB is initialized with the Opt1x schema (as defined in the design document).
        Tables: api_keys, namespaces, api_key_namespaces, secrets, webhooks, access_logs, rotation_logs.
    SecretService class is implemented with all methods (createChildNamespace, deleteNamespace, assignPermissions, getSecret, enableRotation, registerWebhook, etc.).
    A mock webhook server is running on http://localhost:8080/update-credentials to capture notifications.
    Test data is pre-populated (see Setup section).

5. Test Data Setup
Initial State

    Namespaces:
        shared/dev (id: 2), shared/test (id: 3), shared/prod (id: 4) – Created by DevOps.
        team-a (id: 5), team-a/dev (id: 6), team-a/test (id: 7), team-a/prod (id: 8) – Created by DevOps for Team A.
        team-a/dev/team-a-nats-local-dev (id: 10) – To be created by Steve during the test.
    API Keys:
        devops-key (role: admin, parent: NULL) – DevOps root key with full access.
        team-a-root-key (role: application, parent: NULL) – Team A root key, created by DevOps.
        team-a-steve-key (role: application, parent: team-a-root-key) – Steve’s key, to be created.
        team-a-linda-key (role: application, parent: team-a-root-key) – Linda’s key, to be created.
        team-a-key-3 (role: application, parent: team-a-root-key) – Jerry’s key, to be created.
    API Key Permissions (via api_key_namespaces):
        team-a-root-key:
            read/write on team-a and all its sub-namespaces (id: 5, 6, 7, 8).
            read on shared/dev, shared/test, shared/prod (id: 2, 3, 4).
        team-a-steve-key: To inherit read/write on team-a (to be assigned).
        team-a-linda-key, team-a-key-3: To be assigned read on team-a/dev, read/write on team-a/dev/team-a-nats-local-dev.
    Secrets:
        shared/dev/nats_url: "nats://shared.example.com:4222" – Set by DevOps.
        team-a/dev/rds_password: "initial-password" – To be created, with rotation enabled.
    Webhooks:
        To be registered for team-a/dev/rds_password pointing to http://localhost:8080/update-credentials.

6. Test Steps
Step 1: DevOps Sets Up Shared Resources

    Action: Verify DevOps has created shared namespaces and secrets.
        Query: SELECT * FROM namespaces WHERE path LIKE 'shared/%';
        Expected: shared/dev (id: 2), shared/test (id: 3), shared/prod (id: 4).
        Query: SELECT * FROM secrets WHERE namespace_id = 2 AND name = 'nats_url';
        Expected: nats_url with value "nats://shared.example.com:4222".
    Action: Verify DevOps has created Team A’s root namespace and key.
        Query: SELECT * FROM namespaces WHERE path LIKE 'team-a%';
        Expected: team-a (id: 5), team-a/dev (id: 6), team-a/test (id: 7), team-a/prod (id: 8).
        Query: SELECT * FROM api_keys WHERE key = 'team-a-root-key';
        Expected: Exists with role = 'application', parent_key = NULL.

Step 2: Steve Creates Child API Keys

    Action: Steve uses team-a-root-key to create child keys for himself, Linda, and Jerry.
        Call: createChildApiKey("team-a-root-key", "team-a-steve-key").
        Call: createChildApiKey("team-a-root-key", "team-a-linda-key").
        Call: createChildApiKey("team-a-root-key", "team-a-key-3").
        Query: SELECT * FROM api_keys WHERE parent_key = 'team-a-root-key';
        Expected: team-a-steve-key, team-a-linda-key, team-a-key-3 with role = 'application'.
    Action: Verify permissions for child keys.
        Query: SELECT * FROM api_key_namespaces WHERE api_key = 'team-a-steve-key';
        Expected: Inherits read/write on team-a and sub-namespaces (id: 5, 6, 7, 8).
        Call: assignPermissions("team-a-root-key", "team-a-linda-key", "team-a/dev", false) (read-only).
        Call: assignPermissions("team-a-root-key", "team-a-key-3", "team-a/dev", false) (read-only).
        Query: SELECT * FROM api_key_namespaces WHERE api_key IN ('team-a-linda-key', 'team-a-key-3');
        Expected: read on team-a/dev (id: 6), no access to team-a/prod (id: 8).

Step 3: Steve Creates a Child Namespace

    Action: Steve creates team-a/dev/team-a-nats-local-dev for local NATS testing.
        Call: createChildNamespace("team-a-root-key", "team-a/dev", "team-a-nats-local-dev").
        Query: SELECT * FROM namespaces WHERE path = 'team-a/dev/team-a-nats-local-dev';
        Expected: Exists with id: 10, parent_id = 6.
    Action: Assign permissions to Linda and Jerry for the new namespace.
        Call: assignPermissions("team-a-root-key", "team-a-linda-key", "team-a/dev/team-a-nats-local-dev", true) (read/write).
        Call: assignPermissions("team-a-root-key", "team-a-key-3", "team-a/dev/team-a-nats-local-dev", true) (read/write).
        Query: SELECT * FROM api_key_namespaces WHERE namespace_id = 10;
        Expected: team-a-linda-key and team-a-key-3 have write_access = true.

Step 4: Linda Adds an Override Secret

    Action: Linda adds a local override for nats_url in team-a/dev/team-a-nats-local-dev.
        Call: setSecret("team-a-linda-key", "team-a/dev/team-a-nats-local-dev", "nats_url", "nats://localhost:4222").
        Query: SELECT * FROM secrets WHERE namespace_id = 10 AND name = 'nats_url';
        Expected: nats_url with value "nats://localhost:4222".
    Action: Verify Linda can’t write to team-a/dev (read-only).
        Call: setSecret("team-a-linda-key", "team-a/dev", "nats_url", "invalid").
        Expected: Throws SecurityException (“API key does not have write access”).
    Action: Verify access logging.
        Query: SELECT * FROM access_logs WHERE api_key = 'team-a-linda-key';
        Expected: Logs successful write to team-a/dev/team-a-nats-local-dev/nats_url and failed write to team-a/dev/nats_url.

Step 5: Jerry Retrieves the Secret

    Action: Jerry fetches nats_url from team-a/dev/team-a-nats-local-dev.
        Call: getSecret("team-a-key-3", "team-a/dev/team-a-nats-local-dev", "nats_url").
        Expected: Returns "nats://localhost:4222".
    Action: Jerry fetches nats_url from team-a/dev (no override, falls back to shared/dev).
        Call: getSecret("team-a-key-3", "team-a/dev", "nats_url").
        Expected: Returns "nats://shared.example.com:4222" (from shared/dev).
    Action: Verify Jerry can’t access team-a/prod.
        Call: getSecret("team-a-key-3", "team-a/prod", "any_secret").
        Expected: Throws SecurityException (“API key not authorized”).

Step 6: Steve Sets Up Secret Rotation with Webhook

    Action: Steve adds a secret team-a/dev/rds_password.
        Call: setSecret("team-a-root-key", "team-a/dev", "rds_password", "initial-password").
        Query: SELECT * FROM secrets WHERE namespace_id = 6 AND name = 'rds_password';
        Expected: Exists with value "initial-password".
    Action: Steve registers a webhook for team-a/dev/rds_password.
        Call: registerWebhook("team-a-root-key", "team-a/dev", "rds_password", "http://localhost:8080/update-credentials", "shared-secret-123").
        Query: SELECT * FROM webhooks WHERE secret_id = (SELECT id FROM secrets WHERE namespace_id = 6 AND name = 'rds_password');
        Expected: Webhook exists with url = "http://localhost:8080/update-credentials", auth_token = "shared-secret-123".
    Action: Steve enables rotation for team-a/dev/rds_password.
        Call: enableRotation("team-a-root-key", "team-a/dev", "rds_password", "1 minute").
        Query: SELECT * FROM secrets WHERE namespace_id = 6 AND name = 'rds_password';
        Expected: rotation_enabled = true, rotation_interval = '1 minute'.

Step 7: Automatic Rotation and Webhook Notification

    Action: Wait for the rotation scheduler to run (or manually trigger rotateSecrets).
        Call: rotateSecrets().
        Query: SELECT * FROM secrets WHERE namespace_id = 6 AND name = 'rds_password';
        Expected: encrypted_value updated (new random value), last_rotated_at updated.
    Action: Verify webhook notification.
        Check mock server logs at http://localhost:8080/update-credentials.
        Expected: Received POST request with:
            Header: Authorization: Bearer shared-secret-123.
            Payload: {"namespace": "team-a/dev", "secretName": "rds_password", "newValue": "<new-random-value>"}.
    Action: Verify rotation logging.
        Query: SELECT * FROM rotation_logs WHERE secret_id = (SELECT id FROM secrets WHERE namespace_id = 6 AND name = 'rds_password');
        Expected: Log entry with success = true, details = "Rotated successfully".

Step 8: Steve Deletes the Namespace

    Action: Steve deletes team-a/dev/team-a-nats-local-dev after testing.
        Call: deleteNamespace("team-a-root-key", "team-a/dev/team-a-nats-local-dev").
        Query: SELECT * FROM namespaces WHERE path = 'team-a/dev/team-a-nats-local-dev';
        Expected: No results (namespace deleted).
        Query: SELECT * FROM secrets WHERE namespace_id = 10;
        Expected: No results (secrets deleted).
        Query: SELECT * FROM api_key_namespaces WHERE namespace_id = 10;
        Expected: No results (permissions cleaned up).
    Action: Verify Steve can’t delete shared/dev.
        Call: deleteNamespace("team-a-root-key", "shared/dev").
        Expected: Throws SecurityException (“Cannot delete namespaces outside team scope”).

7. Pass/Fail Criteria
Pass Criteria

    All steps execute as expected without errors.
    Namespaces, API keys, secrets, and permissions are created, updated, and deleted correctly.
    Secret retrieval respects hierarchy and inheritance (e.g., nats_url override and fallback).
    Secret rotation occurs, and webhook notifications are sent with correct payload and authentication.
    Access control is enforced (e.g., Linda can’t write to team-a/dev, Jerry can’t access team-a/prod).
    Access and rotation logs are recorded accurately.
    Namespace deletion cleans up associated data and respects scope restrictions.

Fail Criteria

    Any step fails (e.g., incorrect permissions, missing logs, failed webhook).
    Access control is violated (e.g., Jerry accesses team-a/prod).
    Data integrity issues (e.g., orphaned permissions after namespace deletion).

8. Assumptions

    HSQLDB is running in-memory with autocommit enabled, simplifying transaction management for the prototype.
    The mock webhook server is available and correctly configured to log requests.
    The OtSeal encryption library (or equivalent) is implemented to handle secret encryption/decryption.

9. Risks and Mitigations

    Risk: Webhook server is unavailable, causing rotation notification failures.
        Mitigation: The notifyWebhooks method includes retry logic (3 attempts). Failures are logged in rotation_logs for manual intervention.
    Risk: Autocommit may lead to partial updates if a method fails mid-execution.
        Mitigation: Acceptable for the prototype; add transaction management in production.
    Risk: In-memory HSQLDB loses data on shutdown.
        Mitigation: Use file-based HSQLDB (jdbc:hsqldb:file:./opt1x-db/opt1x) for persistence if needed.

10. Test Deliverables

    Test execution log with results for each step.
    Database dump (e.g., SELECT * from all tables) before and after the test.
    Mock webhook server logs showing received notifications.
    Pass/fail report based on the criteria.

11. References

    Design Document: DESIGN.md (or equivalent in source control).
    Schema: As defined in the design document (api_keys, namespaces, etc.).
    Implementation: SecretService class with methods for namespace, API key, and secret management.

This test case specification provides a detailed, actionable plan to validate the Opt1x system’s functionality for Steve’s Team A and DevOps collaboration. You can store it in source control (e.g., as TEST_CASE_SPEC_STEVE_DEVOPS.md) and use it to guide your implementation and testing. Does this meet your needs? Want to proceed with implementing this test case or another aspect of your prototype? 😎