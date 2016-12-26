package com.example.utilisateur.cryptotext;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
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

    public SmsReceiver (Context context) {
        this.context = context;
    }


    // Change the password here or give a user possibility to change it
    //public static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };

    @Override
    public void onReceive( Context context, Intent intent ) {
        String title = "";
        String address;

        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        if ( msgs.length == 1) {// only one new message
            // Get ContentResolver object for pushing SMS to the incoming folder
            ContentResolver contentResolver = this.context.getContentResolver();
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

            //Creation of the notification
            Intent notifIntent = new Intent(this.context, ModifyConversation.class);
            intent.putExtra(PHONE, sms.getOriginatingAddress());
            createNotification(this.context, title, sms.getMessageBody().substring(0, 20) + "...", notifIntent);

            // Display SMS message
            //Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        } else if (msgs.length != 0) {// a list of new messages, cretaion of the adequate notification
            title = "" + msgs.length + " new messages";
            Intent notifIntent = new Intent(this.context, MainActivity.class);
            createNotification(this.context, title, "", notifIntent);
        }

        //this.abortBroadcast();
    }

    private void createNotification(Context context, String title, String txt, Intent intent) {

        /**
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_dialog_email)
                        .setContentTitle("SMS from " + contact)
                        .setContentText(txt);
        Intent intent = new Intent(context, ModifyConversation.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        */


        Toast.makeText(context, title, Toast.LENGTH_SHORT).show();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(txt)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        //Log.i("Wakeup", "Display Wakeup");

        /*PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Phone WakeUp");
        wakeLock.acquire();*/

        //notification.setLatestEventInfo(context, "My Messaging App(New Message)",msg, pendingIntent);
        //notification.sound.
        //notification.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(777, mBuilder.build());





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