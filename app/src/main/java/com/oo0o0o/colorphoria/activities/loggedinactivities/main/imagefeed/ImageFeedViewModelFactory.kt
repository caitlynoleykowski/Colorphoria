package com.oo0o0o.colorphoria.activities.loggedinactivities.main.imagefeed

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.oo0o0o.colorphoria.model.AppDatabase
import com.oo0o0o.colorphoria.repository.ImageMetadataRepositoryImpl

class ImageFeedViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = Room.databaseBuilder(appContext,
                AppDatabase::class.java, "colorphoria-database").build()
        return ImageFeedViewModel(ImageMetadataRepositoryImpl(appContext, db)) as T
    }
}