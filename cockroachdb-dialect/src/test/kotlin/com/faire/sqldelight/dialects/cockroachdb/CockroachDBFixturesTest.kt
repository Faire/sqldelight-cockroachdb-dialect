/*
 * Copyright 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.dialects.postgresql.PostgresqlTestFixtures
import com.alecstrong.sql.psi.test.fixtures.FixturesTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class CockroachDBFixturesTest(name: String, fixtureRoot: File) : FixturesTest(name, fixtureRoot) {
  // The upstream sql-psi ANSI fixtures are written for a SQLite-flavored dialect. These
  // text substitutions translate the most common SQLite-isms into their
  // CockroachDB / PostgreSQL equivalents before the fixture is parsed:
  //
  //  - INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT  -> SERIAL NOT NULL PRIMARY KEY
  //    SQLite's AUTOINCREMENT only applies to INTEGER PRIMARY KEY columns; CockroachDB uses
  //    SERIAL (https://www.cockroachlabs.com/docs/stable/serial).
  //  - AUTOINCREMENT -> ""  (drop the keyword everywhere else; column types like SERIAL
  //    auto-increment by default in CockroachDB).
  //  - ?1 / ?2 -> ?  CockroachDB/PostgreSQL bind parameters are positional ($1, $2, ...) or
  //    unnumbered ?, not ?1/?2.
  //  - BLOB -> TEXT  CockroachDB has BYTEA/BYTES/BLOB, but the upstream BLOB-typed columns
  //    are usually used in TEXT-comparison contexts; mapping to TEXT keeps the test green
  //    without changing what's being asserted.
  //  - "id TEXT GENERATED ALWAYS AS (2) ... NOT NULL" gets a STORED keyword inserted because
  //    PostgreSQL/CockroachDB require GENERATED ALWAYS AS (...) STORED (no VIRTUAL form for
  //    PostgreSQL; CockroachDB allows STORED or VIRTUAL).
  //
  // (The previously-present rule rewriting the multiple-column-where parser-error message
  // was dead code: that fixture is excluded entirely below.)
  override val replaceRules = arrayOf(
    "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT" to "SERIAL NOT NULL PRIMARY KEY",
    "AUTOINCREMENT" to "",
    "?1" to "?",
    "?2" to "?",
    "BLOB" to "TEXT",
    "id TEXT GENERATED ALWAYS AS (2) UNIQUE NOT NULL" to "id TEXT GENERATED ALWAYS AS (2) STORED UNIQUE NOT NULL",
  )

  override fun setupDialect() {
    CockroachDBDialect().setup()
  }

  companion object {
    private val excludedAnsiFixtures = listOf(
      // The cockroach grammar's CreateIndexMixin and DropIndexMixin intentionally leave
      // modifySchema()/annotate() empty (see those files for rationale: CockroachDB allows
      // unnamed indexes, indexes declared inline in CREATE TABLE, and the same index name
      // across different tables, none of which the upstream sql-psi schema tracker handles).
      // The result is that we don't currently surface "duplicate index name" / "no index
      // found" diagnostics, which these fixtures assert.
      //
      // TODO(validation): CockroachDB does enforce index-name uniqueness within a single
      // table (see https://www.cockroachlabs.com/docs/stable/create-index). Once unnamed
      // indexes and inline-CREATE-TABLE indexes are tracked in the schema, these fixtures
      // could be re-enabled.
      "index-migration",
      "create-index-collision",
      // Mostly exercises CREATE TABLE foreign-key constraint validation (composite primary
      // key matching, "unique index on column" lookups, duplicate primary key clauses, etc.)
      // — all of which CockroachDB does enforce — but several assertions also depend on
      // index tracking (see above). Excluded because of the index-tracking gap.
      //
      // TODO(validation): split or re-enable once CockroachDB index tracking is implemented.
      "create-table-validation-failures",
      // Tests CREATE TABLE/VIEW/INDEX/TRIGGER + IF NOT EXISTS collision detection. The
      // CREATE INDEX collision lines hit the same index-tracking gap as above, and the
      // CREATE TRIGGER lines use SQLite-style `BEGIN ... END;` trigger bodies that neither
      // PostgreSQL nor CockroachDB parse (CockroachDB uses `EXECUTE FUNCTION fn()`).
      "create-if-not-exists",
      // The fixture asserts a parser error message that is shorter than what cockroach
      // actually emits, because the cockroach/postgres expression grammar adds many extra
      // operator alternatives (`<jsona binary operator real>`, `<jsonb boolean operator
      // real>`, `'#-'`, `'@@'`, `<contains operator real>`, etc.) to the "expected" list.
      // We've copied the valid-syntax half of this fixture to `multiple-column-where-ansi`.
      // Not a CockroachDB feature gap.
      "multiple-column-where",
      // The following ANSI fixtures use SQLite-style `CREATE TRIGGER ... BEGIN ... END;`
      // syntax, which is not parseable by the PostgreSQL dialect (which we inherit from)
      // and is not the form CockroachDB accepts either. CockroachDB's CREATE TRIGGER uses
      // the PostgreSQL-style `EXECUTE FUNCTION fn()` form (since v24.3 LTS, see
      // https://www.cockroachlabs.com/docs/stable/create-trigger), so these SQLite-flavored
      // fixtures can't be supported as-written.
      "create-trigger-collision",
      "create-trigger-docid",
      "create-trigger-raise",
      "create-trigger-success",
      "create-trigger-validation-failures",
      "rowid-triggers",
      "timestamp-literals",
      "trigger-migration",
      "trigger-new-in-expression",
      "update-view-with-trigger",
      // `DROP TRIGGER IF EXISTS test3;` (no `ON tablename`) is SQLite syntax. The
      // PostgreSQL dialect (and CockroachDB, see
      // https://www.cockroachlabs.com/docs/stable/drop-trigger) require
      // `DROP TRIGGER ... ON tablename`. The CREATE TRIGGER block in this fixture is also
      // SQLite-flavored.
      "if-not-exists",
    )

    private val excludedPgSqlFixtures = listOf(
      // The fixture asserts on PostgreSQL's index storage-parameter validators (fillfactor,
      // deduplicate_items, fastupdate, gin_pending_list_limit, autosummarize, pages_per_range
      // ranges/booleans). The cockroach `create_index_stmt` in CockroachDB.bnf includes
      // [ {with_storage_parameter} ] syntactically but does not run those validators (the
      // cockroach CreateIndexMixin overrides annotate() with a no-op). We've copied the
      // valid-syntax half of this fixture to `create-index-pgsql` (without error
      // assertions).
      //
      // TODO(validation): CockroachDB does support / reject storage parameters on indexes
      // (https://www.cockroachlabs.com/docs/stable/create-index), and the cockroach-specific
      // ones (e.g. `bucket_count` for hash-sharded indexes) ought to be validated here too.
      // Once index tracking is wired up (see excludedAnsiFixtures above) the storage-param
      // validation could be inherited from the postgres mixin.
      "create-index",
      // The following PostgreSQL fixtures exercise statements added to PostgreSQL's
      // `extension_stmt` / `alter_table_rules` rules in sqldelight 2.2.x / 2.3.x. CockroachDB's
      // grammar overrides those rules without these productions, so they currently fail to
      // parse. Each is supported by CockroachDB itself and would be a worthwhile follow-up.
      //
      // TODO(grammar): Add CREATE/ALTER/DROP POLICY to the cockroach `extension_stmt` rule.
      // CockroachDB supports row-level security policies as of v25.2 with PostgreSQL-compatible
      // syntax. https://www.cockroachlabs.com/docs/stable/create-policy
      "alter-policy",
      "create-policy",
      "drop-policy",
      // TODO(grammar): Add COMMENT ON to the cockroach `extension_stmt` rule. CockroachDB
      // supports `COMMENT ON DATABASE/SCHEMA/TYPE/TABLE/COLUMN/INDEX/CONSTRAINT` (note: VIEW is
      // not currently listed in the CockroachDB docs, so the upstream fixture's
      // `COMMENT ON VIEW VSomeTable IS '...'` line may need a dialect-specific variant).
      // https://www.cockroachlabs.com/docs/stable/comment-on
      "comment-on",
      // TODO(grammar): Add CREATE/ALTER/DROP TYPE (enum) to the cockroach `extension_stmt`
      // rule. CockroachDB supports `CREATE TYPE name AS ENUM (...)`, `ALTER TYPE`, and
      // `DROP TYPE` with PostgreSQL-compatible syntax.
      // https://www.cockroachlabs.com/docs/stable/create-type
      "create-enum",
      // TODO(grammar): Add CREATE/DROP FUNCTION to the cockroach `extension_stmt` rule.
      // CockroachDB has supported user-defined functions since v22.2.
      // https://www.cockroachlabs.com/docs/stable/create-function
      // https://www.cockroachlabs.com/docs/stable/drop-function
      "drop-function",
      // TODO(grammar): Add CREATE TRIGGER / CREATE OR REPLACE TRIGGER / DROP TRIGGER to the
      // cockroach grammar. CockroachDB supports triggers (since v24.3 LTS) with the
      // PostgreSQL-style `EXECUTE FUNCTION fn()` form, and `CREATE OR REPLACE TRIGGER` since
      // v26.2. The upstream fixtures should pass once these productions are inherited.
      // https://www.cockroachlabs.com/docs/stable/create-trigger
      // https://www.cockroachlabs.com/docs/stable/drop-trigger
      "create-or-replace-trigger",
      "drop-trigger",
      // TODO(grammar): Add `ALTER TABLE ... { ENABLE | DISABLE } ROW LEVEL SECURITY` to the
      // cockroach `alter_table_rules` rule. CockroachDB supports both subcommands as of v25.2.
      // The upstream fixture also exercises `FORCE ROW LEVEL SECURITY`, which is not currently
      // documented as supported by CockroachDB; that line may need a dialect-specific variant.
      // https://www.cockroachlabs.com/docs/stable/alter-table#enable-row-level-security
      "alter-table-row-level-security",
      // TODO(grammar): Add support for PostgreSQL's `ALTER TABLE ... SET (param = value)` /
      // `RESET (...)` storage-parameter form to the cockroach `alter_table_rules` rule.
      // CockroachDB does support a SET (storage parameter) form on ALTER TABLE, although the
      // set of accepted parameters differs from PostgreSQL (CockroachDB-specific TTL/stats
      // parameters are real, while several PostgreSQL parameters such as `fillfactor` and
      // `autovacuum_vacuum_scale_factor` are parsed as no-ops or unsupported). Cockroach's
      // current `alter_table_set_storage_options` rule covers a different cockroach-only
      // `WITH (k=v)` form.
      // https://www.cockroachlabs.com/docs/stable/alter-table
      "alter-table-set-storage-parameter",
    )

    // Used by Parameterized JUnit runner reflectively.
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun parameters(): List<Array<out Any>> {
      val extraAnsiFixtures = ansiFixtures
        .filter { (it[0] as String) !in excludedAnsiFixtures }

      val extraPgSqlFixtures = PostgresqlTestFixtures.fixtures
        .filter { (it[0] as String) !in excludedPgSqlFixtures }

      return CockroachDBTestFixtures.fixtures + extraPgSqlFixtures + extraAnsiFixtures
    }
  }
}
