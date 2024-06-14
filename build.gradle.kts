plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version("8.1.7")
}


group = "org.lushplugins"
version = "2.0.0-BETA"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven("https://ci.ender.zone/plugin/repository/everything/") // Essentials
    maven("https://repo.opencollab.dev/main/") // Floodgate
    maven("https://repo.lushplugins.org/snapshots/") // LushLib
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // EntityLib, GSit
}

dependencies {
    // Dependencies
    compileOnly("org.spigotmc:spigot:1.20-R0.1-SNAPSHOT")

    // Soft Dependencies
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:1.9.0")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly(files("libs/SimpleSit.jar"))

    // Libraries
    implementation("com.github.retrooper.packetevents:spigot:2.3.0")
    implementation("com.github.OakLoaf.EntityLib:spigot:3117c89d5c")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("org.lushplugins:LushLib:0.7.6")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("net.kyori", "org.lushplugins.followers.libraries.kyori") // Provided by PacketEvents
        relocate("com.github.retrooper.packetevents", "org.lushplugins.followers.libraries.packetevents.api")
        relocate("io.github.retrooper.packetevents", "org.lushplugins.followers.libraries.packetevents.impl")
        relocate("me.tofaa.entitylib", "org.lushplugins.followers.libraries.entitylib")
        relocate("com.mysql", "org.lushplugins.followers.libraries.mysql")
        relocate("org.lushplugins.lushlib", "org.lushplugins.followers.libraries.lushlib")

        minimize()

        val folder = System.getenv("pluginFolder_1-20-6")
        if (folder != null) destinationDirectory.set(file(folder))
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}