package org.kraftia.api.account.serializers

import com.google.gson.*
import org.kraftia.api.account.Account
import java.lang.reflect.Type

object AccountSerializer : JsonSerializer<Account>, JsonDeserializer<Account> {
    override fun serialize(src: Account, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonObject().apply {
            addProperty("Type", src.type)
            when (src) {
                is Account.Offline -> {
                    addProperty("Name", src.username)
                }

                is Account.Microsoft -> {
                    addProperty("RefreshToken", src.refreshToken)
                }
            }
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Account {
        return json.asJsonObject.run {
            when (getAsJsonPrimitive("Type").asString) {
                "Offline" -> {
                    Account.Offline(getAsJsonPrimitive("Name").asString)
                }

                "Microsoft" -> {
                    Account.Microsoft(getAsJsonPrimitive("RefreshToken").asString)
                }

                else -> throw IllegalArgumentException()
            }
        }
    }
}