plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version("8.1.7")
    id("xyz.jpenilla.run-paper") version("2.3.1")
}

group = "org.lushplugins"
version = "2.1.0-beta14"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven("https://ci.ender.zone/plugin/repository/everything/") // Essentials
    maven("https://repo.papermc.io/repository/maven-public/") // PaperLib (for Essentials)
    maven("https://repo.opencollab.dev/main/") // Floodgate
    maven("https://repo.lushplugins.org/snapshots/") // LushLib
    maven("https://repo.inventivetalent.org/repository/public/") // MineSkin
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://maven.evokegames.gg/snapshots") // EntityLib
    maven("https://jitpack.io") // GSit
}

dependencies {
    // Dependencies
    compileOnly("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.mysql:mysql-connector-j:8.3.0") // Uses Spigot copy
    compileOnlyApi("com.github.retrooper:packetevents-spigot:2.10.0")

    // Soft Dependencies
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:2.4.3")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(files("libs/SimpleSit.jar"))

    // Libraries
    api("me.tofaa.entitylib:spigot:+fe61616-SNAPSHOT")
    implementation("org.lushplugins:LushLib:0.10.82")
    implementation("org.mineskin:java-client:3.1.0-SNAPSHOT")
    implementation("org.mineskin:java-client-jsoup:3.1.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("me.tofaa.entitylib", "org.lushplugins.followers.libraries.entitylib")
        relocate("org.lushplugins.lushlib", "org.lushplugins.followers.libraries.lushlib")

        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21.7")

        downloadPlugins {
//            modrinth("packetevents", "2.8.0")
            modrinth("viaversion", "5.2.2-SNAPSHOT+662")
            modrinth("viabackwards", "5.2.2-SNAPSHOT+380")
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }

    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
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