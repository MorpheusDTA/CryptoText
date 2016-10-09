package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModifyConversation extends AppCompatActivity {
    private static final Level level = Level.WARNING;
    private static Logger logger = Logger.getLogger(Encryption.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_conversation);

        String phoneN;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                phoneN = extras.getString(MainActivity.PHONE);
                setPhoneAndContact(phoneN);
            }
        } else {
            phoneN = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
            setPhoneAndContact(phoneN);
        }
    }

    public void setPhoneAndContact(String phoneNumber){
        EditText phone = (EditText) findViewById(R.id.phone);
        TextView contact = (TextView) findViewById(R.id.contactName);
        phone.setText(phoneNumber);
        phone.setEnabled(false);

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contact.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
            cursor.close();
        }
    }

    public void goToConversation(View view) {
        EditText phone = (EditText) findViewById(R.id.phone);
        EditText receptionKeySeed = (EditText) findViewById(R.id.RKeySeedField);
        EditText emissionKeySeed = (EditText) findViewById(R.id.EKeySeedField);
        EditText keyStoreField = (EditText) findViewById(R.id.passwordField);
        String phoneNumber = phone.getText().toString();
        String emissionSeed = emissionKeySeed.getText().toString();
        String receptionSeed = receptionKeySeed.getText().toString();
        String keyStorePassword = keyStoreField.getText().toString();

        phoneNumber = formatNumber(phoneNumber);
        String errors = "";
        if (phoneNumber == null) {
            errors = errors + "The phone number isn't correct\n";
        } else {
            if ((emissionSeed.length() != 0) ^ Encryption.isStocked(phoneNumber + "Out", keyStorePassword)) {
                if (emissionSeed.length() != 0) {
                    errors = errors + "The current emission seed will be overwritten\n";
                } else {
                    errors = errors + "There will be no emission seed\n";
                }
            }
            if ((receptionSeed.length() != 0) ^ Encryption.isStocked(phoneNumber + "In", keyStorePassword)) {
                if (receptionSeed.length() != 0) {
                    errors = errors + "The current reception seed will be overwritten\n";
                } else {
                    errors = errors + "There will be no reception seed\n";
                }
            }
        }
        if (errors.length() == 0) {
            saveSeeds();
        } else {
            createAlertDialog(errors);
        }
    }

    public void saveSeeds() {
        EditText phone = (EditText) findViewById(R.id.phone);
        EditText receptionKeySeed = (EditText) findViewById(R.id.RKeySeedField);
        EditText emissionKeySeed = (EditText) findViewById(R.id.EKeySeedField);
        EditText keyStoreField = (EditText) findViewById(R.id.passwordField);
        String phoneNumber = phone.getText().toString();
        String keyStorePassword = keyStoreField.getText().toString();
        String emissionSeed = emissionKeySeed.getText().toString();
        String receptionSeed = receptionKeySeed.getText().toString();

        KeyStore keyStore = Encryption.createKeyStore(keyStorePassword);
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
        try {
            if (emissionSeed.length() != 0) {
                KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(Encryption.generateKey(emissionSeed.getBytes()));
                keyStore.setEntry(phoneNumber + "Out", keyStoreEntry, passwordProtection);
            }
            if (receptionSeed.length() != 0) {
                KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(Encryption.generateKey(receptionSeed.getBytes()));
                keyStore.setEntry(phoneNumber + "In", keyStoreEntry, passwordProtection);
            }
        } catch (KeyStoreException e) {
            logger.log(level, e.toString());// Keystore not loaded
        }

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("keyStorePassword", keyStorePassword);
        startActivity(intent);
    }

    public void createAlertDialog(String errors) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Warnings");
        alert.setMessage("Warnings :\n" + errors);
        alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveSeeds();
            }
        });
        alert.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
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
