package org.kraftia.api.config

import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass

abstract class AbstractConfigClass<T : AbstractConfig>(
    name: String,
    private val clazz: KClass<T>
) {
    private val path = path(Api.launcherDirectory, "$name.json")

    fun read(): T {
        path.createParentDirectories()

        return if (path.exists()) Api.GSON.fromJson(path.readText(), clazz.java)
        else create()
    }

    fun T.write() {
        path.createParentDirectories()
        path.writeText(Api.GSON.toJson(this, clazz.java))
    }

    abstract fun create(): T
    abstract fun T.apply()
}