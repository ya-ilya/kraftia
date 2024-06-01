package org.kraftia.api.managers

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.kraftia.api.Api
import org.kraftia.api.extensions.download
import org.kraftia.api.extensions.get
import org.kraftia.api.extensions.path
import org.kraftia.api.instance.Instance
import org.kraftia.api.instance.container.InstanceContainer
import org.kraftia.api.version.Version
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile
import kotlin.io.path.*

object InstanceManager : InstanceContainer {
    override val instances = mutableSetOf<Instance>()

    /**
     * By using this key in your builds you accept the terms and conditions laid down in
     * https://support.curseforge.com/en/support/solutions/articles/9000207405-curse-forge-3rd-party-api-terms-and-conditions
     * NOTE: CurseForge requires you to change this if you make any kind of derivative work.
     * This key was issued specifically for kraftia launcher
     */
    private const val CURSEFORGE_TOKEN = "\$2a\$10\$eLRUUPRkJ4x/2zH.y8LH3O.8M50SPYFhbU5T5MtAc9KcIVHlwXJc6"

    fun createVersionInstance(
        name: String,
        version: Version,
        gameDirectory: Path,
    ): Instance {
        gameDirectory.createDirectories()

        downloaderProgress { progress ->
            progress.withLoggingThread("VersionDownloader")
            Api.versionDownloader.download(progress, version)
        }

        return Instance(name, version.id!!, gameDirectory.absolutePathString()).also { addInstance(it) }
    }

    fun createInstanceFromModpackFile(
        name: String,
        gameDirectory: Path,
        modpackPath: Path
    ): Instance {
        gameDirectory.createDirectories()

        return ZipFile(modpackPath.toFile()).use { zip ->
            val manifest = Api.GSON.fromJson(
                zip.getInputStream(zip.getEntry("manifest.json")).reader().readText(),
                Manifest::class.java
            )

            if (manifest.manifestType != "minecraftModpack") {
                throw IllegalArgumentException()
            }

            downloaderProgress { progress ->
                progress.withLoggingThread("VersionDownloader")
                Api.versionDownloader.download(progress, manifest.minecraft.version)
            }

            val modLoaderId = manifest.minecraft.modLoaders.first { it.primary }.id
            var versionId = manifest.minecraft.version

            when {
                modLoaderId.startsWith("forge-") -> {
                    versionId = "${manifest.minecraft.version}-${modLoaderId}"

                    downloaderProgress { progress ->
                        progress.withLoggingThread("ForgeVersionDownloader")
                        Api.forgeVersionDownloader.download(
                            progress,
                            manifest.minecraft.version,
                            modLoaderId.removePrefix("forge-")
                        )
                    }
                }

                modLoaderId.startsWith("fabric-") -> {
                    versionId = "fabric-loader-${modLoaderId.removePrefix("fabric-")}-${manifest.minecraft.version}"

                    downloaderProgress { progress ->
                        progress.withLoggingThread("FabricVersionDownloader")
                        Api.fabricVersionDownloader.download(
                            progress,
                            manifest.minecraft.version,
                            modLoaderId.removePrefix("fabric-")
                        )
                    }
                }
            }

            val modsPath = path(gameDirectory, "mods")
            val resourcePacksPath = path(gameDirectory, "resourcepacks")

            downloaderProgress { progress ->
                progress.withLoggingThread("ModpackFilesDownloader")
                for (file in manifest.files) {
                    val fileInfo = get<JsonObject>(
                        url = "https://api.curseforge.com/v1/mods/${file.projectId}/files/${file.fileId}",
                        headers = mapOf(
                            "x-api-key" to CURSEFORGE_TOKEN
                        )
                    ).getAsJsonObject("data")

                    val fileName = fileInfo.getAsJsonPrimitive("fileName").asString
                    val filePath = when {
                        fileName.endsWith(".zip") -> path(resourcePacksPath, fileName)
                        fileName.endsWith(".jar") -> path(modsPath, fileName)
                        else -> throw IllegalArgumentException()
                    }

                    if (!filePath.exists()) {
                        download(
                            url = fileInfo.getAsJsonPrimitive("downloadUrl").asString,
                            path = filePath,
                            headers = mapOf(
                                "x-api-key" to CURSEFORGE_TOKEN
                            ),
                            progress = progress
                        )
                    }
                }
            }

            for (entry in zip.entries().toList().filter { it.name.startsWith(manifest.overrides) }) {
                val overridePath = path(gameDirectory, entry.name.removePrefix("${manifest.overrides}/"))

                if (overridePath.isDirectory() && !overridePath.exists()) {
                    overridePath.createDirectory()
                }

                if (!entry.isDirectory) {
                    overridePath.createParentDirectories()
                    overridePath.writeBytes(zip.getInputStream(entry).readBytes(), StandardOpenOption.CREATE)
                }
            }

            Instance(name, versionId, gameDirectory.absolutePathString()).also { addInstance(it) }
        }
    }

    data class Manifest(
        val minecraft: Minecraft,
        val manifestType: String,
        val manifestVersion: Long,
        val name: String,
        val version: String,
        val author: String,
        val files: List<File>,
        val overrides: String,
    )

    data class Minecraft(
        val version: String,
        val modLoaders: List<ModLoader>,
    )

    data class ModLoader(
        val id: String,
        val primary: Boolean,
    )

    data class File(
        @SerializedName("projectID")
        val projectId: Long,
        @SerializedName("fileID")
        val fileId: Long,
        val required: Boolean,
    )
}