package org.kraftia.api.version.downloader.downloaders

import com.google.gson.JsonArray
import org.kraftia.api.Api
import org.kraftia.api.extensions.fromJson
import org.kraftia.api.extensions.get
import org.kraftia.api.extensions.path
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.downloader.DownloaderProgress
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class FabricVersionDownloader {
    data class VersionManifest(
        val version: String? = null,
        val stable: Boolean? = null
    )

    data class Installer(
        val url: String? = null,
        val version: String? = null
    )

    companion object {
        private const val MANIFEST_URL = "https://meta.fabricmc.net/v2/versions/game"
        private const val INSTALLERS_URL = "https://meta.fabricmc.net/v2/versions/installer"

        val versions: List<VersionManifest> = run {
            val response = get(MANIFEST_URL).body.string()
            val result = mutableListOf<VersionManifest>()

            for (version in fromJson<JsonArray>(response)) {
                result.add(
                    fromJson<VersionManifest>(version)
                )
            }

            result
        }

        val installer: Installer = run {
            val response = get(INSTALLERS_URL).body.string()

            fromJson<Installer>(
                fromJson<JsonArray>(response).first()
            )
        }
    }

    fun download(
        progress: DownloaderProgress,
        id: String
    ) {
        progress.pushMessage("Downloading $id fabric version")

        val installerPath = path(
            Api.launcherDirectory,
            "fabric-installer.jar"
        )

        if (!installerPath.exists()) {
            org.kraftia.api.extensions.download(
                url = installer.url!!,
                path = installerPath,
                progress = progress
            )
        }

        val process = ProcessBuilder()
            .directory(Api.minecraftDirectory.toFile())
            .command(
                JavaVersionManager.current?.executable ?: "java",
                "-jar",
                installerPath.absolutePathString(),
                "client",
                "-dir",
                Api.minecraftDirectory.absolutePathString(),
                "-mcversion",
                id
            )
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (process.waitFor() != 0) {
            progress.pushMessage("Failed to install fabric $id")
        }

        VersionManager.update()
    }
}