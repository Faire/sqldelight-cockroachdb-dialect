package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.CockroachContainer

class IntegrationTest {
  @Test
  fun `test`() {
    // empty for now
  }

  companion object {
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
    }

    @AfterClass
    @JvmStatic
    fun afterClass() {
      container.stop()
      driver.close()
    }
  }
}
