package br.com.felnanuke.mymusicapp.core.domain.entities

import android.net.Uri
import java.io.InputStream
import java.util.stream.Stream

data class TrackEntity(val name: String, val artistName: String, val audioUri: Uri, val imageUri: Uri?, val duration: Long,  val getAudioByteStream: (() -> InputStream? )? = null) {




    //constructor that receive a bundle and return a TrackEntity
   constructor(bundle: android.os.Bundle) : this(
        bundle.getString("name")!!,
        bundle.getString("artistName")!!,
        Uri.parse(bundle.getString("audioUri"))!!,
        Uri.parse(bundle.getString("imageUri")),
        bundle.getLong("duration")
    )

    /// get bundle from track entity
    fun getBundle() : android.os.Bundle{
        var bundle = android.os.Bundle()
        bundle.putString("name", name)
        bundle.putString("artistName", artistName)
        bundle.putString("audioUri", audioUri.toString())
        bundle.putString("imageUri", imageUri.toString())
        bundle.putLong("duration", duration)
        return bundle
    }

    override fun equals(other: Any?): Boolean {
        return other is TrackEntity && other.name == name && other.artistName == artistName && other.audioUri == audioUri && other.imageUri == imageUri && other.duration == duration
    }
}