package com.example.tenfragrancemusic;

import android.graphics.Bitmap;
import android.net.Uri;

public class Song implements Comparable<Song>{
    private String name, artist, path, type;
    private Uri albumUri;
    private Bitmap album_art;
    private int duration, id, index;

    public Song() {}

    public Song(String name, String artist, int id, Bitmap album_art) {
        this.name = name;
        this.artist = artist;
        this.id = id;
        this.album_art = album_art;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getAlbum_art() {
        return album_art;
    }

    public void setAlbum_art(Bitmap album_art) {
        this.album_art = album_art;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Uri getAlbumUri() {
        return albumUri;
    }

    public void setAlbumUri(Uri albumUri) {
        this.albumUri = albumUri;
    }

    public int getIndex() {return index;}

    public void setIndex(int index) {this.index = index;}

    @Override
    public int compareTo(Song o) {
        if(this.getArtist().equals(o.getArtist())){
            return this.getName().compareTo(o.getName());
        }
        return this.getArtist().compareTo(o.getArtist());
    }
}
