package com.asyraf.codan.object;

/**
 * Created by MyPC on 12/04/2016.
 */
public class User {
    public String id;
    public String name;
    public String email;
    public String connection;
    public String cratedAt;
    public double latitude;
    public double longitude;

    public User() {
    }

    public User(String id, String name, String email, String connection, String cratedAt, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.connection = connection;
        this.cratedAt = cratedAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
