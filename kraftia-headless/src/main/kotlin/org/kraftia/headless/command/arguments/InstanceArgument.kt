package org.kraftia.headless.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.instance.Instance
import org.kraftia.api.managers.InstanceManager

class InstanceArgument : ArgumentType<Instance> {
    companion object {
        private val NO_SUCH_INSTANCE = DynamicCommandExceptionType { name: Any ->
            Message { "Instance $name not found" }
        }

        operator fun get(context: CommandContext<Any>): Instance {
            return context.getArgument("instance", Instance::class.java)
        }
    }

    override fun parse(reader: StringReader): Instance {
        val argument = reader.readString()

        return InstanceManager.instances.firstOrNull { it.name == argument }
            ?: throw NO_SUCH_INSTANCE.create(argument)
    }
}