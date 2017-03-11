package com.example.utilisateur.cryptotext;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.TextBasedSmsColumns;

import static com.example.utilisateur.cryptotext.Constants.MESSAGE_IS_NOT_READ;

/**
 * @author DonatienTERTRAIS
 */
public class SmsReceiver extends BroadcastReceiver {
    // Change the password here or give a user possibility to change it
    //public static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };

    /**
     * Action to do on receiving a SMS
     * @param context Context of the app
     * @param intent Intent given to the Receiver
     */
    public void onReceive( Context context, Intent intent ) {
        String text = "";
        // Get ContentResolver object for pushing SMS to the incoming folder
        ContentResolver contentResolver = context.getContentResolver();

        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages.length == 1) {// Only one new message
            SmsMessage sms = messages[0];
            String address = sms.getOriginatingAddress();

            //Resolving the contact name from the contacts.
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            Cursor c = contentResolver.query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    address = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
                c.close();
            }
            text = "SMS from " + address;
            this.abortBroadcast();
            putSmsToDatabase( contentResolver, sms );
        } else if ( messages.length > 1) {// Several new messages
            for (SmsMessage sms:messages) {
                this.abortBroadcast();
                putSmsToDatabase( contentResolver, sms );
            }
            text = "" + messages.length + " new messages";
        }

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Save SMS in the database
     * @param contentResolver ContentResolver to save the SMS
     * @param sms SMS to be saved
     */
    private void putSmsToDatabase( ContentResolver contentResolver, SmsMessage sms ) {
        // Create SMS row
        ContentValues values = new ContentValues();
        values.put( "address", sms.getOriginatingAddress());
        values.put( "date", sms.getTimestampMillis());
        values.put( "read", false);
        values.put( "status", sms.getStatus());
        values.put( "type", TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        values.put( "seen", MESSAGE_IS_NOT_READ);
        values.put( "body", sms.getMessageBody());
        try {
            values.put( "Body", "toto" );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        // Push row into the SMS table
        contentResolver.insert( Uri.parse("content://sms/inbox"), values );
    }
}