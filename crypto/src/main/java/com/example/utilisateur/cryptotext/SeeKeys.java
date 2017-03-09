package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SeeKeys extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_keys);
    }


    /*AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle(R.string.warnings);
    String msg = getString(R.string.warnings) + " :\n" + getString(R.string.keyOverW);
    alert.setMessage(msg);
    alert.setPositiveButton(R.string.continueStr, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Encryption.deleteKey(getApplication(), phoneNumber, pwd);
            Encryption.saveKey(getApplication(), key, pwd, phoneNumber);//Save reception key
            Intent intent = new Intent(getParent(), Conversation.class);
            intent.putExtra(MainActivity.PHONE, phoneNumber);
            intent.putExtra("keyStorePassword", pwd);
            key = null; keyStr = ""; pwd = "";//Clear sensitive data
            startActivity(intent);
        }
    });
    alert.setNegativeButton(R.string.goBack, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    });
    alert.show();*/
}
