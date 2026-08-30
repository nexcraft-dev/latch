plugins {
    id("latch.java-conventions")
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":repository"))
    implementation(project(":service"))
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.smallrye.health)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.hibernate.validator)
    implementation(libs.quarkus.smallrye.openapi)
    implementation(libs.quarkus.http.problem)

    testImplementation(libs.quarkus.junit)
}

val componentTestRequested = providers
    .systemProperty("latch.component-test")
    .getOrElse("false")
    .toBoolean()

val runsIntegrationTests = componentTestRequested || gradle.startParameter.taskNames.any {
    it == "componentTest" || it.endsWith(":componentTest") || it.endsWith("quarkusIntTest")
}

quarkus {
    if (runsIntegrationTests) {
        set("profile", "test")
    }
}

tasks.named<Test>("quarkusIntTest") {
    systemProperty("quarkus.test.integration-test-profile", "test")
}

val nativeBuildRequested = providers
    .systemProperty("quarkus.native.enabled")
    .map(String::toBoolean)
    .orElse(false)

tasks.withType<io.quarkus.gradle.tasks.QuarkusBuildTask>().configureEach {
    if (nativeBuildRequested.get()) {
        jarEnabled.set(false)
    }
}
