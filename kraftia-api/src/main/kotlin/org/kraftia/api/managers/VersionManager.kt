package org.kraftia.api.managers

import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import org.kraftia.api.version.Version
import org.kraftia.api.version.container.VersionContainer
import kotlin.io.path.*

object VersionManager : VersionContainer {
    override val versions = mutableSetOf<Version>()

    init {
        updateVersions()
    }

    override fun updateVersions() {
        versions.clear()

        for (versionDirectory in path(Api.minecraftDirectory, "versions")
            .listDirectoryEntries()
            .filter { it.isDirectory() }
        ) {
            val versionPath = versionDirectory
                .listDirectoryEntries()
                .firstOrNull { it.extension == "json" }

            if (versionPath != null && versionPath.exists()) {
                try {
                    addVersion(Api.GSON.fromJson(versionPath.readText(), Version::class.java))
                } catch (ex: Exception) {
                    throw RuntimeException("Failed to deserialize ${versionPath.nameWithoutExtension}", ex)
                }
            }
        }
    }
}