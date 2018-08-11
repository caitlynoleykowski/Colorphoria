package com.oo0o0o.colorphoria.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by caitoo0o on 7/23/2018.
 */

@Dao
public interface MediaDao {
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
}