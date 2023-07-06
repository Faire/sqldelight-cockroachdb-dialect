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
   * In CockroachDB, an index name doesn't have to be specified. For now try/catch and do nothing.
   */
  override fun modifySchema(schema: Schema) {
    try {
      super.modifySchema(schema)
    } catch (e: AssertionError) {
      // Hack to support unnamed indices.
    }
  }
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    try {
      super.annotate(annotationHolder)
    } catch (e: AssertionError) {
      // Hack to support unnamed indices.
    }
  }
}
