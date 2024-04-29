package com.application.labgui.Controller;

import com.application.labgui.AppExceptions.AppException;
import com.application.labgui.Domain.*;
import com.application.labgui.Repository.Paging.Page;
import com.application.labgui.Repository.Paging.Pageable;
import com.application.labgui.Service.Service;
import com.application.labgui.SocialNetworkApplication;
import com.application.labgui.Utils.Events.ServiceChangeEvent;
import com.application.labgui.Utils.Observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

public class SocialNetworkController implements Observer<ServiceChangeEvent> {

    public Button loginUser;
    private Service serviceSocialNetwork;

    ObservableList<User> model = FXCollections.observableArrayList();
    ObservableList<FriendshipDTO> modelPrieteni = FXCollections.observableArrayList();

    HashMap<Long, Node> listaUseriLogati;

    @FXML
    TableView<User> utilizatorTableView;
    @FXML
    TableColumn<User, Long> columnID;
    @FXML
    TableColumn<User, String> columnFirstName;
    @FXML
    TableColumn<User, String> columnLastName;
    @FXML
    TableColumn<User,String> columnUsername;

    @FXML
    Button previousButton;

    @FXML
    Button nextButton;

    @FXML
    Slider nrOfUsersSld;
    @FXML
    Label nrOfPageLabel;

    private int currentPage=0;
    private int numberOfRecordsPerPage= 5;


    private int totalNumberOfElements;
    @FXML
    HBox hBoxTables;

    public SocialNetworkController() throws Exception {
    }

    @Override
    public void update(ServiceChangeEvent eventUpdate) {
        initData();
    }

    public void setServiceSocialNetwork(Service serviceSocialNetwork) {
        this.serviceSocialNetwork = serviceSocialNetwork;
        serviceSocialNetwork.addObserver(this);
        initData();

    }

