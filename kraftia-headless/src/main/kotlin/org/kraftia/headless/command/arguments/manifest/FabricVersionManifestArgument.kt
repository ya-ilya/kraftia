package org.kraftia.headless.command.arguments.manifest

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader

class FabricVersionManifestArgument : ArgumentType<FabricVersionDownloader.VersionManifest> {
    companion object {
        private val NO_SUCH_FABRIC_VERSION_MANIFEST = DynamicCommandExceptionType { name: Any ->
            Message { "Fabric version manifest '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): FabricVersionDownloader.VersionManifest {
            return context.getArgument("version", FabricVersionDownloader.VersionManifest::class.java)
        }
    }

    override fun parse(reader: StringReader): FabricVersionDownloader.VersionManifest {
        val argument = reader.readString()

        return FabricVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FABRIC_VERSION_MANIFEST.create(argument)
    }
}