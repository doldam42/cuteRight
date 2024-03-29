package Client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Client/Views/Login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("팀프리");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        SocketConnection.close();
        if(Status.getId() != null) {
            URL url = new URL("http://" +SocketConnection.SERVER_IP+":3000/logout");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Authorization", Status.getId());

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Status.reset();
                System.out.println("성공적으로 로그아웃 했습니다!");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
