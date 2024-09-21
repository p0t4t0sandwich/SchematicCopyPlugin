rootProject.name = "SchematicCopyPlugin"

pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.neuralnexus.dev/releases")
        maven("https://maven.neuralnexus.dev/snapshots")
        maven("https://maven.neuralnexus.dev/mirror")
        mavenCentral()
        gradlePluginPortal()
    }
}
