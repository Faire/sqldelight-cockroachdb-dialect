plugins {
  alias(libs.plugins.sqldelight)
}

dependencies {
  api(libs.sqldelight.jdbc.driver)

  testImplementation(libs.assertj.core)
  testImplementation(libs.postgres.jdbc.driver)
  testImplementation(libs.testcontainers.cockroachdb)
}

sqldelight {
  databases {
    create("CockroachDBIntegrationTesting") {
      packageName.set("com.faire.sqldelight.dialects.cockroachdb")
      deriveSchemaFromMigrations.set(true)

      dialect("com.faire:sqldelight-cockroachdb-dialect:0.3.1-80dc790b417dedb12ae657b9112f0e6edfe4c5a1-SNAPSHOT")

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
