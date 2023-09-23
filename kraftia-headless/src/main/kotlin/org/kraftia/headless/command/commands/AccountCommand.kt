package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import org.kraftia.api.managers.AccountManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.AccountArgument

object AccountCommand : AbstractCommand("account", "Manage accounts") {
    init {
        builder.then(
            literal("add").then(
                argument("name", StringArgumentType.string()).execute { context ->
                    val account = AccountManager.loginOffline(StringArgumentType.getString(context, "name"))

                    println("Login in as ${account.name}")
                }
            )
        )

        builder.then(
            literal("set").then(
                argument("account", AccountArgument()).execute { context ->
                    AccountManager.current = AccountArgument[context]
                    println("Account switched to ${AccountManager.current!!.name} account")
                }
            )
        )

        builder.then(
            literal("remove").then(
                argument("account", AccountArgument()).execute { context ->
                    val account = AccountArgument[context]

                    AccountManager.removeAccount(account)
                    println("Removed ${account.name} account")
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Accounts:")

                for (account in AccountManager.accounts) {
                    println("- ${account.name} (${account.uuid})")
                }
            }
        )

        builder.execute {
            if (AccountManager.current != null) {
                println("Current account: ${AccountManager.current!!.name} (${AccountManager.current!!.uuid}")
            } else {
                println("You are not logged in")
            }
        }
    }
}