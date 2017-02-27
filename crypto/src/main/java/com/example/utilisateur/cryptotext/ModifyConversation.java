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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author DonatienTertrais
 */
public class ModifyConversation extends AppCompatActivity {
    private static final int WRONG_PASSWORD = 2;
    private static final int EXIT_SUCCESS = 1;
    private static final int ERROR_ON_ACTION = 0;
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

    /**
     * Sets the phone and contact fields with the adequate values
     */
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

    /**
     * Go to the chosen conversation
     * @param view View of the button clicked
     */
    private void goToConversation(View view) {
        EditText phone = (EditText) findViewById(R.id.phone);
        EditText keyStoreField = (EditText) findViewById(R.id.passwordField);
        String keyStorePassword = keyStoreField.getText().toString();
        phoneNumber = formatNumber(phone.getText().toString());

        //TODO check the keyStorePassword

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyStorePassword", keyStorePassword);
        startActivity(intent);

        //TODO check if ther are errors
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

    /**
     * Save the Keys
     */
    private void saveKeys() {
        EditText fields[] = new EditText[] {(EditText) findViewById(R.id.RKeySeedField), (EditText) findViewById(R.id.EKeySeedField),
                (EditText) findViewById(R.id.passwordField)};
        String password = fields[2].getText().toString();
        String emissionSeed = fields[1].getText().toString();
        String receptionSeed = fields[0].getText().toString();
        int a = Encryption.saveKey(emissionSeed, password, phoneNumber + "Out");
        if (a == WRONG_PASSWORD || a == ERROR_ON_ACTION) {
            error();
        } else {
            Encryption.saveKey(receptionSeed, password, phoneNumber + "In");
        }

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyStorePassword", password);
        startActivity(intent);
    }

    /**
     * Create alert window when there are some errors
     * @param errors Detected errors
     */
    private void createAlertDialog(String errors) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Warnings");
        alert.setMessage("Warnings :\n" + errors);
        alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveKeys();
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

    /**
     * Formatting the number with the international code (+XX)
     * @param phoneNumber Phone Number to be formatted
     * @return Formatted Phone Number
     */
    private String formatNumber(String phoneNumber) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getSimCountryIso();
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + CountryToPhonePrefix.prefixFor(iso.toUpperCase()) + phoneNumber;
        }
        return PhoneNumberUtils.formatNumberToE164(phoneNumber, iso);
    }

    /**
     * Method called when there is a wrong password or another error saving the keys
     */
    private void error() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("An error occured");
        alert.setMessage("An error occured savng the keys. Please check the password");
        alert.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
