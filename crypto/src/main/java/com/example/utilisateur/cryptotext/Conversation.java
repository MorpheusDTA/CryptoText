package com.example.utilisateur.cryptotext;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class Conversation extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        TextView contact = (TextView) findViewById(R.id.contact);
        String phoneNumber = "";
        //String keyStorePassword = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneNumber = extras.getString(MainActivity.PHONE);
                //keyStorePassword = extras.getString("keyStorePassword");
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
            //keyStorePassword = (String) savedInstanceState.getSerializable("keyStorePassword");
        }

        String contactName = "Unknown";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();

        contact.setText(contactName + "/" + phoneNumber);

        loadMessages(phoneNumber);
    }

    public void loadMessages (String number) {
        ArrayList<String> messages = new ArrayList<>();
        final ArrayList<Integer> types = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[] {"address", "body", "date", "type" },
                "address='" + number + "'", null, null);
        int indexAddress = cursor.getColumnIndex("address");
        int indexDate = cursor.getColumnIndex("date");
        int indexBody = cursor.getColumnIndex("body");
        int indexType = cursor.getColumnIndex("type");

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;
        String message;
        if (cursor.getString(indexAddress).equals(number)){
            message = getDate(cursor.getLong(indexDate)) + "\n" + cursor.getString(indexBody);
            types.add(Integer.valueOf(cursor.getInt(indexType)));
            messages.add(message);
        }

        while( cursor.moveToNext() ){
            if ( number.equals(cursor.getString(indexAddress))) {
                message = getDate(cursor.getLong(indexDate)) + "\n" + cursor.getString(indexBody);
                types.add(Integer.valueOf(cursor.getInt(indexType)));
                messages.add(message);
            }
        }
        Collections.reverse(messages);
        Collections.reverse(types);
        ListView smsListView = (ListView) findViewById( R.id.smsList );
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Background color depends on whether the sms is sent/received
                Color color = new Color();
                if (types.get(position) == 1) {
                    row.setBackgroundColor(color.rgb(255, 255, 102));// pink, received
                } else {
                    row.setBackgroundColor(color.rgb(153, 204, 255));// blue, sent
                }
                return row;
            }
        };
        smsListView.setAdapter(adapter);
        //smsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages));
        smsListView.setOnItemLongClickListener(this);
        cursor.close();
    }

    public void send (View view) {
        // Sending SMS
        EditText messageField = (EditText) findViewById(R.id.message);
        TextView contactAndPhone = (TextView) findViewById(R.id.contact);
        String infos = contactAndPhone.getText().toString();
        int indexSeparator = infos.lastIndexOf("/");
        String phoneNo = infos.substring(indexSeparator + 1);
        String contact = infos.substring(0, indexSeparator);
        String message = messageField.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            //TODO encrypting
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent to " + contact, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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
