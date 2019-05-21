package com.oo0o0o.colorphoria.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by caitoo0o on 7/23/2018.
 */
@Database(entities = {MediaEntity.class}, version = 2)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();
}