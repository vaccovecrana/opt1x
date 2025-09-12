## Configuration

### 1. API Keys

- **Root Key**: Full access. Use to create child keys.
- Create key: POST `/api/v1/key` with JSON `{ "key": { "pKid": <parent-kid>, "name": "my-key", "leaf": true } }`. Headers: `X-Opt1x-Key: <parent-key>`.
- List keys: GET `/api/v1/key?pageSize=10` (paginated).
- UI: Navigate to `/keys` for management.

Keys are hierarchical; leaf keys can't create children.

### 2. Groups

- Groups define roles (Member: basic access; Admin: manage members/sub-groups).
- Create group: POST `/api/v1/group` with `{ "group": { "pGid": <parent-gid>, "name": "my-group" } }`.
- Bind key to group: POST `/api/v1/group/<gid>` with `{ "keyGroupBind": { "kid": <kid>, "role": "Admin" } }`.
- UI: `/groups` to create/view/bind.

Root group: `/o1x` (gid=1).

### 3. Namespaces

- Namespaces organize values/configs.
- Create: POST `/api/v1/namespace` with `{ "ns": { "pNsId": <parent-nsId>, "name": "my-ns" } }`.
- Bind group: POST `/api/v1/namespace/<nsId>` with `{ "group": { "gid": <gid> }, "groupNs": { "read": true, "write": true, "manage": true } }`.
- UI: `/namespaces` to create/grant access.

Root namespace: `/o1x` (nsId=1). Grants: read/write/manage flags control CRUD.

### 4. Values

- Define reusable vars in a namespace.
- Create: POST `/api/v1/value` with `{ "value": { "nsId": <nsId>, "name": "db.url", "value": "jdbc:...", "type": "String", "encrypted": true, "notes": "Dev DB" } }`.
- List: GET `/api/v1/value/<nsId>`.
- UI: In namespace view (`/values/<nsId>`), add/edit values.

Encryption uses AES (unsealed via shares).

### 5. Configs

- Build trees grouping values.
- Create: POST `/api/v1/config` with `{ "config": { "nsId": <nsId>, "name": "my-config" }, "vars": { ... tree structure ... } }`. Vars reference values via labels.
- Export: GET `/api/v1/config/<cid>/<fmt>` (fmt: json/yaml/toml/props).
- UI: In namespace view (`/configs/<nsId>`), use tree editor to build/reference values.

Example tree: `{ "db": { "url": "<ref to db.url value>", "user": "admin" } }`.