plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

dependencies {
    implementation(libs.intellij.util)
    implementation(libs.sqldelight.postgresql.dialect)

    testImplementation(libs.sql.psi.test.fixtures)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
