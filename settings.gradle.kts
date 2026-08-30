pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("latch.settings-conventions")
}

rootProject.name = "latch"

if (gradle.startParameter.taskNames.any { it == "componentTest" || it.endsWith(":componentTest") }) {
    System.setProperty("latch.component-test", "true")
}

includeBuild("latch-control-plane")
