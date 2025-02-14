package com.example.tenfragrancemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    static Playlist playlist = new Playlist();
    static List<Song> localSongs;
    static MusicService.MusicControl musicControl;
    static RelativeLayout mainRll;
    static ImageView appTitle;
    static MyServiceConn conn;
    static Intent intent;
    static boolean playlistActivityStarted = false;
    private boolean isUnbind = false;
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.japanese_btn).setOnClickListener(this);
        findViewById(R.id.chinese_btn).setOnClickListener(this);
        findViewById(R.id.korean_btn).setOnClickListener(this);
        findViewById(R.id.english_btn).setOnClickListener(this);
        findViewById(R.id.local_btn).setOnClickListener(this);

        appTitle = findViewById(R.id.app_title);
        mainRll = findViewById(R.id.main_rll);


        intent = new Intent(this, MusicService.class);
        conn = new MainActivity.MyServiceConn();
        bindService(intent, conn, BIND_AUTO_CREATE);

        new Thread(() -> getPermissions()).start();
        createChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
    }

    static void swapMenu(){
        if(musicControl.playing){
//            appTitle.setImageResource(R.drawable.sxyy2);
//            mainRll.setBackgroundResource(R.drawable.chill_tohka);
            ((AnimationDrawable) mainRll.getBackground()).start();
        }else{
            if(mainRll.getBackground() instanceof AnimationDrawable){
                ((AnimationDrawable) mainRll.getBackground()).stop();
            }
//            mainRll.setBackgroundResource(R.color.dark_tohka_purple);
//            appTitle.setImageResource(R.drawable.sxyy);
        }
    }

    @Override
    public void onClick(View v) {
        int vID = v.getId();
        if(vID ==  R.id.japanese_btn){
            Intent intent = new Intent(MainActivity.this,SongListActivity.class);
            intent.putExtra("type","jp");
            startActivity(intent);
        }else if(vID ==  R.id.chinese_btn){
            Intent intent = new Intent(MainActivity.this,SongListActivity.class);
            intent.putExtra("type","cn");
            startActivity(intent);
        }else if(vID ==  R.id.korean_btn){
            Intent intent = new Intent(MainActivity.this,SongListActivity.class);
            intent.putExtra("type","kr");
            startActivity(intent);
        }else if(vID ==  R.id.english_btn){
            Intent intent = new Intent(MainActivity.this,SongListActivity.class);
            intent.putExtra("type","en");
            startActivity(intent);
        } else if(vID ==  R.id.local_btn){
            Intent intent = new Intent(MainActivity.this,SongListActivity.class);
            intent.putExtra("type","local");
            startActivity(intent);
        }
    }

    class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MusicService.MusicControl) service;
            musicControl.isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

    //From https://stackoverflow.com/questions/33696488/getting-bitmap-from-vector-drawable
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void readAudioFromDevice(){
        localSongs = new ArrayList<>();

        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,null);

        if(localSongs!=null){localSongs.clear();}

        while(cursor.moveToNext()){
            Song song = new Song();
            song.setName(cursor.getString(0));
            song.setPath(cursor.getString(1));
            song.setDuration(cursor.getInt(2));
            song.setArtist(cursor.getString(3));
            song.setAlbumUri(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),cursor.getLong(5)));
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), song.getAlbumUri());
            }catch (FileNotFoundException fnfe){
//                fnfe.printStackTrace();
                bitmap = getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.baseline_album_124);
            }
            catch (Exception e){e.printStackTrace();}

            song.setAlbum_art(bitmap);

            song.setType("local");
            if(new File(song.getPath()).exists()){
                localSongs.add(song);
                count++;
            }
        }

        cursor.close();
//        Toast.makeText(this, "Successfully read " + count + " audio file(s) from your device", Toast.LENGTH_SHORT).show();
    }

    private void getPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] permissionList = new String[]{android.Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES};
            ArrayList<String> list = new ArrayList<>();

            //loop to determine which of the required permissions are not yet authorized
            for(int i=0; i<permissionList.length; i++){
                if(ActivityCompat.checkSelfPermission(this, permissionList[i]) != PackageManager.PERMISSION_GRANTED){ list.add(permissionList[i]); }
            }
            if(list.size() > 0){
                ActivityCompat.requestPermissions(MainActivity.this, list.toArray(new String[list.size()]), 69);
            }else{
                readAudioFromDevice();
//                setData();
            }
        }else{
            readAudioFromDevice();
//            setData();
        }
    }

    NotificationManager notificationManager;
    private void createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    musicControl.prev();
                    if(musicControl.shuffle){
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
                    }else{
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
                    }
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (musicControl != null && musicControl.started && !musicControl.playing) {
                        musicControl.continuePlay();
                    } else if (musicControl == null) {
                        musicControl.play();
                    } else if (musicControl != null && musicControl.started && musicControl.playing) {
                        musicControl.pausePlay();
                    }
                    if(musicControl.shuffle){
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
                    }else{
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    musicControl.next();
                    if(musicControl.shuffle){
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentShuffleSong(), MainActivity.playlist.getShuffleCurrent(), MainActivity.playlist.getShuffledSongs().size());
                    }else{
                        CreateNotification.createNotification(getApplicationContext(), MainActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
                    }
                    break;
            }
        }
    };

    private void unbind(){
        if(!isUnbind){
            musicControl.pausePlay();
            musicControl.isBind=false;
            unbindService(conn);
            stopService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbind();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }
}