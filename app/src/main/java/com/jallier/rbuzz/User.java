package com.jallier.rbuzz;

/**
 * Created by justin on 18/10/17.
 */

public class User {
    public String name;
    public String fcmToken;

    public User(){}

    public User(String name, String fcmToken) {
        this.name = name;
        this.fcmToken = fcmToken;
    }
}
