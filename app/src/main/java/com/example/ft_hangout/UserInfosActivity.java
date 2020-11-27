package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ft_hangout.ColorActivity;
import com.example.ft_hangout.Global;
import com.example.ft_hangout.MainPageActivity2;
import com.example.ft_hangout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;

public class UserInfosActivity extends AppCompatActivity {

    String chatId, chatReceveID;
    TextView name, alias, relations, desc;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainPageActivity2.class));
                finish();
            }
        });

        chatId = getIntent().getExtras().getString("chatID");
        chatReceveID = FirebaseAuth.getInstance().getUid();
        setUserInfos();
    }

    private void setUserInfos() {

        name = findViewById(R.id.current_chat_name);
        alias = findViewById(R.id.current_chat_alias);
        relations = findViewById(R.id.current_chat_relation);
        desc = findViewById(R.id.current_chat_desc);
        image = findViewById(R.id.current_chat_img);

        DatabaseReference mUserinfos = FirebaseDatabase.getInstance().getReference().child("user").child(chatReceveID).child("chat").child(chatId);

        mUserinfos.addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot childSnapchot: snapshot.getChildren()) {

                    if (childSnapchot.getKey().equals("chatName"))
                        name.setText(getResources().getString(R.string.userinfos_activity_pseudo) + " : \n" + childSnapchot.getValue().toString());
                    if (childSnapchot.getKey().equals("alias"))
                        alias.setText(getResources().getString(R.string.userinfos_activity_alias) + " : \n" + childSnapchot.getValue().toString());
                    if (childSnapchot.getKey().equals("relation"))
                        relations.setText(getResources().getString(R.string.userinfos_activity_relation) + " : \n" + childSnapchot.getValue().toString());
                    if (childSnapchot.getKey().equals("desc"))
                        desc.setText(getResources().getString(R.string.userinfos_activity_desc) + " : \n" + childSnapchot.getValue().toString());
                    if (childSnapchot.getKey().equals("chatImg")) {
                        if (childSnapchot.getValue().toString().equals("none"))
                            image.setBackgroundResource(R.drawable.users);
                        else
                            new DownloadImageTask(image).execute(childSnapchot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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