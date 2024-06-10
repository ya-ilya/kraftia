package org.kraftia.api.managers

import org.kraftia.api.Api
import org.kraftia.api.javaVersion.JavaVersion
import org.kraftia.api.javaVersion.container.JavaVersionContainer
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object JavaVersionManager : JavaVersionContainer {
    private val JAVA_VERSION_REGEX = "version \"(.*?)\"".toRegex()

    override val javaVersions = mutableSetOf<JavaVersion>()

    var current: JavaVersion? = null
        get() {
            if (field != null && !javaVersions.contains(field)) {
                field = null
            }

            return field
        }

    init {
        if (Api.javaExecutablePath != null) {
            current = addJavaVersionByPath(Api.javaExecutablePath)
        }
    }

    fun addJavaVersionByPath(path: Path): JavaVersion? {
        val javaVersion = JavaVersion(
            getJavaVersionNumber(path),
            path.absolutePathString()
        )

        if (getJavaVersionByNumberOrNull(javaVersion.versionNumber) != null) {
            return null
        } else {
            addJavaVersion(javaVersion)
        }

        return javaVersion
    }

    private fun getJavaVersionNumber(path: Path): Int {
        val process = ProcessBuilder()
            .command(path.absolutePathString(), "-version")
            .start()

        return try {
            JAVA_VERSION_REGEX
                .find(process.errorStream.reader().readText())!!
                .groupValues[1]
                .let {
                    when {
                        it.contains("1.7") -> 7
                        it.contains("1.8") -> 8
                        else -> try {
                            it.split(".")[0].toInt()
                        } catch (ex: Exception) {
                            8
                        }
                    }
                }
        } finally {
            process.destroy()
        }
    }
}
