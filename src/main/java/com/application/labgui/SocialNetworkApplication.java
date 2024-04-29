package com.application.labgui;

import com.application.labgui.Controller.SocialNetworkController;
import com.application.labgui.Domain.Friendship;
import com.application.labgui.Domain.Tuplu;
import com.application.labgui.Repository.*;
import com.application.labgui.Service.Service;
import com.application.labgui.Validators.ValidatorStrategies;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SocialNetworkApplication extends Application{
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("views/socialnetwork-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 450);

        intializeStructure(fxmlLoader); /// initializam elementele din arhitectura stratificata
        clearSelectionClickOutside(fxmlLoader, scene);
        stage.setTitle("Social Network");

        scene.getStylesheets().add(SocialNetworkApplication.class.getResource("/com/application/labgui/styles/general_style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    private void clearSelectionClickOutside(FXMLLoader fxmlLoader, Scene scene){
        SocialNetworkController controller = fxmlLoader.getController();
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            Node source = evt.getPickResult().getIntersectedNode();
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }
            if (source == null || (source instanceof TableRow && ((TableRow) source).isEmpty())) {
                controller.clearSelectionMainTable();
            }
        });
    }


    private void intializeStructure(FXMLLoader fxmlLoader) throws SQLException {
        DBConnection dbConnection = new DBConnection();

        //Connection connection= DriverManager.getConnection(dbConnection.DB_URL,dbConnection.DB_USER,dbConnection.DB_PASSWD);

        UserDBRepository userDBRepository = new UserDBRepository(dbConnection, ValidatorStrategies.UTILIZATOR);

        Repository<Tuplu<Long, Long>, Friendship> prietenieDBRepository = new FriendshipDBRepository(dbConnection, ValidatorStrategies.PRIETENIE);
        FriendshipRequestsDBRepository repositoryCereriPrietenii = new FriendshipRequestsDBRepository(dbConnection, ValidatorStrategies.CEREREPRIETENIE);
        MessageDBRepository mesajDBRepository = new MessageDBRepository(dbConnection);
        Service serviceApp = new Service(userDBRepository, repositoryCereriPrietenii, prietenieDBRepository, mesajDBRepository, ValidatorStrategies.UTILIZATOR, ValidatorStrategies.PRIETENIE);

        SocialNetworkController socialNetworkController = fxmlLoader.getController();
        socialNetworkController.setServiceSocialNetwork(serviceApp);

    }
    public static void main(String[] args) {
        launch();
    }
}