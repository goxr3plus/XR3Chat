package scenes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import org.controlsfx.control.Notifications;

import application.Main;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * LoginScene
 * 
 * @author SuperGoliath
 *
 */
public class LoginScene extends StackPane implements Initializable {

	@FXML
	private MediaView mediaView;

	@FXML
	private PasswordField userPassword;

	@FXML
	private TextField userName;

	@FXML
	private Button loginAsResident;

	@FXML
	private Button loginAsGuest;

	@FXML
	private TextField hostField;

	@FXML
	private TextField portField;

	@FXML
	private Region region;

	@FXML
	private ProgressIndicator indicator;

	@FXML
	private Label connectingLabel;

	Scene scene;

	// Constructor
	public LoginScene() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScene.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		scene = new Scene(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean connected;
	public static MediaPlayer mediaPlayer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		System.out.println("LoginScene Initialized");

		Media media = null;
		try {
			media = new Media(getClass().getResource("/videos/d.mp4").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} 
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		mediaView.setMediaPlayer(mediaPlayer);
	

		// mediaPlayer.play();
		widthProperty().addListener(l -> {
			mediaView.setFitWidth(this.getWidth());
			mediaView.setFitHeight(this.getHeight());
		});

		heightProperty().addListener(l -> {
			mediaView.setFitWidth(this.getWidth());
			mediaView.setFitHeight(this.getHeight());
		});

		mediaPlayer.setOnError(() -> System.out.println("Media error:" + mediaPlayer.getError().toString()));

		// Region
		region.setStyle("-fx-background-color:rgb(0,0,0,0.7);");
		region.visibleProperty().bind(indicator.visibleProperty());

		// ConnectingLabel
		connectingLabel.setStyle("-fx-text-fill:white; -fx-font-size:17px; -fx-font-weight:bold;");
		connectingLabel.visibleProperty().bind(indicator.visibleProperty());

		// RESIDENT
		loginAsResident.setOnAction(ac -> {
			String name = userName.getText();
			String password = userPassword.getText();

			// Validation Check
			if (!name.isEmpty() && !password.isEmpty())
				tryLogin(Genre.RESIDENT);
			else if (name.isEmpty())
				Notifications.create().text("UserName can not be empty.").showInformation();
			else if (password.isEmpty())
				Notifications.create().text("PasswordField can not be empty.").showInformation();

		});

		// GUEST
		loginAsGuest.setOnAction(ac -> {
			String name = userName.getText();

			// Validation Check
			if (!name.isEmpty())
				tryLogin(Genre.GUEST);
			else if (name.isEmpty())
				Notifications.create().text("UserName can not be empty.").showInformation();
		});
		
		hostField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(hostField.getText().isEmpty())
					hostField.setText("localhost");
			}
		});

		portField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d"))
					portField.setText(newValue.replaceAll("\\D", ""));
				if (portField.getText().length() > 5)
					portField.setText(newValue.substring(0, 5));
				
				if(portField.getText().isEmpty())
					portField.setText("4444");
			}
		});

	}

	/**
	 * Trys a Login
	 * 
	 * @param genre
	 */
	private void tryLogin(Genre genre) {

		indicator.setVisible(true);
		CountDownLatch latch = new CountDownLatch(1);
		connected = false;

		loginAsGuest.setDisable(true);
		loginAsResident.setDisable(true);
		// Προσπάθησε να συνδεθείς στον Server
		new Thread(() -> {
			Main.chatScene.initClient(hostField.getText().replace(".", ""), Integer.parseInt(portField.getText()),
					userName.getText(), userPassword.getText(), genre);
			connected = Main.chatScene.connectToServer();
			latch.countDown();
		}).start();

		// Wait above| Thread to finish
		new Thread(() -> {

			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Platform.runLater(() -> {
				//if successufully connected to Server
				// Εάν συνδέθηκε επιτυχώς στον Server
				if (connected) {
					Main.chatScene.initChatScene();
					Main.stage.setScene(Main.chatScene.getScene());
					indicator.setVisible(false);
					LoginScene.mediaPlayer.pause();
					// Ειδάλως
				} else {
					//Notifications.create().title("Error").text("Can't Connect to Server!").showError();
					indicator.setVisible(false);
				}
				loginAsGuest.setDisable(false);
				loginAsResident.setDisable(false);
			});
		}).start();
	}

}
