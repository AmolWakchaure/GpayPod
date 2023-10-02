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
        jcenter()
        maven ( url = "file:/C:/tools/m2repository/")
    }
}

rootProject.name = "GpayPod"
include(":app")
include(":gpaypod")
