package com.example.utilisateur.cryptotext;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.provider.Telephony.Sms.Intents;

/**
 * Created by DonatienTERTRAIS on 10/09/2016.
 */
public class SmsReceiver extends BroadcastReceiver {
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;

    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;

    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;

    // Change the password here or give a user possibility to change it
    public static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };

    public void onReceive( Context context, Intent intent ) {

        String messages = "";

        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        if ( msgs.length != 0) {

            // Get ContentResolver object for pushing SMS to the incoming folder
            ContentResolver contentResolver = context.getContentResolver();

            for ( int i = 0; i < msgs.length; ++i ) {
                SmsMessage sms = msgs[i];

                String body = sms.getMessageBody().toString();
                String address = sms.getOriginatingAddress();

                messages += "SMS from " + address + " :\n";
                messages += body + "\n";

                putSmsToDatabase( contentResolver, sms );
            }

            // Display SMS message
            Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
        }
        // this.abortBroadcast();
    }

    private void putSmsToDatabase( ContentResolver contentResolver, SmsMessage sms )
    {
        // Create SMS row
        ContentValues values = new ContentValues();
        values.put( "Address", sms.getOriginatingAddress() );
        values.put( "Dat", sms.getTimestampMillis() );
        values.put( "Read", MESSAGE_IS_NOT_READ );
        values.put( "Status", sms.getStatus() );
        values.put( "Type", MESSAGE_TYPE_INBOX );
        values.put( "Seen", MESSAGE_IS_NOT_SEEN );
        try
        {
            String encryptedPassword = Encryption.encrypt( new String(PASSWORD), sms.getMessageBody().toString() );
            values.put( "Body", encryptedPassword );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        // Push row into the SMS table
        contentResolver.insert( Uri.parse("content://sms"), values );
    }
}