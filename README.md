# opt1x

`Opt1x` is a minimal, hassle-free configuration editor.

- Users get assigned API keys to access and modify the system.
- Namespaces are groups of resources.
- Users (API keys) can get granted access or removed from namespaces as the only form of RBAC.
- Users with write access into a namespace can define basic configuration values (for example, dev DB url, username, password, prod DB url, username, password). Each value has a label (like `myservice.db.url` `myservice.db.username`, etc., and an assigned value (which can be optionally encrypted).
- Users can then create basic configuration lists which group configuration values. For example an API key can create a configuration called `my-service-dev` or `my-service-prod`, and reference the variable labels above.
