package scenes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.controlsfx.control.Notifications;
import org.fxmisc.richtext.InlineCssTextArea;

import application.Main;
import graphics.DigitalClock;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

/**
 * The Scene in which the Client is communication with the others
 * 
 * @author GOXR3PLUS
 *
 */
public class ChatScene extends BorderPane implements Initializable {

	DigitalClock digitalClock;
	public ChatClient chatClient;
	ArrayList<PrivateChatWindow> privateChatWindows = new ArrayList<>();

	private static final String PATTERN = "><:><";

	static Map<String, String> map = new TreeMap<String, String>();
	String[] colors = { "ORANGE", "CYAN", "MAGENTA", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "GREEN",
			"BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN",
			"MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE",
			"BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA",
			"BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN" };

	@FXML
	private ListView<String> listView;

	@FXML
	private Label usersInRoom;

	@FXML
	private Button sentPrivateMessage;

	@FXML
	private Label topLabel;

	@FXML
	private Button sentMessage;

	@FXML
	private TextField messageField;

	@FXML
	private InlineCssTextArea messagesArea;

	@FXML
	private GridPane bottomGridPane;

	Scene scene;

	/**
	 * Constructor
	 * 
	 * @param senter
	 */
	public ChatScene() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatScene.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		scene = new Scene(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Inits the Client
	 * 
	 * @param userName
	 * @param password
	 * @param genre
	 */
	public void initClient(String host, int port, String userName, String password, Genre genre) {
		chatClient = new ChatClient(host, port, userName, password, genre);
	}

	/**
	 * Update the ChatScene with the ClientInfo
	 */
	public void initChatScene() {

		// Clear
		privateChatWindows.clear();
		messageField.clear();
		messagesArea.clear();

		// Resident
		if (chatClient.getGenre() == Genre.RESIDENT) {
			// TopLabel
			topLabel.setText("UserName: [" + chatClient.getUserName() + "] Password: [" + chatClient.getPassword()
					+ "] Genre: [RESIDENT]");
			// SentPrivateMessage
			sentPrivateMessage.setOnAction(ac -> {

				if (!listView.getItems().isEmpty() && !listView.getSelectionModel().isEmpty()) {
					String selected = listView.getSelectionModel().getSelectedItem();
					// The Private Window has already been created if i have
					// send message to that user
					if (privateChatWindows.stream().anyMatch(window -> {
						if (window.receiver.equals(selected)) {
							window.show();
							return true;
						}
						return false;
					})) {
						// System.out.println("Already exists!!!");
					} else {
						// I hadn't sent again to that user private message
						PrivateChatWindow window = new PrivateChatWindow(chatClient.getUserName(), selected);
						privateChatWindows.add(window);
						window.show();
					}

				}

			});
			sentPrivateMessage.setDisable(false);
			digitalClock.setVisible(false);

			// Guest
		} else {
			// TopLabel
			topLabel.setText("UserName: [" + chatClient.getUserName() + "] Genre: [GUEST]");
			// SendPrivateMessage
			sentPrivateMessage.setDisable(true);
			digitalClock.setVisible(true);

		}

		// MessageField
		messageField.setOnAction(ac -> {
			String text = messageField.getText();
			if (!text.isEmpty()) {
				if ("QUIT".equals(text))
					chatClient.sentMessage("QUIT");
				else if ("HELLO".equals(text))
					chatClient.sentMessage("HELLO");
				// General Message
				else if ("ROCO".equals(text))
					chatClient.sentMessage("ROCO");
				else if ("PROOMLS".equals(text))
					chatClient.sentMessage("PROOMLS");
				else if ("GETINFO".equals(text))
					chatClient.sentMessage("GETINFO");
				else {

					// Print my Message to Me
					int lengthBefore = messagesArea.getText().length();
					messagesArea.appendText((text.isEmpty() ? "" : "\n") + "You" + "->" + text);
					messagesArea.setStyle(lengthBefore, (lengthBefore + 6),
							"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:maroon;");

					// Send it
					chatClient.sentMessage("GMESS" + text);
				}
				messageField.clear();
			}
		});

		messageField.setCursor(new ImageCursor(new Image(getClass().getResourceAsStream("/images/pencil.png")), 0, 32));

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new CostumeCell();
			}
		});

		// SentMessage
		sentMessage.setOnAction(messageField.getOnAction());

	}

	/**
	 * Costume Cell Class with icon
	 * 
	 * @author SuperGoliath
	 *
	 */
	private static class CostumeCell extends ListCell<String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			// If item is empty
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				setContentDisplay(ContentDisplay.RIGHT);
				ImageView imageView = new ImageView(
						new Image(getClass().getResourceAsStream("/images/" + map.get(item) + ".png")));
				setGraphic(imageView);
				setText(item);
			}
		}
	}

	/**
	 * Connect the Client to Server
	 * 
	 * @return
	 */
	public boolean connectToServer() {
		return chatClient.connectToServer();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// TopLabel
		topLabel.setStyle(
				"-fx-background-color:white; -fx-background-radius:10px; -fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:black;");

		digitalClock = new DigitalClock(85, 32);
		bottomGridPane.add(digitalClock, 2, 0);
	}

	/**
	 * User Thread communicating with the Server
	 * 
	 * @author GOXR3PLUS
	 *
	 */
	public class ChatClient implements Runnable {

		Socket socket;
		Thread thread;
		BufferedReader fromServer;
		PrintWriter toServer;
		private String userName;
		private String password;
		private Genre genre;
		String host;
		int port;

		/**
		 * Constructor
		 * 
		 * @param host
		 * @param port
		 * @param userName
		 * @param password
		 * @param genre
		 */
		public ChatClient(String host, int port, String userName, String password, Genre genre) {
			this.host = host;
			this.port = port;
			this.userName = userName;
			this.password = password;
			this.genre = genre;

		}

		/**
		 * Connects to the Server
		 */
		public boolean connectToServer() {

			/*********** Initialize the Socket *******/
			try {

				/******* Proxy *********/
				// ....some code here

				/******* Not Proxy *********/
				socket = new Socket(host, port);

				toServer = new PrintWriter(socket.getOutputStream(), true);
				fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Start the Thread
				thread = new Thread(this);
				thread.start();

				/////////// Send HELO To Server //////////
				sentMessage("LOGIN" + userName + PATTERN + password + PATTERN + genre);
				sentMessage("ROCO");
				sentMessage("GETALLMESS");
			} catch (UnknownHostException e) {
				Platform.runLater(() -> Notifications.create().title("Unknown Host")
						.text("Can't find that host:\n[" + e.getMessage() + "]").showError());
				return false;
			} catch (IOException e) {
				Platform.runLater(() -> Notifications.create().title(e.getMessage())
						.text("Can't connect to server:\n[" + host + "]").showError());
				e.printStackTrace();
				return false;
			}
			return true;

		}

		/**
		 * Sends the specific message to server
		 * 
		 * @param message
		 */
		public void sentMessage(String message) {
			toServer.println(message);
		}

		/** Returns userName */
		public String getUserName() {
			return userName;
		}

		/** Returns user's password */
		public String getPassword() {
			return password;
		}

		/** Returns user's Genre(GUEST|RESIDENT) */
		public Genre getGenre() {
			return genre;
		}

		private void disconnectFromServer() {
			thread = null;

			// Close toServer
			toServer.close();

			// Close fromServer
			try {
				fromServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Close the socket
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String message;

		/******** Implements the Thread ****************/
		@Override
		public void run() {

			/////////// Read Server Messages //////////

			while (thread != null) {
				try {
					message = fromServer.readLine();

					// System.out.println(message);
					// ----------------------- LIST WITH ALL CLIENTS IN ROOM
					if (message.startsWith("ROOMLS")) {
						String message = this.message;
						// Clear the previous List
						Platform.runLater(() -> {
							map.clear();
							listView.getItems().clear();
							Arrays.stream(message.substring(6).split(";")).forEach(s -> {
								String[] z = s.split(PATTERN);
								map.put(z[0], z[1]);
							});
							listView.getItems().addAll(map.keySet());
							usersInRoom.setText("Users in Room " + listView.getItems().size() + 1);
						});
						// ----------------------- ADD THIS CLIENT IN LIST
					} else if (message.startsWith("ADD")) {
						String[] array = this.message.substring(3).split(PATTERN);
						Platform.runLater(() -> {
							map.put(array[0], array[1]);
							listView.getItems().add(array[0]);
							usersInRoom.setText("Users in Room " + (map.size() + 1));
						});

						// ----------------------- REMOVE THIS CLIENT FROM LIST
					} else if (message.startsWith("REMOVE")) {
						String message = this.message.substring(6);
						Platform.runLater(() -> {
							map.remove(message);
							listView.getItems().remove(message);
							usersInRoom.setText("Users in Room " + (map.size() + 1));
						});
						// ----------------------- GOT TOTAL CLIENTS IN ROOM
					} else if (message.startsWith("ROCO")) {
						String message = this.message;
						Platform.runLater(() -> {
							usersInRoom.setText("Users in Room " + message.substring(4));
						});

						// ----------------------- GOT Total Clients in Room
					} else if (message.startsWith("GMESS")) {
						String[] array = message.substring(5).split(PATTERN);
						int lengthBefore = messagesArea.getText().length();

						//
						Platform.runLater(() -> {
							// Type the message on the screen
							messagesArea.appendText(
									(messagesArea.getText().isEmpty() ? "" : "\n") + array[0] + "->" + array[1]);
							messagesArea.setStyle(lengthBefore, (lengthBefore + array[0].length() + 3),
									"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:"
											+ colors[new ArrayList<String>(map.keySet()).indexOf(array[0])] + ";");
						});

						// ----------------------- GOT Private Message
					} else if (message.startsWith("PMESS")) {
						String[] array = message.substring(5).split(PATTERN);

						// Check if i had ever spoke with this person
						if (privateChatWindows.stream().anyMatch(window -> {
							if (window.receiver.equals(array[0])) {
								window.update(array[0] + "->" + array[1]);
								return true;
							}
							return false;
						})) {
							// System.out.println("Yes the window already
							// exist's i had spoke again with this
							// person!");

							// If i hadn't spoke again create a new Private
							// Window
						} else {
							Platform.runLater(() -> {
								PrivateChatWindow window = new PrivateChatWindow(userName, array[0]);
								privateChatWindows.add(window);
								window.update(array[0] + " ->" + array[1]);
							});
						}

						// DISCONNECTED FROM SERVER XOXOXO go cry
					} else if (message.startsWith("DISC")) {
						String message = this.message;
						disconnectFromServer();
						// Go back to login screen
						Platform.runLater(() -> {
							LoginScene.mediaPlayer.play();
							Main.stage.setScene(Main.loginScene.getScene());
							privateChatWindows.forEach(window -> window.stage.close());
							digitalClock.stopClock();
							Notifications.create().title("Server Disconnection")
									.text("You have been disconnected from the Server.\n" + message.substring(4))
									.showWarning();
						});

						// ----------------------- GOT A TIMELIMIT by Server
					} else if (message.startsWith("TIMELIMIT")) {
						digitalClock.startClock(Integer.parseInt(message.substring(9)));

						// ------------------ GOT INFORMATIONS ABOUT THIS CLIENT
					} else if (message.startsWith("GETINFO")) {
						String[] infos = message.substring(7).split(PATTERN);
						Platform.runLater(() -> {
							messagesArea.appendText((messagesArea.getText().isEmpty() ? "" : "\n") + "LoggedInDate: "
									+ infos[0] + " LoggedInTime: " + infos[1]);
						});
					} else {
						String message = this.message;
						Platform.runLater(() -> {
							messagesArea.appendText((messagesArea.getText().isEmpty() ? "" : "\n") + message);
						});
					}

				} catch (IOException e) {
					e.printStackTrace();
					thread = null;
				}
			}
		}

	}
}
