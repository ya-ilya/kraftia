package org.kraftia.headless.command.commands

import com.mojang.brigadier.arguments.StringArgumentType
import org.kraftia.api.Api
import org.kraftia.api.extensions.path
import org.kraftia.api.managers.InstanceManager
import org.kraftia.headless.command.AbstractCommand
import org.kraftia.headless.command.arguments.InstanceArgument
import org.kraftia.headless.command.arguments.PathArgument
import org.kraftia.headless.command.arguments.VersionArgument

object InstanceCommand : AbstractCommand("instance", "Manage instances") {
    init {
        builder.then(
            literal("create").then(
                argument("name", StringArgumentType.string())
                    .then(
                        literal("version").then(
                            argument("version", VersionArgument()).execute { context ->
                                val name = StringArgumentType.getString(context, "name")
                                val instance = InstanceManager.createVersionInstance(
                                    name,
                                    VersionArgument[context],
                                    path(Api.instancesDirectory, name)
                                )

                                println("Instance ${instance.name} created")
                            }
                        )
                    )
                    .then(
                        literal("modpack").then(
                            argument("modpackPath", PathArgument()).execute { context ->
                                val name = StringArgumentType.getString(context, "name")
                                val instance = InstanceManager.createInstanceFromModpackFile(
                                    name,
                                    path(Api.instancesDirectory, name),
                                    PathArgument[context, "modpackPath"]
                                )

                                println("Instance ${instance.name} created")
                            }
                        )
                    )
            )
        )

        builder.then(
            literal("remove").then(
                argument("instance", InstanceArgument()).execute { context ->
                    val instance = InstanceArgument[context]
                    InstanceManager.removeInstance(instance)
                    println("Instance ${instance.name} removed")
                }
            )
        )

        builder.then(
            literal("list").execute {
                println("Instances:")

                for (instance in InstanceManager.instances) {
                    println("- ${instance.name} (${instance.version}, ${instance.gameDirectory})")
                }
            }
        )
    }
}