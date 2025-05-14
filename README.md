# SQLDelight CockroachDB Dialect
[CockroachDB](https://www.cockroachlabs.com/) dialect for [SQLDelight](https://github.com/cashapp/sqldelight).
CockroachDB supports the majority of the PostgreSQL syntax (please see this [page](https://www.cockroachlabs.com/docs/stable/postgresql-compatibility.html) for more details).

## Important notes

### Disclaimer
This doesn't fully cover the whole SQL syntax of CockroachDB. It's a work in progress and contributions are welcome.

### Differences between the PostgreSQL SQLDelight Dialect
* Supports ALTER PRIMARY KEY
* Supports specifying an index in a CREATE TABLE statement
* Supports DROP INDEX [IF EXISTS] `<table>@<index_name>` [CASCADE]
* Supports STRING data type
* Supports storing index
* Supports unnamed index
  * Index tracking and validation have been disabled until there's a way to track unnamed indexes
* Blob types (BYTEA, BLOB, BYTES)
* [Integer types](https://www.cockroachlabs.com/docs/stable/int.html) (32bit and 64bit aliases)
* [Specify precision to TIMESTAMPTZ](https://www.cockroachlabs.com/docs/stable/timestamp.html#precision)
* [Specify table storage options](https://www.cockroachlabs.com/docs/stable/with-storage-parameter)
* Setting Locality
  * Currently via [ALTER TABLE](https://www.cockroachlabs.com/docs/stable/alter-table#set-locality)
  * [Adding](https://www.cockroachlabs.com/docs/stable/alter-database#add-region)/[Dropping](https://www.cockroachlabs.com/docs/stable/alter-database#drop-region) regions
* [Setting survival goal](https://www.cockroachlabs.com/docs/stable/alter-database#survive-zone-region-failure)

## Usage

### Using this Library

Please see the official [SQLDelight Documentation](https://cashapp.github.io/sqldelight/jvm_postgresql/#getting-started-with-postgresql).

To use this dialect, set the dialect to be:
`dialect("com.faire:sqldelight-cockroachdb-dialect:<version>")`

The latest version can be found [here](https://central.sonatype.com/artifact/com.faire/sqldelight-cockroachdb-dialect).

### Using a Snapshot Release

A snapshot release is built and released on the maven snapshot repo. To use a snapshot release, follow these steps:

1. Add the maven snapshot repo to your project
```kotlin
maven {
  url = uri("https://oss.sonatype.org/content/repositories/snapshots")
  mavenContent {
    snapshotsOnly()
  }
}
```
2. Supply a SNAPSHOT version of the library. The automatic build and release process was introduced after `80dc790b417dedb12ae657b9112f0e6edfe4c5a1`.
The version structure for the releases is as follows `<latestVersion>-<sha>-SNAPSHOT`
* The `latestVersion` is the latest release prior to the commit. For example if there is a release `1.0` and `1.1` and there are commits prior to 1.1 being released,
the version for a snapshot release in between will be `1.0`. You can find the version by looking in the `gradle.properties` file.

For example, for the commit sha of `80dc790b417dedb12ae657b9112f0e6edfe4c5a1`, you can visit the state of the repo for that hash by visiting:

https://github.com/Faire/sqldelight-cockroachdb-dialect/tree/80dc790b417dedb12ae657b9112f0e6edfe4c5a1 (note the hash at the end).

Then access the `gradle.properties` file https://github.com/Faire/sqldelight-cockroachdb-dialect/blob/80dc790b417dedb12ae657b9112f0e6edfe4c5a1/gradle.properties which has `0.3.1` version.

Given these two values, you should use `0.3.1-80dc790b417dedb12ae657b9112f0e6edfe4c5a1-SNAPSHOT` as the version in [the previous section](#using-this-library)

[Here is a sample PR](https://github.com/Faire/sqldelight-cockroachdb-dialect/pull/101) that is using a snapshot release for the dialect.

## Development

This project uses very basic Gradle; importing it into IntelliJ should work out of the box.

We use [testcontainers](https://java.testcontainers.org/) to run integration tests. Please refer to their documentation
on [environment setup](https://java.testcontainers.org/supported_docker_environment/).
