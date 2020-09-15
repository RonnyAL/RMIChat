package Client;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;

import Server.Message;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatClientGUI extends Application {
	private ChatClient client;
	
	@FXML TextArea messageInput;
	@FXML VBox messageContainer;
	@FXML Label userListTitle;
	@FXML VBox vboxUserList;

	
	@Override
	public void start(Stage stage) throws Exception {
		String username = requestUsername();
		if (username == null || username == "") {
			System.err.println("Ingen eller ugyldig brukernavn oppgitt. Avslutter applikasjonen.");
			System.exit(0);
		}
		
		try {
			this.client = new ChatClientImpl(username, this);
		} catch (RemoteException e) {
			System.err.println("FEIL");
			//e.printStackTrace();
		}
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("chatwindow.fxml"));
		loader.setController(this);
		Parent root = (Parent) loader.load();
		
		initEventHandlers();
		
		Scene scene = new Scene(root, 1200, 900);
	    scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("ChatServer");
        stage.setScene(scene);
        vboxUserList.setPadding(new Insets(0, 0, 0, 25));
        stage.show();
        
        Task<Void> task = new Task<Void>() {
            @Override public Void call() {
                try {
					client.connect();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return null;
            }
        };
        
        new Thread(task).start();
        
        
	}
	
	private String requestUsername() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("ChatServer");
		dialog.setHeaderText("Pick a username!");
		dialog.setContentText("Your username:");
		Optional<String> result = dialog.showAndWait();
		String username = null;
		
		if (result.isPresent()){
		    username = result.get();
		} else {
			username = null;
		}
		return username;
	}
	
	private void initEventHandlers() {
		messageInput.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -30%);");
		messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
					String text = messageInput.getText();
					if (ke.isShiftDown()) return;
					ke.consume();
					if (text.length() < 1) return;
					try {
						client.submitMessage(text);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					messageInput.clear();
				}
			}
			
		});
	}
	
	private void addSelfMessage(Message m) {
		Label name = new Label(m.getSender());
		name.getStyleClass().add("name-label");
		Region region = new Region();;
		Label time = new Label((new SimpleDateFormat("H:mm").format(m.getTimestamp())));
		time.getStyleClass().add("time-label");
		HBox metaData = new HBox(name, region, time);
		HBox.setHgrow(region, Priority.ALWAYS);
		
		Label messageContent = new Label(m.getContent());
		messageContent.setPadding(new Insets(10, 10, 10, 10));
		messageContent.getStyleClass().add("self-message");
		messageContent.setWrapText(true);
		messageContent.setPrefSize(300, VBox.USE_PREF_SIZE);
		messageContent.setMaxSize(300, VBox.USE_PREF_SIZE);
		
		VBox root = new VBox(metaData, messageContent);
		root.setPrefSize(300, VBox.USE_PREF_SIZE);
		root.setMaxSize(300, VBox.USE_PREF_SIZE);
		HBox aligner = new HBox(root);
		aligner.setPadding(new Insets(0, 50, 0, 0));
		aligner.setAlignment(Pos.TOP_RIGHT);
		messageContainer.getChildren().add(aligner);
	}
	
	public void addMessage(Message m) {
		try {
			if (m.getSender().equals(client.getName())) {
				addSelfMessage(m);
				return;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		Label name = new Label(m.getSender());
		name.getStyleClass().add("name-label");
		Region region = new Region();;
		Label time = new Label((new SimpleDateFormat("H:mm").format(m.getTimestamp())));
		time.getStyleClass().add("time-label");
		HBox metaData = new HBox(name, region, time);
		HBox.setHgrow(region, Priority.ALWAYS);
		
		Label messageContent = new Label(m.getContent());
		messageContent.setPadding(new Insets(10, 10, 10, 10));
		messageContent.getStyleClass().add("other-message");
		messageContent.setWrapText(true);
		messageContent.setPrefSize(300, VBox.USE_PREF_SIZE);
		messageContent.setMaxSize(300, VBox.USE_PREF_SIZE);
		
		VBox root = new VBox(metaData, messageContent);
		root.setPrefSize(300, VBox.USE_PREF_SIZE);
		root.setMaxSize(300, VBox.USE_PREF_SIZE);
		root.setPadding(new Insets(0, 0, 0, 25));
		messageContainer.getChildren().add(root);
	}
	
	public void updateUserList(ArrayList<ChatClient> clients) {
		userListTitle.setText(String.format("Users (%d)", clients.size()));
		vboxUserList.getChildren().clear();
		for (ChatClient c : clients) {
			try {
				Label l = new Label(c.getName());
				l.getStyleClass().add("user-list-entry");
				vboxUserList.getChildren().add(l);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	@Override
	public void stop() {
		try {
			client.disconnect();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
