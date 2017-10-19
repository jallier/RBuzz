package com.jallier.rbuzz;

import java.util.List;

/**
 * Created by justin on 19/10/17.
 */

public class Message {
    public String sender;
    public List<Long> pattern;

    public Message(){}

    public Message(String sender, List<Long> pattern) {
        this.sender = sender;
        this.pattern = pattern;
    }
}
