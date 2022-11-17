package br.com.felnanuke.mymusicapp.core.infrastructure.android.data_sources

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Audio
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.documentfile.provider.DocumentFile
import br.com.felnanuke.mymusicapp.core.domain.data_sources.ITracksDataSource
import br.com.felnanuke.mymusicapp.core.domain.entities.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.concurrent.CountedCompleter
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class AndroidTracksDataSource(private val context: Context) : ITracksDataSource {


    private val collection = Audio.Media.EXTERNAL_CONTENT_URI
    private val contentResolver = context.contentResolver
    private val projection = arrayOf(
        Audio.Media._ID,
        Audio.Media.DISPLAY_NAME,
        Audio.Media.ALBUM_ID,
        Audio.Media.ARTIST,
        Audio.Media.DURATION
    )

    private val selection = "${Audio.Media.IS_MUSIC} != 0"


    private var bundle = Bundle()
    private val selectionArgs = arrayOf(
        TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS).toString()


    )

    private val sortOrder = "${Audio.Media.DATE_MODIFIED} DESC"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTracks(onSuccess: (Array<TrackEntity>) -> Unit, onError: () -> Unit) {
        contentResolver.refresh(Audio.Media.EXTERNAL_CONTENT_URI, null, null)
        bundle.clear()
        var tracks = arrayOf<TrackEntity>()
        bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, 999)
//        bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
//        query.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)

        val query = context.contentResolver.query(collection, projection, bundle, null)
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
                    val artist = cursor.getStringOrNull(artistColumIndex) ?: "undefined"
                    val albumId = cursor.getLongOrNull(albumIdColumnIndex)
                    val audioUri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val duration = cursor.getLongOrNull(trackDurationColumnIndex)
                    val albumImage = if (albumId != null) ContentUris.withAppendedId(
                        Audio.Albums.EXTERNAL_CONTENT_URI, albumId
                    ) else null
                    val track = TrackEntity(
                        id,
                        name,
                        artist,
                        audioUri,
                        albumImage,
                        duration ?: 0,
                        getAudioByteStream = { uri, completer ->
                            this.getAudioBytes(
                                uri, completer
                            )
                        },
                    )
                    tracks += track
                }


                onSuccess(tracks)
            }
        } catch (e: Exception) {
            onError()
        }


    }

    override fun getTrack(
        id: Long, onSuccess: (TrackEntity) -> Unit, onError: (Exception) -> Unit
    ) {
        val selection = "${Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val query =
            context.contentResolver.query(collection, projection, selection, selectionArgs, null)
        try {
            query?.use { cursor ->
                val nameColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME)
                val artistColumIndex = cursor.getColumnIndexOrThrow(Audio.Media.ARTIST)
                val albumIdColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID)
                val trackDurationColumnIndex = cursor.getColumnIndexOrThrow(Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val name = cursor.getStringOrNull(nameColumnIndex) ?: "undefined"
                    val artist = cursor.getStringOrNull(artistColumIndex) ?: "undefined"
                    val albumId = cursor.getLongOrNull(albumIdColumnIndex)
                    val audioUri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val duration = cursor.getLongOrNull(trackDurationColumnIndex)
                    val albumImage = if (albumId != null) ContentUris.withAppendedId(
                        Audio.Albums.EXTERNAL_CONTENT_URI, albumId
                    ) else null
                    val track = TrackEntity(
                        id,
                        name,
                        artist,
                        audioUri,
                        albumImage,
                        duration ?: 0,
                        getAudioByteStream = { uri, completer ->
                            this.getAudioBytes(
                                uri, completer
                            )
                        },
                    )
                    onSuccess(track)
                }
            }
        } catch (e: Exception) {
            onError(e)
        }

    }

    private fun getAudioBytes(
        audioUri: Uri, completer: ((InputStream?) -> Unit)? = null
    ) {
        thread {
            completer?.invoke(context.contentResolver.openInputStream(audioUri))
        }
    }


}