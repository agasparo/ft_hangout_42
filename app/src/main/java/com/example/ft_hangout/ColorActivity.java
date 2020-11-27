package com.example.ft_hangout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

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

        TextView mRed = findViewById(R.id.red);
        TextView mGreen = findViewById(R.id.green);
        TextView mBlue = findViewById(R.id.blue);

        mRed.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                Global.toolbarColor = "#B22222";
                int myColor = Color.parseColor(Global.toolbarColor);
                toolbar.setBackgroundColor(myColor);
            }
        });

        mGreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Global.toolbarColor = "#4CAF50";
                int myColor = Color.parseColor(Global.toolbarColor);
                toolbar.setBackgroundColor(myColor);

            }
        });

        mBlue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Global.toolbarColor = "#4682B4";
                int myColor = Color.parseColor(Global.toolbarColor);
                toolbar.setBackgroundColor(myColor);
            }
        });
    }

    //***************************** activity ************************************//

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("pause").setValue(Calendar.getInstance().getTime());
    }
}