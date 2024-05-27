package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.version.VersionArgument

object VersionCommand : AbstractCommand("version", "Manage versions") {
    init {
        builder.then(
            literal("download").then(
                argument("version", VersionArgument()).execute { context ->
                    downloaderProgress { progress ->
                        progress.withLoggingThread("VersionDownloader")
                        Api.versionDownloader.download(progress, VersionArgument[context].id!!)
                    }
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Versions:")

                for (version in VersionManager.versions) {
                    println("- ${version.id}")
                }
            }
        )
    }
}