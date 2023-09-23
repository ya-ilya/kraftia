val kraftiaVersion: String by project

plugins {
    kotlin("jvm")
    application
}

group = "org.kraftia"
version = kraftiaVersion

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation(project(":kraftia-api"))
}

application {
    mainClass.set("org.kraftia.headless.KraftiaKt")
}

tasks {
    withType<JavaExec> {
        standardInput = System.`in`
        standardOutput = System.out
    }

    jar {
        manifest.attributes(
            "Main-Class" to "org.kraftia.headless.KraftiaKt"
        )

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        configurations["compileClasspath"].forEach {
            from(zipTree(it.absoluteFile))
        }
    }
}