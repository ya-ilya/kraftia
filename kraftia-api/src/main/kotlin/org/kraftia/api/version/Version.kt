package org.kraftia.api.version

import com.google.gson.annotations.SerializedName

data class Version(
    var inheritsFrom: String? = null,
    @SerializedName("arguments", alternate = ["minecraftArguments"])
    var arguments: Arguments? = null,
    var assetIndex: AssetIndex? = null,
    var assets: String? = null,
    var complianceLevel: Int? = null,
    var downloads: Downloads? = null,
    var id: String? = null,
    var javaVersion: JavaVersion? = null,
    var libraries: List<Library> = emptyList(),
    var logging: Logging? = null,
    var mainClass: String? = null,
    var minimumLauncherVersion: Int? = null,
    var releaseTime: String? = null,
    var time: String? = null,
    var type: String? = null
)

data class JavaVersion(
    var component: String? = null,
    var majorVersion: Int? = null
)

data class Rule(
    var action: Action? = null,
    var os: Os? = null,
    val features: Map<String, Boolean> = emptyMap()
) {
    enum class Action {
        @SerializedName("allow")
        Allow,

        @SerializedName("disallow")
        Disallow
    }

    data class Os(
        var name: String? = null
    )
}

data class Arguments(
    var game: List<Argument> = emptyList(),
    var jvm: List<Argument> = emptyList()
) {
    data class Argument(
        var rules: List<Rule> = emptyList(),
        var value: List<String> = emptyList()
    )
}

data class AssetIndex(
    var id: String? = null,
    var sha1: String? = null,
    var size: Int? = null,
    var totalSize: Int? = null,
    var url: String? = null
)

data class Downloads(
    var client: Client? = null,
    var clientMappings: ClientMappings? = null,
    var server: Server? = null,
    var serverMappings: ServerMappings? = null
) {
    data class Client(
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )

    data class ClientMappings(
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )

    data class Server(
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )

    data class ServerMappings(
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )
}

data class Library(
    var downloads: Downloads? = null,
    var name: String? = null,
    var rules: List<Rule> = emptyList(),
    var natives: Map<String, String> = emptyMap(),
    var extract: Map<String, List<String>> = emptyMap()
) {
    data class Downloads(
        var artifact: Artifact? = null,
        var classifiers: Map<String, Artifact> = emptyMap()
    )

    data class Artifact(
        var path: String? = null,
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )
}

data class Logging(
    var client: Client? = null
) {
    data class Client(
        var argument: String? = null,
        var file: File? = null,
        var type: String? = null
    )

    data class File(
        var id: String? = null,
        var sha1: String? = null,
        var size: Int? = null,
        var url: String? = null
    )
}