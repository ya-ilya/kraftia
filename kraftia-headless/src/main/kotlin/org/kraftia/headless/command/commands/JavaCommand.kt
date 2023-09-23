package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.JavaArgument
import java.nio.file.Paths
import kotlin.io.path.exists

object JavaCommand : AbstractCommand("java", "Manage java versions") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(
            literal("add").then(
                argument("executable", StringArgumentType.greedyString()).executesSuccess { context ->
                    val path = Paths.get(StringArgumentType.getString(context, "executable"))

                    if (path.exists()) {
                        val javaVersion = JavaVersionManager.addJavaVersionByPath(path)
                            ?: return@executesSuccess println("Java version with same version number already exists")

                        println("Added java ${javaVersion.versionNumber}")
                    } else {
                        println("Path to java executable doesn't exists")
                    }
                }
            )
        )

        builder.then(
            literal("set").then(
                argument("java", JavaArgument()).executesSuccess { context ->
                    JavaVersionManager.current = JavaArgument[context]
                    println("Java version switched to ${JavaVersionManager.current!!.versionNumber}")
                }
            )
        )

        builder.then(
            literal("remove").then(
                argument("java", JavaArgument()).executesSuccess { context ->
                    val javaVersion = JavaArgument[context]

                    JavaVersionManager.removeJavaVersion(javaVersion)
                    println("Removed ${javaVersion.versionNumber} (${javaVersion.executable}) java version")
                }
            )
        )

        builder.then(
            literal("list").executesSuccess {
                println("Java versions:")

                for (java in JavaVersionManager.javaVersions) {
                    println("- ${java.versionNumber} (${java.executable})")
                }
            }
        )

        builder.executesSuccess {
            if (JavaVersionManager.current != null) {
                println("Current java version: ${JavaVersionManager.current!!.versionNumber} (${JavaVersionManager.current!!.executable})")
            } else {
                println("You are not set java version")
            }
        }
    }
}