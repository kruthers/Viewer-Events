plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.papermc.paperweight.userdev") version "1.3.9" apply false
}

group = "com.kruthers.piggles"
version = "1.0.0-SNAPSHOT"
description = "Plugins used for piggles' viewer events"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories{
        mavenCentral ()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }

    dependencies {
        shadow ("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
        implementation ("org.jetbrains.kotlin:kotlin-stdlib")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks {
        shadowJar {
            archiveClassifier.set("")
        }
        build {
            dependsOn(shadowJar)
        }
        processResources {
            expand("name" to project.name, "description" to project.description, "version" to project.version)
        }
//        compileKotlin {
//            kotlinOptions.languageVersion = "1.9"
//        }
    }

}
