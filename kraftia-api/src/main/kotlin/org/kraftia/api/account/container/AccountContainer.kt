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
        if (accounts.any { it.name == account.name }) {
            throw IllegalArgumentException("Account with same name already exists")
        }

        accounts.add(account)
    }

    fun removeAccount(account: Account) {
        accounts.remove(account)
    }
}