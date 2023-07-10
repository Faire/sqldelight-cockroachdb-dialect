plugins {
  alias(libs.plugins.grammar.kit.composer)
}

grammarKit {
  intellijRelease.set(libs.versions.idea)
}

dependencies {
  compileOnly(libs.intellij.analysis)
  implementation(libs.intellij.util)
  api(libs.sqldelight.postgresql.dialect)

  testImplementation(libs.intellij.analysis)
  testImplementation(libs.sql.psi.test.fixtures)
}
