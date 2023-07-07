plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.sqldelight)
}

dependencies {
  testImplementation(libs.postgres.jdbc.driver)
  testImplementation(libs.sqldelight.jdbc.driver)
  testImplementation(libs.testcontainers.cockroachdb)
}

sqldelight {
  databases {
    create("CockroachDBIntegrationTesting") {
      packageName.set("com.faire.sqldelight.dialects.cockroachdb")
      deriveSchemaFromMigrations.set(true)

      dialect(projects.cockroachdbDialect)

      srcDirs(
        "src/main/resources/migrations",
      )
    }
  }
}
