package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * In this activity, the user can save some encryption/decryption key
 * @author DonatienTertrais
 */
public class ModifyConversation extends AppCompatActivity {
    private String phoneNumber;
    private static final int KEY_LENGTH = 45;
    private static SecretKey key = null;
    private static String keyStr = null;
    private static String pwd = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        EditText phone = (EditText) findViewById(R.id.phoneField);
        phone.setText(phoneNumber);
        phone.setEnabled(false);

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contact.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
            }
            cursor.close();
        }
    }

    /**
     * Go to the chosen conversation
     * @param view View of the button clicked
     */
    public void goToConversation(View view) {
        Editable phoneField = ((EditText) findViewById(R.id.phoneField)).getText(),
                 pwdField = ((EditText) findViewById(R.id.pwdField)).getText(),
                 keyField = ((EditText) findViewById(R.id.keyField)).getText();
        keyStr = keyField.toString(); pwd = pwdField.toString(); phoneNumber = formatNumber(phoneField.toString());
        if (phoneNumber == null) {
            EditText phField = (EditText) findViewById(R.id.phoneField);
            phField.setTextColor(Color.RED);
            phField.setText(R.string.wrongNumber);
            return;
        }
        pwdField.clear(); keyField.clear();

        if (keyStr.length() == 0) {//If no key is asked, ok
            Intent intent = new Intent(this, Conversation.class);
            intent.putExtra(MainActivity.PHONE, phoneNumber);
            intent.putExtra("keyStorePassword", pwd);
            key = null; keyStr = ""; pwd = "";//Clear sensitive data
            startActivity(intent);
        }

        if (checkForErrors()) return;// Check if there are errors in the data
        // Save the key
        Context context = getApplication();
        if (!keyStr.isEmpty()) {//A key is to be saved
            if (key == null) {// The key is to be generated first from the string
                byte[] keyByte = Base64.decode(keyStr, Base64.DEFAULT);
                key = new SecretKeySpec(keyByte, 0, 256, "AES");
            }
            if (Encryption.isStocked(context, phoneNumber, pwd)) {//Check if key will be overwritten
                createAlertDialog(this); return;
            } else {
                Encryption.saveKey(context, key, pwd, phoneNumber);//Save Key
            }
        }

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyStorePassword", pwd);
        key = null; keyStr = ""; pwd = "";//Clear sensitive data
        startActivity(intent);
    }

    /**
     * Create an emission or reception key
     * @param view View of the button
     */
    public void createKey(View view) {
        TextView keyField = (TextView) findViewById(R.id.keyField);

        key = Encryption.generateKey();
        if (key != null) {
            keyStr = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
            keyField.setText(keyStr);
        }
    }

    /**
     * Checks if there are errors with the password or the keys
     * @return True if an error was spotted
     */
    private boolean checkForErrors () {
        if (!Encryption.testPassword(getApplication(), pwd)) {// Check if the password is correct
            TextView pwdField = (TextView) findViewById(R.id.pwdField);
            pwdField.setTextColor(Color.RED);
            pwdField.setHint(R.string.invalidDataPwd);
            return true;
        }
        int lg = keyStr.length();
        boolean endsWith = keyStr.endsWith("=");
        if (key == null && !(lg == KEY_LENGTH && endsWith) ) {
            // String was not created, to be checked : right end and right length
            TextView eKeyField = (TextView) findViewById(R.id.keyField);
            eKeyField.setTextColor(Color.RED);
            eKeyField.setText(R.string.invalidDataKey);
            return true;
        }
        return false;
    }

    /**
     * Create alert window when there are some errors
     */
    private void createAlertDialog(final Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.warnings);
        String msg = getString(R.string.warnings) + " :\n" + getString(R.string.keyOverW);
        alert.setMessage(msg);
        alert.setPositiveButton(R.string.continueStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Encryption.deleteKey(getApplication(), phoneNumber, pwd);
                Encryption.saveKey(getApplication(), key, pwd, phoneNumber);//Save reception key
                Intent intent = new Intent(context, Conversation.class);
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
}
