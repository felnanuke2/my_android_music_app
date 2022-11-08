package br.com.felnanuke.mymusicapp.core.infrastructure.android.models

import android.net.Uri
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import java.io.InputStream
import java.util.*


class ListableTrackModel(
    id: Long,
    name: String,
    artistName: String,
    audioUri: Uri,
    imageUri: Uri?,
    duration: Long,
    getAudioByteStream: (() -> InputStream?)? = null,
    private val uuid: UUID
) : TrackEntity(id, name, artistName, audioUri, imageUri, duration, getAudioByteStream) {
    constructor(track: TrackEntity) : this(
        track.id,
        track.name,
        track.artistName,
        track.audioUri,
        track.imageUri,
        track.duration,
        track.getAudioByteStream,
        UUID.randomUUID()
    )

    override fun equals(other: Any?): Boolean {
        return other is ListableTrackModel && other.uuid == this.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}