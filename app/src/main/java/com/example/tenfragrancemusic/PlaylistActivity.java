package com.example.tenfragrancemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private static SeekBar seekBar;
    private static TextView tv_progress, tv_total, tv_title, tv_artist;
    static Button continue_btn, pause_btn;
    private static PlaylistAdapter playlistAdapter;
    private static ObjectAnimator animator;
    private static MusicService.MusicControl musicControl = MainActivity.musicControl;
    private static int currentPosition, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        init();
    }

    public void init(){
        tv_progress = findViewById(R.id.tv_progress);
        tv_total = findViewById(R.id.tv_total);
        tv_artist = findViewById(R.id.tv_artist);
        tv_artist.setText(MainActivity.playlist.getCurrentSong().getArtist());
        tv_title = findViewById(R.id.tv_music_title);
        tv_title.setText(MainActivity.playlist.getCurrentSong().getName());
        pause_btn = findViewById(R.id.btn_pause);
        pause_btn.setOnClickListener(this);
        continue_btn = findViewById(R.id.btn_continue);
        continue_btn.setOnClickListener(this);
        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_shuffle).setOnClickListener(this);
        findViewById(R.id.btn_repeat).setOnClickListener(this);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_progress.setText(progressIntToString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                musicControl.seekTo(progress);
//                if(progress==seekBar.getMax()){
//                    musicControl.next();
//                    updateAdapter();
//                }
            }
        });

        ImageView iv_music = findViewById(R.id.iv_music);
        animator = ObjectAnimator.ofFloat(iv_music, "rotation",0f,360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

        recyclerView = findViewById(R.id.rv_playlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistAdapter = new PlaylistAdapter(PlaylistActivity.this);
        recyclerView.setAdapter(playlistAdapter);

        if(musicControl!=null && musicControl.started && musicControl.playing){
            continue_btn.setVisibility(View.GONE);
            pause_btn.setVisibility(View.VISIBLE);
            animator.start();
        }

        if(musicControl.shuffle){findViewById(R.id.btn_shuffle).setBackgroundResource(R.drawable.baseline_shuffle_on_124);}
        else{findViewById(R.id.btn_shuffle).setBackgroundResource(R.drawable.baseline_shuffle_off_124);}

        if(musicControl.repeat){findViewById(R.id.btn_repeat).setBackgroundResource(R.drawable.baseline_repeat_one_on_124);}
        else{findViewById(R.id.btn_repeat).setBackgroundResource(R.drawable.baseline_repeat_one_off_124);}

        MainActivity.playlistActivityStarted = true;
    }

    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            duration = bundle.getInt("duration");
            currentPosition = bundle.getInt("currentPosition");
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            tv_title.setText(MainActivity.playlist.getCurrentSong().getName());
            tv_artist.setText(MainActivity.playlist.getCurrentSong().getArtist());

            tv_total.setText(progressIntToString(duration));
            tv_progress.setText(progressIntToString(currentPosition));

            playlistAdapter.notifyDataSetChanged();

            if(MainActivity.playlist.getSongs().isEmpty()){
                animator.pause();
                tv_progress.setText("00:00");
                tv_total.setText("00:00");
                seekBar.setProgress(0);
                musicControl.playing = false;
            }
        }
    };

    private static String progressIntToString(int progress){
        int minutes = progress / 1000 / 60;
        int seconds = progress / 1000 % 60;

        String strMinute, strSecond;

        if(minutes<10){strMinute = "0" + minutes;}
        else{strMinute = minutes + "";}

        if(seconds<10){strSecond = "0" + seconds;}
        else{strSecond = seconds + "";}

        return strMinute+":"+strSecond;
    }

    @Override
    public void onClick(View v) {
        int vID = v.getId();
        if(vID == R.id.btn_next) {
            musicControl.next();
            animator.start();
            if(musicControl.shuffle){
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }else{
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }
        else if(vID == R.id.btn_prev) {
            musicControl.prev();
            animator.start();
            if(musicControl.shuffle){
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }else{
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }
        else if(vID == R.id.btn_continue){
            if (musicControl!=null && musicControl.started) {
                musicControl.continuePlay();
                if(animator.isPaused()){animator.resume();}
            } else if(musicControl!=null){
                musicControl.play();
                animator.start();
            }
            SongListActivity.iv_pause.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_continue).setVisibility(View.GONE);
            findViewById(R.id.btn_pause).setVisibility(View.VISIBLE);
            if(musicControl.shuffle){
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }else{
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }
        else if(vID == R.id.btn_pause){
            musicControl.pausePlay();
            findViewById(R.id.btn_pause).setVisibility(View.GONE);
            findViewById(R.id.btn_continue).setVisibility(View.VISIBLE);
            animator.pause();
            if(musicControl.shuffle){
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }else{
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }else if(vID == R.id.btn_shuffle){
            if(musicControl.shuffle){
                musicControl.shuffle=false;
                findViewById(R.id.btn_shuffle).setBackgroundResource(R.drawable.baseline_shuffle_off_124);
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }
            else{
                Playlist playlist = MainActivity.playlist;
                playlist.setShuffledSongs(new ArrayList<>(playlist.getSongs()));
                Collections.shuffle(playlist.getShuffledSongs());
                playlist.getShuffledSongs().remove(playlist.getCurrentSong());
                playlist.getShuffledSongs().add(0, playlist.getCurrentSong());
                playlist.setShuffleCurrent(0);
                musicControl.shuffle=true;
                findViewById(R.id.btn_shuffle).setBackgroundResource(R.drawable.baseline_shuffle_on_124);
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }else if(vID == R.id.btn_repeat){
            if(musicControl.repeat){
                musicControl.repeat=false;
                findViewById(R.id.btn_repeat).setBackgroundResource(R.drawable.baseline_repeat_one_off_124);
            }
            else{
                musicControl.repeat=true;
                findViewById(R.id.btn_repeat).setBackgroundResource(R.drawable.baseline_repeat_one_on_124);
            }
            if(musicControl.shuffle){
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
            }else{
                CreateNotification.createNotification(getApplicationContext(), PlaylistActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
            }
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbind(isUnbind);
//    }
}