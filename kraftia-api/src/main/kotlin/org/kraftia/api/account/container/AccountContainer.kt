package org.kraftia.api.account.container

import org.kraftia.api.account.Account
import org.kraftia.api.managers.AccountManager

interface AccountContainer {
    val accounts: MutableSet<Account>

    fun getAccountByName(name: String): Account {
        return getAccountByNameOrNull(name)!!
    }

    fun getAccountByNameOrNull(name: String): Account? {
        return AccountManager.accounts.firstOrNull { it.username == name }
    }

    fun addAccount(account: Account) {
        AccountManager.accounts.removeIf { it.username == account.username }
        AccountManager.accounts.add(account)
    }

    fun removeAccount(account: Account) {
        AccountManager.accounts.remove(account)
    }
}