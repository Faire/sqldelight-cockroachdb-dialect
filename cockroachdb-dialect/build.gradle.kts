plugins {
  alias(libs.plugins.grammar.kit.composer)
  `maven-publish`
}

repositories {
  maven("https://jitpack.io")
}

grammarKit {
  intellijRelease.set(libs.versions.idea)
}

dependencies {
  implementation("com.github.Faire:sqldelight-cockroachdb-dialect:main-SNAPSHOT")

  compileOnly(libs.intellij.analysis)
  implementation(libs.intellij.util)
  api(libs.sqldelight.postgresql.dialect)

  testImplementation(libs.intellij.analysis)
  testImplementation(libs.sql.psi.test.fixtures)
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.faire"
      artifactId = "sqldelight-cockroachdb-dialect"

      from(components["java"])
    }
  }
}
