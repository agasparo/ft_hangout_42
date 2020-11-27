package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.ft_hangout.Utils.CountryToPhonePrefix;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UpdateUserActivity extends AppCompatActivity {

    String contactUid, chatUid;
    EditText mPseudo, mAlias, mImage, mDescription, mRelation;
    Button mImageBtn;

    int PICK_IMAGE_INTENT = 1;
    String ImageChat = "";
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goback();
            }
        });

        mPseudo = findViewById(R.id.contact_pseudo);
        mAlias = findViewById(R.id.contact_alias);
        mImage = findViewById(R.id.contact_image);
        mImageBtn = findViewById(R.id.add_img_contact);
        mDescription = findViewById(R.id.contact_desc);
        mRelation = findViewById(R.id.contact_relation);

        mImage.setEnabled(false);

        contactUid = getIntent().getExtras().getString("contactUid");
        chatUid = getIntent().getExtras().getString("chatUid");
        setUserInfos();

        mImageBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        Button addNewContact = findViewById(R.id.add_new_contact);
        addNewContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mPseudo.getText().toString().isEmpty())
                    return;
                if (mAlias.getText().toString().isEmpty())
                    return;
                if (mDescription.getText().toString().isEmpty())
                    return;
                if (mRelation.getText().toString().isEmpty())
                    return;
                if (mImage.getText().toString().isEmpty())
                    return;

                insertUserData(mPseudo.getText().toString(), mAlias.getText().toString(), mDescription.getText().toString(), mRelation.getText().toString());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.update_userinfos_activity_image)), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE_INTENT) {

                if (data.getClipData() == null) {
                    ImageChat = data.getData().toString();
                    mImage.setText(ImageChat);
                }
            }
        }
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

    private void setUserInfos() {

        DatabaseReference mContact = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(chatUid);

        mContact.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && active) {

                    for (DataSnapshot userInfos: snapshot.getChildren()) {

                        if (userInfos.getKey().equals("chatName"))
                            mPseudo.setText(snapshot.child("chatName").getValue().toString());
                        if (userInfos.getKey().equals("alias"))
                            mAlias.setText(snapshot.child("alias").getValue().toString());
                        if (userInfos.getKey().equals("desc"))
                            mDescription.setText(snapshot.child("desc").getValue().toString());
                        if (userInfos.getKey().equals("relation"))
                            mRelation.setText(snapshot.child("relation").getValue().toString());
                        if (userInfos.getKey().equals("chatImg"))
                            mImage.setText(snapshot.child("chatImg").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    int totalMediaUploaded = 0;
    private void insertUserData(String pseudo, String alias, String desc, String relation) {

        DatabaseReference mContact = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(chatUid);
        String ISOPrefix = getCountyISO();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatUid).child(FirebaseAuth.getInstance().getUid()).child("image");
        final Map newImageChatMap = new HashMap<>();

        newImageChatMap.put("chatName", pseudo);
        newImageChatMap.put("alias",alias);
        newImageChatMap.put("desc", desc);
        newImageChatMap.put("relation", relation);

        if (!ImageChat.isEmpty()) {
            UploadTask uploadTask = filePath.putFile(Uri.parse(ImageChat));
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(Uri uri) {

                            newImageChatMap.put("chatImg", uri.toString());

                            totalMediaUploaded++;
                            if (totalMediaUploaded == 1)
                                updateDatabaseWithNewMessage(mContact, newImageChatMap);
                        }
                    });
                }
            });
        } else {
            updateDatabaseWithNewMessage(mContact, newImageChatMap);
        }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference mContact, Map newImageMap) {

        mContact.updateChildren(newImageMap);
        totalMediaUploaded = 0;
        goback();
    }

    private void goback() {

        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("chatID", chatUid);
        bundle.putString("chatReceveID", contactUid);
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

    private String getCountyISO() {

        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {

            if (!telephonyManager.getNetworkCountryIso().toString().equals("")) {
                iso = telephonyManager.getNetworkCountryIso().toString();
            }
        }
        return (CountryToPhonePrefix.getPhone(iso));
    }
}