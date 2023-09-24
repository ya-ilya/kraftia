package org.kraftia.api.version.downloader

import kotlin.concurrent.thread

class DownloaderProgress : AutoCloseable {
    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        fun <R> downloaderProgress(block: (DownloaderProgress) -> R): R {
            return DownloaderProgress().use(block)
        }
    }

    private val messages = mutableListOf<String>()

    var closed = false

    fun pushMessage(string: String) {
        messages.add(string)
    }

    fun popMessage(): String? {
        return messages.removeFirstOrNull()
    }

    fun withThread(threadName: String, loopBlock: (DownloaderProgress) -> Unit): Thread {
        return thread(name = threadName) {
            while (!closed) {
                loopBlock(this)
            }
        }
    }

    fun withLoggingThread(threadName: String): Thread {
        return withThread(threadName) {
            println(popMessage() ?: return@withThread)
        }
    }

    override fun close() {
        closed = true
    }
}