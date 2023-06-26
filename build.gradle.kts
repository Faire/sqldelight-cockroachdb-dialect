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
}