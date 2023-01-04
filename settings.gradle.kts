rootProject.name = "PigglesEvents"
include("queue-core")
enableFeaturePreview("VERSION_CATALOGS")
include("QueueDimension")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
