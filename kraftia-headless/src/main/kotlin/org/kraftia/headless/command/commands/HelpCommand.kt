package org.kraftia.headless.command.commands

import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.managers.CommandManager

object HelpCommand : AbstractCommand("help", "Available commands list") {
    init {
        builder.execute {
            println("Available commands:")

            for (command in CommandManager.commands) {
                println("- ${command.name}: ${command.description}")
            }
        }
    }
}