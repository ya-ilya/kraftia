package org.kraftia.api.version.downloader

import kotlin.concurrent.thread

class DownloaderProgress : AutoCloseable {
    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        fun <R> downloaderProgress(block: (DownloaderProgress) -> R): R {
            return DownloaderProgress().use(block)
        }

        fun DownloaderProgress.withThread(threadName: String, loopBlock: (DownloaderProgress) -> Unit): Thread {
            return thread(name = threadName) {
                while (!closed) {
                    loopBlock(this)
                }
            }
        }

        fun DownloaderProgress.withLoggingThread(threadName: String): Thread {
            return withThread(threadName) {
                println(popMessage() ?: return@withThread)
            }
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

    override fun close() {
        closed = true
    }
}