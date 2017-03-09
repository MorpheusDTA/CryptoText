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
 * Activity enabling the user to change the keystore password
 * @author DonatienTertrais
 */
public class ChangePassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
    }

    /**
     * Changes the password of the KeyStore file
     * @param view View of the button
     */
    public void changePwd(View view) {
        TextView info = (TextView) findViewById(R.id.chgPwdInfo);
        Editable oldPwdField = ((EditText) findViewById(R.id.oldPwd)).getText(),
                 newPwdField = ((EditText) findViewById(R.id.newPwd)).getText(),
                 newPwdConfField = ((EditText) findViewById(R.id.newPwdConf)).getText();
        String oldPwd = oldPwdField.toString(), newPwd = newPwdField.toString(),
                newPwdConf = newPwdConfField.toString();
        if(!Encryption.testPassword(getApplication(), oldPwd)){//If the old password is not correct
            info.setTextColor(Color.RED);
            info.setText(R.string.wrongOldPwd);
            return;
        }
        if(!newPwd.equals(newPwdConf)) {// If new passwords do not match
            info.setTextColor(Color.RED);
            info.setText(R.string.pwdNoMatch);
            return;
        }
        if (!Encryption.changePwd(getApplication(), oldPwd, newPwd)) {
            info.setTextColor(Color.RED);
            info.setText(R.string.changeFailed);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}
