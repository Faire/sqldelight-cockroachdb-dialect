plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.spotless)
}

tasks.register("installGitHooks") {
  doLast {
    val gitHooksDir = File(rootDir, ".git/hooks")
    val preCommitFile = File(gitHooksDir, "pre-commit")
    val preCommitScript = File(rootDir, "scripts/pre-commit-script.sh")

    if (!gitHooksDir.exists()) {
      throw GradleException(".git/hooks directory does not exist. Are you in the right directory?")
    }

    if (!preCommitScript.exists()) {
      throw GradleException("scripts/pre-commit-script.sh does not exist. Are you in the right directory?")
    }

    preCommitScript.copyTo(preCommitFile, true)
    preCommitFile.setExecutable(true)
  }
}

allprojects {
  configurations.configureEach {
    exclude(group = "com.jetbrains.rd")
    exclude(group = "com.github.jetbrains", module = "jetCheck")
    exclude(group = "com.jetbrains.infra")

    exclude(group = "org.roaringbitmap")

    exclude(group = "ai.grazie.spell")
    exclude(group = "ai.grazie.model")
    exclude(group = "ai.grazie.utils")
    exclude(group = "ai.grazie.nlp")
  }

  repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
  }
}

subprojects {
  apply {
    plugin("org.jetbrains.kotlin.jvm")
  }
}

val ktlintEditorConfigOverride = mapOf(
  "indent_size" to 2,
)

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*.*")
    ktlint(libs.versions.ktlint.get())
      .editorConfigOverride(ktlintEditorConfigOverride)
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktlint(libs.versions.ktlint.get())
      .editorConfigOverride(ktlintEditorConfigOverride)
    trimTrailingWhitespace()
    endWithNewline()
  }
  format("misc") {
    target("**/test/fixtures_*/**/*.*", "**/*.sqm", "**/*.sq")
    trimTrailingWhitespace()
    endWithNewline()
  }
}
