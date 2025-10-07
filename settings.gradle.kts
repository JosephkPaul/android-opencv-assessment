pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "EdgeDetectionApp"
include(":app")

// 1. REMOVE the old incorrect include line
// include(":OpenCV-android-sdk-4_12_0")

// 2. ADD these two lines instead
include(":sdk")
project(":sdk").projectDir = File("D:/Android/SDKs/OpenCV-android-sdk-4_9_0/sdk")