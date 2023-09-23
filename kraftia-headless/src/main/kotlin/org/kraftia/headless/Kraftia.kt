package org.kraftia.headless

import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import org.kraftia.api.extensions.resourceText
import org.kraftia.headless.command.commands.*
import org.kraftia.headless.managers.CommandManager
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories

val configPath = path(
    Api.launcherDirectory,
    "kraftia.json"
)

fun main() {
    try {
        configPath.createParentDirectories()
        configPath.createFile()
    } catch (ex: Exception) {
        // Ignored
    }

    CommandManager.addCommand(AccountCommand)
    CommandManager.addCommand(ExitCommand)
    CommandManager.addCommand(FabricCommand)
    CommandManager.addCommand(HelpCommand)
    CommandManager.addCommand(JavaCommand)
    CommandManager.addCommand(LaunchCommand)
    CommandManager.addCommand(VersionCommand)

    Api.loadConfig(configPath)

    Runtime.getRuntime().addShutdownHook(Thread {
        Api.saveConfig(configPath)
    })

    println(resourceText("ascii_logo.txt"))
    println("Welcome to the Kraftia v${Api.VERSION}, simple and lightweight launcher for minecraft.")

    while (true) {
        print("kraftia>")

        CommandManager.dispatch(readln())
    }
}