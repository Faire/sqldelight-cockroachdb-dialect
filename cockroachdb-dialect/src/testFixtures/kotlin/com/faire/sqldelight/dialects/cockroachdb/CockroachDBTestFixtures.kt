package com.faire.sqldelight.dialects.cockroachdb

import com.alecstrong.sql.psi.test.fixtures.loadFolderFromResources
import com.alecstrong.sql.psi.test.fixtures.toParameter
import java.io.File

object CockroachDBTestFixtures {
  val fixtures = loadFolderFromResources("fixtures_cockroachdb", File("build")).toParameter()
}
