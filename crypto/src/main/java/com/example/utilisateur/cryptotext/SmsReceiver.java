package com.example.utilisateur.cryptotext;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.provider.Telephony.Sms.Intents;


/**
 * @author DonatienTERTRAIS
 */
public class SmsReceiver extends BroadcastReceiver {
    public static String PHONE = "phoneNumber";
    private Context context;

    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;

    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;

    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;


    // Change the password here or give a user possibility to change it
    //public static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };


    /*@Override
    public void onReceive( Context context, Intent intent ) {
        String address;
        String title = "";
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        // Get ContentResolver object for pushing SMS to the incoming folder
        ContentResolver contentResolver = this.context.getContentResolver();

        if ( msgs.length == 1  ) {// only one new message
            SmsMessage sms = msgs[0];
            address = sms.getOriginatingAddress();

            //Resolving the contact name from the contacts.
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            Cursor c = contentResolver.query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) {
                address = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            c.close();
            title = "SMS from " + address;

            putSmsToDatabase( contentResolver, sms );
        } else if (msgs.length != 0) {// a list of new messages, cretaion of the adequate notification
            title = "" + msgs.length + " new messages";
            for (SmsMessage sms :  msgs) {
                putSmsToDatabase( contentResolver, sms );
            }
        }

        Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
        //this.abortBroadcast();
    }*/

    public void onReceive( Context context, Intent intent ) {
        String messages = "";

        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        if ( msgs.length != 0) {
            // Get ContentResolver object for pushing SMS to the incoming folder
            ContentResolver contentResolver = context.getContentResolver();

            for (SmsMessage sms:msgs) {
                String address = sms.getOriginatingAddress();

                //Resolving the contact name from the contacts.
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
                Cursor c = contentResolver.query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    address = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
                c.close();
                messages += "SMS from " + address + " :\n";

                putSmsToDatabase( contentResolver, sms );
            }

            // Display SMS message
            Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
        }

        // this.abortBroadcast();
    }

    private void putSmsToDatabase( ContentResolver contentResolver, SmsMessage sms ) {
        // Create SMS row
        ContentValues values = new ContentValues();
        values.put( "address", sms.getOriginatingAddress());
        values.put( "date", sms.getTimestampMillis());
        values.put( "read", false);
        values.put( "status", sms.getStatus());
        values.put( "type", MESSAGE_TYPE_INBOX);
        values.put( "seen", MESSAGE_IS_NOT_SEEN);
        values.put( "body", sms.getMessageBody());
        /*try {
            String encryptedPassword = Encryption.encrypt( new String(PASSWORD), sms.getMessageBody() );
            values.put( "Body", encryptedPassword );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }*/
        // Push row into the SMS table
        contentResolver.insert( Uri.parse("content://sms"), values );
    }
}