package com.example.utilisateur.cryptotext;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Main class : displays the current conversations
 * @author DonatienTERTRAIS
 */
public class EnterPassword extends AppCompatActivity {
    private static final int LIMIT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Encryption.exists(getApplicationContext())){// If the keystore file does not exist, it is to be created
            Intent intent = new Intent(this, EnterPassword.class);
            startActivity(intent);
        }
    }

    /**
     * Save the password and creates the keystore file
     * @param view View of the button
     */
    public void saveChanges(View view){
        TextView info = (TextView) findViewById(R.id.info);
        EditText pwd = (EditText) findViewById(R.id.password);
        EditText pwdConfirmation = (EditText) findViewById(R.id.passwordConfirmation);
        Editable pwdEdit = pwd.getText(), pwdConfirmationEdit = pwdConfirmation.getText();
        if (pwdEdit == null || pwdConfirmationEdit == null) {
            pwd.setText("");
            pwdConfirmation.setText("");
            info.setTextColor(Color.RED);
            info.setText(R.string.nullPwd);
            return;
        }
        String pwdStr = pwdEdit.toString();
        String pwdConfirmationStr = pwdConfirmationEdit.toString();
        pwd.setText("");
        pwdConfirmation.setText("");

        if(pwdStr.equals(pwdConfirmationStr) && pwdStr.length() >= LIMIT) {
            Encryption.createKeyStore(getApplicationContext(), pwdStr);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            info.setTextColor(Color.RED);
            info.setText(R.string.noMatchPassword);
        }
    }
}