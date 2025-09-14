# Deployment Models

Opt1x offers deployment options for both single-instance and high-availability setups. This page outlines how to configure Opt1x for standalone operation using an embedded [H2](https://www.h2database.com/html/main.html) database or for high availability using an [rqlite](https://rqlite.io/) database, either as a single server or a clustered setup.

## Single Web Server with H2 Database

For simple deployments, Opt1x can run as a single web server instance backed by an H2 database with a local file for storage. This is ideal for small-scale setups or development environments where high availability is not required.

### Configuration

Specify the H2 database file location via the JDBC URL in the Opt1x startup command or configuration. For example:

```bash
./opt1x --jdbc-url=jdbc:h2:file:/data/o1x;DB_CLOSE_DELAY=-1;LOCK_MODE=3;AUTO_RECONNECT=TRUE
```

- The `/data/o1x` path points to the local H2 database file.
- Flags like `DB_CLOSE_DELAY=-1` keep the database open, `LOCK_MODE=3` enables file locking, and `AUTO_RECONNECT=TRUE` handles transient failures.

### Data Protection

To ensure data durability, store the H2 database file on a RAID array (e.g., RAID 1 or 5) to protect against disk failures. Configure the RAID array on the host system where the database file resides (e.g., `/data/o1x`).

### Limitations

This mode supports only a single Opt1x instance, as H2 does not support multi-instance access to a file-based database. Applications connect to this single instance for configuration data.

## High Availability with rqlite

For production environments requiring high availability and redundancy, Opt1x can connect to an rqlite database, either as a single server or a [clustered setup](https://rqlite.io/docs/clustering/general-guidelines/).

rqlite provides distributed SQL storage, ensuring configuration data is safely replicated across multiple nodes, and allows multiple Opt1x instances to serve configuration data concurrently.

### Single rqlite Server

Configure Opt1x to connect to a standalone rqlite instance using the rqlite JDBC driver. Set the JDBC URL to point to the rqlite server’s HTTP endpoint. Example:

```bash
./opt1x --jdbc-url=jdbc:rqlite:http://rqlite-host:4001
```

Replace `rqlite-host:4001` with the server’s hostname and port, and include credentials if required (see [rqlite JDBC driver documentation](https://github.com/rqlite/rqlite-jdbc) for URL syntax).

### rqlite Cluster

For redundancy, configure Opt1x to connect to an rqlite cluster via a DNS-balanced hostname that routes to any cluster member. This ensures failover if a node goes down. Example:

```bash
./opt1x --jdbc-url=jdbc:rqlite:http://rqlite-cluster-dns:4001
```

The `rqlite-cluster-dns` host resolves to any available rqlite node in the cluster. Multiple Opt1x instances can connect to the same cluster, enabling load-balanced configuration management.

Refer to the [rqlite clustering guidelines](https://rqlite.io/docs/clustering/general-guidelines/) for cluster setup details, such as node discovery and leader election. Note that configuring the rqlite cluster itself (e.g., setting up nodes, Raft consensus) is outside the scope of Opt1x documentation.

Using an rqlite cluster ensures configuration data is replicated across nodes, providing fault tolerance and high availability. Multiple Opt1x instances can serve traffic, improving scalability for applications fetching configs via the REST API or external system integrations.

> Caveat: in this setup, you'll need to unseal each Opt1x instance individually before it can start serving configuration traffic. This is a known issue, and solutions are in-planning.

In cluster mode, deploy Opt1x instances behind a load balancer (e.g., nginx, AWS ALB) to distribute requests from Spring Boot clients or other applications.

This deployment flexibility makes Opt1x suitable for both simple, single-instance setups and robust, high-availability environments, with minimal configuration overhead.