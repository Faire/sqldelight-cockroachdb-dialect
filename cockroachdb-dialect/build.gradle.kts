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
  compileOnly(libs.sqldelight.compiler.env)
  api(libs.sqldelight.postgresql.dialect)

  testImplementation(libs.intellij.analysis)
  testImplementation(libs.sql.psi.test.fixtures) {
    exclude(group = "com.jetbrains.intellij.platform")
  }
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
