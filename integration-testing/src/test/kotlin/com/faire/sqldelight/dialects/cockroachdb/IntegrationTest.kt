package com.faire.sqldelight.dialects.cockroachdb

import Computed_column
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.CockroachContainer

class IntegrationTest {
  @Test
  fun `query by computed columns`() {
    // TODO: Figure out how to not pass in the computed column value
    // Intentionally passed in the opposite value to ensure it's not being persisted.
    database.computedColumnQueries.create(Computed_column(1, 100, false))
    database.computedColumnQueries.create(Computed_column(2, -100, true))

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = false).executeAsOne(),
    ).isEqualTo(2)

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = true).executeAsOne(),
    ).isEqualTo(1)

    database.computedColumnQueries.create(Computed_column(3, 200, false))

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = true).executeAsList(),
    ).containsExactlyInAnyOrder(1, 3)
  }

  companion object {
    private lateinit var database: CockroachDBIntegrationTesting

    private lateinit var container: CockroachContainer
    private lateinit var driver: JdbcDriver

    @BeforeClass
    @JvmStatic
    fun beforeClass() {
      container = CockroachContainer("cockroachdb/cockroach")
      container.start()

      val cockroachdbPort = container.getMappedPort(26257)

      driver = PGSimpleDataSource().apply {
        applicationName = "sqldelight"
        databaseName = "postgres"
        user = "root"
        portNumbers = IntArray(1) { cockroachdbPort }
      }.asJdbcDriver()

      CockroachDBIntegrationTesting.Schema.create(driver)
      database = CockroachDBIntegrationTesting(driver)
    }

    @AfterClass
    @JvmStatic
    fun afterClass() {
      container.stop()
      driver.close()
    }
  }
}
