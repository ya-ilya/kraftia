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
                        println("Added java ${javaVersion.version}")
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
                    println("Java version switched to ${JavaVersionManager.current!!.version}")
                }
            )
        )

        builder.then(
            literal("list").executesSuccess {
                println("Java versions:")

                for (java in JavaVersionManager.javaVersions) {
                    println("- ${java.version} (${java.executable})")
                }
            }
        )

        builder.executesSuccess {
            if (JavaVersionManager.current != null) {
                println("Current java version: ${JavaVersionManager.current!!.version} (${JavaVersionManager.current!!.executable})")
            } else {
                println("You are not set java version")
            }
        }
    }
}