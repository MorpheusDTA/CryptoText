package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;

/**
 * @author DonatienTertrais
 */
public class Conversation extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final Level level = Level.WARNING;
    private static Logger logger = Logger.getLogger(Encryption.class.getName());
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<Integer> types = new ArrayList<>();
    private String phoneNumber = "";
    private String contactName = "Unknown";
    private String emissionSeed = "";
    private String receptionSeed = "";

    private ArrayList<String> getMessages() {
        return messages;
    }

    private void setMessages(ArrayList<String> messages) {
        this.messages = messages;
        ListView smsListView = (ListView) findViewById( R.id.smsList );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.messages) {
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Background color depends on whether the sms is sent/received
                if (types.get(position) == 1) {
                    row.setBackgroundColor(Color.rgb(255, 255, 102));// yellow, received
                    row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                } else {
                    row.setBackgroundColor(Color.rgb(153, 204, 255));// blue, sent
                    row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                }
                return row;
            }
        };
        smsListView.setAdapter(adapter);
        smsListView.setOnItemClickListener(this);
    }

    private ArrayList<Integer> getTypes() {
        return types;
    }

    private void setTypes(ArrayList<Integer> types) {
        this.types = types;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        TextView contact = (TextView) findViewById(R.id.contact);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneNumber = extras.getString(MainActivity.PHONE);
                emissionSeed = extras.getString("keyOutSeed");
                receptionSeed = extras.getString("keyInSeed");
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
            receptionSeed = (String) savedInstanceState.getSerializable("keyInSeed");
            emissionSeed = (String) savedInstanceState.getSerializable("keyOutSeed");
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();

        String str = contactName + "/" + phoneNumber;
        contact.setText(str);

        loadMessages();
    }

    public void loadMessages () {
        types.clear();
        messages.clear();
        ArrayList<String> mess = new ArrayList<>();
        final ArrayList<Integer> type = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"_id", "body", "date", "type", "seen"},
                "address='" + phoneNumber + "'", null, null);
        int indexDate = cursor.getColumnIndex("date");
        int indexBody = cursor.getColumnIndex("body");
        int indexType = cursor.getColumnIndex("type");
        int indexSeen = cursor.getColumnIndex("seen");
        int indexId = cursor.getColumnIndex("_id");

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;

        String message;
        message = getDate(cursor.getLong(indexDate)) + "\n" + cursor.getString(indexBody);
        if (cursor.getInt(indexSeen) == 0) {
            ContentValues values = new ContentValues();
            values.put("read",true);
            getContentResolver().update(Uri.parse("content://sms/inbox"),values,
                    "_id="+cursor.getString(indexId), null);
        }
        type.add(0, cursor.getInt(indexType));
        mess.add(0, message);

        while( cursor.moveToNext() ){
            message = getDate(cursor.getLong(indexDate)) + "\n" + cursor.getString(indexBody);
            if (cursor.getInt(indexSeen) == 0) {
                ContentValues values = new ContentValues();
                values.put("read",true);
                getContentResolver().update(Uri.parse("content://sms/inbox"),values,
                        "_id="+cursor.getString(indexId), null);
            }
            type.add(0, cursor.getInt(indexType));
            mess.add(0, message);
        }

        setTypes(type);
        setMessages(mess);
        cursor.close();
    }

    public void send (View view) {
        // Sending SMS
        EditText messageField = (EditText) findViewById(R.id.message);
        String message = messageField.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            //message = Encryption.decrypt( emissionSeed, message);
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent to " + contactName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            logger.log(level, "Failed sending SMS: " + e.toString());
        }

        getTypes().add(0, 0);
        getMessages().add(0, getDate(null) + "\n" + message);
        setTypes(getTypes());
        setMessages(getMessages());

        messageField.getText().clear();
    }

    public String getDate(Long millis){
        Calendar calendar = Calendar.getInstance();
        if (millis != null) {
            calendar.setTimeInMillis(millis);
        }

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        String month = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)];
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return month + " " + date + " " + year + " " + hour + ":" + minute;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Decryption");
        alert.setMessage("Do you want to decrypt or decrypt and save ?");
        alert.setPositiveButton("Decrypt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //decrypt( position, false);
            }
        });
        alert.setNegativeButton("Decrypt & Save (Impossible now)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //decrypt(position, true);
            }
        });
        alert.show();
    }

    private void decrypt(int position, boolean doSave) {
        String txt[] = messages.get(position).split("\n");
        String seed = emissionSeed;
        if (types.get(position) == 1) seed = receptionSeed;
        String decrypted = Encryption.decrypt( seed, txt[1]);

        if (doSave) {
            // TODO : save decryption
        }
        messages.set(position, txt[0] + "\n" + decrypted);
        setMessages(messages);
    }

    /*private void saveSMS(String encryptedBody, String body, String date) {
        try {
            Long id;
            Uri uriSms = Uri.parse("content://sms");
            Cursor c = getContentResolver().query(uriSms, new String[] {"_id", "date"},
                    "address='" + phoneNumber + "' AND body='" + encryptedBody + "'", null, null);
            if (c.moveToFirst()) {
                boolean cond = abs(millisFromDate(date) - c.getLong(c.getColumnIndex("date"))) < 60000;
                while (!cond) {
                    c.moveToNext();
                    cond = abs(millisFromDate(date) - c.getLong(c.getColumnIndex("date"))) < 60000;
                }
                id = c.getLong(c.getColumnIndex("_id"));
                getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
            }
            c.close();
        } catch (Exception e) {
            logger.log(level, e.toString());
        }
    }*/

    private Long millisFromDate(String date) {
        String[] infos = date.split(" ");
        int ind = infos[4].indexOf(":");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(infos[2]), Integer.parseInt(infos[0]), Integer.parseInt(infos[1]),
                Integer.parseInt(infos[4].substring(0, ind)), Integer.parseInt(infos[4].substring(ind)));
        return cal.getTimeInMillis();
    }
}
