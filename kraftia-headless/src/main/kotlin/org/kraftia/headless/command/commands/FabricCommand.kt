package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.fabric.AvailableFabricLoaderArgument
import org.kraftia.headless.command.arguments.fabric.AvailableFabricVersionArgument

object FabricCommand : AbstractCommand("fabric", "Manage fabric versions") {
    init {
        builder.then(
            literal("loaders").execute {
                println("Available fabric loaders:")

                for (loader in FabricVersionDownloader.loaders) {
                    println("- ${loader.version}${if (loader.stable) " [Stable]" else ""}")
                }
            }
        )

        builder.then(
            literal("download").then(
                argument("version", AvailableFabricVersionArgument())
                    .then(
                        argument("loader", AvailableFabricLoaderArgument()).execute { context ->
                            val version = AvailableFabricVersionArgument[context].version
                            val loader = AvailableFabricLoaderArgument[context].version

                            downloaderProgress { progress ->
                                progress.withLoggingThread("VersionDownloader")
                                Api.fabricVersionDownloader.download(
                                    progress,
                                    id = version,
                                    loaderVersion = loader
                                )
                            }
                        }
                    )
                    .execute { context ->
                        downloaderProgress { progress ->
                            progress.withLoggingThread("VersionDownloader")
                            Api.fabricVersionDownloader.download(
                                progress,
                                id = AvailableFabricVersionArgument[context].version
                            )
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