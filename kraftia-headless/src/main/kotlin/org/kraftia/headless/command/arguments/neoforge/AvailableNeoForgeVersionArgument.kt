package org.kraftia.headless.command.arguments.neoforge

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.NeoForgeVersionDownloader

class AvailableNeoForgeVersionArgument : ArgumentType<NeoForgeVersionDownloader.AvailableVersion> {
    companion object {
        private val NO_SUCH_NEOFORGE_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "NeoForge version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): NeoForgeVersionDownloader.AvailableVersion {
            return context.getArgument("version", NeoForgeVersionDownloader.AvailableVersion::class.java)
        }
    }

    override fun parse(reader: StringReader): NeoForgeVersionDownloader.AvailableVersion {
        val argument = reader.readString()

        return NeoForgeVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_NEOFORGE_VERSION.create(argument)
    }
}