package Client.Controllers;

import Client.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.Headers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

import Client.Models.*;
import Server.Models.ChatMessageDTO;
import Server.Models.DAO;
import Client.Status;

public class CreateRoomController implements Initializable {
    @FXML private TextField setTitle;
    @FXML private ComboBox<String> setLimitPersonnel;
    @FXML private ListView usersDisplay;
    @FXML private Button createBtn;

	@FXML private ImageView backToMainBtn2;
	ObservableList<AnchorPane> ListUsers = FXCollections.observableArrayList();
	String[] limit_personList = {"1", "2","3","4","5","6","7","8","9","10"};
	//유저 초대에 쓰는 배열 더 좋은게 생각이 안나므로 임시로
	Vector<String> userInvitation = new Vector<String>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> comboNumList = FXCollections.observableArrayList(limit_personList);
		setLimitPersonnel.setItems(comboNumList);
		setLimitPersonnel.getSelectionModel().selectFirst();
		if(setTitle.getText().equals("")) {
			setTitle.setPromptText("10글자 내로 입력해주세요");
		}

    	backToMainBtn2.setOnMouseClicked(new EventHandler<MouseEvent>() {
    		public void handle(MouseEvent event) {
    			try {
                	// 현재 창을 종료한다.
                    Stage currStage = (Stage) backToMainBtn2.getScene().getWindow();
                    currStage.close();

        			Stage stage = new Stage();
                    Parent root = (Parent) FXMLLoader.load(getClass().getResource("/Client/Views/Main.fxml"));
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
					stage.setResizable(false);
                    stage.show();
    			} catch(Exception e) {
                    e.printStackTrace();
                }
    		}
    	});

    	createBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String title = setTitle.getText();

                String limit_person = setLimitPersonnel.getSelectionModel().getSelectedItem();
                int leader_id = Integer.parseInt(Status.getId());

                if(title=="") {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText(null);
					alert.setContentText("방 제목을 입력하세요.");
					alert.showAndWait();
					return;
				}
                if(title.length()>10) { //방 제목 글자수 제한 20
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText(null);
				alert.setContentText("10글자 이내의 방 제목을 입력해주세요.");
				alert.showAndWait();
				return;
			}
				if(userInvitation.size() + 1 > Integer.parseInt(limit_person)){
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("초대 인원이 최대 인원을 초과하였습니다.");
                    alert.showAndWait();
                    return;
                }

                //System.out.println("title: " + title);
                //System.out.println("limit_person: " + limit_person);
                //System.out.println("add_person: " + userInvitation.size());
                for(int i=0;i<userInvitation.size();i++) {
                	System.out.println("userInvitation: " + userInvitation.get(i));
                }

                
            	ChatRoomInfoDTO Room = new ChatRoomInfoDTO(0, title, Integer.parseInt(limit_person), 1, leader_id);
            	
