plugins {
    `java-library`
    checkstyle
    id("com.diffplug.spotless")
}

group = "dev.nexcraft.latch"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "10.26.1"
    configDirectory.set(rootProject.layout.projectDirectory.dir("../config/checkstyle"))
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(false)
    }
}

spotless {
    java {
        target("src/**/*.java")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
