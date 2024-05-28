package org.kraftia.api.managers

import org.kraftia.api.account.Account
import org.kraftia.api.account.oauth.OAuthServer
import java.awt.Desktop
import java.net.URI

object AccountManager {
    val accounts = mutableSetOf<Account>()

    var current: Account? = null
        get() {
            if (field != null && !accounts.contains(field)) {
                field = null
            }

            return field
        }


    fun getAccountByName(name: String): Account {
        return getAccountByNameOrNull(name)!!
    }

    fun getAccountByNameOrNull(name: String): Account? {
        return accounts.firstOrNull { it.username == name }
    }

    fun addAccount(account: Account) {
        accounts.removeIf { it.username == account.username }
        accounts.add(account)
    }

    fun removeAccount(account: Account) {
        accounts.remove(account)
    }

    fun loginMicrosoft(): Account.Microsoft? {
        var result: Account.Microsoft? = null
        val server = object : OAuthServer() {
            init {
                start()
            }

            override fun onStart(url: String) {
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

            override fun onLogin(account: Account.Microsoft) {
                result = account
            }

            override fun onError(message: String) {
                result = null
            }
        }

        while (!server.isShutdown && !Thread.currentThread().isInterrupted) {
            // Server
        }

        server.stop()

        return result?.also {
            addAccount(it)
            current = it
        }
    }

    fun loginCracked(name: String): Account.Offline {
        return Account.Offline(name).also {
            addAccount(it)
            current = it
        }
    }
}