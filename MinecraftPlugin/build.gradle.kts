import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.github.goooler.shadow") version("8.1.7")
}

group = "dev.neuralnexus.schematiccopyplugin"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation("dev.neuralnexus:ampapi:1.3.0:downgraded")
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("dev.neuralnexus.ampapi", "dev.neuralnexus.schematiccopyplugin.lib.ampapi")
    minimize()
    archiveFileName = "SchematicCopyPlugin-${version}.jar"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

artifacts {
    archives(tasks.named("shadowJar"))
}
