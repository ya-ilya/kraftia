package org.kraftia.api.managers

import org.kraftia.api.Api
import org.kraftia.api.java.JavaVersion
import org.kraftia.api.java.container.JavaVersionContainer
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object JavaVersionManager : JavaVersionContainer {
    private val javaVersionRegex = "version \"(.*?)\"".toRegex()

    override val javaVersions = mutableSetOf<JavaVersion>()

    var current: JavaVersion? = null

    init {
        if (Api.javaExecutablePath != null) {
            current = addJavaVersionByPath(Api.javaExecutablePath)
        }
    }

    fun addJavaVersionByPath(path: Path): JavaVersion {
        val javaVersion = JavaVersion(
            getJavaExecutableVersion(path),
            path.absolutePathString()
        )

        addJavaVersion(javaVersion)

        return javaVersion
    }

    override fun addJavaVersion(javaVersion: JavaVersion) {
        if (javaVersions.any { it.version == javaVersion.version }) {
            throw IllegalArgumentException()
        }

        super.addJavaVersion(javaVersion)
    }

    private fun getJavaExecutableVersion(executable: Path): Int {
        val process = ProcessBuilder()
            .command(executable.absolutePathString(), "-version")
            .start()

        return javaVersionRegex
            .find(process.errorStream.reader().readText())!!
            .groupValues[1]
            .let {
                when {
                    it.contains("1.7") -> 7
                    it.contains("1.8") -> 8
                    else -> it.toInt()
                }
            }
    }
}