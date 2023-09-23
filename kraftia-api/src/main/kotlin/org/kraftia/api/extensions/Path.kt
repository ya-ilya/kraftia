package org.kraftia.api.extensions

import java.nio.file.Path
import java.nio.file.Paths

fun path(first: Any?, vararg more: Any?): Path {
    return Paths.get(first.toString(), *more.map { it.toString() }.toTypedArray())
}