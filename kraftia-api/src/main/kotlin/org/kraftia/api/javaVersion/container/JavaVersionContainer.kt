package org.kraftia.api.javaVersion.container

import org.kraftia.api.javaVersion.JavaVersion

interface JavaVersionContainer {
    val javaVersions: MutableSet<JavaVersion>

    fun getJavaVersionByNumber(versionNumber: Int): JavaVersion {
        return getJavaVersionByNumberOrNull(versionNumber)!!
    }

    fun getJavaVersionByNumberOrNull(versionNumber: Int): JavaVersion? {
        return javaVersions.firstOrNull { it.versionNumber == versionNumber }
    }

    fun addJavaVersion(javaVersion: JavaVersion) {
        javaVersions.add(javaVersion)
    }

    fun removeJavaVersion(javaVersion: JavaVersion) {
        javaVersions.remove(javaVersion)
    }
}