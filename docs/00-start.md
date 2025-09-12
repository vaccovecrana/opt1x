## Key Concepts

- **API Keys**: Authenticate users/apps. Root key manages everything; child keys inherit limited access.
- **Groups**: Bundles of RBAC roles (Member/Admin) for managing permissions.
- **Namespaces**: Containers for values and configs. Hierarchical (e.g., `/o1x/flooper/dev`).
- **Values**: Labeled entries (e.g., `db.url`) with types (number/string/boolean), optional encryption, and notes.
- **Configs**: Tree structures referencing values, exportable in multiple formats.

### Initialization

- On first run, access `/init` in browser to generate root shares (for unsealing).
- Save shares securely; use at least 2 to unseal at `/unseal`.
- Log in at `/login` with root API key (displayed post-init).

App runs on `http://localhost:7070` with UI at `/` and API at `/api/v1`.
