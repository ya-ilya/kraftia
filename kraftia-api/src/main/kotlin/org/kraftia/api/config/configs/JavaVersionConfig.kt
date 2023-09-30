package org.kraftia.api.config.configs

import org.kraftia.api.config.AbstractConfig
import org.kraftia.api.config.AbstractConfigClass
import org.kraftia.api.java.JavaVersion
import org.kraftia.api.managers.JavaVersionManager

class JavaVersionConfig(
    private val javaVersions: Set<JavaVersion> = emptySet(),
    private val current: Int? = null
) : AbstractConfig("java") {
    companion object : AbstractConfigClass<JavaVersionConfig>("java", JavaVersionConfig::class) {
        override fun create(): JavaVersionConfig {
            return JavaVersionConfig(
                JavaVersionManager.javaVersions,
                JavaVersionManager.current?.versionNumber
            )
        }

        override fun JavaVersionConfig.apply() {
            for (javaVersion in javaVersions
                .filter { JavaVersionManager.getJavaVersionByNumberOrNull(it.versionNumber) == null }
            ) {
                JavaVersionManager.addJavaVersion(javaVersion)
            }

            JavaVersionManager.current = JavaVersionManager.getJavaVersionByNumberOrNull(current ?: return)
        }
    }
}