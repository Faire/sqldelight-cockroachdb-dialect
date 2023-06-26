plugins {
    alias(libs.plugins.grammar.kit.composer)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven("https://packages.jetbrains.team/maven/p/dpgpv/maven")
    gradlePluginPortal()
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
