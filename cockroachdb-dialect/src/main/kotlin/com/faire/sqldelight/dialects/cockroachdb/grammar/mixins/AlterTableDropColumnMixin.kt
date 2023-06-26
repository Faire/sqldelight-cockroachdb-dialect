/**
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
package com.faire.sqldelight.dialects.cockroachdb.grammar.mixins

import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.faire.sqldelight.dialects.cockroachdb.grammar.psi.CockroachDBAlterTableDropColumn
import com.intellij.lang.ASTNode

internal abstract class AlterTableDropColumnMixin(
  node: ASTNode,
) : SqlCompositeElementImpl(node),
  CockroachDBAlterTableDropColumn,
  AlterTableApplier {
  private val columnName
    get() = children.filterIsInstance<SqlColumnName>().single()

  override fun applyTo(lazyQuery: LazyQuery): LazyQuery {
    return LazyQuery(
      tableName = lazyQuery.tableName,
      query = {
        val columns = lazyQuery.query.columns.filter { it.element.text != columnName.name }
        lazyQuery.query.copy(columns = columns)
      },
    )
  }
}
