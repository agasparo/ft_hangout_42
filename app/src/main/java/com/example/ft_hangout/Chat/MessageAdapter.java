package com.example.ft_hangout.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    ArrayList<MessageObject> messageList;
    Context context;

    public MessageAdapter(ArrayList<MessageObject> messageList, Context context) {

        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MessageViewHolder rcv = new MessageViewHolder(layoutView);
        return rcv;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {


        if (Objects.equals(FirebaseAuth.getInstance().getUid(), messageList.get(position).getSenderId())) {

            holder.mParent.setGravity(Gravity.RIGHT);
            holder.MessageBody.setBackgroundResource(R.drawable.rounded_rectangle_green);
        } else {

            holder.mParent.setGravity(Gravity.LEFT);
            holder.MessageBody.setBackgroundResource(R.drawable.rounded_rectangle_orange);
        }

        holder.mMessageS.setText(messageList.get(position).getMessage());
        holder.mDateS.setText(messageList.get(position).getmDate());
        if (!messageList.get(position).getMediaUrlist().isEmpty())
            setImg(holder.mListImgS, messageList.get(position).getMediaUrlist());
        else
            holder.mListImgS.removeAllViews();
    }

    private void setImg(GridLayout mListImg, ArrayList<String> mediaUrlist) {

        mListImg.setColumnCount(1);
        mListImg.setOrientation(GridLayout.VERTICAL);
        mListImg.removeAllViews();

        for (int i = 0; i < mediaUrlist.size(); i++) {

            ImageView imgView = new ImageView(mListImg.getContext());
            mListImg.addView(imgView, i);
            imgView.getLayoutParams().width = 400;
            imgView.getLayoutParams().height = 400;
            imgView.setPadding(0,20,0,0);
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            new DownloadImageTask(imgView).execute(mediaUrlist.get(i));
        }
    }

    @Override
    public int getItemCount() {

        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView mMessageS, mDateS;

        GridLayout mListImgS;
        LinearLayout mLayout, MessageBody, mParent;

        MessageViewHolder(View view) {

            super(view);

            MessageBody = view.findViewById(R.id.messageGlobal);

            mMessageS = view.findViewById(R.id.messageS);
            mDateS = view.findViewById(R.id.message_date_s);
            mListImgS = view.findViewById(R.id.list_img_s);

            mParent = view.findViewById(R.id.message_layout);
            mLayout = view.findViewById(R.id.layout);
        }
    }


    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
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
