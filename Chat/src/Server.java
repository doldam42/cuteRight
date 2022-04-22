import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;

public class Server extends Application {
    public static final int MAX_BYTE_SIZE = 1024;

    ExecutorService executorService;
    ServerSocket serverSocket;
    List<Client> connections = new Vector<Client>();

    void startServer() {
        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(serverSocket.getInetAddress(), 5001));
        } catch (Exception e) {
            if(!serverSocket.isClosed()) { stopServer(); }
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Platform.runLater(()->{
                    displayText("[서버 시작]");
                    System.out.println("서버 시작");
                    btnStartStop.setText("종료");
                });
                while(true) {
                    try {
                        Socket socket = serverSocket.accept();
                        String message = "[연결 수락:" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                        Platform.runLater(()->displayText(message));

                        Client client = new Client(socket);
                        connections.add(client);
                        Platform.runLater(()->displayText("[연결 개수" + connections.size() + "]"));
                    } catch (Exception e) {
                        if(!serverSocket.isClosed()) {stopServer();}
                        break;
                    }
                }
            }
        };
        executorService.submit(runnable);
    }

    void stopServer() {
        try {
            Iterator<Client> iterator = connections.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            if(serverSocket!=null && !serverSocket.isClosed()) { serverSocket.close(); }
            if(executorService!=null && !executorService.isShutdown()) { executorService.shutdown(); }
            Platform.runLater(()-> {
                displayText("[서버 멈춤]");
                btnStartStop.setText("시작");
            });
        } catch (Exception e) { }
    }

    public class Client {
        Socket socket;

        Client(Socket socket) {
            this.socket = socket;
            receive();
        }

        void receive() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            byte[] byteArr = new byte[MAX_BYTE_SIZE];
                            InputStream inputStream = socket.getInputStream();

                            //클라이언트가 비정상 종료를 했을 경우 IOException 발생
                            int readByteCount = inputStream.read(byteArr);

                            if(readByteCount == -1) { throw new IOException(); }

                            String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                            Platform.runLater(()->displayText(message));
                            for(Client client : connections) {
                                client.send(byteArr);
                            }
                        } 
                    } catch (Exception e) {
                        try {
                            connections.remove(Client.this);
                            String message = "클라이언트 통신 안됨";
                            Platform.runLater(()->displayText(message));
                            socket.close();
                        } catch (IOException e2) {}
                    }
                }
            };
            executorService.submit(runnable);
        }
        void send(byte[] byteArr) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(byteArr);
                        outputStream.flush();
                    } catch (Exception e) {
                        try {
                            Platform.runLater(()->displayText("[클라이언트 통신 안됨]"));
                            connections.remove(Client.this);
                            socket.close();
                        } catch (IOException e2) {}
                    }
                }
            };
            executorService.submit(runnable);
        }
    }


    ////////////////////
    // UI 생성 코드

    TextArea txtDisplay;
    Button btnStartStop;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
        root.setCenter(txtDisplay);

        btnStartStop = new Button("시작");
        btnStartStop.setPrefHeight(30);
        btnStartStop.setMaxWidth(Double.MAX_VALUE);

        btnStartStop.setOnAction(e->{
            if (btnStartStop.getText().equals("시작")) {
                startServer();
            } else if(btnStartStop.getText().equals("종료")) {
                stopServer();
            }
        });
        root.setBottom(btnStartStop);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("app.css").toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(event->stopServer());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}


