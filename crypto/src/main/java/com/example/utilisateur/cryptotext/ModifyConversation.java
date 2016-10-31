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

/**
 * @author DonatienTertrais
 */
public class ModifyConversation extends AppCompatActivity {
    private String phoneNumber;
    private static final Level level = Level.WARNING;
    private static Logger logger = Logger.getLogger(Encryption.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_conversation);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                phoneNumber = extras.getString(MainActivity.PHONE);
                setPhoneAndContact();
            }
        } else {
            phoneNumber = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
            setPhoneAndContact();
        }
    }

    private void setPhoneAndContact(){
        TextView contact = (TextView) findViewById(R.id.contactName);
        EditText phone = (EditText) findViewById(R.id.phone);
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
        /*EditText receptionKeySeed = (EditText) findViewById(R.id.RKeySeedField);
        EditText emissionKeySeed = (EditText) findViewById(R.id.EKeySeedField);
        EditText keyStoreField = (EditText) findViewById(R.id.passwordField);*/
        /*String emissionSeed = emissionKeySeed.getText().toString();
        String receptionSeed = receptionKeySeed.getText().toString();
        String keyStorePassword = keyStoreField.getText().toString();*/

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, formatNumber(phoneNumber));
        //intent.putExtra("keyStorePassword", keyStorePassword);
        startActivity(intent);

        /*String errors = "";
        if (phoneNumber.isEmpty()) {
            errors = errors + "The phone number isn't correct\n";
        } else {
            if ((!emissionSeed.isEmpty()) ^ Encryption.isStocked(phoneNumber + "Out", keyStorePassword)) {
                if (emissionSeed.length() != 0) {
                    errors = errors + "The current emission seed will be overwritten\n";
                } else {
                    errors = errors + "There will be no emission seed\n";
                }
            }
            if ((!receptionSeed.isEmpty()) ^ Encryption.isStocked(phoneNumber + "In", keyStorePassword)) {
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
        }*/
    }

    private void saveSeeds() {
        EditText fields[] = new EditText[] {(EditText) findViewById(R.id.RKeySeedField), (EditText) findViewById(R.id.EKeySeedField),
                (EditText) findViewById(R.id.passwordField)};
        String password = fields[2].getText().toString();
        String emissionSeed = fields[1].getText().toString();
        String receptionSeed = fields[0].getText().toString();

        KeyStore keyStore = Encryption.createKeyStore(password);
        if (keyStore == null) {
            wrongPassword();
            return;
        }
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());
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
            logger.log(level, "KeyStore is not loaded/initialized: " + e.toString());
        }

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyInSeed", receptionSeed);
        intent.putExtra("keyOutSeed", emissionSeed);
        startActivity(intent);
    }

    private void createAlertDialog(String errors) {
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

    private void wrongPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wrong Password");
        alert.setMessage("The given password doesn't match with the KeyStore password");
        alert.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
