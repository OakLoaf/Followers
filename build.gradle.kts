import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("9.3.1")
    id("xyz.jpenilla.run-paper") version("3.1.0")
}

group = "org.lushplugins"
version = "3.0.0-alpha.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://ci.ender.zone/plugin/repository/everything/") // Essentials
    maven("https://repo.papermc.io/repository/maven-public/") // PaperLib (for Essentials)
    maven("https://repo.opencollab.dev/main/") // Floodgate
    maven("https://repo.lushplugins.org/snapshots/") // LushLib
    maven("https://repo.inventivetalent.org/repository/public/") // MineSkin
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://maven.pvphub.me/tofaa") // EntityLib
    maven("https://jitpack.io") // GSit
}

dependencies {
    // Dependencies
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("com.mysql:mysql-connector-j:8.3.0") // Provided by Paper
    compileOnlyApi("com.github.retrooper:packetevents-spigot:2.8.0")

    // Soft Dependencies
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:2.4.3")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(files("libs/SimpleSit.jar"))

    // Libraries
    api("io.github.tofaa2:spigot:4.0.0-alpha.1")
    implementation("org.lushplugins:LushLib:0.10.82")
    implementation("org.mineskin:java-client:3.1.0-SNAPSHOT")
    implementation("org.mineskin:java-client-jsoup:3.1.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        enableAutoRelocation = true
        relocationPrefix = "org.lushplugins.followers.libraries"

        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        inputs.property("version", rootProject.version)
        inputs.property("commit", getCurrentCommitHash())

        filesMatching("plugin.yml") {
            expand(
                "version" to rootProject.version,
                "commit" to getCurrentCommitHash()
            )
        }
    }

    runServer {
        minecraftVersion("26.2")

        downloadPlugins {
            modrinth("packetevents", "2.14.0+spigot")
            modrinth("viaversion", "5.12.1")
            modrinth("viabackwards", "5.12.1")
            hangar("PlaceholderAPI", "2.12.3")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "lushReleases"
            url = uri("https://repo.lushplugins.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "lushSnapshots"
            url = uri("https://repo.lushplugins.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}

fun getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash ?: ""
    } else {
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}