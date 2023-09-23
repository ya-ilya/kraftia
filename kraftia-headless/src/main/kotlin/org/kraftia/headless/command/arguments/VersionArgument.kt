package org.kraftia.headless.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.Version

class VersionArgument : ArgumentType<Version> {
    companion object {
        private val NO_SUCH_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Version '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): Version {
            return context.getArgument("version", Version::class.java)
        }
    }

    override fun parse(reader: StringReader): Version {
        val argument = reader.readString()

        return VersionManager.getVersionByIdOrNull(argument)
            ?: throw NO_SUCH_VERSION.create(argument)
    }
}