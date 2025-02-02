package org.kraftia.api.extensions

import org.kraftia.api.Api
import org.kraftia.api.version.Rule

fun checkRules(
    rules: List<Rule>,
    operatingSystem: Api.OperatingSystem,
    features: Map<String, Boolean>
): Boolean {
    return when {
        rules.any { it.os == null && it.features.isEmpty() } -> {
            val defaultRule = rules.first { it.os == null && it.features.isEmpty() }
            val otherRules = rules.filter { it != defaultRule }

            for (rule in otherRules) {
                if (checkRule(rule, operatingSystem, features)) {
                    return true
                }
            }

            defaultRule.action == Rule.Action.Allow
        }

        else -> {
            rules.all { checkRule(it, operatingSystem, features) }
        }
    }
}

fun checkRule(
    rule: Rule,
    operatingSystem: Api.OperatingSystem,
    features: Map<String, Boolean>
): Boolean {
    return when {
        rule.os != null && rule.features.isNotEmpty() -> {
            checkFeatures(rule, features) && if (checkIsCompatibleOperatingSystem(rule, operatingSystem)) {
                rule.action == Rule.Action.Allow
            } else {
                false
            }
        }

        rule.os != null -> {
            if (checkIsCompatibleOperatingSystem(rule, operatingSystem)) {
                rule.action == Rule.Action.Allow
            } else {
                false
            }
        }

        rule.features.isNotEmpty() -> {
            checkFeatures(rule, features)
        }

        else -> {
            rule.action == Rule.Action.Allow
        }
    }
}

fun checkIsCompatibleOperatingSystem(
    rule: Rule,
    operatingSystem: Api.OperatingSystem
): Boolean {
    return rule.os!!.name == operatingSystem.name
}

fun checkFeatures(
    rule: Rule,
    features: Map<String, Boolean>
): Boolean {
    return rule.features.all { features.containsKey(it.key) && features[it.key] == it.value }
}