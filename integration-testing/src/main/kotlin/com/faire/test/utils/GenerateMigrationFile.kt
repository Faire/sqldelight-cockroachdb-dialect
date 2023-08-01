package com.faire.test.utils

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val MIGRATION_FOLDER_PATH =
  "src/main/resources/migration/com/faire/sqldelight/dialects/cockroachdb"

fun main() {
  val current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
  val fileName = "v${current}__ADD_DESCRIPTION.sqm"

  val filePath = Paths.get(System.getProperty("user.dir"), MIGRATION_FOLDER_PATH, fileName)

  Files.createFile(filePath)

  println("Created migration file: ${filePath.fileName}")
}
