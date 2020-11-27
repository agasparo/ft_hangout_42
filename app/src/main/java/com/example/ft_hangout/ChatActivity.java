package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.ft_hangout.Chat.MediaAdapter;
import com.example.ft_hangout.Chat.MessageAdapter;
import com.example.ft_hangout.Chat.MessageObject;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat, mMedia ;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;

    ArrayList<MessageObject> messageList;

    String chatID;

    String chatReceveID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainPageActivity2.class));
                finish();
            }
        });

        chatID = getIntent().getExtras().getString("chatID");
        chatReceveID = getIntent().getExtras().getString("chatReceveID");

        setTitleChat();

        Button mSend = findViewById(R.id.send);
        Button mAddMedia = findViewById(R.id.addMedia);

        mAddMedia.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {

                openGallery();
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        initializeRecyclerView();
        initializeMedia();
        getChatMessages();
    }

    private void setTitleChat() {

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().child("user").child(chatReceveID).child("chat").child(chatID).child("chatName");

        usernameRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null)
                    setTitle(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //***************************** activity ************************************//

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("pause").setValue(Calendar.getInstance().getTime());
    }

    //****************************** menu ***************************************//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.suppr_contact:
                deleteContact();
                return true;
            case R.id.modify_contact:
                updateContact();
                return true;
            case R.id.call_contact:
                callContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateContact() {

        Intent intent = new Intent(getApplicationContext(), UpdateUserActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("contactUid", chatReceveID);
        bundle.putString("chatUid", chatID);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void callContact() {

        DatabaseReference userPhone = FirebaseDatabase.getInstance().getReference().child("user").child(chatReceveID).child("phone");

        userPhone.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    if (snapshot.getValue() == null)
                        return;

                    String phone = snapshot.getValue().toString();
                    if (phone.isEmpty())
                        return;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //******************************* action menu *******************************//

    private void deleteContact() {

        DatabaseReference deleteInfos = FirebaseDatabase.getInstance().getReference();

        deleteInfos.child("chat").child(chatID).removeValue();

        deleteInfos.child("user").child(chatReceveID).child("chat").child(chatID).removeValue();
        deleteInfos.child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(chatID).removeValue();

        startActivity(new Intent(getApplicationContext(), MainPageActivity2.class));
        finish();
    }

    //***************************************************************************//

    private void getChatMessages() {

        FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {

                    String text = "", creatorID = "", sendAt = "";
                    ArrayList<String> mediaUrlList = new ArrayList<>();

                    if (snapshot.child("text").getValue() != null)
                        text = snapshot.child("text").getValue().toString();
                    if (snapshot.child("creator").getValue() != null)
                        creatorID = snapshot.child("creator").getValue().toString();
                    if (snapshot.child("send_at").getValue() != null)
                        sendAt = snapshot.child("send_at").getValue().toString();
                    if (snapshot.child("media").getChildrenCount() > 0){

                        for (DataSnapshot mediaSnapshot: snapshot.child("media").getChildren()) {

                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    MessageObject mMessage = new MessageObject(snapshot.getKey(), creatorID, text, mediaUrlList, sendAt);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size() - 1);
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    int totalMediaUploaded = 0;
    ArrayList<String> mediaIdList = new ArrayList<>();
    EditText mMessage;
    private void sendMessage() {

        mMessage = findViewById(R.id.messagetext);

        String messageId = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).push().getKey();
        DatabaseReference newMessageDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).child(messageId);
        final Map newMessageMap = new HashMap<>();

        newMessageMap.put("creator", FirebaseAuth .getInstance().getUid());

        if (!mMessage.getText().toString().isEmpty())
            newMessageMap.put("text", mMessage.getText().toString());

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        newMessageMap.put("send_at", df.format(c));
        if (!mediaUriList.isEmpty()) {

            for (String mediaUri: mediaUriList) {

                String mediaId = newMessageDb.child("media").push().getKey();
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatID).child(messageId).child(mediaId);

                mediaIdList.add(mediaId);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            public void onSuccess(Uri uri) {

                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                                totalMediaUploaded++;
                                if (totalMediaUploaded == mediaUriList.size())
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                            }
                        });
                    }
                });
            }
        } else {
            if (!mMessage.getText().toString().isEmpty())
                updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
        }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap) {

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatValue = "image";

        if (!mMessage.getText().toString().isEmpty())
            chatValue = mMessage.getText().toString();

        FirebaseDatabase.getInstance().getReference().child("user").child(chatReceveID).child("chat").child(chatID).child("chatLastMsg").setValue(chatValue);
        FirebaseDatabase.getInstance().getReference().child("user").child(currentUserID).child("chat").child(chatID).child("chatLastMsg").setValue(chatValue);

        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        totalMediaUploaded = 0;
        mMediaAdapter.notifyDataSetChanged();
    }

    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.chat_activity_image)), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE_INTENT) {

                if (data.getClipData() == null)
                    mediaUriList.add(data.getData().toString());
                else {

                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {

                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }
    @SuppressLint("WrongConstant")
    private void initializeMedia() {

        mediaUriList = new ArrayList<>();

        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);

        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false);

        mMedia.setLayoutManager(mMediaLayoutManager);

        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }


    @SuppressLint("WrongConstant")
    private void initializeRecyclerView() {

        messageList = new ArrayList<>();

        mChat = findViewById(R.id.message);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);

        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);

        mChat.setLayoutManager(mChatLayoutManager);

        mChatAdapter = new MessageAdapter(messageList, getApplicationContext());
        mChat.setAdapter(mChatAdapter);
    }
}