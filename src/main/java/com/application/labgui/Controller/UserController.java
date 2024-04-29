package com.application.labgui.Controller;

import com.application.labgui.AppExceptions.AppException;
import com.application.labgui.Domain.FriendshipRequest;
import com.application.labgui.Domain.Message;
import com.application.labgui.Domain.User;
import com.application.labgui.Service.Service;
import com.application.labgui.Utils.Events.ChangeEventType;
import com.application.labgui.Utils.Events.ServiceChangeEvent;
import com.application.labgui.Utils.Observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

public class UserController implements Observer<ServiceChangeEvent> {
    public Label numeCunoscut;
    public ListView<User> listaPrieteni;
    public ListView<User> listaUtilizatoriCereri;
    public ButtonBar baraButoane;
    public Button refuzaCererea;
    public Button acceptaCererea;
    public Button trimiteCerere;
    public ButtonBar baraPrietenButoane;
    public Label numePrieten;
    public GridPane gridSendMessage;
    private Service service;
    private User userLogat;
    public TextField idPrietenNou;
    public TextField mesajNou;
    public Button sendButton;
    public ScrollPane scrollPane;

    private Message messageLaCareSeDaReply;

    ObservableList<User> modelPrieteni = FXCollections.observableArrayList();
    ObservableList<User> modelUtilizatoriCereri = FXCollections.observableArrayList();
    public void initUserController(Service service, User user){
        this.messageLaCareSeDaReply = null;
        this.service = service;
        userLogat = user;
        this.loadListe();
        this.service.addObserver(this);
        this.baraButoane.setVisible(false);
        this.baraPrietenButoane.setVisible(false);
        listaPrieteni.getSelectionModel().selectedItemProperty().addListener((observable -> {
            listaUtilizatoriCereri.getSelectionModel().clearSelection();
            this.baraButoane.setVisible(false);
            this.baraPrietenButoane.setVisible(true);
            this.gridSendMessage.setVisible(false);
            var utilizatorCunoscut = listaPrieteni.getSelectionModel().getSelectedItem();
            if(utilizatorCunoscut!=null){
                loadMesaje(utilizatorCunoscut);
            }
        }));


        listaUtilizatoriCereri.getSelectionModel().selectedItemProperty().addListener((observable -> {
            listaPrieteni.getSelectionModel().clearSelection();
            this.baraButoane.setVisible(true);
            this.gridSendMessage.setVisible(false);
            this.baraPrietenButoane.setVisible(false);
            var utilizatorCunoscut = listaUtilizatoriCereri.getSelectionModel().getSelectedItem();
            if(utilizatorCunoscut!=null){
                loadButoaneCereri(utilizatorCunoscut);
            }

        }));
    }

    private void loadButoaneCereri(User cunoscut){
        numeCunoscut.setText(cunoscut.getFirstName() + " " + cunoscut.getLastName());
        this.acceptaCererea.setDisable(false);
        this.refuzaCererea.setDisable(false);
        var lblNou = new Label("Nu poti trimite mesaj cat timpu nu esti prieten cu el!");
        var relatie = service.getRelatieBetween(userLogat.getId() , cunoscut.getId()).get();
        if(relatie.getId().getLeft().equals(userLogat.getId())){
            this.baraButoane.setVisible(false);
            if(relatie.getStatus() == FriendshipRequest.REFUSED){
                this.numeCunoscut.setText("Cererea ta a fost refuzata");
                lblNou.setText("Cererea ta a fost refuzata");
            }
            else{
                this.numeCunoscut.setText("Cererea ta e in asteptare");
                lblNou.setText("Cererea ta e in asteptare");
            }
        }
        else{
            if(relatie.getStatus() == FriendshipRequest.REFUSED){
                this.refuzaCererea.setDisable(true);
                this.numeCunoscut.setText("I-ai refuzat cererea lui " + cunoscut.getFirstName() + " " + cunoscut.getLastName());
                lblNou.setText("I-ai refuzat cererea lui " + cunoscut.getFirstName() + " " + cunoscut.getLastName());
            }
            else{
//                this.acceptaCererea.setText("Accepta cererea");
//                this.refuzaCererea.setText("Refuza cererea");
            }
        }
//        this.baraButoane.setVisible(false);
//        var lblNou = new Label("Nu poti trimite mesaj cat timpu nu esti prieten cu el!");
        this.scrollPane.setContent(lblNou);
    }


    private void loadListe(){
        listaPrieteni.setItems(modelPrieteni);
        listaUtilizatoriCereri.setItems(modelUtilizatoriCereri);
        this.reloadListe();
    }

