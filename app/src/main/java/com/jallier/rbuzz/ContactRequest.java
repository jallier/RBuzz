package com.jallier.rbuzz;

/**
 * Class to represent a contact request to send to the firebase /contactRequest queue
 */
public class ContactRequest {
    public String type;
    public String sender;
    public String recipient;

    public ContactRequest(){};

    /**
     *
     * @param type Type of contact request (either contactRequest or contactAccept
     * @param sender email address or uid of sender
     * @param recipient email address or uid of intended recipient
     */
    public ContactRequest(String type, String sender, String recipient) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
    }
}
