package com.example.utilisateur.cryptotext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static com.example.utilisateur.cryptotext.Constants.PHONE;

/**
 * Asks for the password of the keystore file and creates it
 * @author DonatienTERTRAIS
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ArrayList<Integer> seenList = new ArrayList<>();
    private ArrayList<String> conversationList = new ArrayList<>();

    /**
     * Sets the list of the current conversations
     * @param conversationList List of the conversations
     */
    private void setConversationList(ArrayList<String> conversationList) {
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
                    row.setBackgroundColor(Color.rgb(255, 255, 255));//white
                }
                return row;
            }
        };
        conversationsListView.setAdapter(adapter);
        conversationsListView.setOnItemClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        //if (savedInstanceState != null) update();
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    /**
     * Create a new conversation
     * @param view View of the button clicked to create a new conversation
     */
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
        Intent intent;
        switch(id){
            case R.id.seeKeys:
                intent = new Intent(this, SeeKeys.class);
                startActivity(intent);
                break;
            case R.id.changePassword:
                intent = new Intent(this, ChangePassword.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the conversations list
     */
    private void update() {
        seenList.clear();
        conversationList.clear();
        ArrayList<String> conversations = new ArrayList<>();// List of conversations
        ArrayList<String> numbers = new ArrayList<>();// List of phone numbers of the conversations
        final ArrayList<Integer> seen = new ArrayList<>();// To know whether a conversation contains unresd sms or not

        // Get the messages with the address/seen/body fields
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"address", "read", "body", "type"}, null, null, null);
        if (cursor == null) {
            Log.d("CT: null cursor get SMS", "");
            return;
        }
        // Indexes of the fields
        int indexAddress = cursor.getColumnIndex("address");
        int indexSeen = cursor.getColumnIndex("read");

        boolean nextCursor = cursor.moveToFirst();
        if ( cursor.getColumnIndex( "body" ) < 0 || !nextCursor ) return;

        String number;
        while( nextCursor ){// See all the sms
            if (!cursor.isNull(indexAddress) && !numbers.contains(number = formatNumber(cursor.getString(indexAddress)))){// Not a draft and the phone number not already listed => new conversation
                seen.add(cursor.getInt(indexSeen));
                numbers.add(number);
                conversations.add( getString(R.string.conversation)+ ": " + getContactName(number) + "\n" + number );
            }
            nextCursor = cursor.moveToNext();
        }
        numbers.clear();
        setConversationList(conversations);
        seenList = seen;
        cursor.close();
    }

    /**
     * Get the contact name from a phone number
     *
     * @param phoneNumber Phone number
     * @return Contact's name
     */
    private String getContactName(String phoneNumber){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        String contactName = "";
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }

    /**
     * When a conversation is clicked, go to the ModifyConversation Activity
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param pos The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id ) {
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
