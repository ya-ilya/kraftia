val kraftiaVersion: String by project
val gsonVersion: String by project
val brigadierVersion: String by project
val okhttpVersion: String by project

plugins {
    kotlin("jvm")
}

group = "org.kraftia"
version = kraftiaVersion

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    api("com.google.code.gson:gson:$gsonVersion")
    api("com.mojang:brigadier:$brigadierVersion")
    api("com.squareup.okhttp3:okhttp:$okhttpVersion")
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("kraftia.json") {
            expand(
                mapOf(
                    "version" to version
                )
            )
        }
    }
}