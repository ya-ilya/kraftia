package org.kraftia.api.version.container

import org.kraftia.api.version.Version

interface VersionContainer {
    val versions: MutableSet<Version>

    fun getVersionById(id: String): Version {
        return getVersionByIdOrNull(id)!!
    }

    fun getVersionByIdOrNull(id: String): Version? {
        return versions.firstOrNull { it.id == id }
    }

    fun addVersion(version: Version) {
        versions.add(version)
    }

    fun removeVersion(version: Version) {
        versions.remove(version)
    }
}