plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version("8.1.7")
}


group = "org.lushplugins"
version = "2.0.0-beta8"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven("https://ci.ender.zone/plugin/repository/everything/") // Essentials
    maven("https://repo.opencollab.dev/main/") // Floodgate
    maven("https://repo.lushplugins.org/snapshots/") // LushLib
    maven("https://repo.inventivetalent.org/repository/public/") // MineSkin
    maven("https://repo.codemc.io/repository/maven-snapshots/") // PacketEvents
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // EntityLib, GSit
}

dependencies {
    // Dependencies
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnlyApi("com.github.retrooper:packetevents-spigot:2.3.1-20240624.151351-33")

    // Soft Dependencies
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.1")
    compileOnly("com.github.Gecolay.GSit:core:1.9.0")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly(files("libs/SimpleSit.jar"))

    // Libraries
    api("com.github.OakLoaf:EntityLib:aef1wrf")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.lushplugins:LushLib:0.7.6")
    implementation("org.mineskin:java-client:1.2.4-SNAPSHOT")
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
        relocate("com.mysql", "org.lushplugins.followers.libraries.mysql")
        relocate("org.lushplugins.lushlib", "org.lushplugins.followers.libraries.lushlib")

        minimize {
            exclude(dependency("com.mysql:.*:.*"))
        }

        val folder = System.getenv("pluginFolder_1-21")
        if (folder != null) {
            destinationDirectory.set(file(folder))
        }

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