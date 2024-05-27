package org.kraftia.headless.command.arguments.fabric

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader

class FabricLoaderArgument : ArgumentType<FabricVersionDownloader.Loader> {
    companion object {
        private val NO_SUCH_FABRIC_LOADER = DynamicCommandExceptionType { name: Any ->
            Message { "Fabric loader '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): FabricVersionDownloader.Loader {
            return context.getArgument("loader", FabricVersionDownloader.Loader::class.java)
        }
    }

    override fun parse(reader: StringReader): FabricVersionDownloader.Loader {
        val argument = reader.readString()

        return FabricVersionDownloader.loaders.firstOrNull { it.version == argument }
            ?: throw NO_SUCH_FABRIC_LOADER.create(argument)
    }
}