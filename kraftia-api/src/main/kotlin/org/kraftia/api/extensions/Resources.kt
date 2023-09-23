package org.kraftia.api.extensions

inline fun <reified T> resourceJson(name: String): T {
    return fromJson(resourceText(name))
}

fun resourceText(name: String): String {
    return Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(name)!!
        .reader()
        .readText()
}