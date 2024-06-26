package org.kraftia.api.extensions

import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.exists

const val DEFAULT_ALGORITHM = "SHA-1"

fun Path.needToDownload(hash: String? = null, algorithm: String = DEFAULT_ALGORITHM): Boolean {
    return !exists() || !checkFileHash(hash, algorithm)
}

fun Path.checkFileHash(other: String?, algorithm: String = DEFAULT_ALGORITHM): Boolean {
    return hash(algorithm).equals(other, true)
}

fun Path.hash(algorithm: String = DEFAULT_ALGORITHM): String {
    return FileInputStream(toFile()).use { stream -> stream.hash(algorithm) }
}

fun InputStream.hash(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(8192)
    var length: Int = read(buffer)

    while (length != -1) {
        digest.update(buffer, 0, length)
        length = read(buffer)
    }

    val builder = StringBuilder()

    for (byte in digest.digest()) {
        val value = byte.toInt() and 0xFF
        if (value < 16) {
            builder.append("0")
        }
        builder.append(Integer.toHexString(value).uppercase(Locale.getDefault()))
    }

    return builder.toString().lowercase()
}