package org.kraftia.headless.command.container

import org.kraftia.headless.command.AbstractCommand

interface CommandContainer {
    val commands: MutableSet<AbstractCommand>

    fun addCommand(command: AbstractCommand) {
        commands.add(command)
    }

    fun removeCommand(command: AbstractCommand) {
        commands.remove(command)
    }
}