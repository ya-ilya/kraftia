package org.kraftia.headless.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext

abstract class AbstractCommand(val name: String, val description: String) {
    abstract fun build(builder: LiteralArgumentBuilder<Any>)

    protected companion object {
        fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<Any, T> {
            return RequiredArgumentBuilder.argument(name, type)
        }

        fun literal(name: String): LiteralArgumentBuilder<Any> {
            return LiteralArgumentBuilder.literal(name)
        }

        fun <S, T : ArgumentBuilder<S, T>> T.executesSuccess(block: (CommandContext<S>) -> Unit): T {
            return executes { context ->
                block(context)
                Command.SINGLE_SUCCESS
            }
        }
    }
}