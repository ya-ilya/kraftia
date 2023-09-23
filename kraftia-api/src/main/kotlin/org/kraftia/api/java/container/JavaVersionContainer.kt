package org.kraftia.api.java.container

import org.kraftia.api.java.JavaVersion

interface JavaVersionContainer {
    val javaVersions: MutableSet<JavaVersion>

    fun addJavaVersion(javaVersion: JavaVersion) {
        javaVersions.add(javaVersion)
    }

    fun removeJavaVersion(javaVersion: JavaVersion) {
        javaVersions.remove(javaVersion)
    }
}