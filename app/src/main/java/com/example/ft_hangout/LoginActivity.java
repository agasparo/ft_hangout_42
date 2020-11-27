package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.se.omapi.Session;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText myPhoneNumber, myCode;
    private Button mySend;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    String mVerifivationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        userIsLoggedIn();

        myPhoneNumber = findViewById(R.id.phoneNumber);
        myCode = findViewById(R.id.code);
        mySend = findViewById(R.id.send);

        mySend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mVerifivationId != null) {
                    verifyPhoneNumberWithCode(mVerifivationId, myCode.getText().toString());
                } else {
                    startPhoneNumberVerification();
                }
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {}

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String mVerifivation, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                super.onCodeSent(mVerifivation, forceResendingToken);

                mVerifivationId = mVerifivation;
                mySend.setText(getResources().getString(R.string.login_activity_send_p2));
            }
        };
    }

    //****************************** menu ***************************************//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void ChangeLanguage(String lang) {

        Locale locale;
        Context context;

        context = LoginActivity.this;
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

    private void verifyPhoneNumberWithCode(String verifivationId, String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifivationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {

                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    mUserDB.updateChildren(userMap);
                                }
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }
        });
    }

    private void userIsLoggedIn() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainPageActivity2.class));
            finish();
            return;
        }
    }

    private void startPhoneNumberVerification() {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setActivity(this) // Activity (for callback binding)
                        .setPhoneNumber(myPhoneNumber.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}