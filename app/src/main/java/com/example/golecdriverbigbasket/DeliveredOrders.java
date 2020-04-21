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
import android.widget.TextView;

import java.util.Locale;

public class DeliveredOrders extends AppCompatActivity {
    private TextView yes, change_language;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivered_orders);
        yes = findViewById(R.id.textView3);

        final String slot_name = getIntent().getStringExtra("slot");
        change_language = findViewById(R.id.textView7);

        sharedPreferences = getSharedPreferences("com.example.golecdriverbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String language = sharedPreferences.getString("set_language", "en");
        setAppLocale(language);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveredOrders.this, OrderCount.class);
                intent.putExtra("slot", slot_name);
                startActivity(intent);
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
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DeliveredOrders.this);
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
