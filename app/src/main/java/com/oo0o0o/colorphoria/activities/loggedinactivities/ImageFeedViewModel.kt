package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.ViewModel;
import androidx.room.Room
import com.oo0o0o.colorphoria.data.AppDatabase
import com.oo0o0o.colorphoria.data.MediaEntity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ImageFeedViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    fun getData(appcontext: Context?, callback: (List<MediaEntity>) -> Unit) {

        if (appcontext == null) {
            return;
        }

        val db = Room.databaseBuilder(appcontext,
                AppDatabase::class.java, "colorphoria-database")
                .fallbackToDestructiveMigration()
                .build()

        val fetchMediaEntities = db.mediaDao().allRx.replay(1).autoConnect()

        // Load the media urls from the database and display the images (if in db, should be cached)
        fetchMediaEntities
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    callback(it)
                }.addTo(disposables)

//        fetchMediaEntities
//                .flatMap {
//                    // TODO Fetch images
//                }.addTo(disposables)

        // TODO attempt to fetch the urls from the server
    }

    fun unsubscribe() {
        disposables.clear()
    }
}
