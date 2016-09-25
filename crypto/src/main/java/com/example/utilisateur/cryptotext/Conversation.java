package com.example.utilisateur.cryptotext;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Conversation extends AppCompatActivity {
    private TextView contact = (TextView) findViewById(R.id.contact);
    private TextView phone = (TextView) findViewById(R.id.phone);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String phoneNumber = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneNumber = extras.getString(MainActivity.PHONE);
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
        }

        String contactName = "Unknown";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();

        contact.setText(contactName);
        phone.setText(phoneNumber);

        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cursor.getLong(indexDate));

        int date = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);*/

        // TODO move
        // Sending SMS
        /*Log.i("Send SMS", "");
        String phoneNo = txtphoneNo.getText().toString();
        String message = txtMessage.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            //TODO encrypting / decrypting
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/
    }

    public void loadMessages (String number) {
        ArrayList<String> messages = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        int indexAddr = cursor.getColumnIndex("Address");

        if ( cursor.getColumnIndex( "Body" ) < 0 || !cursor.moveToFirst() ) return;

        String str = "Conversation: " + getContactName(cursor.getString( indexAddr ), contentResolver) + "\n" + cursor.getString(indexAddr) /*+ "\n" + date + " " + hour*/;
        conv.add(cursor.getString(indexAddr));
        while( cursor.moveToNext() ){
            if ( !conv.contains(cursor.getString( indexAddr )) ) {
                str = "Conversation: " + getContactName(cursor.getString( indexAddr ), contentResolver) + "\n" + cursor.getString(indexAddr)/*+ "\n" + cursor.getString( indexDate )*/;
                conv.add(cursor.getString(indexAddr));
                conversationList.add(str);
            }
        }

        ListView smsListView = (ListView) findViewById( R.id.listView );
        smsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversationList));
        smsListView.setOnItemClickListener(this);
        smsListView.setOnItemLongClickListener(this);
        cursor.close();
    }

    public void send (View view) {

    }

}
