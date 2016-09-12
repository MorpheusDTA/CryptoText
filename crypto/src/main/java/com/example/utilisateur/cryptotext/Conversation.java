package com.example.utilisateur.cryptotext;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class Conversation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String phoneNumber = "";
        String contactName = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneNumber = extras.getString("phoneNumber");
                contactName = extras.getString("contactName");
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable("phoneNumber");
            contactName = (String) savedInstanceState.getSerializable("contactName");
        }
        TextView contact = (TextView) findViewById(R.id.contact);
        TextView phone = (TextView) findViewById(R.id.phone);
        contact.setText(contactName);
        phone.setText(phoneNumber);

    }

}
