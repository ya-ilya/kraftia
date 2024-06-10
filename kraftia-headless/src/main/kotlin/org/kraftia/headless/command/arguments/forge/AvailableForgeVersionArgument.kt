package org.kraftia.headless.command.arguments.forge

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader

class AvailableForgeVersionArgument : ArgumentType<ForgeVersionDownloader.AvailableVersion> {
    companion object {
        private val NO_SUCH_FORGE_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Forge version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): ForgeVersionDownloader.AvailableVersion {
            return context.getArgument("version", ForgeVersionDownloader.AvailableVersion::class.java)
        }
    }

    override fun parse(reader: StringReader): ForgeVersionDownloader.AvailableVersion {
        val argument = reader.readString()

        return ForgeVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FORGE_VERSION.create(argument)
    }
}