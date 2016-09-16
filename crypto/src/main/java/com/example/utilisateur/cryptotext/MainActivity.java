package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
    public static final String PHONE = "com.example.utilisateur.cryptotext.PHONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void newConversation(View view){
        Intent intent = new Intent(MainActivity.this, ModifyConversationActivity.class);
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

    public void update( View v ) {
        ArrayList<String> conv = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexAddr = cursor.getColumnIndex("Address");

        if ( cursor.getColumnIndex( "Body" ) < 0 || !cursor.moveToFirst() ) return;

        conversationList.clear();
        String str = "Conversation: " + getContactName(cursor.getString( indexAddr ), contentResolver) + "\n" + cursor.getString(indexAddr) /*+ "\n" + date + " " + hour*/;
        conv.add( cursor.getString(indexAddr));
        conversationList.add(str);
        while( cursor.moveToNext() ){
            if ( !conv.contains(cursor.getString( indexAddr )) ) {
                str = "Conversation: " + getContactName(cursor.getString( indexAddr ), contentResolver) + "\n" + cursor.getString(indexAddr)/*+ "\n" + cursor.getString( indexDate )*/;
                conv.add(cursor.getString(indexAddr));
                conversationList.add(str);
            }
        }

        ListView smsListView = (ListView) findViewById( R.id.listView );
        smsListView.setAdapter( new ArrayAdapter<>( this, android.R.layout.simple_list_item_1, conversationList) );
        smsListView.setOnItemClickListener(this);
        smsListView.setOnItemLongClickListener(this);
        cursor.close();
    }

    public String getContactName(String phoneNumber, ContentResolver cr){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return phoneNumber;
    }
    //The SMS text is obtained from the list, decrypted and then shown. SmsReceiver.PASSWORD can be changed in any way. The list item listener is as follows:

    public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
        String phoneNumber = conversationList.get(pos).split("\n")[1];
        Intent intent = new Intent(this, ModifyConversationActivity.class);
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