plugins {
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
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven("https://packages.jetbrains.team/maven/p/dpgpv/maven")
    gradlePluginPortal()
  }
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*.*")
    ktlint(libs.versions.ktlint.get())
      .editorConfigOverride(
        mapOf(
          "indent_size" to 2,
        )
      )
  }
}
