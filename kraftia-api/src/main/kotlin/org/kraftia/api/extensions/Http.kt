package org.kraftia.api.extensions

import okhttp3.Request
import okhttp3.Response
import org.kraftia.api.Api
import org.kraftia.api.version.downloader.DownloaderProgress
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.io.path.writeBytes

fun get(url: String): Response {
    val request = Request.Builder()
        .get()
        .url(url)
        .build()

    return Api.HTTP
        .newCall(request)
        .execute()
}

fun download(url: String, path: Path, name: String = path.name, progress: DownloaderProgress? = null) {
    val response = get(url).body.bytes()

    path.createParentDirectories()
    path.writeBytes(response, StandardOpenOption.CREATE)

    progress!!.pushMessage("Downloaded $name")
}