package org.kraftia.api.extensions

import com.google.gson.JsonElement
import org.kraftia.api.Api

inline fun <reified T> fromJson(string: String): T {
    return Api.GSON.fromJson(string, T::class.java)
}

inline fun <reified T> fromJson(element: JsonElement): T {
    return Api.GSON.fromJson(element, T::class.java)
}

inline fun <reified T> T.toJson(): String {
    return Api.GSON.toJson(this, T::class.java)
}