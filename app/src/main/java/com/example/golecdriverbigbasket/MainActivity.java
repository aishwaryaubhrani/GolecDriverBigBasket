package com.example.golecdriverbigbasket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText phone_num, password;
    private Button login;
    private TextView change_language;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone_num = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        login = findViewById(R.id.button);
        change_language = findViewById(R.id.textView5);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("drivers");
        sharedPreferences = getSharedPreferences("com.example.golecdriverbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String language = sharedPreferences.getString("set_language", "en");
        setAppLocale(language);

        change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlertDialogBox();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String driver_phone_number = phone_num.getText().toString();
                String driver_password = password.getText().toString();
                if(driver_phone_number.isEmpty()){
                    phone_num.setError("Required");
                    phone_num.requestFocus();
                    return;
                }
                else if(driver_password.isEmpty()){
                    password.setError("Required");
                    password.requestFocus();
                    return;
                }
                checkLoginDetails(driver_phone_number, driver_password);
            }
        });
    }

    private void checkLoginDetails(final String driver_phone_number, final String driver_password) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(driver_phone_number)){
                    if(dataSnapshot.child(driver_phone_number).child("Driver Password").getValue().toString().equals(driver_password)){
                        Intent intent = new Intent(MainActivity.this, ShowSlots.class);
                        editor.putString("driver_phone_number", driver_phone_number);
                        editor.commit();
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Entered Password is Wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please signup to continue", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    //setting app language.
    private void setAppLocale(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(new Locale(localeCode.toLowerCase()));
        }
        else {
            configuration.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }

    private void openAlertDialogBox() {
        final String[] supported_langs = {"English", "हिंदी", "मराठी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Select Language");
        mBuilder.setSingleChoiceItems(supported_langs, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    setAppLocale("en");
                    editor.putString("set_language", "en");
                    editor.commit();
                    recreate();
                }
                else if(which == 1){
                    setAppLocale("hi");
                    editor.putString("set_language", "hi");
                    editor.commit();
                    recreate();
                }
                else if(which == 2){
                    setAppLocale("mr");
                    editor.putString("set_language", "mr");
                    editor.commit();
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
}
