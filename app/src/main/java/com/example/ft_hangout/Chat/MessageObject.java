package com.example.ft_hangout.Chat;

import java.util.ArrayList;

public class MessageObject {

    String messageId, senderId, message, mDate;

    ArrayList<String> mediaUrlist;

    public MessageObject(String messageId, String senderId, String message, ArrayList<String> mediaUrlist, String mDate) {

        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaUrlist = mediaUrlist;
        this.mDate = mDate;
    }

    public String getMessage() { return message; }

    public String getMessageId() { return messageId; }

    public String getSenderId() { return senderId; }

    public ArrayList<String> getMediaUrlist() { return mediaUrlist; }

    public String getmDate() { return mDate; }
}
