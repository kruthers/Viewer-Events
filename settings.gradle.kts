rootProject.name = "PigglesEvents"
include("queue-core")
enableFeaturePreview("VERSION_CATALOGS")
include("QueueDimension", "QueuePermsLink", "PigglesSG")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
