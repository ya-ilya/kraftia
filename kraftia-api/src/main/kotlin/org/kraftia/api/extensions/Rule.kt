package org.kraftia.api.extensions

import org.kraftia.api.Api
import org.kraftia.api.version.Rule

fun List<Rule>.checkRules(operatingSystem: Api.OperatingSystem, features: Map<String, Boolean>): Boolean {
    return when {
        isEmpty() -> true

        size == 1 -> {
            val rule = first()

            when {
                rule.features.isNotEmpty() -> {
                    val feature = rule.features.entries.first()

                    !features.containsKey(feature.key) || features[feature.key] == feature.value
                }

                rule.os == null -> {
                    rule.action == Rule.Action.Allow
                }

                else -> {
                    operatingSystem.name.equals(rule.os!!.name, true)
                }
            }
        }

        else -> {
            when {
                any { it.os == null && it.action == Rule.Action.Allow } -> {
                    return !any { operatingSystem.name.equals(it.os?.name, true) && it.action == Rule.Action.Disallow }
                }

                any { operatingSystem.name.equals(it.os!!.name, true) && it.action == Rule.Action.Allow } -> {
                    true
                }

                else -> throw IllegalArgumentException()
            }
        }
    }
}