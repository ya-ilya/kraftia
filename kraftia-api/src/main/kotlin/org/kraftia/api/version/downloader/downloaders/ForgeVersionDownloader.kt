package org.kraftia.api.version.downloader.downloaders

import org.kraftia.api.Api
import org.kraftia.api.extensions.get
import org.kraftia.api.extensions.path
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.downloader.DownloaderProgress
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class ForgeVersionDownloader {
    data class VersionManifest(val version: String) {
        val installerUrl by lazy {
            FORGE_VERSION_INSTALLER_REGEX
                .findAll(get("${FORGE_URL}index_$version.html").body.string())
                .lastOrNull()
                ?.groupValues
                ?.getOrNull(1)
        }
    }

    companion object {
        private const val FORGE_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/"

        private val FORGE_VERSION_PAGE_REGEX = "<a href=\"index_(.*?).html\">.*?</a>".toRegex()
        private val FORGE_VERSION_INSTALLER_REGEX = "<a href=\"(.*?)\" title=\"Installer\">".toRegex()

        val versions: List<VersionManifest> = run {
            FORGE_VERSION_PAGE_REGEX.findAll(get(FORGE_URL).body.string()).toList()
                .map { matchResult -> matchResult.groupValues[1] }
                .map { VersionManifest(it) }
        }
    }

    fun download(
        progress: DownloaderProgress,
        id: String
    ) {
        progress.pushMessage("Downloading $id forge version")

        val version = versions.first { it.version == id }

        if (version.installerUrl == null) {
            throw IllegalArgumentException("Forge version $id doesn't have installer")
        }

        val installerPath = path(
            Api.launcherDirectory,
            "forge-installer-${version.version}.jar"
        )

        if (!installerPath.exists()) {
            org.kraftia.api.extensions.download(
                url = version.installerUrl!!,
                path = installerPath,
                progress = progress
            )
        }

        val process = ProcessBuilder()
            .directory(Api.minecraftDirectory.toFile())
            .command(
                JavaVersionManager.current?.executable ?: "java",
                "-jar",
                installerPath.absolutePathString()
            )
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (process.waitFor() != 0) {
            progress.pushMessage("Failed to install forge $id")
        }

        VersionManager.update()
    }
}