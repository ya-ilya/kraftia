package org.kraftia.api.account.oauth

import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.kraftia.api.account.Account
import org.kraftia.api.account.Account.Microsoft.Companion.XBOX_AUTH_DATA
import org.kraftia.api.account.Account.Microsoft.Companion.XBOX_AUTH_URL
import org.kraftia.api.account.Account.Microsoft.Companion.XBOX_PRE_AUTH_URL
import org.kraftia.api.account.Account.Microsoft.Companion.replaceAuthKeys
import org.kraftia.api.extensions.post
import java.io.FileNotFoundException
import java.net.InetSocketAddress
import java.util.concurrent.Executors

open class OAuthServer(hostname: String = "localhost", port: Int = 1919) {
    private companion object {
        fun HttpExchange.response(message: String, code: Int) {
            val byte = message.toByteArray()
            sendResponseHeaders(code, byte.size.toLong())
            responseBody.write(byte)
            close()
        }
    }

    private val httpServer = HttpServer.create(InetSocketAddress(hostname, port), 0)
    private val threadPoolExecutor = Executors.newFixedThreadPool(10)

    val isShutdown get() = threadPoolExecutor.isShutdown

    open fun onStart(url: String) {}
    open fun onLogin(account: Account.Microsoft) {}
    open fun onError(message: String) {}

    fun start() {
        httpServer.executor = threadPoolExecutor
        httpServer.createContext("/login") { exchange ->
            val query = exchange.requestURI.query
                .split("&")
                .map { it.split("=") }
                .associate { it[0] to it[1] }

            if (query.containsKey("code")) {
                try {
                    val response = post<JsonObject>(
                        XBOX_AUTH_URL,
                        XBOX_AUTH_DATA.replaceAuthKeys() + query["code"],
                        mapOf("Content-Type" to "application/x-www-form-urlencoded")
                    )

                    onLogin(
                        if (response.has("refresh_token")) {
                            Account.Microsoft(response.get("refresh_token").asString)
                        } else {
                            throw Exception("Failed to get refresh token")
                        }
                    )

                    exchange.response("Login Success", 200)
                } catch (ex: FileNotFoundException) {
                    val message = "No minecraft account associated with this Microsoft account"

                    onError(message)
                    exchange.response("Error: $message", 500)
                } catch (ex: Exception) {
                    onError(ex.toString())
                    exchange.response("Error: $ex", 500)
                }
            } else {
                onError("No code in the query")
                exchange.response("No code in the query", 500)
            }

            stop()
        }
        httpServer.start()
        onStart(XBOX_PRE_AUTH_URL.replaceAuthKeys())
    }

    fun stop() {
        httpServer.stop(0)
        threadPoolExecutor.shutdown()
    }
}