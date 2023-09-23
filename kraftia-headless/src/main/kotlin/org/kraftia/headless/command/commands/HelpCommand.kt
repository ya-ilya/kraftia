package org.kraftia.headless.command.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.managers.CommandManager

object HelpCommand : AbstractCommand("help", "Available commands list") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.executesSuccess {
            println("Available commands:")

            for (command in CommandManager.commands) {
                println("- ${command.name}: ${command.description}")
            }
        }
    }
}