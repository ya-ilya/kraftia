package org.kraftia.api.managers

import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.compat.OAuthServer
import org.kraftia.api.Api
import org.kraftia.api.account.Account
import org.kraftia.api.account.container.AccountContainer
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.ThreadPoolExecutor

object AccountManager : AccountContainer {
    private val THREAD_POOL_EXECUTOR_FIELD = OAuthServer::class.java
        .getDeclaredField("threadPoolExecutor")
        .also { it.isAccessible = true }

    override val accounts = mutableSetOf<Account>()

    var current: Account? = null
        get() {
            if (field != null && !accounts.contains(field)) {
                field = null
            }

            return field
        }

    fun loginMicrosoft(): Account? {
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

        return result?.let {
            Account.Microsoft(
                result!!.session.username,
                result!!.session.uuid,
                result!!.session.token
            ).also {
                addAccount(it)
                current = it
            }
        }
    }

    fun loginOffline(name: String): Account {
        return Account.Offline(name).also {
            addAccount(it)
            current = it
        }
    }
}