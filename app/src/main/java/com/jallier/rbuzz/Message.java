package com.jallier.rbuzz;

import java.util.List;

/**
 * Created by justin on 19/10/17.
 */

public class Message {
    public String sender;
    public String recipient;
    public List<Long> pattern;

    public Message(){}

    public Message(String sender, String recipient, List<Long> pattern) {
        this.sender = sender;
        this.recipient = recipient;
        this.pattern = pattern;
    }
}
