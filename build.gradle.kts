plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version("7.1.2")
}


group = "me.dave"
version = "1.5.6-BETA"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")}
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.20-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:1.4.3")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly(files("libs/SimpleSit.jar"))
    shadow("mysql:mysql-connector-java:8.0.25")
    shadow("com.github.CoolDCB:ChatColorHandler:v2.1.3")
}

java {
    configurations.shadow.get().dependencies.remove(dependencies.gradleApi())
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.shadowJar {
    minimize()
    configurations = listOf(project.configurations.shadow.get())
    val folder = System.getenv("pluginFolder_1-20")
    if (folder != null) destinationDirectory.set(file(folder))
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

// Handles version variables
tasks.processResources {
    expand(project.properties)

    inputs.property("version", rootProject.version)
    filesMatching("plugin.yml") {
        expand("version" to rootProject.version)
    }
}