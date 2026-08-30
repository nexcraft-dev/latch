plugins {
    id("latch.java-conventions")
}

dependencies {
    compileOnly(libs.jakarta.validation.api)

    testImplementation(libs.junit.jupiter)
}
