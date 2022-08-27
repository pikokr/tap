package io.github.monun.tap.mojangapi

import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * https://wiki.vg/Mojang_API
 */
object MojangAPI {

    @Serializable
    data class Profile(
        val name: String,
        val id: String
    ) {
        fun uuid(): UUID = BigInteger(id, 16).let { bigInteger ->
            return UUID(bigInteger.shiftRight(64).toLong(), bigInteger.toLong())
        }
    }

    private inline fun <reified T> fetch(url: String): CompletableFuture<T?> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build()

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { response ->
            val body = response.body()
            if (body.isBlank()) null else Json.decodeFromString<T>(body)
        }
    }

    fun fetchProfileAsync(username: String) =
        fetch<Profile>("https://api.mojang.com/users/profiles/minecraft/$username")

    fun fetchProfile(username: String) = fetchProfileAsync(username).get()

    @Serializable
    data class SkinProfile(
        val id: String,
        val name: String,
        val properties: List<Property>
    ) {
        fun textureProfile() = properties.find { it.name == "textures" }?.let { textures ->
            val string = Base64.getDecoder().decode(textures.value).decodeToString()
            Json.decodeFromString<TextureProfile>(string)
        }

        fun profileProperties() = properties.map { it.profileProperty() }
    }

    @Serializable
    data class Property(
        val name: String,
        val value: String,
        val signature: String? = null
    ) {
        fun profileProperty() = ProfileProperty(name, value, signature)
    }

    @Serializable
    data class TextureProfile(
        val timestamp: Long,
        val profileId: String,
        val profileName: String,
        val signatureRequired: Boolean,
        val textures: Textures
    )

    @Serializable
    data class Textures(
        val SKIN: TextureURL,
        val CAPE: TextureURL? = null
    )

    @Serializable
    data class TextureURL(val url: String)

    fun fetchSkinProfileAsync(trimmedUUID: String) =
        fetch<SkinProfile>("https://sessionserver.mojang.com/session/minecraft/profile/$trimmedUUID?unsigned=false")

    fun fetchSkinProfile(trimmedUUID: String) = fetchSkinProfileAsync(trimmedUUID).get()

    fun fetchSkinProfileAsync(uuid: UUID) = fetchSkinProfileAsync(uuid.toString().replace("-", ""))

    fun fetchSkinProfile(uuid: UUID) = fetchSkinProfileAsync(uuid.toString().replace("-", "")).get()
}