# SQLDelight CockroachDB Dialect
[CockroachDB](https://www.cockroachlabs.com/) dialect for [SQLDelight](https://github.com/cashapp/sqldelight).
CockroachDB supports the majority of the PostgreSQL syntax (please see this [page](https://www.cockroachlabs.com/docs/stable/postgresql-compatibility.html) for more details).

### Disclaimer
This doesn't fully cover the whole SQL syntax of CockroachDB. It's a work in progress and contributions are welcome.

### Differences between the PostgreSQL SQLDelight Dialect
* Supports ALTER PRIMARY KEY
* Supports specifying an index in a CREATE TABLE statement
* Supports DROP INDEX `<table>@<index_name>` syntax
* Supports STRING data type
* Supports storing index
* Supports unnamed index
  * Index tracking and validation has been disabled until there's a way to track unnamed indexes
* Blob types (BYTEA, BLOB, BYTES)
* [Integer types](https://www.cockroachlabs.com/docs/stable/int.html) (32bit and 64bit aliases)
* [Specify precision to TIMESTAMPTZ](https://www.cockroachlabs.com/docs/stable/timestamp.html#precision)
* [Specify table storage options](https://www.cockroachlabs.com/docs/stable/with-storage-parameter)
