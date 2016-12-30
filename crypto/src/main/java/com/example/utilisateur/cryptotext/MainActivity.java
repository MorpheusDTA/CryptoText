package com.example.utilisateur.cryptotext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author DonatienTERTRAIS
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    public static String PHONE = "phoneNumber";
    private static final Level level = Level.WARNING;
    private ArrayList<Integer> seenList = new ArrayList<>();
    private ArrayList<String> conversationList = new ArrayList<>();
    private static Logger logger = Logger.getLogger(Encryption.class.getName());

    public void setConversationList(ArrayList<String> conversationList) {
        this.conversationList = conversationList;
        ListView conversationsListView = (ListView) findViewById( R.id.conversationsView );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.conversationList) {
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Text color depends on whether the sms is read or not
                if (seenList.get(position) == 0) {// unseen
                    row.setBackgroundColor(Color.rgb(230, 240, 255));//light blue
                } else {// seen
                    row.setBackgroundColor(Color.rgb(255, 255, 255));// white
                }
                return row;
            }
        };
        conversationsListView.setAdapter(adapter);
        conversationsListView.setOnItemClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        update();
    }

    public void newConversation(View view){
        Intent intent = new Intent(this, ModifyConversation.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) return true;
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the conversations list
     */
    public void update() {
        seenList.clear();
        conversationList.clear();
        ArrayList<String> conversations = new ArrayList<>();// List of conversations
        ArrayList<String> numbers = new ArrayList<>();// List of phone numbers of the conversations
        final ArrayList<Integer> seen = new ArrayList<>();// To know whether a conversation contains unresd sms or not

        // Get the messages with the address/seen/body fields
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"address", "seen", "body"}, null, null, null);
        if (cursor == null) {
            logger.log(level, "Null Cursor on getting the text messages.");
            return;
        }
        // Indexes of the fields
        int indexAddress = cursor.getColumnIndex("address");
        int indexSeen = cursor.getColumnIndex("seen");


        boolean nextCursor = cursor.moveToFirst();
        if ( cursor.getColumnIndex( "body" ) < 0 || !nextCursor ) return;

        String number;
        while( nextCursor ){// See all the sms
            if (!cursor.isNull(indexAddress) && !numbers.contains(number = formatNumber(cursor.getString(indexAddress)))){// Not a draft and the phone number not already listed => new conversation
                seen.add(cursor.getInt(indexSeen));
                numbers.add(number);
                conversations.add( "Conversation: " + getContactName(number) + "\n" + number );
            }
            nextCursor = cursor.moveToNext();
        }
        numbers.clear();
        setConversationList(conversations);
        seenList = seen;
        cursor.close();
    }

    /**
     * Get contact name from a phone number
     *
     * @param phoneNumber Phone number
     * @return Contact name
     */
    public String getContactName(String phoneNumber){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        String contactName = "";
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }
    //The SMS text is obtained from the list, decrypted and then shown. SmsReceiver.PASSWORD can be changed in any way. The list item listener is as follows:

    /**
     * When a coversation is clicked, go to the ModifyConversation Activity
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param pos The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
        String phoneNumber = conversationList.get(pos).split("\n")[1];
        Intent intent = new Intent(this, ModifyConversation.class);
        intent.putExtra(PHONE, phoneNumber);
        startActivity(intent);
    }

    /**
     * Formatting the phone Number to the Sim Country ISO Standard
     *
     * @param phoneNumber Phone Number to be formatted
     * @return Formatted phone number
     */
    private String formatNumber(String phoneNumber) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getSimCountryIso();
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + CountryToPhonePrefix.prefixFor(iso.toUpperCase()) + phoneNumber;
        }
        return PhoneNumberUtils.formatNumberToE164(phoneNumber, iso);
    }
}