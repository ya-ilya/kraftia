package org.kraftia.api.config.configs

import com.google.gson.*
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.manage.AccountSerializer
import me.liuli.elixir.utils.set
import org.kraftia.api.config.AbstractConfig
import org.kraftia.api.config.AbstractConfigClass
import org.kraftia.api.managers.AccountManager
import java.lang.reflect.Type

class AccountConfig(
    private val accounts: Set<MinecraftAccount> = emptySet(),
    private val current: String? = null
) : AbstractConfig("account") {
    companion object : AbstractConfigClass<AccountConfig>("accounts", AccountConfig::class) {
        override fun create(): AccountConfig {
            return AccountConfig(
                AccountManager.accounts,
                AccountManager.current?.name
            )
        }

        override fun AccountConfig.apply() {
            for (account in accounts) {
                AccountManager.addAccount(account)
            }

            AccountManager.current = AccountManager.getAccountByNameOrNull(current ?: return)
        }

        override fun serialize(src: AccountConfig, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val jsonObject = JsonObject()

            jsonObject["accounts"] = src.accounts.map { AccountSerializer.toJson(it) }.let {
                val array = JsonArray()

                for (account in it) {
                    array.add(account)
                }

                array
            }

            if (src.current != null) jsonObject["current"] = src.current

            return jsonObject
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountConfig {
            val jsonObject = json.asJsonObject

            return AccountConfig(
                jsonObject.getAsJsonArray("accounts").map { AccountSerializer.fromJson(it.asJsonObject) }.toSet(),
                jsonObject.get("current")?.asString
            )
        }
    }
}