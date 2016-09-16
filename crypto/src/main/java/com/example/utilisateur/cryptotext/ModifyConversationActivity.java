package com.example.utilisateur.cryptotext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author DonatienTERTRAIS
 */
public class ModifyConversationActivity extends AppCompatActivity {
    private static final Level level = Level.WARNING;
    private static Logger logger = Logger.getLogger(Encryption.class.getName());
    private EditText phone = (EditText) findViewById(R.id.phone);
    private EditText receptionKeySeed = (EditText) findViewById(R.id.receptionKeySeed);
    private EditText emissionKeySeed = (EditText) findViewById(R.id.emissionkeySeed);
    private EditText keyStoreField = (EditText) findViewById(R.id.keyStorePassword);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String phoneN = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifyconversation);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                phoneN = extras.getString(MainActivity.PHONE);
            }
        } else {
            phoneN = (String) savedInstanceState.getSerializable(MainActivity.PHONE);
        }
        if (phoneN != null) {
            phone.setText((phoneN));
            phone.setEnabled(false);
        }
    }

    public void createConversation(View view){
        String phoneNumber = phone.getText().toString();
        String emissionSeed = emissionKeySeed.getText().toString();
        String receptionSeed = receptionKeySeed.getText().toString();
        String keyStorePassword = keyStoreField.getText().toString();
        phoneNumber = formatNumber(phoneNumber);
        String errors ="";
        if (phoneNumber == null) {
            errors = errors + "The phone number isn't correct\n";
        } else {
            if (emissionSeed.length() != 0 ^ Encryption.isStocked(phoneNumber + "Out", keyStorePassword)) {
                if (emissionSeed.length() != 0) {
                    errors = errors + "The current emission seed will be overwritten\n";
                } else {
                    errors = errors + "The emission seed must be set\n";
                }
            } if (receptionSeed.length() != 0 ^ Encryption.isStocked(phoneNumber + "In", keyStorePassword)) {
                if (receptionSeed.length() != 0) {
                    errors = errors + "The current reception seed will be overwritten\n";
                } else {
                    errors = errors + "The reception seed must be set\n";
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
        /*String phoneNumber = phone.getText().toString();
        String emissionSeed = emissionKeySeed.getText().toString();
        String receptionSeed = receptionKeySeed.getText().toString();
        String keyStorePassword = keyStoreField.getText().toString();
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
        }*/
        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra("phoneNumber", phone.getText());
        startActivity(intent);
    }

    public void createAlertDialog (String errors) {
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
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return PhoneNumberUtils.formatNumberToE164(phoneNumber, iso);
        } else {
            return PhoneNumberUtils.formatNumber(phoneNumber);
        }
    }
}