plugins {
    id("latch.java-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":repository"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
