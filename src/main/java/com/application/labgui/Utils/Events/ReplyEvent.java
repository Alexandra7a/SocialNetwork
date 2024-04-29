package com.application.labgui.Utils.Events;

import com.application.labgui.Domain.Message;
import com.application.labgui.Domain.User;

public class ReplyEvent implements Event{
    private ChangeEventType type=ChangeEventType.REPLY;
    private Message message;
    private User replyer;

    public ReplyEvent(Message message, User replyer){
        this.message = message;
        this.replyer = replyer;
    }


    public ChangeEventType getType() {
        return type;
    }

    public Message getMesaj() {
        return message;
    }

    public void setMesaj(Message message) {
        this.message = message;
    }
}
