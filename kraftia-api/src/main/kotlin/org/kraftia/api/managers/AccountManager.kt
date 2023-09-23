package org.kraftia.api.managers

import org.kraftia.api.account.Account
import org.kraftia.api.account.container.AccountContainer

object AccountManager : AccountContainer {
    override val accounts = mutableSetOf<Account>()

    var current: Account? = null

    fun loginOffline(name: String): Account {
        return Account.Offline(name).also {
            addAccount(it)
            current = it
        }
    }
}