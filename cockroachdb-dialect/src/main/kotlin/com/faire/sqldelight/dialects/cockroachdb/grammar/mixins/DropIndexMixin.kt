package com.faire.sqldelight.dialects.cockroachdb.grammar.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlDropIndexStmt
import com.alecstrong.sql.psi.core.psi.impl.SqlDropIndexStmtImpl
import com.intellij.lang.ASTNode

internal abstract class DropIndexMixin(
  node: ASTNode?,
) : SqlDropIndexStmtImpl(node),
  SqlDropIndexStmt {

  /**
   * Intentionally left blank to prevent validating indices.
   *
   * To support this in CockroachDB:
   * 1. Index name can be the same across tables. When tracking, embed the table name in the index name.
   * 2. An index can be created in the CREATE TABLE. Need to track that as well.
   * 3. Need to handle unnamed indices.
   */
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    // Intentionally left blank.
  }
}
