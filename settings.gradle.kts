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
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle/")
        }
        maven {
            url = uri("https://nexus.adforus.com/repository/cubid")
        }
    }
}

rootProject.name = "CubidSDKSample"
include(":app")
include(":app-java")
