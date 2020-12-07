package com.oo0o0o.colorphoria.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.oo0o0o.colorphoria.backgroundtasks.RestHelperKotlinRx
import com.oo0o0o.colorphoria.model.AppDatabase
import com.oo0o0o.colorphoria.model.MediaDao
import com.oo0o0o.colorphoria.model.MediaEntity
import com.oo0o0o.colorphoria.utils.Utils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

interface ImageMetadataRepository {
    fun fetchFeedImages(): Single<Unit>
//    fun fetchAccountImages()
    fun getFeedImages(): Flowable<List<MediaEntity>>
//    fun getAccountImages()
}

//class ImageMetadataRepositoryImpl(val context: Context, val mediaDao: MediaDao) : ImageMetadataRepository {
class ImageMetadataRepositoryImpl(val context: Context, val db: AppDatabase) : ImageMetadataRepository {

    override fun fetchFeedImages(): Single<Unit> {
        val user = FirebaseAuth.getInstance().currentUser!!
        return RestHelperKotlinRx.recentImages(context, user)
                    .subscribeOn(Schedulers.io())
                    .map { response ->
                        db.mediaDao().deleteAll()

                        val mediaEntities = ArrayList<MediaEntity>()

                        try {
                            val responseJson = JSONObject(response)
                            if (responseJson.has("mediaEntities")) {
                                // TODO: Handle this better in case somehow it has "urls" but the array is empty

                                val mediaEntitiesJson = responseJson.getJSONArray("mediaEntities")
                                val gson = Gson()
                                for (i in 0 until mediaEntitiesJson.length()) {
                                    val mediaEntity = gson.fromJson(mediaEntitiesJson.get(i).toString(), MediaEntity::class.java)
                                    mediaEntity.id = i.toLong()
                                    db.mediaDao().insertAll(mediaEntity) // Change id from i
                                    mediaEntities.add(mediaEntity)
                                }
                            }
                        } catch (e: JSONException) {
                            // TODO: Handle this
                            Log.i(this.javaClass.name, "Error parsing json.", e)
                        }

                        return@map mediaEntities
                    }.map {
                        return@map Unit
                    }
    }

    override fun getFeedImages(): Flowable<List<MediaEntity>> {
        // Load the media urls from the database and display the images (if in db, should be cached)
        return  db.mediaDao().allRx
                .subscribeOn(Schedulers.io())
    }

}