package com.example.tenfragrancemusic;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class MusicService extends Service {
    public MusicService() {}
    private MediaPlayer player;
    private Timer timer;
    private Playlist playlist = MainActivity.playlist;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player==null){return;}
        if(player.isPlaying()){player.stop();}
        player.release();
        player = null;
    }

    private static int duration, currentPosition;
    public void addTimer(){
        if(timer==null){
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if(player==null){return;}
                    duration = player.getDuration();
                    currentPosition = player.getCurrentPosition();
                    Message msg = SongListActivity.handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currentPosition",currentPosition);
                    int diff = duration - currentPosition;
                    msg.setData(bundle);
                    SongListActivity.handler.sendMessage(msg);
                    if(MainActivity.playlistActivityStarted){
                        Message msg2 = PlaylistActivity.handler.obtainMessage();
                        msg2.setData(bundle);
                        PlaylistActivity.handler.sendMessage(msg2);
                    }

                    MainActivity.swapMenu();

                    if (diff <= 1) {
                        if (MainActivity.musicControl.repeat) {
                            MainActivity.musicControl.play();
                        } else {
                            MainActivity.musicControl.next();
                        }
                    }
                }
            };
            timer.schedule(task,5,500);
        }
    }

//    final static Thread serviceThread = new Thread() {
//        @Override
//        public void run() {
//            try {
//                while(MainActivity.musicControl.isBind) {
//                    while (MainActivity.musicControl.playing) {
//
//                        Thread.sleep(100);
//                    }
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    };


    class MusicControl extends Binder {
        boolean started = false, playing = false, shuffle = false, repeat = false, isBind = false;

        public void play(){
            try {
                player.reset();
                if(playlist.getCurrentSong().getType().equals("local")){player = MediaPlayer.create(getApplicationContext(),Uri.parse(playlist.getCurrentSong().getPath()));}
                else{player = MediaPlayer.create(getApplicationContext(), playlist.getCurrentSong().getId());}
                player.start();
                started = true;
                playing = true;
                addTimer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void next(){
            playlist.next(shuffle);
            if(playlist.getSongs().size()!=1){
                try {
                    player.reset();
                    player = MediaPlayer.create(getApplicationContext(), playlist.getCurrentSong().getId());
                    player.start();
                    started = true;
                    playing = true;
                    addTimer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void prev(){
            playlist.prev(shuffle);
            try {
                player.reset();
                player = MediaPlayer.create(getApplicationContext(), playlist.getCurrentSong().getId());
                player.start();
                started = true;
                playing = true;
                addTimer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void pausePlay(){
            player.pause();
            playing = false;
        }

        public void continuePlay(){
            player.start();
            started = true;
            playing = true;
        }

        public void seekTo(int progress){player.seekTo(progress);}
    }
}