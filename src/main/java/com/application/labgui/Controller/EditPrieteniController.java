package com.application.labgui.Controller;

import com.application.labgui.AppExceptions.AppException;
import com.application.labgui.Domain.FriendshipDTO;
import com.application.labgui.Domain.User;
import com.application.labgui.Service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class EditPrieteniController {

    @FXML
    TextField idUtilizator1;
    @FXML
    TextField idUtilizator2;
    @FXML
    Button saveButton;
    @FXML
    Button cancelButton;
    @FXML
    ComboBox<FriendshipDTO> utilizatoriComboBox;

    private Service service;
    private Stage dialogStage;
    private User user;

    public void setService(Service service, Stage dialogStage, User user){
        this.service = service;
        this.dialogStage = dialogStage;
        this.user = user;
        this.idUtilizator1.setEditable(false);
        utilizatoriComboBox.setVisible(false);
        loadPossibleFriends();
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (Pattern.matches("[0-9]*", newText)) {
                return change;
            }
            return null;
        };
        TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, filter);
        idUtilizator2.setTextFormatter(textFormatter);
        idUtilizator2.setText("1");
    }
    private void loadPossibleFriends(){
        this.idUtilizator1.setText(user.getId().toString());
//        var listaNeprieteni = service.relatiiDePrietenie(utilizator.getId());
//        var listaLista = StreamSupport.stream(listaNeprieteni.spliterator(), false).toList();
//        utilizatoriComboBox.getItems().setAll(listaLista);
    }

    public void handlerCancel(ActionEvent actionEvent) {
        this.dialogStage.close();
    }

    public void handlerSave(ActionEvent actionEvent) {
        //idk
        Long id_utilizator1 = user.getId();
        Long id_utilizator2 = Long.parseLong(idUtilizator2.getText());
        try{
            service.addPrietenie(id_utilizator1, id_utilizator2);
            this.dialogStage.close();
        } catch (AppException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", e.getMessage());
        }
    }
}
