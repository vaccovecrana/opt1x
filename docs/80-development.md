## Development setup

Requires Java 21+m and Gradle 8 or later.

Create a file with the following content at `~/.gsOrgConfig.json`:
```
{
  "orgId": "vacco-oss",
  "orgConfigUrl": "https://raw.githubusercontent.com/vaccovecrana/org-config/refs/heads/main/vacco-oss.json"
}
```

Then run:
```
gradle clean build
```

Run the `OtMain` class from within IntelliJ, or run with Gradle via command line:
```
gradle :opt1x-linux-amd64:run \
  --args="--api-spring --jdbc-url=jdbc:h2:file:./o1x;DB_CLOSE_DELAY=-1;LOCK_MODE=3;AUTO_RECONNECT=TRUE"
```

If you are testing against a `rqlite` cluster, use:
```
gradle :opt1x-linux-amd64:run \
  --args="--api-spring --jdbc-url=jdbc:rqlite:http://localhost:4001"
```
