package org.kraftia.headless

import org.kraftia.api.Api
import org.kraftia.api.extensions.resourceText
import org.kraftia.headless.command.commands.*
import org.kraftia.headless.managers.CommandManager

fun main() {
    Api.configs()

    CommandManager.addCommand(AccountCommand)
    CommandManager.addCommand(ExitCommand)
    CommandManager.addCommand(FabricCommand)
    CommandManager.addCommand(ForgeCommand)
    CommandManager.addCommand(HelpCommand)
    CommandManager.addCommand(InstanceCommand)
    CommandManager.addCommand(JavaCommand)
    CommandManager.addCommand(LaunchCommand)
    CommandManager.addCommand(NeoForgeCommand)
    CommandManager.addCommand(VersionCommand)

    println(resourceText("ascii_logo.txt"))
    println("Welcome to the Kraftia v${Api.VERSION}, simple and lightweight launcher for Minecraft.")

    while (true) {
        print("kraftia>")

        CommandManager.dispatch(readlnOrNull() ?: break)
    }
}