package org.kraftia.headless.command.arguments.fabric

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader

class FabricVersionArgument : ArgumentType<FabricVersionDownloader.Version> {
    companion object {
        private val NO_SUCH_FABRIC_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Fabric version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): FabricVersionDownloader.Version {
            return context.getArgument("version", FabricVersionDownloader.Version::class.java)
        }
    }

    override fun parse(reader: StringReader): FabricVersionDownloader.Version {
        val argument = reader.readString()

        return FabricVersionDownloader.versions.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FABRIC_VERSION.create(argument)
    }
}