package com.jallier.rbuzz;

/**
 * Created by justin on 18/10/17.
 */

public class User {
    public String name;
    public String fcmToken;
    public String email;

    public User(){}

    public User(String name, String email, String fcmToken) {
        this.name = name;
        this.fcmToken = fcmToken;
        this.email = email;
    }
}
