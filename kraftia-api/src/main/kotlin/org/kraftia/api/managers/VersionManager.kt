package org.kraftia.api.managers

import org.kraftia.api.Api
import org.kraftia.api.version.Version
import org.kraftia.api.version.container.VersionContainer
import java.nio.file.Paths
import kotlin.io.path.*

object VersionManager : VersionContainer {
    override val versions = mutableSetOf<Version>()

    init {
        update()
    }

    fun update() {
        versions.clear()

        for (versionDirectory in Paths.get(Api.minecraftDirectory.toString(), "versions")
            .listDirectoryEntries()
            .filter { it.isDirectory() }
        ) {
            val versionPath = Paths.get(versionDirectory.toString(), "${versionDirectory.name}.json")

            if (versionPath.exists()) {
                try {
                    addVersion(Api.GSON.fromJson(versionPath.readText(), Version::class.java))
                } catch (ex: Exception) {
                    throw RuntimeException("Failed to deserialize ${versionPath.nameWithoutExtension}", ex)
                }
            }
        }
    }
}