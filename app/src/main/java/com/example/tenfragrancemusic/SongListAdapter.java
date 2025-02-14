package com.example.tenfragrancemusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder>{
    private Context mContext;
    private List<Song> songList;

    public SongListAdapter(Context context, List<Song> songList){
        this.mContext = context;
        this.songList = songList;
    }

    @Override
    public SongListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_item,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(SongListAdapter.MyViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tv_name.setText(song.getName());
        holder.tv_artist.setText(song.getArtist());
        holder.iv_photo.setImageBitmap(song.getAlbum_art());
        holder.id = song.getId();
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song.setIndex(MainActivity.playlist.getSongs().size());
                MainActivity.playlist.addSong(song);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.playlist.addSong(song);
                MainActivity.playlist.addShuffleSong(song);
                if(MainActivity.musicControl.shuffle){
                    MainActivity.playlist.setShuffleCurrent(MainActivity.playlist.getShuffledSongs().size()-1);
                }else{
                    MainActivity.playlist.setCurrent(MainActivity.playlist.getSongs().size()-1);
                }
                MainActivity.musicControl.play();
            }
        });
    }

    @Override
    public int getItemCount() {return songList.size();}

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name, tv_artist;
        ImageView iv_photo, add;
        int id;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_songname);
            tv_artist = view.findViewById(R.id.tv_songartist);
            iv_photo = view.findViewById(R.id.iv_songimg);
            add = view.findViewById(R.id.iv_addsong);
        }
    }
}
