package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangout.Chat.ChatListAdapter;
import com.example.ft_hangout.Chat.ChatObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainPageActivity2 extends AppCompatActivity {

    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;
    static boolean active = false;
    static boolean background = false;

    ArrayList<ChatObject> chatList;
    Menu optionsMenu;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page2);

        setLangInit(Global.appLang);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        getPermissions();
        initializeRecyclerView();
        getUserChatList();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setLangInit(String appLang) {
        Locale locale;
        Context context;

        context = MainPageActivity2.this;
        locale = new Locale(appLang);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);

        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void onStart() {

        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {

        super.onStop();
        active = false;
    }

    private void show_date() {

        Context context = getApplicationContext();
        DatabaseReference menuDate = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("pause");

        menuDate.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && background) {

                    String hours = snapshot.child("hours").getValue().toString();
                    String min = snapshot.child("minutes").getValue().toString();
                    String sec = snapshot.child("seconds").getValue().toString();

                    CharSequence text = hours + ":" + min + ":" + sec;
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //***************************** activity ************************************//



    @Override
    protected void onPause() {

        background = false;
        super.onPause();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("pause").setValue(Calendar.getInstance().getTime());
        }
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        background = true;
        show_date();
    }

    //****************************** menu ***************************************//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.page_menu, menu);
        optionsMenu = menu;
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_deco:
                decoUser();
                return true;
            case R.id.change_color:
                ChangeColor();
                return true;
            case R.id.search_contact:
                searchUser();
                return true;
            case R.id.fr_l:
                ChangeLanguage("fr");
                return true;
            case R.id.en_l:
                ChangeLanguage("en");
                return true;
            case R.id.es_l:
                ChangeLanguage("es");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //******************************* action menu *******************************//

    private void ChangeColor() {

        startActivity(new Intent(getApplicationContext(), ColorActivity.class));
    }

    private void searchUser() {

        startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
    }

    private void decoUser() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void ChangeLanguage(String lang) {

        Locale locale;
        Context context;

        context = MainPageActivity2.this;
        locale = new Locale(lang);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);
        Global.appLang = lang;

        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

        finish();
        startActivity(getIntent());

    }

    //***************************************************************************//

    private void getUserChatList() {

        DatabaseReference mUserChat = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");
        mUserChat.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatList.clear();

                if (snapshot.exists() && active) {

                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {

                        if (childSnapshot.getChildrenCount() == 7) {

                            ChatObject mChat = new ChatObject(
                                    childSnapshot.getKey(),
                                    childSnapshot.child("chatName").getValue().toString(),
                                    childSnapshot.child("chatImg").getValue().toString(),
                                    childSnapshot.child("chatLastMsg").getValue().toString(),
                                    childSnapshot.child("chatReceved").getValue().toString(),
                                    childSnapshot.child("desc").getValue().toString(),
                                    childSnapshot.child("alias").getValue().toString(),
                                    childSnapshot.child("relation").getValue().toString()
                            );
                            boolean exists = false;
                            for (ChatObject mChatIterator : chatList) {

                                if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                    exists = true;
                            }
                            if (exists)
                                continue;
                            chatList.add(mChat);
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @SuppressLint("WrongConstant")
    private void initializeRecyclerView() {

        chatList = new ArrayList<>();

        mChatList = findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);

        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);

        mChatList.setLayoutManager(mChatListLayoutManager);

        mChatListAdapter = new ChatListAdapter(chatList);
        mChatList.setAdapter(mChatListAdapter);
    }

    private void getPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE}, 1);
        }
    }
}