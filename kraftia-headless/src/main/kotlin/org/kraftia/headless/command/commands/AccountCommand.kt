package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import org.kraftia.api.managers.AccountManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.AccountArgument

object AccountCommand : AbstractCommand("account", "Manage accounts") {
    init {
        builder.then(
            literal("add")
                .then(
                    literal("offline").then(
                        argument("username", StringArgumentType.string()).execute { context ->
                            val account = AccountManager.loginCracked(StringArgumentType.getString(context, "username"))

                            println("Login in as ${account.username}")
                        }
                    )
                )
                .then(
                    literal("microsoft").execute {
                        val account = AccountManager.loginMicrosoft()

                        if (account != null) {
                            println("Login in as ${account.username}")
                        } else {
                            println("Failed to login with microsoft account")
                        }
                    }
                )
        )

        builder.then(
            literal("set").then(
                argument("account", AccountArgument()).execute { context ->
                    AccountManager.current = AccountArgument[context]
                    println("Account switched to ${AccountManager.current!!.username} account")
                }
            )
        )

        builder.then(
            literal("remove").then(
                argument("account", AccountArgument()).execute { context ->
                    val account = AccountArgument[context]

                    AccountManager.removeAccount(account)
                    println("Removed ${account.username} account")
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Accounts:")

                for (account in AccountManager.accounts) {
                    println("- [${account.javaClass.simpleName.first()}] ${account.username}")
                }
            }
        )

        builder.execute {
            AccountManager.current.also {
                if (it != null) {
                    println("Current account: [${it.javaClass.simpleName.first()}] ${it.username}")
                } else {
                    println("You are not logged in")
                }
            }
        }
    }
}