    private void reloadListe(){
        var toateListele = this.service.cereriDePrietenie(userLogat.getId());
        if(toateListele.isEmpty()){
            return;
        }
        modelPrieteni.setAll(toateListele.get(1));
        modelUtilizatoriCereri.setAll(toateListele.get(2));
    }

    /**
     * @param cunoscut - userul cu care se face convorbirea
     * Da istoricul mesajelor dintre doi useri - cel logat si prietenul sau*/
    private void loadMesaje(User cunoscut){
        this.gridSendMessage.setVisible(true);
        this.numePrieten.setText(cunoscut.getFirstName() + " " + cunoscut.getLastName());

        var listaToateMesajele = this.service.getAllMessagesBetween(userLogat.getId(), cunoscut.getId());


        VBox vBox = new VBox();
        this.scrollPane.setContent(vBox);  // pentru a putea da in jos pe mesaje
        vBox.setPrefWidth(scrollPane.getPrefWidth());
        listaToateMesajele.forEach(mesaj->{
            // pentru fiecare mesaj pe rand se face un nou single pane unde se pozitioneaza mesajul
            try{
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("views/single_mesaj_view.fxml"));
                AnchorPane root = fxmlLoader.load();
                vBox.getChildren().add(root); // il adugi
                MesajViewController controllerMesaj = fxmlLoader.getController();
                controllerMesaj.initMesaj(this,mesaj, userLogat);
                root.setPrefWidth(vBox.getPrefWidth()); // sa fie cat mesajul de mare
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        scrollPane.setVvalue(1.0); // pune scrollingul la punctul del mai de jos (ca pe mess)
    }

    public void sendMesssage(ActionEvent actionEvent) {
        var cunoscut = listaPrieteni.getSelectionModel().getSelectedItem(); // se ia utilizatorul cu care se vorbeste

        var text_scris = this.mesajNou.getText(); // mesajul nou scris
        try{
            /**
             * Sunt doua tipuri de send
             * */
            if(messageLaCareSeDaReply == null) {
                this.service.sentNewMessage(userLogat.getId(), Collections.singletonList(cunoscut.getId()), text_scris, LocalDateTime.now());
            }
            else{
                this.service.sentNewMessage(userLogat.getId(), cunoscut.getId(), messageLaCareSeDaReply.getId(), text_scris, LocalDateTime.now());
            }
            this.mesajNou.clear();
            this.messageLaCareSeDaReply = null;
        }
        catch (AppException e){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", e.getMessage());
        }
    }

    @Override
    public void update(ServiceChangeEvent eventUpdate) {
        Long idCelalaltUser;
        if(Objects.equals(eventUpdate.getUser1(), userLogat.getId())){
            idCelalaltUser = eventUpdate.getUser2();
        }
        else if(Objects.equals(eventUpdate.getUser2(), userLogat.getId())){
            idCelalaltUser = eventUpdate.getUser1();
        }
        else {
            return;
        }
        var user = service.findOne(idCelalaltUser).get();
        this.baraButoane.setVisible(false);
        this.baraPrietenButoane.setVisible(false);
        if (eventUpdate.getType().equals(ChangeEventType.MESSAGES)) {
            var cunoscut = listaPrieteni.getSelectionModel().getSelectedItem();
            listaPrieteni.getSelectionModel().select(user);
            this.loadMesaje(user);
        } else if (eventUpdate.getType().equals(ChangeEventType.FRIENDS)) {
            this.reloadListe();
        }
    }

    public void handlerRefuzaCererea(ActionEvent actionEvent) {
        var cunoscut = listaUtilizatoriCereri.getSelectionModel().getSelectedItem();
//        System.out.println("refuza");
        this.service.refuseCererePrietenie(cunoscut.getId(), userLogat.getId());
    }

    public void handlerAcceptaCererea(ActionEvent actionEvent) {
        var cunoscut = listaUtilizatoriCereri.getSelectionModel().getSelectedItem();
//        System.out.println("Accepta");
        this.service.acceptCererePrietenie(cunoscut.getId(), userLogat.getId());
    }

    public void handlerTrimiteCerere(ActionEvent actionEvent) {
        if(this.idPrietenNou.getText().isEmpty()) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", "Campul id e gol");
            return;
        }
        var idViitorPrieten = Long.parseLong(this.idPrietenNou.getText());
        try{
            service.trimiteCererePrietenie(userLogat.getId(), idViitorPrieten);
            this.idPrietenNou.clear();
        }
        catch (AppException e){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", e.getMessage());
        }
    }

    public void setReply(Message message){
        messageLaCareSeDaReply = message;
    }
}