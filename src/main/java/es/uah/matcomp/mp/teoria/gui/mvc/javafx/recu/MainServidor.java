package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainServidor extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("servidorRMI.fxml"));
        Parent root = loader.load();
        ServidorController app = loader.getController();
        stage.setTitle("Age of Threads");
        stage.setScene(new Scene(root));
        stage.show();}

    public static void main(String[] args) {
        launch(args);
    }
}