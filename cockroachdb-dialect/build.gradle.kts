import com.vanniktech.maven.publish.SonatypeHost

group = "com.faire"
version = "0.3.1"

plugins {
  alias(libs.plugins.grammar.kit.composer)
  alias(libs.plugins.maven.publishing)
  `java-test-fixtures`
}

grammarKit {
  intellijRelease.set(libs.versions.idea)
}

dependencies {
  compileOnly(libs.sqldelight.compiler.env)
  implementation(libs.sqldelight.postgresql.dialect)

  testImplementation(libs.intellij.analysis)
  testImplementation(libs.sqldelight.postgresql.dialect) {
    artifact {
      classifier = "test-fixtures"
    }
  }

  testFixturesApi(testFixtures(libs.sql.psi))
}

mavenPublishing {
  coordinates("com.faire", "sqldelight-cockroachdb-dialect", "0.3.1")
  publishToMavenCentral(SonatypeHost.DEFAULT, true)
  signAllPublications()

  pom {
    name = "sqldelight-cockroachdb-dialect"
    description = "A SQLDelight dialect for CockroachDB"
    inceptionYear = "2023"
    url = "https://github.com/Faire/sqldelight-cockroachdb-dialect"

    licenses {
      license {
        name = "The Apache License, Version 2.0"
        distribution = "repo"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }

    developers {
      developer {
        id = "Faire"
        name = "Faire Developers"
      }
    }

    organization {
      name = "Faire"
      url = "https://www.faire.com"
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
