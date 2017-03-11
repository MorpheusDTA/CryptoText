package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SeeKeys extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
    private ArrayList<String> keysList = new ArrayList<>();
    private static String pwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_keys);
        askForPassword(0);
    }

    /**
     * Prints an alertDialog asking for the password
     * @param time If the password has already been tried once
     */
    private void askForPassword(final int time) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enterPassword);
        if (time == 0) {// If the first time
            alert.setMessage(R.string.msgSeeKeys);
        } else if (time < 3) {// Only 3 chances to get the right password
            alert.setMessage(R.string.wrongPassword);
        } else {
            pwd = null;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                pwd = input.getText().toString();
                if (Encryption.testPassword(getApplication(), pwd)) {
                    getKeys();
                } else {
                    askForPassword(time + 1);
                }
            }
        });
        alert.setView(input);
        alert.show();
    }

    /**
     * Gets all the keys stored in the KeyStore and represent them with  their aliases
     */
    private void getKeys () {
        keysList = Encryption.getKeys(getApplication(), pwd);
        ListView keysListView = (ListView) findViewById(R.id.keysList);
        if (keysList == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, keysList);
        keysListView.setAdapter(adapter);
        keysListView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String keyRef = keysList.get(position);
        String contact = keyRef.substring(0, keyRef.indexOf("\n"));
        final String phoneNumber = contact.substring(0, contact.indexOf("/"));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.warnings);
        alert.setMessage(getString(R.string.deleteKey) + " " + contact);
        alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Encryption.deleteKey(getApplication(), phoneNumber, pwd);
                getKeys();
            }
        });
        alert.setNegativeButton(R.string.goBack, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
        return true;
    }
}
