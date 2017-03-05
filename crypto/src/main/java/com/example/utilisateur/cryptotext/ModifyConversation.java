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
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * In this activity, the user can save some encryption/decryption key
 * @author DonatienTertrais
 */
public class ModifyConversation extends AppCompatActivity {
    private String phoneNumber;

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
        EditText phone = (EditText) findViewById(R.id.phone);
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
        Editable pwdField = ((EditText) findViewById(R.id.passwordField)).getText();
        Editable phone = ((EditText) findViewById(R.id.phone)).getText();
        Editable emission = ((EditText) findViewById(R.id.EKeySeedField)).getText();
        Editable reception = ((EditText) findViewById(R.id.RKeySeedField)).getText();

        if (pwdField == null || phone == null) {
            invalidData();
            return;
        }
        String pwd = pwdField.toString();
        ((EditText) findViewById(R.id.passwordField)).setText("");
        phoneNumber = formatNumber(phone.toString());
        if (!Encryption.testPassword(getApplication(), pwd)) {
            invalidData();
            return;
        }

        String eSeed = emission.toString(), rSeed = reception.toString();
        //If the given password is correct, look for errors in the given info
        boolean emissionCode = false, receptionCode = false;
        Context context = getApplication();
        if (eSeed.length() != 0) {
            if (Encryption.isStocked(context, phoneNumber + "Out", pwd)) {//emission key will be overwritten
                emissionCode = true;
            } else {
                Encryption.saveKey(context, eSeed, pwd, phoneNumber + "Out");//Save emission Key
            }
        }
        if (rSeed.length() != 0) {
            if (Encryption.isStocked(context, phoneNumber + "In", pwd)) {//reception key will be overwritten
                receptionCode = true;
            } else {
                Encryption.saveKey(context, rSeed, pwd, phoneNumber + "In");//Save reception key
            }
        }
        if ( emissionCode || receptionCode) {
            createAlertDialog(emissionCode, receptionCode);
            return;
        }

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyStorePassword", pwd);
        startActivity(intent);
    }

    private void invalidData () {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.warnings);
        alert.setMessage(getString(R.string.warnings) + " :\n" + getString(R.string.invalidData));
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * Save the keys
     * @param emissionCode if the emission key is to be overwritten
     * @param receptionCode if the reception key is to be overwritten
     */
    private void saveKeys(boolean emissionCode, boolean receptionCode) {
        Editable fields[] = new Editable[] {((EditText) findViewById(R.id.RKeySeedField)).getText(),
                ((EditText) findViewById(R.id.EKeySeedField)).getText(),
                ((EditText) findViewById(R.id.passwordField)).getText()};
        String pwd = fields[2].toString();
        String eSeed = fields[1].toString();
        String rSeed = fields[0].toString();
        if (emissionCode) Encryption.saveKey(getApplication(), eSeed, pwd, phoneNumber + "Out");
        if (receptionCode) Encryption.saveKey(getApplication(), rSeed, pwd, phoneNumber + "In");

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra(MainActivity.PHONE, phoneNumber);
        intent.putExtra("keyStorePassword", pwd);
        startActivity(intent);
    }

    /**
     * Create alert window when there are some errors
     * @param emissionCode if the emission key is to be overwritten
     * @param receptionCode if the reception key is to be overwritten
     */
    private void createAlertDialog(final boolean emissionCode, final boolean receptionCode) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.warnings);
        String msg = getString(R.string.warnings) + " :\n";
        if (emissionCode) msg += getString(R.string.eOverW) + "\n";
        if (receptionCode) msg += getString(R.string.rOverW);
        alert.setMessage(msg);
        alert.setPositiveButton(R.string.continueStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveKeys(emissionCode, receptionCode);
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
