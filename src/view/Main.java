package view;

import constants.Messages;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainPage.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Solver of transport problems");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        ((Controller) fxmlLoader.getController()).setStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
