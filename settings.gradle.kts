rootProject.name = "kraftia"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

include(
    "kraftia-api",
    "kraftia-headless"
)