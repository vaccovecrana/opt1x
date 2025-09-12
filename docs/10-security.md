## Security Notes

- Always unseal before use.
- Rotate keys periodically: POST `/api/v1/key/rotate` with `{ "kid": <kid> }`.
- Audit via logs; no built-in auditing yet.

Report issues or contribute at <repo-url>.