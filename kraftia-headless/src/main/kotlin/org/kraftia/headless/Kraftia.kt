package org.kraftia.headless

import org.kraftia.api.Api
import org.kraftia.api.config.configs.AccountConfig
import org.kraftia.api.config.configs.AccountConfig.Companion.applyConfig
import org.kraftia.api.config.configs.AccountConfig.Companion.writeConfig
import org.kraftia.api.config.configs.JavaVersionConfig
import org.kraftia.api.config.configs.JavaVersionConfig.Companion.applyConfig
import org.kraftia.api.config.configs.JavaVersionConfig.Companion.writeConfig
import org.kraftia.api.extensions.resourceText
import org.kraftia.headless.command.commands.*
import org.kraftia.headless.managers.CommandManager

fun main() {
    CommandManager.addCommand(AccountCommand)
    CommandManager.addCommand(ExitCommand)
    CommandManager.addCommand(FabricCommand)
    CommandManager.addCommand(HelpCommand)
    CommandManager.addCommand(JavaCommand)
    CommandManager.addCommand(LaunchCommand)
    CommandManager.addCommand(VersionCommand)

    AccountConfig
        .readConfig()
        .applyConfig()

    JavaVersionConfig
        .readConfig()
        .applyConfig()

    Runtime.getRuntime().addShutdownHook(Thread {
        AccountConfig
            .createConfig()
            .writeConfig()

        JavaVersionConfig
            .createConfig()
            .writeConfig()
    })

    println(resourceText("ascii_logo.txt"))
    println("Welcome to the Kraftia v${Api.VERSION}, simple and lightweight launcher for Minecraft.")

    while (true) {
        print("kraftia>")

        CommandManager.dispatch(readlnOrNull() ?: break)
    }
}