package org.kraftia.api.config

import com.google.gson.*
import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import java.lang.reflect.Type
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass

abstract class AbstractConfigClass<T : AbstractConfig>(
    name: String,
    private val clazz: KClass<T>
) : JsonSerializer<T>, JsonDeserializer<T> {
    private val path = path(Api.launcherDirectory, "$name.json")
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun read(): T {
        path.createParentDirectories()

        return if (path.exists()) Api.GSON.fromJson(path.readText(), clazz.java) else create()
    }

    fun T.write() {
        path.createParentDirectories()
        path.writeText(Api.GSON.toJson(this, clazz.java))
    }

    override fun serialize(src: T, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return gson.toJsonTree(src, clazz.java)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): T {
        return gson.fromJson(json, clazz.java)
    }

    abstract fun create(): T
    abstract fun T.apply()
}