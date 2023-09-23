package org.kraftia.api.version.downloader.downloaders

import com.google.gson.JsonObject
import org.kraftia.api.Api
import org.kraftia.api.extensions.*
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.Arguments
import org.kraftia.api.version.Version
import org.kraftia.api.version.downloader.DownloaderProgress
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.jar.JarFile
import kotlin.io.path.*

@Suppress("MemberVisibilityCanBePrivate")
class VersionDownloader(
    val operatingSystem: Api.OperatingSystem,
    val versionsDirectory: Path,
    val librariesDirectory: Path,
    val binDirectory: Path,
    val assetsDirectory: Path
) {
    data class VersionManifest(
        var id: String? = null,
        var type: String? = null,
        var url: String? = null,
        var time: String? = null,
        var releaseTime: String? = null,
        var sha1: String? = null,
        var complianceLevel: Int? = null
    )

    data class Result(
        val version: Version,
        val classpath: List<Path>,
        val versionBinDirectory: Path
    ) {
        init {
            VersionManager.update()
        }
    }

    companion object {
        private const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
        private const val ASSETS_URL = "https://resources.download.minecraft.net"

        val versions: List<VersionManifest> = run {
            val response = get(MANIFEST_URL).body.string()
            val result = mutableListOf<VersionManifest>()

            for (version in fromJson<JsonObject>(response).getAsJsonArray("versions")) {
                result.add(
                    fromJson<VersionManifest>(version)
                )
            }

            result
        }
    }

    init {
        librariesDirectory.createDirectories()
        binDirectory.createDirectories()
        assetsDirectory.createDirectories()
    }

    fun download(
        progress: DownloaderProgress,
        id: String
    ): Result {
        return download(
            progress,
            downloadVersion(progress, id)
        )
    }

    fun download(
        progress: DownloaderProgress,
        version: Version
    ): Result {
        if (version.inheritsFrom != null) {
            val parent = downloadVersion(
                progress,
                version.inheritsFrom!!
            )

            val parentBinDirectory = path(binDirectory, parent.id)

            val classpath = download(
                progress,
                parent
            ).classpath.toMutableList()

            progress.pushMessage("Downloading ${version.id} version")

            for (library in downloadLibraries(progress, version, parentBinDirectory)) {
                classpath.add(library)
            }

            progress.pushMessage("Download successful")

            return Result(
                parent.copy(
                    id = version.id,
                    time = version.time,
                    releaseTime = version.releaseTime,
                    type = version.type,
                    mainClass = version.mainClass,
                    logging = version.logging,
                    arguments = Arguments(
                        parent.arguments!!.game + version.arguments!!.game,
                        parent.arguments!!.jvm + version.arguments!!.jvm
                    ),
                    libraries = parent.libraries + version.libraries
                ),
                classpath,
                parentBinDirectory
            )
        }

        progress.pushMessage("Downloading ${version.id} version")

        val versionDirectory = path(versionsDirectory, version.id)
        val versionBinDirectory = path(binDirectory, version.id)
        val versionPath = path(versionDirectory, "${version.id}.json")

        if (!versionPath.exists()) {
            throw IllegalArgumentException("Version not found")
        }

        val classpath = mutableListOf<Path>()

        for (library in downloadLibraries(progress, version, versionBinDirectory)) {
            classpath.add(library)
        }

        classpath.add(
            downloadGameJar(progress, version)
        )

        downloadAssets(progress, version)

        progress.pushMessage("Download successful")

        return Result(version, classpath, versionBinDirectory)
    }

    private fun downloadVersion(
        progress: DownloaderProgress,
        id: String
    ): Version {
        val versionManifest = versions.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Version $id not found in manifest")

        val versionDirectory = path(versionsDirectory, id)
        val versionPath = path(versionDirectory, "$id.json")

        if (versionPath.needToDownload(versionManifest.sha1)) {
            download(
                url = versionManifest.url!!,
                path = versionPath,
                progress = progress
            )
        }

        return VersionManager.getVersionByIdOrNull(id)
            ?: fromJson<Version>(versionPath.readText())
    }

    private fun downloadLibraries(
        progress: DownloaderProgress,
        version: Version,
        versionBinDirectory: Path
    ): List<Path> {
        val classpath = mutableListOf<Path>()

        versionBinDirectory.createDirectories()
        progress.pushMessage("Downloading libraries...")

        for (library in version.libraries) {
            if (!library.rules.checkRules(operatingSystem, emptyMap())) continue
            if (library.downloads == null && library.name != null) {
                val (libraryGroup, libraryModule, libraryVersion) = library.name!!.split(":")

                classpath.add(
                    path(
                        librariesDirectory,
                        *libraryGroup.split(".").toTypedArray(),
                        libraryModule,
                        libraryVersion,
                        "$libraryModule-$libraryVersion.jar"
                    )
                )

                continue
            }

            val artifact = library.downloads!!.artifact
            val nativeArtifact = library.downloads!!.classifiers[
                library.natives[operatingSystem.toString()]
            ]

            if (artifact != null) {
                val libraryPath = path(
                    librariesDirectory,
                    artifact.path!!.replace("/", File.separator)
                )

                if (libraryPath.needToDownload(artifact.sha1)) {
                    download(
                        url = artifact.url!!,
                        path = libraryPath,
                        progress = progress
                    )
                }

                classpath.add(libraryPath)
            }

            if (nativeArtifact != null) {
                val nativeLibraryPath = path(
                    librariesDirectory,
                    nativeArtifact.path!!.replace("/", File.separator)
                )

                if (nativeLibraryPath.needToDownload(nativeArtifact.sha1)) {
                    download(
                        url = nativeArtifact.url!!,
                        path = nativeLibraryPath,
                        progress = progress
                    )
                }

                val jar = JarFile(nativeLibraryPath.toFile())

                for (entry in jar.entries()) {
                    if (entry.isDirectory || entry.name.contains("MANIFEST")) continue

                    val nativePath = path(
                        versionBinDirectory,
                        entry.name
                    )

                    nativePath.createParentDirectories()
                    nativePath.writeBytes(
                        jar.getInputStream(entry).readBytes(),
                        StandardOpenOption.CREATE
                    )
                }

                jar.close()
            }
        }

        return classpath
    }

    private fun downloadGameJar(
        progress: DownloaderProgress,
        version: Version
    ): Path {
        progress.pushMessage("Downloading game jar...")

        val versionDirectory = path(versionsDirectory, version.id)
        val gameJarPath = path(versionDirectory, "${version.id}.jar")

        if (gameJarPath.needToDownload(version.downloads!!.client!!.sha1)) {
            download(
                url = version.downloads!!.client!!.url!!,
                path = gameJarPath,
                progress = progress
            )
        }

        return gameJarPath
    }

    private fun downloadAssets(
        progress: DownloaderProgress,
        version: Version
    ) {
        val indexPath = path(
            assetsDirectory,
            "indexes",
            "${version.assetIndex!!.id}.json"
        )

        if (indexPath.needToDownload(version.assetIndex!!.sha1)) {
            download(
                url = version.assetIndex!!.url!!,
                path = indexPath,
                progress = progress
            )
        }

        val objects = fromJson<JsonObject>(indexPath.readText()).getAsJsonObject("objects")

        progress.pushMessage("Downloading assets...")

        for ((name, assetInfo) in objects.entrySet()) {
            val assetHash = assetInfo.asJsonObject.getAsJsonPrimitive("hash").asString
            val assetPrefix = assetHash.substring(0, 2)
            val assetPath = path(
                assetsDirectory,
                "objects",
                assetPrefix,
                assetHash
            )

            if (assetPath.needToDownload(assetHash)) {
                download(
                    url = "$ASSETS_URL/$assetPrefix/$assetHash",
                    path = assetPath,
                    name = name,
                    progress = progress
                )
            }
        }
    }
}