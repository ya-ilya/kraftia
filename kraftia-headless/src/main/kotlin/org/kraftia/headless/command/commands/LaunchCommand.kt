package org.kraftia.headless.command.commands

import org.kraftia.api.Api
import org.kraftia.api.managers.AccountManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.InstanceArgument

object LaunchCommand : AbstractCommand("launch", "Launch specified version") {
    var process: Process? = null
        get() {
            if (field != null && !field!!.isAlive) {
                field = null
            }

            return field
        }

    init {
        builder.then(
            argument("instance", InstanceArgument()).execute { context ->
                if (process?.isAlive == true) {
                    return@execute println("Minecraft process already launched")
                }

                process = Api.launch(
                    InstanceArgument[context],
                    AccountManager.current
                        ?: throw IllegalArgumentException("Login in account before launching game")
                )

                println("Minecraft exited with code: ${process!!.waitFor()}")

                process = null
            }
        )
    }
}