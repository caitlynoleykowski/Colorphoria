package com.oo0o0o.colorphoria.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by caitoo0o on 7/23/2018.
 */

@Database(entities = {MediaEntity.class}, version = 2)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();
}