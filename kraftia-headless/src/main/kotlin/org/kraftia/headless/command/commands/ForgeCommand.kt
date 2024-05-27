package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.forge.ForgeInstallerArgument
import org.kraftia.headless.command.arguments.forge.ForgeVersionArgument

object ForgeCommand : AbstractCommand("forge", "Manage forge versions") {
    init {
        builder.then(
            literal("installers").then(
                argument("version", ForgeVersionArgument()).execute { context ->
                    val version = ForgeVersionArgument[context]

                    println("Available ${version.version} forge installers:")

                    for (installer in version.installers) {
                        println("- ${installer.id}${if (installer.latest) " [Latest]" else ""}")
                    }
                }
            )
        )

        builder.then(
            literal("download").then(
                argument("version", ForgeVersionArgument())
                    .then(
                        argument("installer", ForgeInstallerArgument()).execute { context ->
                            val version = ForgeVersionArgument[context]
                            val installer = ForgeInstallerArgument[context]

                            downloaderProgress { progress ->
                                progress.withLoggingThread("VersionDownloader")
                                Api.forgeVersionDownloader.download(
                                    progress,
                                    id = version.version,
                                    installerId = installer.id
                                )
                            }
                        }
                    )
                    .execute { context ->
                        downloaderProgress { progress ->
                            progress.withLoggingThread("VersionDownloader")
                            Api.forgeVersionDownloader.download(
                                progress,
                                id = ForgeVersionArgument[context].version
                            )
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