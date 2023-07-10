rootProject.name = "sqldelight-cockroachdb-dialect"

include("buildLogic")
include("cockroachdb-dialect")
include("integration-testing")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
