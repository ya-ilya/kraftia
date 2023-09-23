package org.kraftia.headless.managers

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.container.CommandContainer

object CommandManager : CommandContainer {
    override val commands = mutableSetOf<AbstractCommand>()

    private val dispatcher = CommandDispatcher<Any>()

    override fun addCommand(command: AbstractCommand) {
        val literal = LiteralArgumentBuilder.literal<Any>(command.name)
        command.build(literal)
        dispatcher.register(literal)
        super.addCommand(command)
    }

    fun dispatch(input: String) {
        try {
            dispatcher.execute(input, Any())
        } catch (ex: CommandSyntaxException) {
            if (ex.cursor == 0) {
                println("[ERROR] Command not found")
            } else {
                println("[ERROR] Invalid syntax: ${ex.message}")
            }
        } catch (ex: Exception) {
            println("[ERROR] Failed to execute command: (${ex.javaClass.simpleName}) ${ex.message}")
        }
    }
}