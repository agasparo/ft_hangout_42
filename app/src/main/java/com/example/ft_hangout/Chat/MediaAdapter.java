package com.example.ft_hangout.Chat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangout.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MeadiaViewHolder> {

    ArrayList<String> mediaList;
    Context context;

    public MediaAdapter(Context context, ArrayList<String> mediaList) {

        this.mediaList = mediaList;
        this.context = context;
    }

    @NonNull
    @Override
    public MeadiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        MeadiaViewHolder mediaViewHolder = new MeadiaViewHolder(layoutView);

        return mediaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeadiaViewHolder holder, int position) {

        Uri uri = Uri.parse(mediaList.get(position));
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(holder.mMdedia.getContext().getContentResolver(), uri);
            Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap,500,400);
            holder.mMdedia.setImageBitmap(thumbBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        return mediaList.size();
    }

    public class MeadiaViewHolder extends RecyclerView.ViewHolder {

        ImageView mMdedia;

        public MeadiaViewHolder(@NonNull View itemView) {

            super(itemView);

            mMdedia = itemView.findViewById(R.id.media);
        }
    }
}
