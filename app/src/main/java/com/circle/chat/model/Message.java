package com.circle.chat.model;

public class Message {
    private String messageId, message, senderId, imageUrl;
    private String loggedin_username;
    private long timestamp;
    private int feeling = -1;

    public Message() {
    }

    public Message(String message, String senderId, long timestamp, String loggedin_username) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.loggedin_username = loggedin_username;
    }

    public Message(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLoggedin_username() {
        return loggedin_username;
    }

    public void setLoggedin_username(String loggedin_username) {
        this.loggedin_username = loggedin_username;
    }

//    public int getCountOfUsers() {
//        int count =
//    }
}
