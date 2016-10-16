package com.example.utilisateur.cryptotext;

import java.util.EventObject;

/**
 * @author DonatienTertrais
 */
public class ReceiveEvent extends EventObject{
    private String number;

    public ReceiveEvent(Object source, String number) {
        super(source);
        this.number = number;
    }

    public String getNumber () {
        return this.number;
    }
}
