plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.kaato"
version = "3.4-pre2"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
//    maven("https://oss.sonatype.org/content/repositories/snapshots/")
//    maven("https://oss.sonatype.org/content/groups/public/") {
//        name = "sonatype"
//    }
//    maven("https://maven.elmakers.com/repository/") {
//        name = "elmakers-repo"
//    }
    maven("https://repo.extendedclip.com/releases/")
//    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("dev.kaato:NotzAPI:0.4.9")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.mysql:mysql-connector-j:9.5.0")
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
    implementation("org.jetbrains.exposed:exposed-json:0.61.0")
//    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0-RC")
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
