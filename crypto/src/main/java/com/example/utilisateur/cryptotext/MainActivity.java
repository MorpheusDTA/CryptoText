package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import java.util.ArrayList;

/**
 * @author DonatienTERTRAIS
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener, OnItemLongClickListener {
    protected ArrayList<String> conversationList = new ArrayList<>();
    public static String PHONE = "phoneNumber";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            update();
        }
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
        ArrayList<String> numbers = new ArrayList<>();
        final ArrayList<Integer> seen = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), new String[]{"address", "seen"}, null, null, null);
        int indexAddress = cursor.getColumnIndex("address");
        int indexType = cursor.getColumnIndex("seen");

        if ( cursor.getColumnIndex( "Body" ) < 0 || !cursor.moveToFirst() ) return;

        conversationList.clear();
        seen.add(cursor.getInt(indexType));
        numbers.add(cursor.getString(indexAddress));
        String str = "Conversation: " + getContactName(cursor.getString( indexAddress ), contentResolver) + "\n" + cursor.getString(indexAddress) ;
        conversationList.add(str);

        while( cursor.moveToNext() ){
            if ( !numbers.contains(cursor.getString( indexAddress )) ) {
                seen.add(cursor.getInt(indexType));
                numbers.add(cursor.getString(indexAddress));
                str = "Conversation: " + getContactName(cursor.getString( indexAddress ), contentResolver) + "\n" + cursor.getString(indexAddress);
                conversationList.add(str);
            }
        }

        ListView smsListView = (ListView) findViewById( R.id.conversationsView );
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, conversationList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Text color depends on whether the sms is read or not
                if (seen.get(position) == 0) {// unseen
                    row.setBackgroundColor(Color.rgb(230, 240, 255));//light blue
                } else {// seen
                    row.setBackgroundColor(Color.rgb(255, 255, 255));// white
                }
                return row;
            }
        };
        smsListView.setAdapter(adapter);
        smsListView.setOnItemClickListener(this);
        smsListView.setOnItemLongClickListener(this);
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

    public boolean onItemLongClick( AdapterView<?> parent, View view, int pos, long id ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert!!");
        alert.setMessage("Are you sure to delete the conversation ?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO delete the conversation
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
        return true;
    }
}