package com.asyraf.codan.common;

/**
 * Created by MyPC on 12/04/2016.
 */
public class Constant {
    public static final String FIREBASE_CHAT_URL="https://demofirebasebylam1.firebaseio.com/"; //Your app main firebase url
    public static final String CHILD_USERS="users";
    public static final String CHILD_CHAT="chat";
    public static final String KEY_SEND_USER="key_send_user";
    public static final String CHILD_CONNECTION="connection";
    public static final String CHILD_LATITUDE="latitude";
    public static final String CHILD_LONGITUDE="longitude";
    public static final String KEY_EMAIL="email";
    public static final String KEY_ONLINE="online";
    public static final String KEY_OFFLINE="offline";
    public static final String KEY_CLOSE="key_close";

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 60;
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Stores the lat / long pairs in a text file
    public static final String LOCATION_FILE = "sdcard/location.txt";
    // Stores the connect / disconnect data in a text file
    public static final String LOG_FILE = "sdcard/log.txt";

    public static final String RUNNING = "runningInBackground"; // Recording data in background

    public static final String APP_PACKAGE_NAME = "com.asyraf.codan";

}
