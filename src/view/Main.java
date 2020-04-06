package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


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
