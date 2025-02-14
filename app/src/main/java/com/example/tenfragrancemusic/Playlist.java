package com.example.tenfragrancemusic;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private List<Song> songs;
    private List<Song> shuffledSongs;
    private List<Song> playedSongs;
    private int current = 0;
    private int shuffleCurrent = 0;

    public Playlist() {
        songs = new ArrayList<>();
        shuffledSongs = new ArrayList<>();
    }

    public List<Song> getSongs() {return songs;}

    public int findSong(Song song) {
        int index = 0;
        for(Song s:songs){
            if(s.equals(song)){
                return index;
            }
            index++;
        }
        return -1;
    }

    public int findShuffledSong(Song song) {
        int index = 0;
        for(Song s:shuffledSongs){
            if(s.equals(song)){
                return index;
            }
            index++;
        }
        return -1;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getShuffleCurrent() {
        return shuffleCurrent;
    }

    public void setShuffleCurrent(int shuffleCurrent) {
        this.shuffleCurrent = shuffleCurrent;
    }

    public Song getCurrentSong(){
        if(songs.isEmpty()){return new Song();}
//        return getSongsCopy().get(current);
        return songs.get(current);
    }

    public Song getCurrentShuffleSong(){
        if(shuffledSongs.isEmpty()){return new Song();}
//        return getSongsCopy().get(current);
        return shuffledSongs.get(shuffleCurrent);
    }

    public boolean hasNext(boolean shuffle){
        if(shuffle){return shuffleCurrent+1 < shuffledSongs.size();}
        return current+1 < songs.size();
    }

    public void next(boolean shuffle){
        if(shuffle){if(hasNext(true)){
            shuffleCurrent++;
            current = shuffledSongs.get(shuffleCurrent).getIndex();
        }}
        else{if(hasNext(false)){current++;}}
    }

    public void prev(boolean shuffle){
        if(shuffle){if (shuffleCurrent > 0) {
            shuffleCurrent--;
            current = shuffledSongs.get(shuffleCurrent).getIndex();
        }}
        else{if (current > 0) {current--;}}
    }

    public Song getSong(int index){
        return songs.get(index);
//        return getSongsCopy().get(index);
    }

    public void addSong(Song song){
        songs.add(song);
    }

    public void addShuffleSong(Song song){
        shuffledSongs.add(song);
    }

    public void removeSong(Song song){
        songs.remove(song);
        if(current>songs.size()-1 && !songs.isEmpty()){current=songs.size()-1;}
    }

    public List<Song> getShuffledSongs() {
        return shuffledSongs;
    }

    public void setShuffledSongs(List<Song> shuffledSongs) {
        this.shuffledSongs = shuffledSongs;
    }

    public List<Song> getPlayedSongs() {
        return playedSongs;
    }

    public void setPlayedSongs(List<Song> playedSongs) {
        this.playedSongs = playedSongs;
    }
}
