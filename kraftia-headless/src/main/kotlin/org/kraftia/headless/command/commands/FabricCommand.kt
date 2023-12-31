package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.manifest.FabricVersionManifestArgument

object FabricCommand : AbstractCommand("fabric", "Manage fabric versions") {
    init {
        builder.then(
            literal("download").then(
                argument("version", FabricVersionManifestArgument()).execute { context ->
                    downloaderProgress { progress ->
                        progress.withLoggingThread("VersionDownloader")
                        Api.fabricVersionDownloader.download(progress, FabricVersionManifestArgument[context].version!!)
                    }
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Available fabric versions:")

                for (version in FabricVersionDownloader.versions) {
                    println("- ${version.version}")
                }
            }
        )
    }
}