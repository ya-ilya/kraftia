package org.kraftia.headless.command.arguments.manifest

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.VersionDownloader

class VersionManifestArgument : ArgumentType<VersionDownloader.VersionManifest> {
    companion object {
        private val NO_SUCH_VERSION_MANIFEST = DynamicCommandExceptionType { name: Any ->
            Message { "Version manifest '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): VersionDownloader.VersionManifest {
            return context.getArgument("version", VersionDownloader.VersionManifest::class.java)
        }
    }

    override fun parse(reader: StringReader): VersionDownloader.VersionManifest {
        val argument = reader.readString()

        return VersionDownloader.versions.firstOrNull { it.id == argument }
            ?: throw NO_SUCH_VERSION_MANIFEST.create(argument)
    }
}