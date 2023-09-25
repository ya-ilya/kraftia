package org.kraftia.headless.command.arguments.manifest

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader

class ForgeVersionManifestArgument : ArgumentType<ForgeVersionDownloader.VersionManifest> {
    companion object {
        private val NO_SUCH_FORGE_VERSION_MANIFEST = DynamicCommandExceptionType { name: Any ->
            Message { "Forge version manifest '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): ForgeVersionDownloader.VersionManifest {
            return context.getArgument("version", ForgeVersionDownloader.VersionManifest::class.java)
        }
    }

    override fun parse(reader: StringReader): ForgeVersionDownloader.VersionManifest {
        val argument = reader.readString()

        return ForgeVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FORGE_VERSION_MANIFEST.create(argument)
    }
}