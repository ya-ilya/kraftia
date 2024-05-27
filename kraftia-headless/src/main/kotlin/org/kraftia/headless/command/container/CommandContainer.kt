package org.kraftia.headless.command.container

import org.kraftia.headless.command.AbstractCommand

interface CommandContainer {
    val commands: MutableSet<AbstractCommand>

    fun getCommandByName(name: String): AbstractCommand {
        return getCommandByNameOrNull(name)!!
    }

    fun getCommandByNameOrNull(name: String): AbstractCommand? {
        return commands.firstOrNull { it.name == name }
    }

    fun addCommand(command: AbstractCommand) {
        commands.add(command)
    }

    fun removeCommand(command: AbstractCommand) {
        commands.remove(command)
    }
}