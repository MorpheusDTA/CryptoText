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
import android.view.LayoutInflater;
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

/**
 * @author DonatienTertrais
 */
public class Conversation extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String phoneNumber = "";
    private String emissionSeed = "";
    private String receptionSeed = "";
    private String contactName = "Unknown";
    private static final Level level = Level.WARNING;
    private ArrayList<Integer> types = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private static Logger logger = Logger.getLogger(Encryption.class.getName());

    private ArrayList<String> getMessages() {
        return this.messages;
    }
    private void setMessages(ArrayList<String> messages) {
        this.messages = messages;
        ListView smsListView = (ListView) findViewById( R.id.smsList );

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.messages) {
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());

                View row = super.getView(position, convertView, parent);
                // Background color depends on whether the sms is sent/received
                if (types.get(position) == 1) {
                    convertView = inflater.inflate(R.layout.activity_conversation_received, null);
                    row.setBackgroundColor(Color.rgb(255, 255, 102));// yellow, received
                    row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                } else {
                    convertView = inflater.inflate(R.layout.activity_conversation_sent, null);
                    row.setBackgroundColor(Color.rgb(153, 204, 255));// blue, sent
                    row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                }
                return row;

                /*
                 ViewHolder holder;

                 if (convertView == null) {
                    if (list.get(position).getTypeOfSms().equals("send")) {
                        convertView     = myInflater.inflate(R.layout.raw_left, null);
                    else {
                        convertView     = myInflater.inflate(R.layout.raw_right, null);
                        holder          = new ViewHolder();
                        holder.message      = (TextView) convertView.findViewById(R.id.message);
                        holder.dateAndTime      = (TextView) convertView.findViewById(R.id.dataAndTime);
                        convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.message.setText(list.get(position).getMessageContent());
                holder.dateAndTime.setText(list.get(position).geTime()+list.get(position).getDate());

                return convertView;
                 */
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
    private void setMessagesAndTypes(Cursor cursor){
        types.clear();
        messages.clear();
        ArrayList<String> mess = new ArrayList<>();
        final ArrayList<Integer> type = new ArrayList<>();

        // Index de différentes infos sur le sms
        int indexes[] = new int[]{cursor.getColumnIndex("date"), cursor.getColumnIndex("body"),
                cursor.getColumnIndex("type"), cursor.getColumnIndex("seen"), cursor.getColumnIndex("_id")};

        boolean nextCursor = cursor.moveToFirst();
        if ( indexes[1] < 0 || !nextCursor ) return;

        String message;
        while( nextCursor ){ // Lecture des sms du cursor, modification pour le cas de sms non vu TODO mal fait ?
            message = getDate(cursor.getLong(indexes[0])) + "\n" + cursor.getString(indexes[1]);
            if (cursor.getInt(indexes[3]) == 0) {
                ContentValues values = new ContentValues();
                values.put("seen",1);
                getContentResolver().update(Uri.parse("content://sms/inbox"),values,
                        "_id="+cursor.getString(indexes[4]), null);
            }
            type.add(0, cursor.getInt(indexes[2]));
            mess.add(0, message);
            nextCursor = cursor.moveToNext();
        }

        setTypes(type);
        setMessages(mess);
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
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"_id", "body", "date", "type", "seen"},
                "address='" + phoneNumber + "'", null, null);
        if (cursor == null) {
            logger.log(level, "Null Cursor on getting the text messages.");
            return;
        }
        setMessagesAndTypes(cursor);
        cursor.close();
    }

    public void send (View view) {
        // Sending SMS
        EditText messageField = (EditText) findViewById(R.id.message);
        String message = messageField.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            //message = Encryption.encrypt(emissionSeed, message);
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent to " + contactName, Toast.LENGTH_LONG).show();
            getTypes().add(0);
            getMessages().add(getDate((long) 0) + "\n" + message);
            setTypes(getTypes());
            setMessages(getMessages());

            messageField.getText().clear();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            logger.log(level, "Failed sending SMS: " + e.toString());
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Decryption");
        alert.setMessage("Please, confirm decryption.");
        alert.setPositiveButton("Decrypt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //decrypt( position );
                //TODO : enablie saving a decryption
            }
        });
        alert.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void decrypt(int position) {
        String txt = messages.get(position);
        int idx = txt.indexOf("\n");
        String date = txt.substring(0, idx);
        String message = txt.substring(idx + 1);
        String seed = emissionSeed;
        if (types.get(position) == 1) seed = receptionSeed;
        String decrypted = Encryption.decrypt( seed, message);

        messages.set(position, date + "\n" + decrypted);
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

    private String getDate(Long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        String month = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)];
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return month + " " + date + " " + year + " " + hour + ":" + minute;
    }
}
