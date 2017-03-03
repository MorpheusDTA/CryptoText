package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.CheckBox;
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
 * Activity for a conversation, the user can send messages, see the messages he received
 * @author DonatienTertrais
 */
public class Conversation extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String phoneNumber = "";
    private String keyStorePassword = "";
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
                //LayoutInflater inflater = LayoutInflater.from(getContext());

                View row = super.getView(position, convertView, parent);
                // Background color depends on whether the sms is sent/received
                if (types.get(position) == 1) {
                    //convertView = inflater.inflate(R.layout.activity_conversation_received, null);
                    row.setBackgroundColor(Color.rgb(255, 255, 102));// yellow, received
                    row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                } else {
                    //convertView = inflater.inflate(R.layout.activity_conversation_sent, null);
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

    /**
     * Sets the lists of messages and their types
     * @param cursor Cursor used to search the text messages
     */
    private void setMessagesAndTypes(Cursor cursor){
        types.clear();
        messages.clear();
        ArrayList<String> mess = new ArrayList<>();
        final ArrayList<Integer> type = new ArrayList<>();

        // Indexes of the sms information
        int indexes[] = new int[]{cursor.getColumnIndex("date"), cursor.getColumnIndex("body"),
                cursor.getColumnIndex("type"), cursor.getColumnIndex("seen"), cursor.getColumnIndex("_id")};

        boolean nextCursor = cursor.moveToFirst();
        if ( indexes[1] < 0 || !nextCursor ) return;

        String message;
        while( nextCursor ){ // Read the sms cursor, modify if there are unread sms
            message = getDate(cursor.getLong(indexes[0])) + "\n" + cursor.getString(indexes[1]);
            if (cursor.getInt(indexes[3]) == SmsReceiver.MESSAGE_IS_NOT_READ) {
                ContentValues values = new ContentValues();
                values.put("seen",SmsReceiver.MESSAGE_IS_READ);
                getContentResolver().update(Uri.parse("content://sms/inbox"),values,
                        "_id="+cursor.getString(indexes[4]), null);
            }
            type.add(0, cursor.getInt(indexes[2]));
            mess.add(0, message);
            nextCursor = cursor.moveToNext();
        }

        this.types = type;
        setMessages(mess);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        CheckBox checkEncryption = (CheckBox) findViewById(R.id.checkEncryption);
        // Get info in the intent
        TextView contact = (TextView) findViewById(R.id.contact);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneNumber = extras.getString(MainActivity.PHONE);
                keyStorePassword = extras.getString("keyStorePassword");
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
            keyStorePassword = (String) savedInstanceState.getSerializable("keyStorePassword");
        }
        // Manage the encryption checkbox
        if (!Encryption.isStocked(getApplicationContext(), phoneNumber + "Out", keyStorePassword)) {
            checkEncryption.setChecked(false);
            checkEncryption.setEnabled(false);
        }
        // Get the contact name from the phone number
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }

        String str = contactName + "/" + phoneNumber;
        contact.setText(str);
        loadMessages(); // Load the messages into the list
    }

    /**
     * Loads the messages from the database
     */
    private void loadMessages () {
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

    /**
     * Sending sms
     * @param view View of the floating action button
     */
    public void send (View view) {
        CheckBox checkEncryption = (CheckBox) findViewById(R.id.checkEncryption);
        EditText messageField = (EditText) findViewById(R.id.message);
        String message = messageField.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            Context context = getApplicationContext();
            // Encrypting if possible and wanted
            if (Encryption.isStocked(context, phoneNumber + "Out", keyStorePassword) && checkEncryption.isChecked()) {
                // If a key isStocked and encryption asked, the message is encrypted
                String key = Encryption.getKey(context, phoneNumber + "Out", keyStorePassword);
                message = Encryption.encrypt(key, message);
            }
            //Send SMS
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), R.string.sentTo + contactName, Toast.LENGTH_LONG).show();
            // Update the view
            getTypes().add(0);
            getMessages().add(getDate((long) 0) + "\n" + message);
            this.types = getTypes();
            setMessages(getMessages());

            messageField.getText().clear();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.sendFail, Toast.LENGTH_LONG).show();
            logger.log(level, "Failed sending SMS: " + e.toString());
        }
    }

    @Override
    // On click of a text message : decrypt it
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.decryption);
        alert.setPositiveButton(R.string.decrypt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                decrypt( position );
            }
        });
        alert.setNegativeButton(R.string.goBack, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * Decryption of the sms on the given position of the list
     * @param position Position of the SMS in the list
     */
    private void decrypt(int position) {
        String txt = messages.get(position);
        int idx = txt.indexOf("\n");
        String date = txt.substring(0, idx);
        String message = txt.substring(idx + 1);

        String key = "";
        Context context = getApplicationContext();
        // Get key
        if (types.get(position) == 1 && Encryption.isStocked(context, phoneNumber + "In", keyStorePassword)) {
            key = Encryption.getKey(context, phoneNumber + "In", keyStorePassword);
        } else if (types.get(position) == 2 && Encryption.isStocked(context, phoneNumber + "Out", keyStorePassword)) {
            key = Encryption.getKey(context, phoneNumber + "Out", keyStorePassword);
        }
        message = Encryption.decrypt(key, message);

        messages.set(position, date + "\n" + message);
        setMessages(messages);// TODO slight mistake : impossible to have several sms decrypted in the view
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

    /**
     * Get the milliseconds equivalent for the date
     * @param date Date
     * @return Number of milliseconds for the referred date
     */
    private Long millisFromDate(String date) {
        String[] infos = date.split(" ");
        int ind = infos[4].indexOf(":");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(infos[2]), Integer.parseInt(infos[0]), Integer.parseInt(infos[1]),
                Integer.parseInt(infos[4].substring(0, ind)), Integer.parseInt(infos[4].substring(ind)));
        return cal.getTimeInMillis();
    }

    /**
     * Get the date from the the date
     * @param millis Number of milliseconds
     * @return Date linked to the number of milliseconds
     */
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
