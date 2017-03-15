package com.example.utilisateur.cryptotext;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.utilisateur.cryptotext.Constants.PASSWORD_MIN_LENGTH;

/**
 * Enables the user to enter the password for the keyStore file
 * @author DonatienTERTRAIS
 */
public class EnterPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);
        if (Encryption.exists(getApplication())){
            findViewById(R.id.saveChanges).setEnabled(false);
            findViewById(R.id.pwd).setEnabled(false);
            findViewById(R.id.pwdConf).setEnabled(false);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void saveChanges(View view) {
        EditText pwdConfEdit = (EditText) findViewById(R.id.pwdConf),
                 pwdEdit = (EditText) findViewById(R.id.pwd);
        TextView info = (TextView) findViewById(R.id.info);
        Editable pwd = pwdEdit.getText(), pwdConf = pwdConfEdit.getText();
        String pwdStr = pwd.toString(), pwdConfStr = pwdConf.toString();
        pwd.clear(); pwdConf.clear();

        if ((pwdStr.isEmpty() || pwdConfStr.isEmpty()) && info != null){
            info.setTextColor(Color.RED);
            info.setText(R.string.nullPwd);
        }

        if(pwdStr.equals(pwdConfStr) && pwdStr.length() >= PASSWORD_MIN_LENGTH) {
            Encryption.createKeyStore(getApplication(), pwdStr);

            findViewById(R.id.saveChanges).setEnabled(false);
            pwdEdit.setEnabled(false); pwdConfEdit.setEnabled(false);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (info != null){
            info.setTextColor(Color.RED);
            info.setText(R.string.noMatchPassword);
        }
    }
}