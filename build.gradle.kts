plugins {
    java
    `maven-publish`
    id("io.github.goooler.shadow") version("8.1.7")
}


group = "me.dave"
version = "1.6.6-BETA"

repositories {
    mavenCentral()
    mavenLocal()
    maven(url="https://oss.sonatype.org/content/groups/public/")
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven(url="https://ci.ender.zone/plugin/repository/everything/") // Essentials
    maven(url="https://repo.opencollab.dev/main/") // Floodgate
    maven(url="https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven(url="https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven(url="https://jitpack.io") // ChatColorHandler
}

dependencies {
    // Dependencies
    compileOnly("org.spigotmc:spigot:1.20-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    // Soft Dependencies
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:1.9.1")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly(files("libs/SimpleSit.jar"))

    // Libraries
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("com.github.CoolDCB:ChatColorHandler:v2.1.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("com.mysql", "me.dave.followers.libraries.mysql")
        relocate("me.dave.chatcolorhandler", "me.dave.followers.libraries.chatcolor")

        minimize()

        val folder = System.getenv("pluginFolder_1-20")
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