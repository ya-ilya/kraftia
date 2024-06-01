package org.kraftia.api.version.serializers

import com.google.gson.*
import org.kraftia.api.extensions.fromJson
import org.kraftia.api.version.Arguments
import java.lang.reflect.Type

object ArgumentSerializer : JsonDeserializer<Arguments> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Arguments {
        return when (json) {
            is JsonPrimitive -> {
                Arguments(
                    json.asString.split(" ").map { Arguments.Argument(value = listOf(it)) }
                )
            }

            is JsonObject -> {
                Arguments(
                    json.getAsJsonArray("game").arguments(),
                    json.getAsJsonArray("jvm").arguments()
                )
            }

            else -> throw IllegalArgumentException()
        }
    }

    private fun JsonArray.arguments(): List<Arguments.Argument> {
        return map { element ->
            when (element) {
                is JsonPrimitive -> {
                    Arguments.Argument(value = listOf(element.asString))
                }

                is JsonObject -> {
                    val rules = element.getAsJsonArray("rules")
                    val value = element.get("value")

                    Arguments.Argument(rules.map { fromJson(it) }, when (value) {
                        is JsonPrimitive -> listOf(value.asString)
                        is JsonArray -> value.asJsonArray.map { it.asString }
                        else -> throw IllegalArgumentException()
                    })
                }

                else -> {
                    throw IllegalArgumentException()
                }
            }
        }
    }
}