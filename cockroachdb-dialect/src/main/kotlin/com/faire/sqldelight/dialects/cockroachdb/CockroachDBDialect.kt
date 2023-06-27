package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.dialect.api.SqlDelightDialect
import app.cash.sqldelight.dialects.postgresql.PostgreSqlDialect
import com.alecstrong.sql.psi.core.SqlParserUtil
import com.faire.sqldelight.dialects.cockroachdb.grammar.CockroachDBParser
import com.faire.sqldelight.dialects.cockroachdb.grammar.CockroachDBParserUtil
import com.intellij.icons.AllIcons
import com.intellij.lang.parser.GeneratedParserUtilBase

class CockroachDBDialect : SqlDelightDialect by WrappedDialect(PostgreSqlDialect())

private class WrappedDialect(
  private val postgresqlDialect: PostgreSqlDialect,
) : SqlDelightDialect by postgresqlDialect {
  override val icon = AllIcons.Providers.CockroachDB

  override fun setup() {
    postgresqlDialect.setup()

    CockroachDBParserUtil.reset()
    CockroachDBParserUtil.overrideSqlParser()
    CockroachDBParserUtil.overridePostgreSqlParser()
  }
}

private fun CockroachDBParserUtil.overrideSqlParser() {
  SqlParserUtil.drop_index_stmt = GeneratedParserUtilBase.Parser { psiBuilder, i ->
    drop_index_stmt?.parse(psiBuilder, i) ?: CockroachDBParser.drop_index_stmt_real(psiBuilder, i)
  }
}