                // 서버와 연결                
                try {               	
                	URL url = new URL("http://"+SocketConnection.SERVER_IP+":3000/createRoom");
                	HttpURLConnection http = (HttpURLConnection) url.openConnection();
                	http.setRequestMethod("POST");  
                	http.setDoOutput(true);
                	
                	//json 처음에 방 정보를 처음에 추가하고 나머지는 추가할 유저 목록
                    JSONArray list = new JSONArray();
                    list.add(Room.toJSONObject());

                    // std_id를 담은 JSONArray 생성
					JSONArray std_ids = new JSONArray();
					std_ids.add(Status.getId());
					for(String user : userInvitation) {
						std_ids.add(user);
					}
					list.add(std_ids);


                    ByteBuffer bb = StandardCharsets.UTF_8.encode(list.toJSONString());
                    int contentLength = bb.limit();
                    byte[] content = new byte[contentLength];
                    bb.get(content, 0, contentLength);

                    // Set Response Headers
                	http.setRequestProperty("Content-Type", "application/json;utf-8");
                    http.setRequestProperty("Content-Length", String.valueOf(contentLength));
    	
    				OutputStream os = http.getOutputStream();
					os.write(content,0,content.length);
                    
                    // 요청 방식 구하기
                	System.out.println("getRequestMethod():" + http.getRequestMethod());
                    // 응답 코드 구하기
                	System.out.println("getResponseCode():" + http.getResponseCode());
                    // 응답 메시지 구하기
                	System.out.println("getResponseMessage():" + http.getResponseMessage());
                	
                	if (http.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                		System.out.println("방 생성에 성공했습니다.");
                		// 현재 창을 종료한다.
                		Stage currStage = (Stage) setTitle.getScene().getWindow();
                		currStage.close();
                		
                		//우선 메인으로 이동한다.
                        Stage stage = new Stage();
                        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/Client/Views/Main.fxml"));
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
						stage.setResizable(false);
                        stage.show();
                		}
                	else if (http.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                		// 경고 메세지를 출력한다.
                		Alert alert = new Alert(AlertType.INFORMATION);
                		alert.setHeaderText(null);                		
                		alert.setContentText("오류가 발생했습니다.");
                		alert.showAndWait();
                		}
                } catch (Exception e) {
                	e.printStackTrace();
                	}             
            }
        });
    	usersList();
    }
    
    //유저 정보를 요청하고 리스트에 출력한다.
    void usersList() {
    	try {
    		URL url = new URL("http://"+SocketConnection.SERVER_IP+":3000/createRoom");
    		HttpURLConnection http = (HttpURLConnection) url.openConnection();
    		http.setRequestMethod("GET");
    		http.setRequestProperty("Authorization", Status.getId()+":"+Status.getPw());

    		if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
    			InputStream is = http.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
    			JSONParser parser = new JSONParser();
    			JSONArray list = (JSONArray)parser.parse(br);
    			for(int i=0; i<list.size(); i++) {
    				JSONObject obj = (JSONObject) list.get(i);
    				if(Status.getId().equals(obj.get("std_id").toString()))continue;
    				ListUsers.add(UserInfoBox(obj.get("std_id").toString(),obj.get("name").toString()));
    			}
    			usersDisplay.setItems(ListUsers);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	public AnchorPane UserInfoBox(String std_id,String name) {
		Label userStd_id = new Label();
		Label userName = new Label();
		userStd_id.setText(std_id);
		userName.setText(name);
		userName.setFont(new Font("System", 16));
		userStd_id.setFont(new Font("System", 16));
		CheckBox userCheck = new CheckBox();
		Image img = new Image("/Client/Views/img/user.png");
		ImageView view = new ImageView(img);
		view.setFitHeight(30);
		view.setFitWidth(30);

		AnchorPane UserInfoBox = new AnchorPane(userStd_id, userName);

		AnchorPane.setTopAnchor(userStd_id,12.0);
		AnchorPane.setTopAnchor(userName,12.0);
		AnchorPane.setLeftAnchor(userStd_id,130.0);
		AnchorPane.setLeftAnchor(userName,50.0);


		AnchorPane.setTopAnchor(view,5.0);
		AnchorPane.setLeftAnchor(view, 5.0);
		AnchorPane.setBottomAnchor(view, 5.0);
		//AnchorPane.setRightAnchor(view,70.0);

		UserInfoBox.getChildren().add(view);

		AnchorPane.setTopAnchor(userCheck, 15.0);
		AnchorPane.setLeftAnchor(userCheck,280.0);
		AnchorPane.setBottomAnchor(userCheck,15.0);
		//AnchorPane.setRightAnchor(userCheck,7.0);
		UserInfoBox.getChildren().add(userCheck);
		UserInfoBox.setMaxHeight(40);
		UserInfoBox.setMaxWidth(240);
    
//    public GridPane UserInfoBox(String std_id,String name) {
//        GridPane userInfoBox = new GridPane();
//        Label userStd_id = new Label();
//        userStd_id.setPrefWidth(100);
//        Label userName = new Label();
//        userName.setPrefWidth(100);
//        CheckBox userCheck = new CheckBox();
//
//        userStd_id.setText(std_id);
//        userName.setText(name);
//
//        userInfoBox.add(userStd_id, 1, 0);
//        userInfoBox.add(userName, 2, 0);
//        userInfoBox.add(userCheck, 0, 0);
        
        userCheck.setOnAction(new EventHandler<ActionEvent>() {
    		public void handle(ActionEvent event) {
    	    	if(userCheck.isSelected()) {
    	    		userInvitation.add(std_id);
    	    	}else {
    	    		userInvitation.remove(std_id);
    	    	}
    		}
    	});
        return UserInfoBox;
    }


}
