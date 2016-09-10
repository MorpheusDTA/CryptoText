package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import java.util.ArrayList;

/**
 * @author DonatienTERTRAIS
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener, OnItemLongClickListener {
    ArrayList<String> smsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onFabClick(View view){
        Intent intent = new Intent(this, Main2Activity.class);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void update( View v ) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query( Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = cursor.getColumnIndex( "Body" );
        int indexAddr = cursor.getColumnIndex( "Address" );

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;

        smsList.clear();

        do {
            String str = "Sender: " + cursor.getString( indexAddr ) + "\n" + cursor.getString( indexBody );
            smsList.add( str );
        }
        while( cursor.moveToNext() );

        ListView smsListView = (ListView) findViewById( R.id.listView );
        smsListView.setAdapter( new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, smsList) );
        smsListView.setOnItemClickListener(this);
        smsListView.setOnItemLongClickListener(this);
    }
    //The SMS text is obtained from the list, decrypted and then shown. SmsReceiver.PASSWORD can be changed in any way. The list item listener is as follows:

    public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
        try {
            String sender = smsList.get( pos ).split("\n")[0];
            String encryptedData = smsList.get( pos ).split("\n")[1];
            String data = sender + "\n" + Encryption.decrypt( new String(SmsReceiver.PASSWORD), encryptedData );
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onItemLongClick( AdapterView<?> parent, View view, int pos, long id ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert!!");
        alert.setMessage("Are you sure to delete record: to be done");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*ContentResolver cttresolver = getContentResolver();
                cttresolver.*/
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