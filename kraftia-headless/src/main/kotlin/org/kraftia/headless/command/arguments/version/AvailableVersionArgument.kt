package org.kraftia.headless.command.arguments.version

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.VersionDownloader

class AvailableVersionArgument : ArgumentType<VersionDownloader.AvailableVersion> {
    companion object {
        private val NO_SUCH_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): VersionDownloader.AvailableVersion {
            return context.getArgument("version", VersionDownloader.AvailableVersion::class.java)
        }
    }

    override fun parse(reader: StringReader): VersionDownloader.AvailableVersion {
        val argument = reader.readString()

        return VersionDownloader.versions.firstOrNull { it.id == argument }
            ?: throw NO_SUCH_VERSION.create(argument)
    }
}