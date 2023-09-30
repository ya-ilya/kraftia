package org.kraftia.headless.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.liuli.elixir.account.MinecraftAccount
import org.kraftia.api.managers.AccountManager

class AccountArgument : ArgumentType<MinecraftAccount> {
    companion object {
        private val NO_SUCH_ACCOUNT = DynamicCommandExceptionType { name: Any ->
            Message { "Account '$name' not found" }
        }

        operator fun get(context: CommandContext<Any>): MinecraftAccount {
            return context.getArgument("account", MinecraftAccount::class.java)
        }
    }

    override fun parse(reader: StringReader): MinecraftAccount {
        val argument = reader.readString()

        return AccountManager.getAccountByNameOrNull(argument)
            ?: throw NO_SUCH_ACCOUNT.create(argument)
    }
}