package com.oo0o0o.colorphoria.model

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oo0o0o.colorphoria.R

class RoomSingleton {
    private var instance: RoomDatabase? = null

    // This is not
    fun getInstance(appContext: Context): RoomDatabase {
        instance?.let {
            return it
        } ?: run {
            instance =
                    Room.databaseBuilder(
                            appContext,
                            AppDatabase::class.java,
                            appContext.getString(R.string.database_name))
                            .fallbackToDestructiveMigration()
                            .build()
            return instance!!  // must force unwrap here because instance could have been changed on another thread
                                // however, if it was, it would have been assigned to the same thing, so there is no issue
        }
    }
}