package org.kraftia.headless.command.arguments.forge

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader

class ForgeInstallerArgument : ArgumentType<String> {
    companion object {
        private val NO_SUCH_FORGE_INSTALLER = DynamicCommandExceptionType { name: Any ->
            Message { "Forge installer '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): ForgeVersionDownloader.Installer {
            val argument = StringArgumentType.getString(context, "installer")
            val version = ForgeVersionArgument[context]

            return version.installers.firstOrNull { it.id == argument }
                ?: throw NO_SUCH_FORGE_INSTALLER.create(argument)
        }
    }

    override fun parse(reader: StringReader): String {
        return reader.readString()
    }
}