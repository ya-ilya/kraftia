package org.kraftia.api.version.downloader.downloaders

import com.google.gson.JsonObject
import org.kraftia.api.Api
import org.kraftia.api.extensions.get
import org.kraftia.api.extensions.path
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.downloader.DownloaderProgress
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class NeoForgeVersionDownloader {
    data class AvailableVersion(
        val version: String,
        val installers: List<AvailableInstaller>
    )

    data class AvailableInstaller(
        val latest: Boolean,
        val id: String,
        val downloadUrl: String
    )

    companion object {
        private const val NEOFORGE_MAVEN_URL =
            "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge"

        val versions: List<AvailableVersion> = run {
            get<JsonObject>(NEOFORGE_MAVEN_URL)
                .getAsJsonArray("versions")
                .map { it.asString }
                .groupBy { it.slice(0..3) }
                .map { entry ->
                    AvailableVersion(
                        "1.${entry.key}",
                        entry.value.reversed().mapIndexed { index, it ->
                            AvailableInstaller(
                                index == 0,
                                it,
                                "https://maven.neoforged.net/releases/net/neoforged/neoforge/$it/neoforge-$it-installer.jar"
                            )
                        }
                    )
                }
        }
    }

    fun download(
        progress: DownloaderProgress,
        id: String,
        installerId: String? = null
    ) {
        progress.pushMessage("Downloading $id NeoForge version")

        val version = versions.first { it.version == id }

        if (version.installers.isEmpty()) {
            throw IllegalArgumentException("NeoForge version $id doesn't have installers")
        }

        val installer = if (installerId != null) {
            version.installers.first { it.id == installerId }
        } else {
            version.installers.first { it.latest }
        }

        val installerPath = path(
            Api.launcherDirectory,
            "versions",
            "neoforge-${installer.id}",
            "installer.jar"
        )

        if (!installerPath.exists()) {
            org.kraftia.api.extensions.download(
                url = installer.downloadUrl,
                path = installerPath,
                progress = progress
            )
        }

        val process = ProcessBuilder()
            .directory(Api.launcherDirectory.toFile())
            .command(
                JavaVersionManager.current?.executable ?: "java",
                "-jar",
                installerPath.absolutePathString(),
                "--installClient"
            )
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (process.waitFor() != 0) {
            progress.pushMessage("Failed to install NeoForge $id")
        }

        VersionManager.updateVersions()
    }
}