    public void initialize() {
        listaUseriLogati = new HashMap<>();
        columnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));//numele din domeniu al atributului
        columnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));// username field din Utilizator class


        utilizatorTableView.setItems(model);
        utilizatorTableView.getSelectionModel().selectedItemProperty().addListener((observable -> {
            var utilizator = utilizatorTableView.getSelectionModel().getSelectedItem();
            if(utilizator == null) {
                utilizatorTableView.setPrefHeight(hBoxTables.getHeight());
                utilizatorTableView.setPrefWidth(hBoxTables.getWidth());
            }
            else {
                utilizatorTableView.setPrefHeight(hBoxTables.getHeight()/2);
                utilizatorTableView.setPrefWidth(hBoxTables.getWidth()/2);
                reloadFriendsModel(utilizator.getId());
            }
        }));
    }

    public void handleAddUtilizator(ActionEvent actionEvent){
        this.showUtilizatorEditDialog(null);
    }

    private void showUtilizatorEditDialog(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("views/edituser_view.fxml"));
            //aici la resources trb sa fie cam aceeasi chestie ca in folderul celalalt

            AnchorPane root = fxmlLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);

            dialogStage.setScene(scene);

            EditUtilizatorController editUtilizatorController = fxmlLoader.getController();
            editUtilizatorController.setService(serviceSocialNetwork, dialogStage, user);
            dialogStage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void showPrieteniAddDialog(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("views/editprieteni_view.fxml"));
            //aici la resources trb sa fie cam aceeasi chestie ca in folderul celalalt
            AnchorPane root = fxmlLoader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add prietenie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EditPrieteniController editPrieteniController = fxmlLoader.getController();
            editPrieteniController.setService(serviceSocialNetwork, dialogStage, user);
            dialogStage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void handleUpdateUtilizator(ActionEvent actionEvent){
        User user = utilizatorTableView.getSelectionModel().getSelectedItem();
        if(user == null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", "Nu ai selectat niciun student");
            return;
        }
        showUtilizatorEditDialog(user);
    }

    public void handleDeleteUtilizator(ActionEvent actionEvent){
        User user = utilizatorTableView.getSelectionModel().getSelectedItem();
        if(user == null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", "Nu ai selectat niciun student");
            return;
        }
        try {
            serviceSocialNetwork.deleteUtilizator(user.getId());
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "", "");
        }
        catch (AppException appException){
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "", appException.getMessage());
        }
    }

    private void reloadFriendsModel(Long idUser){
        Iterable<FriendshipDTO> listaPrieteni = serviceSocialNetwork.relatiiDePrietenie(idUser);
        List<FriendshipDTO> listaDTO = StreamSupport.stream(listaPrieteni.spliterator(), false).toList();
        modelPrieteni.setAll(listaDTO);
    }

    private void initModel(){
        Iterable<User> listaUsers = serviceSocialNetwork.getAllUtilizatori();
        List<User> userList = StreamSupport.stream(listaUsers.spliterator(), false).toList();
        model.setAll(userList);
        this.utilizatorTableView.getSelectionModel().clearSelection();
    }


    public void clearSelectionMainTable(){
        this.utilizatorTableView.getSelectionModel().clearSelection();
    }

    private void reloadColumns(){
        columnID.setPrefWidth(utilizatorTableView.getWidth()/5);
        columnLastName.setPrefWidth(2* utilizatorTableView.getWidth()/5);
        columnFirstName.setPrefWidth(2 * utilizatorTableView.getWidth()/5);
    }

    public void handleDeletePrietenie(ActionEvent actionEvent) {
//        PrietenieDTO prietenieDTO = prieteniTableView.getSelectionModel().getSelectedItem();
        FriendshipDTO friendshipDTO = null;
        if(friendshipDTO ==null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", "Nu ai selectat niciun student");
            return;
        }
        try{
            serviceSocialNetwork.deletePrietenie(new Tuplu<>(friendshipDTO.getId1(), friendshipDTO.getId2()));
        }
        catch (AppException e){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", e.getMessage());
        }
    }

    public void handleAddPrietenie(ActionEvent actionEvent){
        var utilizator = utilizatorTableView.getSelectionModel().getSelectedItem();
        if(utilizator==null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", "Nu ai selectat niciun student");
            return;
        }
        showPrieteniAddDialog(utilizator);
    }
    public void handleLoginUser(ActionEvent actionEvent) {
        var utilizator = utilizatorTableView.getSelectionModel().getSelectedItem();
        if(utilizator == null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", "Nu ai selectat niciun utilizator");
            return;
        }

        var gasit = listaUseriLogati.get(utilizator.getId());
        if(gasit != null){
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "", "Exista deja userul!");
            return;
        }


        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("views/login_view.fxml"));
            AnchorPane root = fxmlLoader.load();
            Stage userStage = new Stage();
            userStage.setTitle("LOG AS");

            Scene scene = new Scene(root);
            userStage.setScene(scene);
            LogInController logInController = fxmlLoader.getController();
            logInController.initLogInController(serviceSocialNetwork,userStage,listaUseriLogati);
            userStage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    private void handlePageNavigationChecks(){
        previousButton.setDisable(currentPage ==0);
        nextButton.setDisable((currentPage+1)*numberOfRecordsPerPage >= totalNumberOfElements);
    }



    public void initData(){
       // numberOfRecordsPerPage=slider.getT
        Page<User> moviesOnCurrentPage = serviceSocialNetwork.getUsersOnPage(new Pageable(currentPage, numberOfRecordsPerPage));
        totalNumberOfElements = moviesOnCurrentPage.getTotalNumberOfElements();
        List<User> userList = StreamSupport.stream(moviesOnCurrentPage.getElementsOnPage().spliterator(), false).toList();
        model.setAll(userList);
        handlePageNavigationChecks();
        this.utilizatorTableView.getSelectionModel().clearSelection();
    }


    public void goToNextPage(ActionEvent actionEvent) {
        //System.out.println("NEXT PAGE");
        currentPage++;
        nrOfPageLabel.setText(String.valueOf(currentPage+1));
        initData();

    }

    public void goToPreviousPage(ActionEvent actionEvent) {
        //System.out.println("PREVIOUS PAGE");
        currentPage--;
        nrOfPageLabel.setText(String.valueOf(currentPage+1));
        initData();
    }



    public void handleNrOfUsersPerPage(MouseEvent mouseEvent) {
        numberOfRecordsPerPage = (int) nrOfUsersSld.getValue(); // take the number from the slide
        currentPage=0; // in case the user is at a page and changes the slider to a grater value to avoid popping up an empty sheet
        initData();
        nrOfPageLabel.setText(String.valueOf(currentPage+1));
    }
}
