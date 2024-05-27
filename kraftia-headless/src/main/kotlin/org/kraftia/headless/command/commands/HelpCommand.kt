package org.kraftia.headless.command.commands

import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.managers.CommandManager

object HelpCommand : AbstractCommand("help", "Available commands list") {
    init {
        builder.execute {
            println("Available commands:")

            for ((command, usage) in CommandManager.commandUsages) {
                println("- ${usage}: ${command.description}")
            }
        }
    }
}