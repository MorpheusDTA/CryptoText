package com.example.utilisateur.cryptotext;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class Conversation extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
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

        loadMessages(phoneNumber);

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
        int indexDate = cursor.getColumnIndex("Date");
        int indexBody = cursor.getColumnIndex("Body");

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;
        String message;
        if (cursor.getString(indexAddr) == number){
            message = getDate(cursor.getLong(indexDate)) + cursor.getString(indexBody);
            messages.add(message);
        }

        while( cursor.moveToNext() ){
            if ( number.equals(cursor.getString(indexAddr))) {
                message = getDate(cursor.getLong(indexDate)) + cursor.getString(indexBody);
                messages.add(message);
            }
        }

        ListView smsListView = (ListView) findViewById( R.id.listView );
        smsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages));
        smsListView.setOnItemLongClickListener(this);
        cursor.close();
    }

    public void send (View view) {

    }

    public String getDate(Long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        String month = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)].substring(0,3);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return month + ". " + date + " " + year + " " + hour + ":" + minute;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
