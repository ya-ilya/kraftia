package org.kraftia.api.config

import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass

abstract class AbstractConfigClass<T : AbstractConfig>(
    fileName: String,
    private val clazz: KClass<T>
) {
    private val path = path(Api.launcherDirectory, fileName)

    fun readConfig(): T {
        path.createParentDirectories()

        return if (path.exists()) Api.GSON.fromJson(path.readText(), clazz.java)
        else createConfig()
    }

    fun T.writeConfig() {
        path.createParentDirectories()
        path.writeText(Api.GSON.toJson(this, clazz.java))
    }

    abstract fun createConfig(): T
    abstract fun T.applyConfig()
}