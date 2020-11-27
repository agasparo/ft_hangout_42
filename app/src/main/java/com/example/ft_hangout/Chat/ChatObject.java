package com.example.ft_hangout.Chat;

public class ChatObject {

    private String  chatId,
                    chatName,
                    chatImg,
                    lastMsg,
                    recevedId,
                    desc,
                    alias,
                    relation;

    public ChatObject(String chatId, String chatName, String chatImg, String lastMsg, String recevedId, String desc, String alias, String relation) {

        this.chatId = chatId;
        this.chatName = chatName;
        this.chatImg = chatImg;
        this.lastMsg = lastMsg;
        this.recevedId = recevedId;
        this.desc = desc;
        this.alias = alias;
        this.relation = relation;
    }

    public String getChatId() { return chatId; }

    public String getChatName() { return chatName; }

    public String getChatImg() { return chatImg; }

    public String getLastMsg() { return lastMsg; }

    public String getRecevedId() { return recevedId; }

    public String getAlias() { return alias; }

    public String getDesc() { return desc; }

    public String getRelation() { return relation; }
}
