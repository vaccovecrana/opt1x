## Security Notes

- Always unseal before use.
- Rotate keys periodically: POST `/api/v1/key/rotate` with `{ "kid": <kid> }`.
- Audit is planned to be implemented via system operation log exports via OTEL.

Report issues or contribute at https://github.com/vaccovecrana/opt1x/issues.