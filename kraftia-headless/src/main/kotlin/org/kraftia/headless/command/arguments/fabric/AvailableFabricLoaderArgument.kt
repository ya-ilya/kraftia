package org.kraftia.headless.command.arguments.fabric

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader

class AvailableFabricLoaderArgument : ArgumentType<FabricVersionDownloader.AvailableLoader> {
    companion object {
        private val NO_SUCH_FABRIC_LOADER = DynamicCommandExceptionType { name: Any ->
            Message { "Fabric loader '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): FabricVersionDownloader.AvailableLoader {
            return context.getArgument("loader", FabricVersionDownloader.AvailableLoader::class.java)
        }
    }

    override fun parse(reader: StringReader): FabricVersionDownloader.AvailableLoader {
        val argument = reader.readString()

        return FabricVersionDownloader.loaders.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FABRIC_LOADER.create(argument)
    }
}