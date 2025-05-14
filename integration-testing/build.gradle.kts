plugins {
  alias(libs.plugins.sqldelight)
}

dependencies {
  api(libs.sqldelight.jdbc.driver)

  testRuntimeOnly(libs.slf4j.simple)

  testImplementation(libs.assertj.core)
  testImplementation(libs.postgres.jdbc.driver)
  testImplementation(libs.testcontainers.cockroachdb)
}

sqldelight {
  databases {
    create("CockroachDBIntegrationTesting") {
      packageName.set("com.faire.sqldelight.dialects.cockroachdb")
      deriveSchemaFromMigrations.set(true)

      dialect(projects.cockroachdbDialect)

      srcDirs(
        "src/main/resources/migration",
        "src/main/sqldelight",
      )
    }
  }
}

tasks.register<JavaExec>("generateMigrationFile") {
  group = "test-utils"
  mainClass = "com.faire.test.utils.GenerateMigrationFileKt"
  classpath = sourceSets["main"].runtimeClasspath
  dependsOn("assemble")
}
