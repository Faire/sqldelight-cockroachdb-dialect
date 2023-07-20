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
  implementation(libs.sqldelight.postgresql.dialect)

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
      version = "0.1.0"

      from(components["java"])

      pom {
        name = "sqldelight-cockroachdb-dialect"
        description = "A SQLDelight dialect for CockroachDB"
        inceptionYear = "2023"

        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }

        issueManagement {
          system = "GitHub"
          url = "https://github.com/Faire/sqldelight-cockroachdb-dialect/issues"
        }

        scm {
          connection = "scm:git:git://github.com/Faire/sqldelight-cockroachdb-dialect.git"
          url = "https://github.com/Faire/sqldelight-cockroachdb-dialect"
        }
      }
    }
  }
}
