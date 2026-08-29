plugins {
    base
}

group = "dev.nexcraft.latch.controlplane"
version = "0.1.0-SNAPSHOT"

fun moduleTasks(name: String) = subprojects.map { it.tasks.named(name) }

tasks.named("clean") {
    dependsOn(moduleTasks("clean"))
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("test") {
    group = "verification"
    description = "Run tests for all control-plane modules."
    dependsOn(subprojects.map { it.tasks.named("test") })
}

tasks.named("check") {
    dependsOn(subprojects.map { it.tasks.named("check") })
}

tasks.register("componentTest") {
    group = "verification"
    description = "Run the control-plane component test suite."
    dependsOn(":web:quarkusIntTest")
}

tasks.register("spotlessApply") {
    group = "formatting"
    description = "Apply formatting to all control-plane modules."
    dependsOn(moduleTasks("spotlessApply"))
}

tasks.register("checkstyleMain") {
    group = "verification"
    description = "Run main-source Checkstyle for all control-plane modules."
    dependsOn(moduleTasks("checkstyleMain"))
}

tasks.register("checkstyleTest") {
    group = "verification"
    description = "Run test-source Checkstyle for all control-plane modules."
    dependsOn(moduleTasks("checkstyleTest"))
}
