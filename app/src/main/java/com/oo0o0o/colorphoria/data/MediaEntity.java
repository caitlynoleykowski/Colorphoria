package com.oo0o0o.colorphoria.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by caitoo0o on 7/23/2018.
 */

@Entity
public class MediaEntity {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "download_url")
    private String downloadUrl;

    public MediaEntity(long id, String downloadUrl) {
        this.id = id;
        this.downloadUrl = downloadUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
