package org.kraftia.api.version.downloader

import kotlin.concurrent.thread

@Suppress("MemberVisibilityCanBePrivate")
class DownloaderProgress : AutoCloseable {
    companion object {
        fun <R> downloaderProgress(block: (DownloaderProgress) -> R): R {
            return DownloaderProgress().use(block)
        }
    }

    private val messages = mutableListOf<String>()
    private var closed = false

    fun pushMessage(string: String) {
        messages.add(string)
    }

    fun popMessage(): String? {
        return messages.removeFirstOrNull()
    }

    fun withLoggingThread(threadName: String): Thread {
        return withThread(threadName) {
            println(popMessage() ?: return@withThread)
        }
    }

    private fun withThread(threadName: String, loopBlock: (DownloaderProgress) -> Unit): Thread {
        return thread(name = threadName) {
            while (!closed) {
                loopBlock(this)
            }
        }
    }

    override fun close() {
        closed = true
    }
}