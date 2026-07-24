//import org.gradle.kotlin.dsl.assign
//import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.viaversion.com")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("net.kyori:adventure-text-serializer-legacy:5.2.0")
    
    compileOnly("com.viaversion:viaversion-api:5.11.0")
    compileOnly("me.clip:placeholderapi:2.12.3")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")
    implementation("com.mysql:mysql-connector-j:9.7.0")
    implementation("org.jetbrains.exposed:exposed-core:1.3.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.1")
    implementation("org.jetbrains.exposed:exposed-dao:1.3.1")
    implementation("org.jetbrains.exposed:exposed-java-time:1.3.1")
    implementation("org.jetbrains.exposed:exposed-json:1.3.1")
}


kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
