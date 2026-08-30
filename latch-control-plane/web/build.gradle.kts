plugins {
    id("latch.java-conventions")
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(project(":service"))
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.smallrye.health)

    testImplementation(libs.quarkus.junit)
}
