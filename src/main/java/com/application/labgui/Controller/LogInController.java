package com.application.labgui.Controller;

import com.application.labgui.AppExceptions.ServiceException;
import com.application.labgui.Domain.AES;
import com.application.labgui.Domain.User;
import com.application.labgui.Service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;


public class LogInController {

    @FXML
    TextField usernameField;
    @FXML
    PasswordField passwordField;
    Service service;
    HashMap<Long, Node> listaUseriLogati;
    @FXML
    Stage stage;


    public void initLogInController(Service serviceSocialNetwork, Stage stage,HashMap<Long, Node> l) {
        this.service = serviceSocialNetwork;
        this.listaUseriLogati = l;
        this.stage=stage;
    }

    public void LogInController() {
        /*TODO de pus aici lucrurile de care avem nevoie pentru logarea unui user
         *  TODo sau putem lua direct log in fara a mai selecta un om, bagam un username si parola si dupa
         *   cautam dupa cele doua care sa fie un tabel cu cheia primara username
         *
         * CCRED CA VARIANTA DOI E MAI BUNA, SI DUPA DE ACOLO LUAM TOATE DATELE
         * */
    }

    public void OnLogInAction(ActionEvent actionEvent) throws Exception {
        System.out.println("LOG IN BUTTON CLICKED");
        System.out.println(usernameField.getText());
        System.out.println(passwordField.getText());
        String username = usernameField.getText();
        String password = passwordField.getText();
        AES aes = AES.getInstance();
        byte[] hashedPasswordArrayByte = aes.encryptMessage(password.getBytes());
        String hasedPassword = Base64.getEncoder().encodeToString(hashedPasswordArrayByte);
        System.out.println(hasedPassword);

        try {
            User user = service.findOnesAccount(username, hasedPassword);
            stage.close();
            showUserChatRoom(user);
        } catch (ServiceException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", e.getMessage());
        }
    }
    private void showUserChatRoom(User user) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("views/user_view.fxml"));
            AnchorPane root = fxmlLoader.load();
            Stage userStage = new Stage();
            userStage.setTitle(user.getFirstName());
            this.listaUseriLogati.put(user.getId(), root);
            Scene scene = new Scene(root);
            userStage.setScene(scene);
            userStage.setOnCloseRequest(event -> {
//              System.out.println("Close button was clicked!");
                listaUseriLogati.remove(user.getId());
            });
            UserController userController = fxmlLoader.getController();
            userController.initUserController(service, user);
            userStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}


/*
       FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("views/login_view.fxml"));
            AnchorPane root = fxmlLoader.load();
            Stage userStage = new Stage();
            userStage.setTitle(utilizator.getFirstName());
            this.listaUseriLogati.put(utilizator.getId(), root);
            Scene scene = new Scene(root);
            userStage.setScene(scene);
            userStage.setOnCloseRequest(event -> {
//              System.out.println("Close button was clicked!");
                listaUseriLogati.remove(utilizator.getId());
            });
            UserController userController = fxmlLoader.getController();
            userController.initUserController(serviceSocialNetwork, utilizator);
            userStage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
 */