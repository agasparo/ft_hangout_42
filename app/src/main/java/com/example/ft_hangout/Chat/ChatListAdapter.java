package com.example.ft_hangout.Chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangout.ChatActivity;
import com.example.ft_hangout.R;
import com.example.ft_hangout.UserInfosActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    ArrayList<ChatObject> ChatList;

    public ChatListAdapter(ArrayList<ChatObject> ChatList) {

        this.ChatList = ChatList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ChatListViewHolder rcv = new ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, int position) {

        holder.mTitle.setText(ChatList.get(position).getChatName());

        if (ChatList.get(position).getChatImg().equals("none"))
            holder.mImageView.setImageResource(R.drawable.users);
        else
            new ChatListAdapter.DownloadImageTask(holder.mImageView).execute(ChatList.get(position).getChatImg());

        holder.mlastMessage.setText(ChatList.get(position).getLastMsg());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("chatID", ChatList.get(holder.getAdapterPosition()).getChatId());
                bundle.putString("chatReceveID", ChatList.get(holder.getAdapterPosition()).getRecevedId());
                intent.putExtras(bundle);

                view.getContext().startActivity(intent);
            }
        });

        holder.mMoreInfos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), UserInfosActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("chatID", ChatList.get(holder.getAdapterPosition()).getChatId());
                bundle.putString("chatReceveID", ChatList.get(holder.getAdapterPosition()).getRecevedId());
                intent.putExtras(bundle);

                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        return ChatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle, mMoreInfos;
        public LinearLayout mLayout;
        public ImageView mImageView;
        public TextView mlastMessage;

        public ChatListViewHolder(View view) {

            super(view);

            mLayout = view.findViewById(R.id.layout);
            mTitle = view.findViewById(R.id.title);
            mImageView = view.findViewById(R.id.imgChatList);
            mlastMessage = view.findViewById(R.id.lastMessage);
            mMoreInfos = view.findViewById(R.id.more_infos);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
