package org.kraftia.headless.command.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.api.Api
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.withLoggingThread
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.manifest.VersionManifestArgument

object VersionCommand : AbstractCommand("version", "Manage versions") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(
            literal("download").then(
                argument("version", VersionManifestArgument()).executesSuccess { context ->
                    downloaderProgress { progress ->
                        progress.withLoggingThread("VersionDownloader")
                        Api.versionDownloader.download(progress, VersionManifestArgument[context].id!!)
                    }
                }
            )
        )

        builder.then(
            literal("list").executesSuccess {
                println("Versions:")

                for (version in VersionManager.versions) {
                    println("- ${version.id}")
                }
            }
        )
    }
}