package org.kraftia.headless.command.arguments.neoforge

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.NeoForgeVersionDownloader

class AvailableNeoForgeInstallerArgument : ArgumentType<String> {
    companion object {
        private val NO_SUCH_NEOFORGE_INSTALLER = DynamicCommandExceptionType { name: Any ->
            Message { "NeoForge installer '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): NeoForgeVersionDownloader.AvailableInstaller {
            val argument = StringArgumentType.getString(context, "installer")
            val version = AvailableNeoForgeVersionArgument[context]

            return version.installers.firstOrNull { it.id == argument }
                ?: throw NO_SUCH_NEOFORGE_INSTALLER.create(argument)
        }
    }

    override fun parse(reader: StringReader): String {
        return reader.readString()
    }
}