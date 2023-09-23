package org.kraftia.headless.command.commands

import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.Api
import org.kraftia.api.managers.AccountManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.VersionArgument

object LaunchCommand : AbstractCommand("launch", "Launch specified version") {
    private val ACCOUNT_ERROR = DynamicCommandExceptionType { _: Any ->
        Message { "Login in account before launching game" }
    }

    var process: Process? = null

    init {
        builder.then(
            argument("version", VersionArgument()).execute { context ->
                if (process?.isAlive == true) {
                    return@execute println("Minecraft process already launched")
                }

                process = Api.launch(
                    VersionArgument[context],
                    AccountManager.current ?: throw ACCOUNT_ERROR.create(Any())
                )

                println("Minecraft exited with code: ${process!!.waitFor()}")
            }
        )
    }
}