pluginManagement {
    includeBuild("../build-logic")
}

plugins {
    id("latch.settings-conventions")
}

rootProject.name = "latch-control-plane"

include(":core")
include(":service")
include(":repository")
include(":web")
