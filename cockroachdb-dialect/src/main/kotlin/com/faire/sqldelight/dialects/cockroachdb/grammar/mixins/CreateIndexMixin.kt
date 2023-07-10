package com.faire.sqldelight.dialects.cockroachdb.grammar.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SqlCreateIndexStmt
import com.alecstrong.sql.psi.core.psi.impl.SqlCreateIndexStmtImpl
import com.intellij.lang.ASTNode

internal abstract class CreateIndexMixin(
  node: ASTNode?,
) : SqlCreateIndexStmtImpl(node),
  SqlCreateIndexStmt {
  /**
   * In CockroachDB, an index name doesn't have to be specified. For now disable tracking the index.
   */
  override fun modifySchema(schema: Schema) {
    // Intentionally left blank.
  }
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    // Intentionally left blank.
  }
}
