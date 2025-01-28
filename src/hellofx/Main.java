package hellofx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hellofx.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setMainWindow(primaryStage);
        primaryStage.setTitle("Reproductor de Videos");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }
}