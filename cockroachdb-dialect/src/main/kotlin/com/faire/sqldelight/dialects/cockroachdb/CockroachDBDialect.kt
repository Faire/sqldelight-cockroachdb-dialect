package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.dialect.api.SqlDelightDialect
import app.cash.sqldelight.dialects.postgresql.PostgreSqlDialect
import com.faire.sqldelight.dialects.cockroachdb.grammar.CockroachDBParserUtil
import com.intellij.icons.AllIcons

class CockroachDBDialect : SqlDelightDialect by WrappedDialect(PostgreSqlDialect())

private class WrappedDialect(
  private val postgresqlDialect: PostgreSqlDialect,
) : SqlDelightDialect by postgresqlDialect {
  override val icon = AllIcons.Providers.CockroachDB

  override fun setup() {
    postgresqlDialect.setup()

    CockroachDBParserUtil.reset()
    CockroachDBParserUtil.overridePostgreSqlParser()
  }
}
