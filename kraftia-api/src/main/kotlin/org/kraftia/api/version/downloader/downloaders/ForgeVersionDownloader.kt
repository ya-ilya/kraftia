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
    data class Version(val version: String) {
        val installers by lazy {
            FORGE_ENTRY_REGEX
                .findAll(get("${FORGE_URL}index_$version.html").body.string().replace("\n", ""))
                .toList()
                .mapNotNull { it.groupValues.getOrNull(1) }
                .filter { it.contains("download-version") }
                .map {
                    Installer(
                        it.contains("promo-latest"),
                        FORGE_ENTRY_ID_REGEX.find(it)!!.groupValues[1],
                        FORGE_ENTRY_DOWNLOAD_URL_REGEX.find(it)!!.groupValues[1]
                    )
                }
        }
    }

    data class Installer(
        val latest: Boolean,
        val id: String,
        val downloadUrl: String
    )

    companion object {
        private const val FORGE_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/"

        private val FORGE_ENTRY_REGEX =
            "<tr>(.*?)</tr>".toRegex()
        private val FORGE_ENTRY_ID_REGEX =
            "<td class=\"download-version\">(.*?)<".toRegex()
        private val FORGE_ENTRY_DOWNLOAD_URL_REGEX =
            "<a class=\"info-link\" data-toggle=\"popup\" href=\"(.*?)\" title=\"Direct Download\">".toRegex()

        private val FORGE_VERSION_PAGE_REGEX =
            "<a href=\"index_(.*?).html\">.*?</a>".toRegex()

        val versions: List<Version> = run {
            FORGE_VERSION_PAGE_REGEX.findAll(get(FORGE_URL).body.string()).toList()
                .map { matchResult -> matchResult.groupValues[1] }
                .map { Version(it) }
        }
    }

    fun download(
        progress: DownloaderProgress,
        id: String,
        installerId: String? = null
    ) {
        progress.pushMessage("Downloading $id forge version")

        val version = versions.first { it.version == id }

        if (version.installers.isEmpty()) {
            throw IllegalArgumentException("Forge version $id doesn't have installers")
        }

        val installer = if (installerId != null) {
            version.installers.first { it.id == installerId }
        } else {
            version.installers.first { it.latest }
        }

        val installerPath = path(
            Api.launcherDirectory,
            "forge-installer-${version.version}-${installer.id}.jar"
        )

        if (!installerPath.exists()) {
            org.kraftia.api.extensions.download(
                url = installer.downloadUrl,
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
                "--installClient"
            )
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        if (process.waitFor() != 0) {
            progress.pushMessage("Failed to install forge $id")
        }

        VersionManager.updateVersions()
    }
}