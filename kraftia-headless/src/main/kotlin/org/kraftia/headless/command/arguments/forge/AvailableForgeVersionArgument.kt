package org.kraftia.headless.command.arguments.forge

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader

class AvailableForgeVersionArgument : ArgumentType<ForgeVersionDownloader.Version> {
    companion object {
        private val NO_SUCH_FORGE_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Forge version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): ForgeVersionDownloader.Version {
            return context.getArgument("version", ForgeVersionDownloader.Version::class.java)
        }
    }

    override fun parse(reader: StringReader): ForgeVersionDownloader.Version {
        val argument = reader.readString()

        return ForgeVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FORGE_VERSION.create(argument)
    }
}