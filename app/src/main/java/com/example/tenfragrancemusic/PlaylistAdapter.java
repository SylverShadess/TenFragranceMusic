package com.example.tenfragrancemusic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder>{
    private Context mContext;
    private static Playlist playlist = MainActivity.playlist;
    public PlaylistAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.playlist_item,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(PlaylistAdapter.MyViewHolder holder, int position) {
        Song song = playlist.getSong(position);
        holder.index = holder.getBindingAdapterPosition();
        holder.tv_name.setText(song.getName());
        holder.tv_artist.setText(song.getArtist());
        holder.iv_photo.setImageBitmap(song.getAlbum_art());
        holder.id = song.getId();
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.index<playlist.getCurrent()){playlist.setCurrent(playlist.getCurrent()-1);}
                else if(holder.index==playlist.getCurrent() && playlist.getSongs().size()>1){MainActivity.musicControl.play();}
                else if(playlist.getSongs().size()==1){MainActivity.musicControl.pausePlay();}
                MainActivity.playlist.removeSong(song);
                notifyItemRemoved(holder.index);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist.setCurrent(holder.index);
                MainActivity.musicControl.play();
                PlaylistActivity.continue_btn.setVisibility(View.GONE);
                PlaylistActivity.pause_btn.setVisibility(View.VISIBLE);
//                notifyDataSetChanged();
            }
        });

        if(playlist.getCurrent()==position){holder.itemView.setBackgroundResource(R.color.light_tohka_purple);}
        else{holder.itemView.setBackgroundResource(R.color.darker_tohka_purple);}
    }

    @Override
    public int getItemCount() {return playlist.getSongs().size();}

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name, tv_artist;
        ImageView iv_photo, remove;
        int id, index;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tvplay_name);
            tv_artist = view.findViewById(R.id.tvplay_artist);
            iv_photo = view.findViewById(R.id.ivplay_img);
            remove = view.findViewById(R.id.iv_delsong);
        }
    }
}
