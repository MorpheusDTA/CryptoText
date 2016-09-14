package com.example.utilisateur.cryptotext;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

//import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;

/**
 * @author DonatienTERTRAIS
 */
public class CreateConversationActivity extends AppCompatActivity {
    protected ArrayList<String> conversationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createconversation);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                conversationList = (ArrayList<String>) extras.get("conversationList");
            }
        } else {
            conversationList = (ArrayList<String>) savedInstanceState.getSerializable("conversationList");
        }
    }

    public void createConversation(View view){
        EditText phone = (EditText) findViewById(R.id.phone);
        EditText emissionKey = (EditText) findViewById(R.id.emissionkey);
        EditText keyStore = (EditText) findViewById(R.id.keyStorePassword);
        TextView error = (TextView) findViewById(R.id.error);
        String phoneNumber = phone.getText().toString();
        String key = emissionKey.getText().toString();
        String keyStorePassword = keyStore.getText().toString();

        // Formatting the phone number
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getSimCountryIso();
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + CountryToPhonePrefix.prefixFor(iso.toUpperCase()) + phoneNumber;
        }
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            //error.setText( "more than 21");
            phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, iso);
        } else {
            phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
        }

        if ((key.length() == 16 || key.length() == 0) && phoneNumber.length() >= 10){
            String contactName = "Unknown";
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();

            // TODO store the key, check if the number has already been used



            Intent intent = new Intent(this, Conversation.class);
            intent.putExtra("phoneNumber", phone.getText());
            intent.putExtra("contactName", contactName);
            intent.putExtra("alreadyExists", true);
            startActivity(intent);
        } else if (key.length() != 16) { // Error on the phoneNumber or the key
            emissionKey.setError("The key must be 16 char long");
        } else {
            phone.setError("Please check the phone is correct");
        }




        // TODO move
        // Sending SMS
        /*Log.i("Send SMS", "");
        String phoneNo = txtphoneNo.getText().toString();
        String message = txtMessage.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            //TODO encrypting / decrypting
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/
    }

    protected void displayError(int i) {
        TextView error = (TextView) findViewById(R.id.error);
        error.setTextColor(Color.RED);
        switch (i){
            case 0: error.setText("Error. Please check the key is 16 characters long.");
                break;
            case 1: error.setText("Error. Please check the phone number.");
                break;
        }
    }
}
