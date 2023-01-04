version = "1.0-SNAPSHOT"
group = "com.kruthers.piggles"

plugins {

}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.cloud.api)
    implementation(libs.cloud.kotlin)
    implementation(libs.cloud.minecraft.paper)
    implementation(libs.cloud.minecraft.extras)

    shadow(libs.adventure.api)
    shadow(libs.adventure.mm)
}

tasks {
//    shadowJar {
//        dependencies {
//
//        }
//    }
//    build {
//        dependsOn(shadowJar)
//    }
}

