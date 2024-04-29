package com.application.labgui.Controller;


import com.application.labgui.Domain.Message;
import com.application.labgui.Domain.User;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.format.DateTimeFormatter;

public class MesajViewController  {

    public Label dataTextEu;
    public Label mesajReplyTextEu;
    public TextFlow textAreaEu;
    public Label dataTextEl;
    public Label mesajReplyTextEl;
    public TextFlow textAreaEl;
    public Button replyButton;
    private User sender;
    private Message message;

    private UserController parent;

    public void initMesaj(UserController parent, Message message, User sender){
        this.parent = parent;
        this.message = message;
        this.sender = sender;
        configure();
    }

    private void configure(){
        if(message.getFromUser().equals(sender.getId())){
            //inseamna ca suntem la cel care a trimis mesajul, deci il afisam pe partea lui
            this.textAreaEl.setVisible(false);
            this.textAreaEu.setVisible(true);
            this.textAreaEu.getChildren().add(new Text(this.message.getMesajScris()));
            this.dataTextEu.setText(message.getData().format(DateTimeFormatter.ISO_DATE_TIME));
            if(message.getReplyTo()==null){
                mesajReplyTextEu.setVisible(false);

            }
            else{
                mesajReplyTextEu.setVisible(true);
                var mesajulLaCareSeDa = message.getReplyTo().getMesajScris().substring(0, Math.min(20, message.getReplyTo().getMesajScris().length()));
                mesajulLaCareSeDa += "...";
                mesajReplyTextEu.setText(mesajulLaCareSeDa);
            }
        }
        else{
            this.textAreaEu.setVisible(false);
            this.textAreaEl.setVisible(true);
            this.textAreaEl.getChildren().add(new Text(message.getMesajScris()));
            this.dataTextEl.setText(message.getData().format(DateTimeFormatter.ISO_DATE_TIME));
            if(message.getReplyTo()==null){
                mesajReplyTextEl.setVisible(false);
            }
            else{
                mesajReplyTextEl.setVisible(true);
                var mesajScris = message.getReplyTo().getMesajScris();
                var mesajulLaCareSeDa = mesajScris.substring(0, Math.min(20, mesajScris.length())) + "...";
                mesajulLaCareSeDa += "...";
                mesajReplyTextEl.setText(mesajulLaCareSeDa);
            }
        }
    }


    public void handlerReply(ActionEvent actionEvent) {
        this.parent.setReply(message);
    }
}
