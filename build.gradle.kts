plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

group = "me.m0dii"
version = "3.0.0"

tasks.shadowJar {
    relocate("org.bstats", "me.m0dii.corecord")
    minimize()
    archiveFileName.set("M0-CoreCord-$version.jar")
}

tasks.processResources {
    filesMatching("**/*.yml") {
        expand("version" to version)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
    maven { url = uri("https://repo.extendedclip.com/releases/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven { url = uri("https://repo.essentialsx.net/snapshots/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly(files("libs/CoreProtect-22.2.jar"))
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("org.projectlombok:lombok:1.18.30")

    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("mysql:mysql-connector-java:8.0.28")

    implementation("org.bstats:bstats-bukkit:2.2.1")

//    implementation("net.dv8tion:JDA:6.0.0-rc.2") {
    implementation("net.dv8tion:JDA:5.6.1") {
        exclude(module = "opus-java")
    }

    implementation("club.minnced:discord-webhooks:0.8.0")
    implementation("com.github.ygimenez:Pagination-Utils:4.1.4b")

    // implementation("org.apache.commons:commons-compress:1.3")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
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