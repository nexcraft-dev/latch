group = "dev.nexcraft.latch"
version = "0.1.0-SNAPSHOT"

val controlPlaneBuild = gradle.includedBuild("latch-control-plane")

fun controlPlaneTask(name: String) = controlPlaneBuild.task(":$name")

tasks.register("clean") {
    group = "build"
    description = "Clean all included Latch backend projects."
    dependsOn(controlPlaneTask("clean"))
}

tasks.register("build") {
    group = "build"
    description = "Build all included Latch backend projects."
    dependsOn(gradle.includedBuild("latch-control-plane").task(":build"))
}

tasks.register("test") {
    group = "verification"
    description = "Run tests in all included Latch backend projects."
    dependsOn(gradle.includedBuild("latch-control-plane").task(":test"))
}

tasks.register("check") {
    group = "verification"
    description = "Run checks in all included Latch backend projects."
    dependsOn(gradle.includedBuild("latch-control-plane").task(":check"))
}

tasks.register("componentTest") {
    group = "verification"
    description = "Run component tests in all included Latch backend projects."
    dependsOn(gradle.includedBuild("latch-control-plane").task(":componentTest"))
}

tasks.register("spotlessApply") {
    group = "formatting"
    description = "Apply formatting to all included Latch backend projects."
    dependsOn(controlPlaneTask("spotlessApply"))
}

tasks.register("checkstyleMain") {
    group = "verification"
    description = "Run main-source Checkstyle for all included Latch backend projects."
    dependsOn(controlPlaneTask("checkstyleMain"))
}

tasks.register("checkstyleTest") {
    group = "verification"
    description = "Run test-source Checkstyle for all included Latch backend projects."
    dependsOn(controlPlaneTask("checkstyleTest"))
}
