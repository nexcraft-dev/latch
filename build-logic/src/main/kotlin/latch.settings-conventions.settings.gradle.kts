import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }

    val versionCatalogFile = generateSequence(settingsDir) { it.parentFile }
        .map { it.resolve("gradle/libs.versions.toml") }
        .firstOrNull { it.isFile }
        ?: error("Unable to locate the shared Gradle version catalog")

    val localVersionCatalogFile = settingsDir.resolve("gradle/libs.versions.toml")
    if (!localVersionCatalogFile.isFile && versionCatalogs.findByName("libs") == null) {
        versionCatalogs {
            create("libs") {
                from(files(versionCatalogFile))
            }
        }
    }
}
