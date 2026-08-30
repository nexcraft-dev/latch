plugins {
    id("latch.java-conventions")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(project(":core"))
    implementation(libs.quarkus.hibernate.orm.panache)
    implementation(libs.quarkus.flyway)
    runtimeOnly(libs.quarkus.jdbc.postgresql)
    runtimeOnly(libs.quarkus.jdbc.h2)
}
