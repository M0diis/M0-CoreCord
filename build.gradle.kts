plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

group = "me.m0dii"
version = "3.0.1"

tasks.shadowJar {
    relocate("org.bstats", "me.m0dii.corecord")
    minimize()
    archiveFileName.set("M0-CoreCord-$version.jar")
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("**/*.yml") {
        expand("version" to version)
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    listOf(
        "https://repo.papermc.io/repository/maven-public/",
        "https://jitpack.io/",
        "https://repo.extendedclip.com/content/repositories/placeholderapi/",
        "https://repo.extendedclip.com/content/repositories/",
        "https://ci.ender.zone/plugin/repository/everything/",
        "https://repo.essentialsx.net/content/repositories/snapshots/",
        "https://repo.essentialsx.net/content/repositories/releases/",
        "https://maven.enginehub.org/repo/",
        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
    ).forEach { maven { url = uri(it) } }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(files("libs/CoreProtect-CE-23.1.jar"))
    compileOnly("me.clip:placeholderapi:2.12.2")

    compileOnly("org.projectlombok:lombok:1.18.46")

    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("com.mysql:mysql-connector-j:8.4.0")

    implementation("org.bstats:bstats-bukkit:2.2.1")

    implementation("net.dv8tion:JDA:5.6.1") {
        exclude(module = "opus-java")
    }

    implementation("club.minnced:discord-webhooks:0.8.0")
    implementation("com.github.ygimenez:Pagination-Utils:4.1.4b")

    annotationProcessor("org.projectlombok:lombok:1.18.46")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        downloadPlugins {
            modrinth("viaversion", "5.5.0-SNAPSHOT+793")
            modrinth("viabackwards", "5.4.2")
            modrinth("luckperms", "v5.5.0-bukkit")
            modrinth("coreprotect", "22.4")
            url("https://www.spigotmc.org/resources/placeholderapi.6245/download?version=541946") // 2.11.6
        }
        minecraftVersion("1.21.8")
    }
}