package org.kraftia.headless.command.commands

import org.kraftia.headless.command.AbstractCommand
import kotlin.system.exitProcess

object ExitCommand : AbstractCommand("exit", "Exit from kraftia") {
    init {
        builder.execute {
            if (LaunchCommand.process != null) {
                return@execute println("Minecraft is still running. Close it to exit")
            }

            exitProcess(0)
        }
    }
}