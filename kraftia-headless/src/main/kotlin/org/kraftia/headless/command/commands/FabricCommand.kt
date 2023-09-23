package org.kraftia.headless.command.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.withLoggingThread
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.manifest.FabricVersionManifestArgument

object FabricCommand : AbstractCommand("fabric", "Manage fabric versions") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(
            literal("download").then(
                argument("version", FabricVersionManifestArgument()).executesSuccess { context ->
                    downloaderProgress { progress ->
                        progress.withLoggingThread("VersionDownloader")
                        Api.fabricVersionDownloader.download(progress, FabricVersionManifestArgument[context].version!!)
                    }
                }
            )
        )
    }
}