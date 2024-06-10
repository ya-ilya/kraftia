package org.kraftia.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import org.kraftia.api.account.Account
import org.kraftia.api.config.configs.AccountConfig
import org.kraftia.api.config.configs.AccountConfig.Companion.apply
import org.kraftia.api.config.configs.AccountConfig.Companion.write
import org.kraftia.api.config.configs.InstanceConfig
import org.kraftia.api.config.configs.InstanceConfig.Companion.apply
import org.kraftia.api.config.configs.InstanceConfig.Companion.write
import org.kraftia.api.config.configs.JavaVersionConfig
import org.kraftia.api.config.configs.JavaVersionConfig.Companion.apply
import org.kraftia.api.config.configs.JavaVersionConfig.Companion.write
import org.kraftia.api.extensions.checkRules
import org.kraftia.api.extensions.path
import org.kraftia.api.extensions.resourceJson
import org.kraftia.api.instance.Instance
import org.kraftia.api.managers.AccountManager
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.api.managers.VersionManager
import org.kraftia.api.version.Arguments
import org.kraftia.api.version.Version
import org.kraftia.api.version.downloader.DownloaderProgress.Companion.downloaderProgress
import org.kraftia.api.version.downloader.downloaders.FabricVersionDownloader
import org.kraftia.api.version.downloader.downloaders.ForgeVersionDownloader
import org.kraftia.api.version.downloader.downloaders.NeoForgeVersionDownloader
import org.kraftia.api.version.downloader.downloaders.VersionDownloader
import org.kraftia.api.version.serializers.ArgumentSerializer
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

object Api {
    val operatingSystem = OperatingSystem.current
    val launcherDirectory: Path = path("kraftia")
    val instancesDirectory: Path = path(launcherDirectory, "instances")
    val javaExecutablePath: Path? = operatingSystem.javaExecutablePath

    val HTTP = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Arguments::class.java, ArgumentSerializer)
        .registerTypeAdapter(AccountConfig::class.java, AccountConfig)
        .registerTypeAdapter(InstanceConfig::class.java, InstanceConfig)
        .registerTypeAdapter(JavaVersionConfig::class.java, JavaVersionConfig)
        .create()

    val VERSION: String = resourceJson<JsonObject>("kraftia.json")
        .getAsJsonPrimitive("Version")
        .asString

    val fabricVersionDownloader = FabricVersionDownloader()
    val forgeVersionDownloader = ForgeVersionDownloader()
    val neoForgeVersionDownloader = NeoForgeVersionDownloader()
    val versionDownloader = VersionDownloader()

    init {
        instancesDirectory.createDirectories()

        val launcherProfiles = path(launcherDirectory, "launcher_profiles.json")

        if (!launcherProfiles.exists()) {
            launcherProfiles.createFile()
            launcherProfiles.writeText("{ \"profiles\": { } }")
        }
    }

    init {
        AccountManager
        JavaVersionManager
        VersionManager
    }

    fun configs() {
        AccountConfig.read().apply()
        InstanceConfig.read().apply()
        JavaVersionConfig.read().apply()

        Runtime.getRuntime().addShutdownHook(Thread {
            AccountConfig.create().write()
            InstanceConfig.create().write()
            JavaVersionConfig.create().write()
        })
    }

    fun launch(
        instance: Instance,
        account: Account,
        features: Map<String, Boolean> = emptyMap()
    ): Process {
        println("Launching ${instance.name} instance (${instance.version}) using ${account.username} account")

        val (resultVersion, resultClasspath, resultBinDirectory) = downloaderProgress { progress ->
            progress.withLoggingThread("VersionDownloader")
            versionDownloader.download(progress, VersionManager.getVersionById(instance.version))
        }

        val missingClasspath = resultClasspath.filter { !it.exists() }

        if (missingClasspath.isNotEmpty()) {
            println("Missing classpath:")

            for (classpath in missingClasspath) {
                println("- ${classpath.absolutePathString()}")
            }

            throw RuntimeException()
        }

        val gameDirectory = path(instance.gameDirectory)
        val command = listOf(
            JavaVersionManager.current?.executable ?: "java",
            "-Djava.library.path=${resultBinDirectory.absolutePathString()}",
            "-cp",
            resultClasspath.joinToString(File.pathSeparator),
            *arguments(
                resultVersion.arguments!!.jvm,
                resultVersion,
                account,
                gameDirectory,
                resultBinDirectory,
                features
            ).toTypedArray(),
            resultVersion.mainClass!!,
            *arguments(
                resultVersion.arguments!!.game,
                resultVersion,
                account,
                gameDirectory,
                resultBinDirectory,
                features
            ).toTypedArray()
        )

        println("Launch arguments: ${command.joinToString(" ")}")

        return ProcessBuilder()
            .command(command)
            .directory(gameDirectory.toFile())
            .inheritIO()
            .start()

    }

    private fun arguments(
        arguments: List<Arguments.Argument>,
        version: Version,
        account: Account,
        gameDirectory: Path,
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

        adapter["game_directory"] = gameDirectory.absolutePathString()
        adapter["natives_directory"] = versionBinDirectory.absolutePathString()
        adapter["library_directory"] = versionDownloader.librariesDirectory.absolutePathString()
        adapter["assets_root"] = versionDownloader.assetsDirectory.absolutePathString()

        adapter["launcher_name"] = "kraftia"
        adapter["launcher_version"] = VERSION

        adapter["version_name"] = version.id!!
        adapter["version_type"] = version.type!!

        adapter["classpath_separator"] = File.pathSeparator

        adapter["assets_index_name"] = version.assets!!

        adapter["user_type"] = "msa"

        try {
            adapter["auth_player_name"] = account.username
            adapter["auth_uuid"] = account.uuid
            adapter["auth_access_token"] = (account as? Account.Microsoft)?.accessToken ?: ""
        } catch (ex: IOException) {
            throw IOException("Failed to login to microsoft account. The refresh token may be outdated. Try to remove and add account again. Error message: ${ex.message}")
        }

        adapter["auth_xuid"] = ""
        adapter["clientid"] = ""
        adapter["user_properties"] = "{}"
        adapter["profile_properties"] = "{}"

        return adapter.arguments
    }

    enum class OperatingSystem(var javaExecutablePath: Path?) {
        Windows(path(System.getProperty("java.home"), "bin", "java.exe")),
        Linux(path(System.getProperty("java.home"), "bin", "java")),
        OsX(path(System.getProperty("java.home"), "bin", "java"));

        init {
            if (!javaExecutablePath!!.exists()) {
                javaExecutablePath = null
            }
        }

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