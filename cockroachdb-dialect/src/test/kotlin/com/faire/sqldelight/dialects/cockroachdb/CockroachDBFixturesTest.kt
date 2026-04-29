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
      // which is not parseable by the PostgreSQL dialect (which we inherit from). PostgreSQL
      // (and CockroachDB) use `CREATE TRIGGER ... EXECUTE FUNCTION fn();` form instead.
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
      // `DROP TRIGGER IF EXISTS test3;` (without `ON tablename`) is SQLite syntax. The PostgreSQL
      // dialect requires `DROP TRIGGER ... ON tablename`.
      "if-not-exists",
    )

    private val excludedPgSqlFixtures = listOf(
      // Excluded since we're not validating indices when creating them;
      // we've copied the test case, but without error assertions, into `create-index-pgsql`.
      "create-index",
      // The following PostgreSQL fixtures exercise statements that are part of PostgreSQL's
      // `extension_stmt` rule but which CockroachDB's `extension_stmt` override does not
      // currently include (CREATE/ALTER/DROP POLICY, CREATE TYPE/ENUM, CREATE/DROP FUNCTION,
      // COMMENT ON, CREATE/DROP TRIGGER, CREATE OR REPLACE TRIGGER). Adding them is a future
      // expansion of this dialect; for now we exclude these inherited tests.
      "alter-policy",
      "comment-on",
      "create-enum",
      "create-or-replace-trigger",
      "create-policy",
      "drop-function",
      "drop-policy",
      "drop-trigger",
      // The following PostgreSQL fixtures exercise statements that are part of PostgreSQL's
      // `alter_table_rules` but which CockroachDB's override of `alter_table_rules` does not
      // currently include. CockroachDB has its own `alter_table_set_storage_options` for the
      // SET (k=v) form, but its semantics differ from PostgreSQL's `SET (storage_param=...)`.
      "alter-table-row-level-security",
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
