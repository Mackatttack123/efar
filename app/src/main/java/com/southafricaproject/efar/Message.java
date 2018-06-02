package com.southafricaproject.efar;

/**
 * Created by mackfitzpatrick on 6/2/18.
 */

class Message {
    private String name;
    private String timestamp;
    private String message;

    // constructor
    public Message(String name, String timestamp, String message) {
        this.name = name;
        this.timestamp = timestamp;
        this.message = message;
    }

    // getter
    public String getName() { return name; }
    public String getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
}
