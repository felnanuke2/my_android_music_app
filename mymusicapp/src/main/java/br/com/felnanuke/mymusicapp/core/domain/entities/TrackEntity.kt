package br.com.felnanuke.mymusicapp.core.domain.entities

import android.net.Uri
import java.io.InputStream

open class TrackEntity(
    val id: Long,
    val name: String,
    val artistName: String,
    val audioUri: Uri,
    val imageUri: Uri?,
    val duration: Long,
) {
    lateinit var getInputStream: (uri: Uri, completer: (InputStream?) -> Unit) -> Unit

    constructor(
        id: Long,
        name: String,
        artistName: String,
        audioUri: Uri,
        imageUri: Uri?,
        duration: Long,
        getAudioByteStream: (uri: Uri, completer: (InputStream?) -> Unit) -> Unit = { _, _ -> },
    ) : this(
        id,
        name,
        artistName,
        audioUri,
        imageUri,
        duration,
    ) {
        this.getInputStream = getAudioByteStream
    }

    fun getAudioByteStream(completer: (InputStream?) -> Unit) {
        getInputStream(audioUri, completer)
    }


    //constructor that receive a bundle and return a TrackEntity
    constructor(
        bundle: android.os.Bundle
    ) : this(
        bundle.getLong("id"),
        bundle.getString("name")!!,
        bundle.getString("artistName")!!,
        Uri.parse(bundle.getString("audioUri"))!!,
        Uri.parse(bundle.getString("imageUri")),
        bundle.getLong("duration")
    )

    /// get bundle from track entity
    fun getBundle(): android.os.Bundle {
        val bundle = android.os.Bundle()
        bundle.putString("name", name)
        bundle.putString("artistName", artistName)
        bundle.putString("audioUri", audioUri.toString())
        bundle.putString("imageUri", imageUri.toString())
        bundle.putLong("duration", duration)
        bundle.putLong("id", id)
        return bundle
    }


}