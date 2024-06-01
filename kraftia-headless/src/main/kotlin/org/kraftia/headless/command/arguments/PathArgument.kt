package org.kraftia.headless.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.kraftia.api.extensions.path
import java.nio.file.Path

class PathArgument : ArgumentType<Path> {
    companion object {
        operator fun get(context: CommandContext<Any>, name: String): Path {
            return context.getArgument(name, Path::class.java)
        }
    }

    override fun parse(reader: StringReader): Path {
        return if (reader.peek() == '\'' || reader.peek() == '"') {
            reader.skip()

            val result = StringBuilder()

            while (reader.canRead()) {
                val char = reader.read()
                if (char == '\'' || char == '"') {
                    return path(result.toString())
                } else {
                    result.append(char)
                }
            }

            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader)
        } else {
            path(reader.readString())
        }
    }
}