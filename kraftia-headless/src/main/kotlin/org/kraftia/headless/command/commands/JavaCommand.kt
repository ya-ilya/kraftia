package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import org.kraftia.api.managers.JavaVersionManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.JavaVersionArgument
import java.nio.file.Paths
import kotlin.io.path.exists

object JavaCommand : AbstractCommand("java", "Manage java versions") {
    init {
        builder.then(
            literal("add").then(
                argument("executable", StringArgumentType.greedyString()).execute { context ->
                    val path = Paths.get(StringArgumentType.getString(context, "executable"))

                    if (path.exists()) {
                        val javaVersion = JavaVersionManager.addJavaVersionByPath(path)
                            ?: return@execute println("Java version with same version number already exists")

                        println("Added java ${javaVersion.versionNumber}")
                    } else {
                        println("Path to java executable doesn't exists")
                    }
                }
            )
        )

        builder.then(
            literal("set").then(
                argument("java", JavaVersionArgument()).execute { context ->
                    JavaVersionManager.current = JavaVersionArgument[context]
                    println("Java version switched to ${JavaVersionManager.current!!.versionNumber}")
                }
            )
        )

        builder.then(
            literal("remove").then(
                argument("java", JavaVersionArgument()).execute { context ->
                    val javaVersion = JavaVersionArgument[context]

                    JavaVersionManager.removeJavaVersion(javaVersion)
                    println("Removed ${javaVersion.versionNumber} (${javaVersion.executable}) java version")
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Java versions:")

                for (java in JavaVersionManager.javaVersions) {
                    println("- ${java.versionNumber} (${java.executable})")
                }
            }
        )

        builder.execute {
            if (JavaVersionManager.current != null) {
                println("Current java version: ${JavaVersionManager.current!!.versionNumber} (${JavaVersionManager.current!!.executable})")
            } else {
                println("You are not set java version")
            }
        }
    }
}