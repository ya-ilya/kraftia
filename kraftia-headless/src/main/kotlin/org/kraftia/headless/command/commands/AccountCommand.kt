package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.api.managers.AccountManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.AccountArgument

object AccountCommand : AbstractCommand("account", "Manage accounts") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(
            literal("add").then(
                argument("name", StringArgumentType.string()).executesSuccess { context ->
                    val account = AccountManager.loginOffline(StringArgumentType.getString(context, "name"))

                    println("Login in as ${account.name}")
                }
            )
        )

        builder.then(
            literal("set").then(
                argument("account", AccountArgument()).executesSuccess { context ->
                    AccountManager.current = AccountArgument[context]
                    println("Account switched to ${AccountManager.current!!.name} account")
                }
            )
        )

        builder.then(
            literal("remove").then(
                argument("account", AccountArgument()).executesSuccess { context ->
                    val account = AccountArgument[context]

                    AccountManager.removeAccount(account)
                    println("Removed ${account.name} account")
                }
            )
        )

        builder.then(
            literal("list").executesSuccess {
                println("Accounts:")

                for (account in AccountManager.accounts) {
                    println("- ${account.name} (${account.uuid})")
                }
            }
        )

        builder.executesSuccess {
            if (AccountManager.current != null) {
                println("Current account: ${AccountManager.current!!.name} (${AccountManager.current!!.uuid}")
            } else {
                println("You are not logged in")
            }
        }
    }
}