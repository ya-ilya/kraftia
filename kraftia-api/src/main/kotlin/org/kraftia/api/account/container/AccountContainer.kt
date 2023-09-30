package org.kraftia.api.account.container

import org.kraftia.api.account.Account

interface AccountContainer {
    val accounts: MutableSet<Account>

    fun getAccountByName(name: String): Account {
        return getAccountByNameOrNull(name)!!
    }

    fun getAccountByNameOrNull(name: String): Account? {
        return accounts.firstOrNull { it.name == name }
    }

    fun addAccount(account: Account) {
        accounts.removeIf { it.name == account.name || it.uuid == account.uuid }
        accounts.add(account)
    }

    fun removeAccount(account: Account) {
        accounts.remove(account)
    }
}