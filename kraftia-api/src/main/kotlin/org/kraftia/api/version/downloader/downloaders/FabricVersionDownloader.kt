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
    data class AvailableVersion(
        val version: String,
        val stable: Boolean
    )

    data class AvailableInstaller(
        val url: String,
        val version: String,
        val stable: Boolean
    )

    data class AvailableLoader(
        val version: String,
        val stable: Boolean
    )

    companion object {
        private const val MANIFEST_URL = "https://meta.fabricmc.net/v2/versions/game"
        private const val INSTALLERS_URL = "https://meta.fabricmc.net/v2/versions/installer"
        private const val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"

        val versions: List<AvailableVersion> = run {
            get<JsonArray>(MANIFEST_URL)
                .map { fromJson<AvailableVersion>(it) }
        }

        val installers: List<AvailableInstaller> = run {
            get<JsonArray>(INSTALLERS_URL)
                .map { fromJson<AvailableInstaller>(it) }
        }

        val loaders: List<AvailableLoader> = run {
            get<JsonArray>(LOADERS_URL)
                .map { fromJson<AvailableLoader>(it) }
        }
    }

    fun download(
        progress: DownloaderProgress,
        id: String,
        loaderVersion: String? = null
    ) {
        progress.pushMessage("Downloading $id fabric version")

        val installer = installers.first { it.stable }

        val installerPath = path(
            Api.launcherDirectory,
            "fabric-installer-${installer.version}.jar"
        )

        if (!installerPath.exists()) {
            org.kraftia.api.extensions.download(
                url = installer.url,
                path = installerPath,
                progress = progress
            )
        }

        val loader = if (loaderVersion != null) {
            loaders.first { it.version == loaderVersion }
        } else {
            loaders.first { it.stable }
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
                id,
                "-loader",
                loader.version
            )
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (process.waitFor() != 0) {
            progress.pushMessage("Failed to install fabric $id")
        }

        VersionManager.updateVersions()
    }
}