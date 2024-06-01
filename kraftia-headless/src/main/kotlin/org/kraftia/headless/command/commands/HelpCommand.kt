package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.managers.CommandManager

object HelpCommand : AbstractCommand("help", "Available commands list") {
    init {
        builder.then(
            argument("commandName", StringArgumentType.string()).execute { context ->
                val commandName = StringArgumentType.getString(context, "commandName")
                val command = CommandManager.getCommandByNameOrNull(commandName)

                if (command == null) {
                    println("Command $commandName not found")
                } else {
                    println("All $commandName command usages:")

                    for (usage in CommandManager.getAllCommandUsages(command)) {
                        println("- ${command.name} $usage")
                    }
                }
            }
        )

        builder.execute {
            println("Available commands:")

            for ((command, usage) in CommandManager.commandUsages) {
                println("- ${usage}: ${command.description}")
            }
        }
    }
}