package com.application.labgui.Controller;

import com.application.labgui.AppExceptions.AppException;
import com.application.labgui.Domain.User;
import com.application.labgui.Service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditUtilizatorController {
    @FXML
    TextField idTextField;
    @FXML
    TextField prenumeTextField;
    @FXML
    TextField numeTextField;
    @FXML
    TextField usernameField;
    @FXML
    PasswordField passwordField;


    @FXML
    Button saveButton;

    @FXML
    Button cancelButton;

    private Service service;
    private Stage dialogStage;

    private User user;

    void setService(Service service, Stage dialogStage, User user){
        this.service = service;
        this.dialogStage = dialogStage;
        this.user = user;
        this.idTextField.setEditable(false);
        if(this.user != null){   // partea asta se ocupa si de save si de add. Daca utilizatorul nu e null=>update
            loadFields(user);
            this.saveButton.setText("Update");
        }
        else{
            this.idTextField.setText("Will be generated");
        }
    }

    private void loadFields(User user) {
        this.idTextField.setText(user.getId().toString());
        this.prenumeTextField.setText(user.getFirstName());
        this.numeTextField.setText(user.getLastName());
    }

    public void handlerCancel(ActionEvent actionEvent) {
        this.dialogStage.close();
    }

    public void handlerSave(ActionEvent actionEvent) {
        String prenume = this.prenumeTextField.getText();
        String nume = this.numeTextField.getText();
        String username=this.usernameField.getText();
        String password=this.passwordField.getText(); // not sure it is ok,i mean it is ok cuz you can't convert it if you don't get it first


        if(user ==null) {
            try {
                /**IMPORTANT: Un arrey de bytes daca e sout el nu va da the actual content pentru ca
                 * va apela metoda toString(),
                 * CI va da tipul de obiect ArrayByte [B urmat de @ si HashCode-ul obiectului
                 * respectiv in hexa => nu e raspunsul la criptare
                 * PENTRU A VEDEA ADEVARATA CRIPTARE TREBUIE:
                 * 1. sa afisam byte by byte
                 * 2. convertim in a readable format
                 *
                 *                 AES aes= AES.getInstance();
                 *                 AES aes1=AES.getInstance();
                 *                 System.out.println(aes1.hashCode()==aes.hashCode());
                 *                 System.out.println(aes1.encryptMessage("aer".getBytes()));
                 *                 System.out.println(aes.encryptMessage("aer".getBytes()));
                 *                 String encryptedBase64 = Base64.getEncoder().encodeToString(aes1.encryptMessage("aer".getBytes()));
                 *                 String encryptedBase642 = Base64.getEncoder().encodeToString(aes.encryptMessage("aer".getBytes()));
                 *                 System.out.println(encryptedBase64);
                 *                 System.out.println(encryptedBase642);*/
                this.service.addNewUser(nume, prenume,username,password);
                MessageAlert.showMessage(dialogStage, Alert.AlertType.CONFIRMATION, "", "A mers!");
                dialogStage.close(); //sa nu mai dea save de mai multe ori sau cv
            } catch (AppException e) {
                MessageAlert.showMessage(dialogStage, Alert.AlertType.ERROR, "EROARE", e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        //altfel inseamna ca e vorba de un update  // lasam asa momentan update-ul
        else{
            try{
                this.service.updateUser(user.getId(), nume, prenume);
                MessageAlert.showMessage(dialogStage, Alert.AlertType.CONFIRMATION, "", "A mers!");
                dialogStage.close();
            }catch (AppException e) {
                MessageAlert.showMessage(dialogStage, Alert.AlertType.ERROR, "EROARE", e.getMessage());
            }
        }
    }

}
