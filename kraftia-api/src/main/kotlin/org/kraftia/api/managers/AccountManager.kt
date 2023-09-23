package org.kraftia.api.managers

import org.kraftia.api.account.AbstractAccount
import org.kraftia.api.account.accounts.OfflineAccount
import org.kraftia.api.account.container.AccountContainer

object AccountManager : AccountContainer {
    override val accounts = mutableSetOf<AbstractAccount>()

    var current: AbstractAccount? = null

    fun loginOffline(name: String): AbstractAccount {
        return OfflineAccount(name).also {
            addAccount(it)
            current = it
        }
    }
}