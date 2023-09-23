package org.kraftia.api.account

import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.Request
import org.kraftia.api.Api
import org.kraftia.api.account.accounts.OfflineAccount

abstract class AbstractAccount(
    val uuid: String? = null,
    val name: String? = null
) {
    @JsonAdapter(AbstractAccount::class)
    object TypeAdapter : com.google.gson.TypeAdapter<AbstractAccount>() {
        override fun write(out: JsonWriter, value: AbstractAccount) {
            out.beginObject()
            out.name("type").value(value.javaClass.simpleName)
            out.name("uuid").value(value.uuid)
            out.name("name").value(value.name)

            when (value) {
                is OfflineAccount -> {
                    // Additional fields
                }

                else -> throw IllegalArgumentException()
            }

            out.endObject()
        }

        override fun read(`in`: JsonReader): AbstractAccount {
            `in`.beginObject()
            `in`.nextName()
            val type = `in`.nextString()
            `in`.nextName()
            val uuid = `in`.nextString()
            `in`.nextName()
            val name = `in`.nextString()

            val account = when (type) {
                "OfflineAccount" -> OfflineAccount(name, uuid)
                else -> throw IllegalArgumentException()
            }

            `in`.endObject()

            return account
        }
    }

    protected companion object {
        fun uuid(username: String): String {
            try {
                val request = Request.Builder()
                    .get()
                    .url("https://api.mojang.com/users/profiles/minecraft/$username")
                    .build()

                val response = Api.HTTP
                    .newCall(request)
                    .execute()
                    .body.string()

                val jsonElement = Api.GSON.fromJson(response, JsonElement::class.java)

                if (jsonElement.isJsonObject) {
                    return jsonElement.asJsonObject.get("id").asString
                }
            } catch (_: Exception) {
            }

            return ""
        }
    }
}