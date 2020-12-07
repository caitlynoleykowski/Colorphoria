package com.oo0o0o.colorphoria.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Created by caitoo0o on 7/23/2018.
 */

@Entity
public class MyMediaEntity {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "username")
    String username;
    @ColumnInfo(name = "download_url")
    String downloadUrl;
    @ColumnInfo(name = "filename")
    String filename;
    @ColumnInfo(name = "privacy")
    String privacy;
    @ColumnInfo(name = "comment")
    String comment;
    @ColumnInfo(name = "timestamp")
    Date timestamp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    public String getDownloadUrl() {
        return downloadUrl;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
    public String getPrivacy() {
        return privacy;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return comment;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public Date getTimestamp() {
        return timestamp;
    }
}