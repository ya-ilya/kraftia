package org.kraftia.api.account

import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.Request
import org.kraftia.api.Api

sealed class Account(
    val uuid: String? = null,
    val name: String? = null
) {
    class Offline(name: String, uuid: String? = null) : Account(uuid ?: uuid(name), name)

    @JsonAdapter(Account::class)
    object TypeAdapter : com.google.gson.TypeAdapter<Account>() {
        override fun write(out: JsonWriter, value: Account) {
            out.beginObject()
            out.name("type").value(value.javaClass.simpleName)
            out.name("uuid").value(value.uuid)
            out.name("name").value(value.name)

            when (value) {
                is Offline -> {
                    // Additional fields
                }
            }

            out.endObject()
        }

        override fun read(`in`: JsonReader): Account {
            `in`.beginObject()
            `in`.nextName()
            val type = `in`.nextString()
            `in`.nextName()
            val uuid = `in`.nextString()
            `in`.nextName()
            val name = `in`.nextString()

            val account = when (type) {
                "Offline" -> Offline(name, uuid)
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