package com.oo0o0o.colorphoria.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Created by caitoo0o on 7/23/2018.
 */

@Dao
public interface MediaDao {
    @Query("SELECT * FROM mediaentity")
    Flowable<List<MediaEntity>> getAllRx();

    @Query("SELECT * FROM mediaentity")
    List<MediaEntity> getAll();

//    @Query("SELECT * FROM mediaentity WHERE ud IN (:ids)")
//    List<MediaEntity> loadAllByIds(int[] ids);
//
//    @Query("SELECT * FROM mediaentity WHERE download_url LIKE :first AND "
//            + "download_url LIKE :last LIMIT 1")
//    MediaEntity findByDownloadUrl(String download_url);
//
    @Insert
    void insertAll(MediaEntity... mediaEntities);

//    @Delete
//    void delete(MediaEntity mediaEntity);

    @Query("DELETE FROM mediaentity")
    void deleteAll();

    @Query("DELETE FROM mediaentity")
    Completable deleteAllRx();
}