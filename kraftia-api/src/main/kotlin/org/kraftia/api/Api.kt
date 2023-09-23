package org.kraftia.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import org.kraftia.api.account.Account
import org.kraftia.api.extensions.checkRules
import org.kraftia.api.extensions.path
import org.kraftia.api.extensions.resourceJson
import org.kraftia.api.managers.AccountManager
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.Arguments
import org.kraftia.api.version.Version
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.withLoggingThread
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader
import org.kraftia.api.version.downloader.downloaders.VersionDownloader
import org.kraftia.api.version.serializers.ArgumentsDeserializer
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object Api {
    private val operatingSystem = OperatingSystem.current

    val GSON: Gson = GsonBuilder()
        .registerTypeAdapter(Arguments::class.java, ArgumentsDeserializer)
        .registerTypeHierarchyAdapter(Account::class.java, Account.TypeAdapter)
        .create()

    val HTTP = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val VERSION = resourceJson<JsonObject>("kraftia.json")
        .getAsJsonPrimitive("Version")
        .asString

    val launcherDirectory: Path = Paths.get("kraftia")
    val minecraftDirectory: Path = operatingSystem.minecraftDirectory()
    val javaExecutablePath: Path? = try {
        val path = operatingSystem.javaExecutable()
        if (path.exists()) path else null
    } catch (ex: Exception) {
        null
    }

    val fabricVersionDownloader = FabricVersionDownloader()
    val versionDownloader = VersionDownloader(
        operatingSystem,
        path(minecraftDirectory, "versions"),
        path(minecraftDirectory, "libraries"),
        path(launcherDirectory, "bin"),
        path(minecraftDirectory, "assets")
    )

    init {
        AccountManager
        JavaVersionManager
        VersionManager
    }

    fun launch(
        version: Version,
        account: Account,
        features: Map<String, Boolean> = emptyMap()
    ): Process {
        println("Launching ${version.id} using ${account.name} account (${account.uuid}")

        val (resultVersion, resultClasspath, resultBinDirectory) = downloaderProgress { progress ->
            progress.withLoggingThread("VersionDownloader")
            versionDownloader.download(progress, version)
        }

        val command = mutableListOf(
            JavaVersionManager.current?.executable ?: "java",
            "-Djava.library.path=${resultBinDirectory.absolutePathString()}",
            "-cp",
            resultClasspath.joinToString(File.pathSeparator),
            *arguments(
                resultVersion.arguments!!.jvm,
                resultVersion,
                account,
                resultBinDirectory,
                features
            ).toTypedArray(),
            resultVersion.mainClass!!,
            *arguments(
                resultVersion.arguments!!.game,
                resultVersion,
                account,
                resultBinDirectory,
                features
            ).toTypedArray()
        )

        println("Launch arguments: ${command.joinToString(" ")}")

        return ProcessBuilder()
            .command(command)
            .directory(minecraftDirectory.toFile())
            .inheritIO()
            .start()
    }

    private fun arguments(
        arguments: List<Arguments.Argument>,
        version: Version,
        account: Account,
        versionBinDirectory: Path,
        features: Map<String, Boolean>
    ): List<String> {
        val adapter = object {
            var arguments = arguments
                .filter { argument -> argument.rules.checkRules(operatingSystem, features) }
                .filter { argument -> !argument.rules.any { it.features.isNotEmpty() } }
                .flatMap { it.value }

            operator fun set(replaceName: String, replaceValue: String) {
                this.arguments = this.arguments.map { it.replace("\${$replaceName}", replaceValue) }
            }

            fun remove(argument: String) {
                this.arguments = this.arguments.filter { it != argument }
            }
        }

        adapter.remove("-cp")
        adapter.remove("\${classpath}")
        adapter.remove("-Djava.library.path\\u003d\${natives_directory}")

        adapter["game_directory"] = minecraftDirectory.absolutePathString()
        adapter["natives_directory"] = versionBinDirectory.absolutePathString()
        adapter["library_directory"] = versionDownloader.librariesDirectory.absolutePathString()
        adapter["assets_root"] = versionDownloader.assetsDirectory.absolutePathString()

        adapter["launcher_name"] = "kraftia"
        adapter["launcher_version"] = VERSION

        adapter["version_name"] = version.id!!
        adapter["version_type"] = version.type!!

        adapter["classpath_separator"] = File.pathSeparator

        adapter["assets_index_name"] = version.assets!!

        adapter["user_type"] = "mojang"
        adapter["auth_player_name"] = account.name!!
        adapter["auth_uuid"] = account.uuid!!
        adapter["auth_access_token"] = "00000000000000000000000000000000"
        adapter["auth_session"] = "00000000000000000000000000000000"

        adapter["auth_xuid"] = ""
        adapter["clientid"] = ""
        adapter["user_properties"] = "{}"
        adapter["profile_properties"] = "{}"

        return adapter.arguments
    }

    enum class OperatingSystem(
        val minecraftDirectory: () -> Path,
        val javaExecutable: () -> Path
    ) {
        Windows(
            { path(System.getenv("APPDATA"), ".minecraft") },
            { path(System.getProperty("java.home"), "bin", "java.exe") }
        ),
        Linux(
            { path(System.getProperty("user.home"), ".minecraft") },
            { path(System.getProperty("java.home"), "bin", "java") }
        ),
        OsX(
            { path(System.getProperty("user.home"), "Library/Application Support/minecraft/") },
            { path(System.getProperty("java.home"), "bin", "java") }
        );

        override fun toString(): String {
            return name.lowercase()
        }

        companion object {
            val current: OperatingSystem
                get() {
                    val property = System.getProperty("os.name").lowercase()

                    return when {
                        property.contains("win") -> Windows
                        property.contains("mac") || property.contains("osx") -> OsX
                        property.contains("nix") || property.contains("nux") || property.contains("aix") -> Linux
                        else -> throw IllegalArgumentException("Unsupported operating system")
                    }
                }
        }
    }
}