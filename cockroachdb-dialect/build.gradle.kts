plugins {
    alias(libs.plugins.grammar.kit.composer)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
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

spotless {
    kotlin {
        targetExclude("**/build/**/*.*")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "indent_size" to 2,
                )
            )
    }
}
