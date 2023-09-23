package org.kraftia.headless.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import org.kraftia.api.account.AbstractAccount
import org.kraftia.api.managers.AccountManager

class AccountArgument : ArgumentType<AbstractAccount> {
    companion object {
        private val NO_SUCH_ACCOUNT = DynamicCommandExceptionType { name: Any ->
            Message { "Account '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): AbstractAccount {
            return context.getArgument("account", AbstractAccount::class.java)
        }
    }

    override fun parse(reader: StringReader): AbstractAccount {
        val argument = reader.readString()

        return AccountManager.getAccountByNameOrNull(argument)
            ?: throw NO_SUCH_ACCOUNT.create(argument)
    }
}