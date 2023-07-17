package com.faire.sqldelight.dialects.cockroachdb

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.CockroachContainer
import java.time.LocalDateTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.ZoneOffset

class IntegrationTest {
  @Test
  fun `query by computed columns`() {
    database.computedColumnQueries.create(1, 100)
    database.computedColumnQueries.create(2, -100)

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = false).executeAsOne(),
    ).isEqualTo(2)

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = true).executeAsOne(),
    ).isEqualTo(1)

    database.computedColumnQueries.create(3, 200)

    assertThat(
      database.computedColumnQueries.queryByComputedColumn(is_positive = true).executeAsList(),
    ).containsExactlyInAnyOrder(1, 3)
  }

  @Test
  fun `persist STRING type`() {
    database.stringTypeQueries.create(String_type(1, "hello"))
    database.stringTypeQueries.create(String_type(2, "world"))

    assertThat(
      database.stringTypeQueries.selectAll().executeAsList().map { it.name },
    ).containsExactlyInAnyOrder("hello", "world")
  }

  @Test
  fun `persist BLOB types`() {
    val charset = Charsets.UTF_8
    database.blobTypesQueries.create(
      Blob_data_types(
        id = 1,
        bytea_col = "foo".toByteArray(charset),
        blob_col = "bar".toByteArray(charset),
        bytes_col = "baz".toByteArray(charset),
      ),
    )
    with(database.blobTypesQueries.selectAll().executeAsOne()) {
      assertThat(id).isEqualTo(1)
      assertThat(bytea_col.toString(charset)).isEqualTo("foo")
      assertThat(blob_col.toString(charset)).isEqualTo("bar")
      assertThat(bytes_col.toString(charset)).isEqualTo("baz")
    }
  }

  @Test
  fun `integer types`() {
    database.intBigIntQueries.create(
      Int_big_int(
        id = 1,
        int4_col = 2,
        int_col = 3L,
        integer_col = 4L,
        int8_col = 5L,
        int64_col = 6L,
        bigint_col = 7L,
      ),
    )
  }

  @Test
  fun `set precision to timestamptz`() {
    val localDateTime = LocalDateTime.of(
      2023,
      Month.JANUARY,
      1,
      0,
      0,
      0,
      123456789, // nano of seconds
    )

    val localDateTimeInstantNano = localDateTime.toInstant(ZoneOffset.UTC).nano
    assertThat(localDateTimeInstantNano).isEqualTo(123456789)

    val offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.UTC)

    database.timestamptzPrecisionQueries.create(
      Timestamptz_precision(
        id = 1,
        tstz = offsetDateTime,
      ),
    )

    val storedInstantNano = database.timestamptzPrecisionQueries.selectAll().executeAsOne().tstz.toInstant().nano

    assertThat(storedInstantNano).isNotEqualTo(localDateTimeInstantNano)
    assertThat(storedInstantNano).isEqualTo(123000000)
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
