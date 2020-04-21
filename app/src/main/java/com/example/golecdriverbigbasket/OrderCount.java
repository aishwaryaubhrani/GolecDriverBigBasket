package com.example.golecdriverbigbasket;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrderCount extends AppCompatActivity {
    private EditText order_count;
    private Button submit;
    private TextView change_language;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_count);

        order_count = findViewById(R.id.editText3);
        submit = findViewById(R.id.button4);
        change_language = findViewById(R.id.textView8);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("drivers");

        sharedPreferences = getSharedPreferences("com.example.golecdriverbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String language = sharedPreferences.getString("set_language", "en");
        setAppLocale(language);

        final String slot_name = getIntent().getStringExtra("slot");
        Calendar calendar = Calendar.getInstance();
        final String time = calendar.getTime().toString();
        final String driver_phone_number = sharedPreferences.getString("driver_phone_number", "+910");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(order_count.getText().toString());
                if(Integer.toString(count).isEmpty()){
                    order_count.setError("Required");
                    order_count.requestFocus();
                    return;
                }

                databaseReference.child(driver_phone_number).child(time).child("orders in "+slot_name).setValue(count);
                Toast.makeText(OrderCount.this, "Delivery Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OrderCount.this, ShowSlots.class);
                startActivity(intent);
                finish();
            }
        });

        change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlertDialogBox();
            }
        });
    }

    private void openAlertDialogBox() {
        final String[] supported_langs = {"English", "हिंदी", "मराठी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(OrderCount.this);
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
}
