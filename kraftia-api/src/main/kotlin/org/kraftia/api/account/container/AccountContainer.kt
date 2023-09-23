package org.kraftia.api.account.container

import org.kraftia.api.account.AbstractAccount

interface AccountContainer {
    val accounts: MutableSet<AbstractAccount>

    fun getAccountByName(name: String): AbstractAccount {
        return getAccountByNameOrNull(name)!!
    }

    fun getAccountByNameOrNull(name: String): AbstractAccount? {
        return accounts.firstOrNull { it.name == name }
    }

    fun addAccount(account: AbstractAccount) {
        accounts.add(account)
    }

    fun removeAccount(account: AbstractAccount) {
        accounts.remove(account)
    }
}