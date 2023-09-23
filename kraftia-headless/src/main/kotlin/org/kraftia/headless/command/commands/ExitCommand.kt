package org.kraftia.headless.command.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.headless.command.AbstractCommand
import kotlin.system.exitProcess

object ExitCommand : AbstractCommand("exit", "Exit from kraftia") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.executesSuccess {
            if (LaunchCommand.process != null) {
                return@executesSuccess println("Minecraft is still running. Close it to exit")
            }

            exitProcess(0)
        }
    }
}