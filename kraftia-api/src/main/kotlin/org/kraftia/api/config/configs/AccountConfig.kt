package org.kraftia.api.config.configs

import com.google.gson.*
import org.kraftia.api.account.Account
import org.kraftia.api.account.serializers.AccountSerializer
import org.kraftia.api.config.AbstractConfig
import org.kraftia.api.config.AbstractConfigClass
import org.kraftia.api.managers.AccountManager
import java.lang.reflect.Type

class AccountConfig(
    private val accounts: Set<Account> = emptySet(),
    private val current: String? = null
) : AbstractConfig() {
    companion object : AbstractConfigClass<AccountConfig>("accounts", AccountConfig::class) {
        override fun create(): AccountConfig {
            return AccountConfig(
                AccountManager.accounts,
                AccountManager.current?.username
            )
        }

        override fun AccountConfig.apply() {
            for (account in accounts) {
                AccountManager.addAccount(account)
            }

            AccountManager.current = AccountManager.getAccountByNameOrNull(current ?: return)
        }

        override fun serialize(src: AccountConfig, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonObject().apply {
                add("accounts", JsonArray().apply {
                    for (account in src.accounts) {
                        add(AccountSerializer.serialize(account, null, null))
                    }
                })

                if (src.current != null) addProperty("current", src.current)
            }
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountConfig {
            val jsonObject = json.asJsonObject

            return AccountConfig(
                jsonObject.getAsJsonArray("accounts")
                    .map { AccountSerializer.deserialize(it.asJsonObject, null, null) }
                    .toSet(),
                jsonObject.get("current")?.asString
            )
        }
    }
}