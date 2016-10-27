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

/**
 * @author DonatienTERTRAIS
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private ArrayList<String> conversationList = new ArrayList<>();
    private ArrayList<Integer> seenList = new ArrayList<>();
    public static String PHONE = "phoneNumber";

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

    public void setSeen(ArrayList<Integer> seen) {
        this.seenList = seen;
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

    public void update() {
        seenList.clear();
        conversationList.clear();
        ArrayList<String> conversations = new ArrayList<>();
        ArrayList<String> numbers = new ArrayList<>();
        final ArrayList<Integer> seen = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"address", "seen", "body"}, null, null, null);
        int indexAddress = cursor.getColumnIndex("address");
        int indexSeen = cursor.getColumnIndex("seen");

        if ( cursor.getColumnIndex( "body" ) < 0 || !cursor.moveToFirst() ) return;

        conversations.clear();
        seen.add(cursor.getInt(indexSeen));
        numbers.add(formatNumber(cursor.getString(indexAddress)));
        String str = "Conversation: " + getContactName(cursor.getString( indexAddress ), contentResolver) + "\n" + cursor.getString(indexAddress) ;
        conversations.add(str);

        while( cursor.moveToNext() ){
            if ( !numbers.contains(formatNumber(cursor.getString(indexAddress)))) {
                seen.add(cursor.getInt(indexSeen));
                numbers.add(cursor.getString(indexAddress));
                str = "Conversation: " + getContactName(cursor.getString( indexAddress ), contentResolver) + "\n" + cursor.getString(indexAddress);
                conversations.add(str);
            }
        }
        numbers.clear();
        setConversationList(conversations);
        setSeen(seen);
        cursor.close();
    }

    public String getContactName(String phoneNumber, ContentResolver cr){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        String contactName = "";
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }
    //The SMS text is obtained from the list, decrypted and then shown. SmsReceiver.PASSWORD can be changed in any way. The list item listener is as follows:

    public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
        String phoneNumber = conversationList.get(pos).split("\n")[1];
        Intent intent = new Intent(this, ModifyConversation.class);
        intent.putExtra(PHONE, phoneNumber);
        startActivity(intent);
    }

    private String formatNumber(String phoneNumber) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getSimCountryIso();
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + CountryToPhonePrefix.prefixFor(iso.toUpperCase()) + phoneNumber;
        }
        return PhoneNumberUtils.formatNumberToE164(phoneNumber, iso);
    }
}