package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.manifest.ForgeVersionManifestArgument

object ForgeCommand : AbstractCommand("forge", "Manage forge versions") {
    init {
        builder.then(
            literal("download").then(
                argument("version", ForgeVersionManifestArgument()).execute { context ->
                    DownloaderProgress.downloaderProgress { progress ->
                        progress.withLoggingThread("VersionDownloader")

                        try {
                            Api.forgeVersionDownloader.download(progress, ForgeVersionManifestArgument[context].version)
                        } catch (ex: IllegalArgumentException) {
                            println(ex.message)
                        }
                    }
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Available forge versions:")

                for (version in ForgeVersionDownloader.versions) {
                    println("- ${version.version}")
                }
            }
        )
    }
}