package org.kraftia.api.config.configs

import org.kraftia.api.config.AbstractConfig
import org.kraftia.api.config.AbstractConfigClass
import org.kraftia.api.instance.Instance
import org.kraftia.api.managers.InstanceManager

class InstanceConfig(
    private val instances: Set<Instance> = emptySet()
) : AbstractConfig() {
    companion object : AbstractConfigClass<InstanceConfig>("instances", InstanceConfig::class) {
        override fun create(): InstanceConfig {
            return InstanceConfig(InstanceManager.instances)
        }

        override fun InstanceConfig.apply() {
            for (instance in instances) {
                InstanceManager.addInstance(instance)
            }
        }
    }
}