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
  override val replaceRules = arrayOf(
    // TODO: document why
    "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT" to "SERIAL NOT NULL PRIMARY KEY",
    "AUTOINCREMENT" to "",
    "?1" to "?",
    "?2" to "?",
    "BLOB" to "TEXT",
    "id TEXT GENERATED ALWAYS AS (2) UNIQUE NOT NULL" to "id TEXT GENERATED ALWAYS AS (2) STORED UNIQUE NOT NULL",
    "'(', ')', ',', '.', <binary like operator real>, BETWEEN or IN expected, got ','"
      to "'#-', '(', ')', ',', '.', <binary like operator real>, <jsona binary operator real>, <jsonb boolean operator real>, '@@', BETWEEN or IN expected, got ','",
  )

  override fun setupDialect() {
    CockroachDBDialect().setup()
  }

  companion object {
    private val excludedAnsiFixtures = listOf(
      // Excluded since we're not validating indices when dropping them.
      "index-migration",
      // Excluded since we're not validating indices when creating them.
      "create-table-validation-failures",
      // Excluded since we're not validating indices when creating them.
      "create-if-not-exists",
      // Excluded since we're not validating indices when creating them.
      "create-index-collision",
      // Excluded since our error message is different;
      // we've copied the test case, but without the failure case, into `multiple-column-where-ansi`.
      "multiple-column-where",
      // The following ANSI fixtures use SQLite-style `CREATE TRIGGER ... BEGIN ... END;` syntax,
      // which is not parseable by the PostgreSQL dialect (which we inherit from) and is not
      // supported by CockroachDB. CockroachDB's `CREATE TRIGGER` uses the PostgreSQL-style
      // `EXECUTE FUNCTION fn()` form (see https://www.cockroachlabs.com/docs/stable/create-trigger),
      // so these SQLite-flavored fixtures cannot be supported as-written.
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
      // `DROP TRIGGER IF EXISTS test3;` (no `ON tablename`) is SQLite syntax. The PostgreSQL
      // dialect (and CockroachDB; see https://www.cockroachlabs.com/docs/stable/drop-trigger)
      // require `DROP TRIGGER ... ON tablename`.
      "if-not-exists",
    )

    private val excludedPgSqlFixtures = listOf(
      // Excluded since we're not validating indices when creating them;
      // we've copied the test case, but without error assertions, into `create-index-pgsql`.
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
