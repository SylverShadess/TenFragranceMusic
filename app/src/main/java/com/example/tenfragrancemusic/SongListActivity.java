package com.example.tenfragrancemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Song> songs;
    private String type = "";
    static ImageView iv_play, iv_pause;
    private static ImageView iv_img;
//    boolean isUnbind = false;
    private static TextView tv_songName, tv_artist, tv_progress;
    private EditText et_search;
    static MusicService.MusicControl musicControl = MainActivity.musicControl;
//    static MyServiceConn conn;
//    static Intent intent;
    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        init();
    }

    public void init(){
        iv_play = findViewById(R.id.iv_playsong);
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.playlist.getSongs().size()>0){
                    iv_play.setVisibility(View.GONE);
                    iv_pause.setVisibility(View.VISIBLE);
                    if (musicControl.started) {
                        musicControl.continuePlay();
                    } else {
                        musicControl.play();
                    }
                }
            }
        });

        iv_pause = findViewById(R.id.iv_pausesong);
        iv_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_pause.setVisibility(View.GONE);
                iv_play.setVisibility(View.VISIBLE);
                musicControl.pausePlay();
            }
        });

        findViewById(R.id.rl_currentplaying).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SongListActivity.this,PlaylistActivity.class);
                startActivity(intent);
            }
        });
        et_search = findViewById(R.id.searchbox);
        findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAppSongs(type,et_search.getText().toString());
                setData();
            }
        });
        tv_songName = findViewById(R.id.tv_songname);
        tv_artist = findViewById(R.id.tv_songartist);
        tv_progress = findViewById(R.id.tv_progress);
        iv_play = findViewById(R.id.iv_playsong);
        iv_pause = findViewById(R.id.iv_pausesong);
        iv_img = findViewById(R.id.iv_songpic);
        recyclerView = findViewById(R.id.rv_songlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        type = getIntent().getStringExtra("type");
        getAppSongs(type,"");
        setData();

//        intent = new Intent(this, MusicService.class);
//        conn = new MyServiceConn();
//        bindService(intent, conn, BIND_AUTO_CREATE);
    }


    private void getAppSongs(String type, String search){
        songs = new ArrayList<>();
        Bitmap bitmap = getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.baseline_album_124);
        int[] temp = new int[0];
        if(type.equals("jp")){
            temp = new int[]{R.raw.jp0, R.raw.jp1,R.raw.jp2,R.raw.jp3,R.raw.jp4,R.raw.jp5,R.raw.jp6};
        }else if(type.equals("kr")){
            temp = new int[]{R.raw.kr0, R.raw.kr1,R.raw.kr2,R.raw.kr3,R.raw.kr4};
        }else if(type.equals("en")){
            temp = new int[]{R.raw.en0, R.raw.en1, R.raw.en2, R.raw.en3};
        }else if(type.equals("cn")){
            temp = new int[]{R.raw.cn0, R.raw.cn1, R.raw.cn2, R.raw.cn3, R.raw.cn4, R.raw.cn5};
        }else if(type.equals("local")){
            getPermissions(search);
            return;
        }
        if(!type.equals("local")){
            for (int i = 0; i < temp.length; i++) {
                Song song = new Song();
                song.setType("external");
                song.setId(temp[i]);
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + type + i);
                metaRetriever.setDataSource(getApplicationContext(), uri);
                song.setArtist(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                song.setName(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                song.setAlbum_art(bitmap);
                if(containsIgnoreCase(song.getName(), search) || containsIgnoreCase(song.getArtist(), search)){
                    songs.add(song);
                }
            }
        }
    }

    //https://stackoverflow.com/questions/86780/how-to-check-if-a-string-contains-another-string-in-a-case-insensitive-manner-in
    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int currentPosition = bundle.getInt("currentPosition");

            tv_songName.setText(MainActivity.playlist.getCurrentSong().getName());
            tv_artist.setText(MainActivity.playlist.getCurrentSong().getArtist());
            iv_img.setImageBitmap(MainActivity.playlist.getCurrentSong().getAlbum_art());

            String strMinute, strSecond;
            int minutes = currentPosition / 1000 / 60;
            int seconds = currentPosition / 1000 % 60;

            if(minutes<10){strMinute = "0" + minutes;}
            else{strMinute = minutes + "";}

            if(seconds<10){strSecond = "0" + seconds;}
            else{strSecond = seconds + "";}

            tv_progress.setText(strMinute+":"+strSecond);

            if(musicControl!=null && musicControl.started && musicControl.playing){
                iv_play.setVisibility(View.GONE);
                iv_pause.setVisibility(View.VISIBLE);
            }

            if(MainActivity.playlist.getSongs().isEmpty()){
                iv_play.setVisibility(View.GONE);
                iv_pause.setVisibility(View.GONE);
                tv_progress.setText("00:00");
            }
        }
    };


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

    private void readAudioFromDevice(String search){
        songs = new ArrayList<>();
        int count = 0;

        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,null);
        if(MainActivity.localSongs!=null){
            for(int i=0;i<MainActivity.localSongs.size();i++){
                Song song = MainActivity.localSongs.get(i);
                if(containsIgnoreCase(song.getName(), search) || containsIgnoreCase(song.getArtist(), search)){
                    songs.add(song);
                }
            }
            Toast.makeText(this, "Successfully preloaded " + songs.size() + " audio file(s) from your device", Toast.LENGTH_SHORT).show();
            return;
        }
        if(songs!=null){songs.clear();}

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
            if(new File(song.getPath()).exists() && (containsIgnoreCase(song.getName(), search) || containsIgnoreCase(song.getArtist(), search))){
                songs.add(song);
                count++;
            }
        }

        cursor.close();
        Toast.makeText(this, "Successfully read " + count + " audio file(s) from your device", Toast.LENGTH_SHORT).show();
        MainActivity.localSongs = new ArrayList<>(songs);
    }

    private void getPermissions(String search){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] permissionList = new String[]{Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_IMAGES};
            ArrayList<String> list = new ArrayList<>();

            //loop to determine which of the required permissions are not yet authorized
            for(int i=0; i<permissionList.length; i++){
                if(ActivityCompat.checkSelfPermission(this, permissionList[i]) != PackageManager.PERMISSION_GRANTED){ list.add(permissionList[i]); }
            }
            if(list.size() > 0){
                ActivityCompat.requestPermissions(SongListActivity.this, list.toArray(new String[list.size()]), 69);
            }else{
                readAudioFromDevice(search);
//                setData();
            }
        }else{
            readAudioFromDevice(search);
//            setData();
        }
    }

    private void setData() {
        Collections.sort(songs);
        SongListAdapter adapter = new SongListAdapter(SongListActivity.this, songs);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode == 69){
            for(int i=0; i<permissions.length; i++){
                if(permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Successfully read audio album images", Toast.LENGTH_SHORT).show();
                }else if(permissions[i].equals(Manifest.permission.READ_MEDIA_AUDIO) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Successfully read audio files ", Toast.LENGTH_SHORT).show();
                    readAudioFromDevice("");
                    setData();
                }else{
                    Toast.makeText(this, "Failed to read system files", Toast.LENGTH_SHORT).show();
                }
            }
        }else if(requestCode == 70){
            CreateNotification.createNotification(getApplicationContext(), SongListActivity.this, MainActivity.playlist.getCurrentSong(), MainActivity.playlist.getCurrent(), MainActivity.playlist.getSongs().size());
        }
    }
}