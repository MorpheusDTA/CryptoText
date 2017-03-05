package com.example.utilisateur.cryptotext;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Enables the user to enter the password for the keyStore file
 * @author DonatienTERTRAIS
 */
public class EnterPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO create the file, if it exists go to next else, create keystore
        if (Encryption.exists(getApplication())){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);
    }

    public void saveChanges(View view) {
        TextView info = (TextView) findViewById(R.id.info);
        Editable pwd = ((EditText) findViewById(R.id.pwd)).getText();
        Editable pwdConfirmation = ((EditText) findViewById(R.id.pwdConf)).getText();
        String pwdStr = pwd.toString(), pwdConfirmationStr = pwdConfirmation.toString();
        pwd.clear();
        pwdConfirmation.clear();

        if (pwdStr.isEmpty() || pwdConfirmationStr.isEmpty()){
            info.setText(R.string.nullPwd);
            info.setTextColor(Color.RED);
        }

        if(pwdStr.equals(pwdConfirmationStr)) {
            Encryption.createKeyStore(getApplication(), pwdStr);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            info.setText(R.string.noMatchPassword);
            info.setTextColor(Color.RED);
        }
    }
}