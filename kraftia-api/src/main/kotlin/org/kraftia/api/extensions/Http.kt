package org.kraftia.api.extensions

import okhttp3.Headers.Companion.toHeaders
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.io.path.writeBytes

inline fun <reified T> get(
    url: String,
    headers: Map<String, String> = emptyMap()
): T {
    return fromJson(get(url, headers).body.string())
}

fun get(
    url: String,
    headers: Map<String, String> = emptyMap()
): Response {
    val request = Request.Builder()
        .get()
        .url(url)
        .headers(headers.toHeaders())
        .build()

    return Api.HTTP
        .newCall(request)
        .execute()
}

inline fun <reified T> post(
    url: String,
    data: String,
    headers: Map<String, String> = emptyMap()
): T {
    return fromJson(post(url, data, headers).body.string())
}

fun post(
    url: String,
    data: String,
    headers: Map<String, String> = emptyMap()
): Response {
    val request = Request.Builder()
        .post(data.toRequestBody())
        .url(url)
        .headers(headers.toHeaders())
        .build()

    return Api.HTTP
        .newCall(request)
        .execute()
}

fun download(
    url: String,
    path: Path,
    name: String = path.name,
    headers: Map<String, String> = emptyMap(),
    progress: DownloaderProgress? = null
) {
    val response = get(url, headers).body.bytes()

    path.createParentDirectories()
    path.writeBytes(response, StandardOpenOption.CREATE)

    progress?.pushMessage("Downloaded $name")
}