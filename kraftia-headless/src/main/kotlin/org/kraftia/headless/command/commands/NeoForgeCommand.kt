package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.downloaders.NeoForgeVersionDownloader
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.neoforge.AvailableNeoForgeInstallerArgument
import org.kraftia.headless.command.arguments.neoforge.AvailableNeoForgeVersionArgument

object NeoForgeCommand : AbstractCommand("neoforge", "Manage NeoForge versions") {
    init {
        builder.then(
            literal("installers").then(
                argument("version", AvailableNeoForgeVersionArgument()).execute { context ->
                    val version = AvailableNeoForgeVersionArgument[context]

                    println("Available ${version.version} NeoForge installers:")

                    for (installer in version.installers) {
                        println("- ${installer.id}${if (installer.latest) " [Latest]" else ""}")
                    }
                }
            )
        )

        builder.then(
            literal("download").then(
                argument("version", AvailableNeoForgeVersionArgument())
                    .then(
                        argument("installer", AvailableNeoForgeInstallerArgument()).execute { context ->
                            val version = AvailableNeoForgeVersionArgument[context]
                            val installer = AvailableNeoForgeInstallerArgument[context]

                            downloaderProgress { progress ->
                                progress.withLoggingThread("NeoForgeVersionDownloader")
                                Api.neoForgeVersionDownloader.download(
                                    progress,
                                    id = version.version,
                                    installerId = installer.id
                                )
                            }
                        }
                    )
                    .execute { context ->
                        downloaderProgress { progress ->
                            progress.withLoggingThread("NeoForgeVersionDownloader")
                            Api.neoForgeVersionDownloader.download(
                                progress,
                                id = AvailableNeoForgeVersionArgument[context].version
                            )
                        }
                    }
            )
        )

        builder.then(
            literal("list").execute {
                println("Available NeoForge versions:")

                for (version in NeoForgeVersionDownloader.versions) {
                    println("- ${version.version}")
                }
            }
        )
    }
}