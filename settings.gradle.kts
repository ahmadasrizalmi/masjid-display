pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MasjidDisplay"
include(":app-tv")
include(":core:domain")
include(":core:prayer")
include(":core:database")
include(":core:license")
include(":core:protocol")
include(":core:designsystem")
