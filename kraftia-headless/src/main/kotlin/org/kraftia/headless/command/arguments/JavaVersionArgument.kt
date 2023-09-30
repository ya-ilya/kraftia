package org.kraftia.headless.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.java.JavaVersion
import org.kraftia.api.managers.JavaVersionManager

class JavaVersionArgument : ArgumentType<JavaVersion> {
    companion object {
        private val NO_SUCH_JAVA_VERSION = DynamicCommandExceptionType { name: Any ->
            Message { "Java version $name not found" }
        }

        operator fun get(context: CommandContext<Any>): JavaVersion {
            return context.getArgument("java", JavaVersion::class.java)
        }
    }

    override fun parse(reader: StringReader): JavaVersion {
        val argument = reader.readInt()

        return JavaVersionManager.javaVersions.firstOrNull { it.versionNumber == argument }
            ?: throw NO_SUCH_JAVA_VERSION.create(argument)
    }
}