package com.example.ft_hangout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.ft_hangout.User.UserListAdapter;
import com.example.ft_hangout.User.UserObject;
import com.example.ft_hangout.Utils.CountryToPhonePrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        int myColor = Color.parseColor(Global.toolbarColor);
        toolbar.setBackgroundColor(myColor);
        setSupportActionBar(toolbar);

        contactList = new ArrayList<>();
        userList = new ArrayList<>();
        initializeRecyclerView();
        getContactList();
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
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.goBack:
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //******************************* action menu *******************************//

    private void goBack() {

        startActivity(new Intent(getApplicationContext(), MainPageActivity2.class));
    }

    //***************************************************************************//

    private void getContactList() {

        String ISOPrefix = getCountyISO();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone.replace(" ", "");
            phone.replace("-", "");
            phone.replace("(", "");
            phone.replace(")", "");
            if (!String.valueOf(phone.charAt(0)).equals("+")) {
                phone = phone.substring(1);
                phone = ISOPrefix + phone;
            }
            for (int i = 0; i < phone.length(); i++) {

                if (Character.isWhitespace(phone.codePointAt(i))) {
                    phone = phone.substring(0, i) + phone.substring(i + 1);
                }
            }
            UserObject mContact = new UserObject("", name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }
    }

    private void getUserDetails(UserObject mContact) {

        TextView noRes = findViewById(R.id.NoUser);

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    String phone = "", name = "";
                    for (DataSnapshot childSnapshot: snapshot.getChildren() ) {

                        if (childSnapshot.child("phone").getValue() != null)
                            phone = childSnapshot.child("phone").getValue().toString();
                        if (childSnapshot.child("name").getValue() != null)
                            name = childSnapshot.child("name").getValue().toString();

                        UserObject mUser = new UserObject(childSnapshot.getKey(), name, phone);

                        for (UserObject ub: userList) {

                            if (ub.getPhone().equals(mUser.getPhone()))
                                return;
                        }

                        if (name.equals(phone)) {

                            for (UserObject mContactIterator: contactList) {

                                if (mContactIterator.getPhone().equals(phone)) {

                                    mUser.setName(mContactIterator.getName());
                                }
                            }
                        }

                        if (noRes.getVisibility() == View.VISIBLE) {

                            noRes.setVisibility(View.INVISIBLE);
                            LinearLayout.LayoutParams np = (LinearLayout.LayoutParams) noRes.getLayoutParams();
                            np.height = 0;
                            noRes.setLayoutParams(np);
                        }
                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                        hide_loading();
                        return;
                    }
                }
                hide_loading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void hide_loading() {

        TextView loading = findViewById(R.id.searchInProgress);

        if (loading.getVisibility() == View.VISIBLE) {

            loading.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) loading.getLayoutParams();
            lp.height = 0;
            loading.setLayoutParams(lp);
        }
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

    @SuppressLint("WrongConstant")
    private void initializeRecyclerView() {

        mUserList = findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);

        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);

        mUserList.setLayoutManager(mUserListLayoutManager);

        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}