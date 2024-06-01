package org.kraftia.api.instance.container

import org.kraftia.api.instance.Instance

interface InstanceContainer {
    val instances: MutableSet<Instance>

    fun getInstance(name: String): Instance {
        return getInstanceByNameOrNull(name)!!
    }

    fun getInstanceByNameOrNull(name: String): Instance? {
        return instances.firstOrNull { it.name == name }
    }

    fun addInstance(instance: Instance) {
        instances.add(instance)
    }

    fun removeInstance(instance: Instance) {
        instances.remove(instance)
    }
}