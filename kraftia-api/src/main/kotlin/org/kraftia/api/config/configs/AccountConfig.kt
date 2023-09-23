package org.kraftia.api.config.configs

import org.kraftia.api.account.Account
import org.kraftia.api.config.AbstractConfig
import org.kraftia.api.config.AbstractConfigClass
import org.kraftia.api.managers.AccountManager

class AccountConfig(
    private val accounts: Set<Account> = emptySet(),
    private val current: String? = null
) : AbstractConfig("account") {
    companion object : AbstractConfigClass<AccountConfig>("accounts.json", AccountConfig::class) {
        override fun createConfig(): AccountConfig {
            return AccountConfig(
                AccountManager.accounts,
                AccountManager.current?.name
            )
        }

        override fun AccountConfig.applyConfig() {
            for (account in accounts) {
                AccountManager.addAccount(account)
            }

            AccountManager.current = AccountManager.getAccountByNameOrNull(current ?: return)
        }
    }
}