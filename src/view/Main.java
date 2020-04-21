package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utilities.I18N;

import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.titleProperty().bind(I18N.createStringBinding("title"));
        primaryStage.getIcons().add(new Image("/res/icon.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainPage.fxml"));
        ResourceBundle bundle = ResourceBundle.getBundle("messages", I18N.getLocale());
        fxmlLoader.setResources(bundle);
        Parent root = fxmlLoader.load();

        ((Controller) fxmlLoader.getController()).bind();
        ((Controller) fxmlLoader.getController()).setStage(primaryStage);

        primaryStage.setScene(new Scene(root, 850, 450));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
