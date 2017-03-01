package com.example.utilisateur.cryptotext;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Asks for the password of the keystore file and creates it
 * @author DonatienTERTRAIS
 */
public class EnterPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);
    }

    /**
     * Save the password and creates the keystore file
     */
    public void saveChanges(){
        TextView info = (TextView) findViewById(R.id.info);
        EditText pwd = (EditText) findViewById(R.id.password);
        EditText pwdConfirmation = (EditText) findViewById(R.id.passwordConfirmation);
        String pwdStr = pwd.getText().toString();
        String pwdConfirmationStr = pwdConfirmation.getText().toString();

        if(pwdStr.equals(pwdConfirmationStr)) {
            pwd.setText("");
            pwdConfirmation.setText("");
            Encryption.createKeyStore(pwdStr);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            pwd.setText("");
            pwdConfirmation.setText("");
            info.setText(R.string.noMatchPassword);
            info.setTextColor(Color.RED);
        }
    }
}
