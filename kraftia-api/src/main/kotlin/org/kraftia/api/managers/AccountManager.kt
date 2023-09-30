package org.kraftia.api.managers

import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.compat.OAuthServer
import org.kraftia.api.Api
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.ThreadPoolExecutor

object AccountManager {
    private val THREAD_POOL_EXECUTOR_FIELD = OAuthServer::class.java
        .getDeclaredField("threadPoolExecutor")
        .also { it.isAccessible = true }

    val accounts = mutableSetOf<MinecraftAccount>()

    var current: MinecraftAccount? = null
        get() {
            if (field != null && !accounts.contains(field)) {
                field = null
            }

            return field
        }


    fun getAccountByName(name: String): MinecraftAccount {
        return getAccountByNameOrNull(name)!!
    }

    fun getAccountByNameOrNull(name: String): MinecraftAccount? {
        return accounts.firstOrNull { it.name == name }
    }

    fun addAccount(account: MinecraftAccount) {
        accounts.removeIf { it.name == account.name }
        accounts.add(account)
    }

    fun removeAccount(account: MinecraftAccount) {
        accounts.remove(account)
    }

    fun loginMicrosoft(): MinecraftAccount? {
        var result: MicrosoftAccount? = null

        val server = MicrosoftAccount.buildFromOpenBrowser(object : MicrosoftAccount.OAuthHandler {
            override fun authError(error: String) {
                println("Microsoft auth error: $error")
            }

            override fun authResult(account: MicrosoftAccount) {
                result = account
            }

            override fun openUrl(url: String) {
                println("Trying to open $url")

                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(URI.create(url))
                    } catch (ex: Exception) {
                        println("Open the link yourself")
                    }
                } else {
                    println("Open the link yourself")
                }
            }

        }, Api.AUTH)

        val threadPoolExecutor = THREAD_POOL_EXECUTOR_FIELD.get(server) as ThreadPoolExecutor

        while (!threadPoolExecutor.isShutdown) {
            // Server
        }

        return result?.also {
            addAccount(it)
            current = it
        }
    }

    fun loginCracked(name: String): MinecraftAccount {
        return CrackedAccount().also {
            it.name = name
            addAccount(it)
            current = it
        }
    }
}