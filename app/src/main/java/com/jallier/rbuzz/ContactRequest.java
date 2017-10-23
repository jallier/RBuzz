package com.jallier.rbuzz;

/**
 * Created by justin on 23/10/17.
 */

public class ContactRequest {
    public String sender;
    public String recipient;

    public ContactRequest(){};

    public ContactRequest(String sender, String recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }
}
