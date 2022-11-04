package br.com.felnanuke.mymusicapp.core.infrastructure.android.data_sources

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import br.com.felnanuke.mymusicapp.core.domain.data_sources.TracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import android.provider.MediaStore.Audio
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import java.io.InputStream
import java.util.concurrent.TimeUnit

class AndroidTracksDataSource(private val context: Context) : TracksDataSource {


    private val collection = Audio.Media.EXTERNAL_CONTENT_URI
    private val contentResolver = context.contentResolver
    private val projection = arrayOf(
        Audio.Media._ID, Audio.Media.DISPLAY_NAME, Audio.Media.ALBUM_ID, Audio.Media.ARTIST
    )

    private val selection = "${Audio.Media.IS_MUSIC} != 0"


    private var query = Bundle()
    private val selectionArgs = arrayOf(
        TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS).toString()


    )

    private val sortOrder = "${Audio.Media.DISPLAY_NAME} ASC"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit) {
        query.clear()
        var tracks = arrayOf<TrackEntity>()
        query.putInt(ContentResolver.QUERY_ARG_LIMIT, 5)
//        query.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
//        query.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        query.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)

        val query = context.contentResolver.query(collection, null, null, null)
        try {
            query?.use { cursor ->
                val nameColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME)
                val idColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media._ID)
                val artistColumIndex = cursor.getColumnIndexOrThrow(Audio.Media.ARTIST)
                val albumIdColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID)
                val trackDurationColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumnIndex)
                    val name = cursor.getStringOrNull(nameColumnIndex) ?: "undefined"
                    val artist = cursor.getString(artistColumIndex)
                    val albumId = cursor.getLongOrNull(albumIdColumnIndex)
                    val audioUri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val duration = cursor.getLongOrNull(trackDurationColumnIndex)
                    val albumImage = if (albumId != null) ContentUris.withAppendedId(
                        Audio.Albums.EXTERNAL_CONTENT_URI, albumId
                    ) else null
                    val track = TrackEntity(name,
                        artist,
                        audioUri,
                        albumImage,
                        duration ?: 0,
                        getAudioByteStream = { this.getAudioBytes(audioUri) })
                    tracks += track
                }
                onSuccess(tracks)
            }
        } catch (e: Exception) {
            onError()
        }


    }

    private fun getAudioBytes(audioUri: Uri): InputStream? {
        return context.contentResolver.openInputStream(audioUri)
    }

}