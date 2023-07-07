package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import org.junit.BeforeClass
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.CockroachContainer

class IntegrationTest {
  @Test
  fun `test`() {
    CockroachDBIntegrationTesting.Schema.create(driver)
  }

  companion object {
    lateinit var driver: JdbcDriver

    @BeforeClass
    @JvmStatic
    fun beforeClass() {
      val container = CockroachContainer("cockroachdb/cockroach")
      container.start()

      driver = PGSimpleDataSource().apply {
        applicationName = "sqldelight"
        databaseName = "postgres"
        user = "root"
        portNumbers = listOf(container.firstMappedPort).toIntArray()
      }.asJdbcDriver()
    }
  }
}
