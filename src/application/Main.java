package application;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import scenes.ChatScene;
import scenes.LoginScene;

public class Main extends Application {

	public static Stage stage;
	public static ChatScene chatScene;
	public static LoginScene loginScene;

	@Override
	public void start(Stage stage) throws Exception {
		Main.stage = stage;
		chatScene = new ChatScene();
		loginScene = new LoginScene();

		// Stage
		stage.setTitle("XR3Chat");
		stage.setMinWidth(600);
		stage.setMinHeight(450);
		stage.setMaxWidth(700);
		stage.setMaxHeight(500);
		stage.initStyle(StageStyle.UNIFIED);
		stage.setOnCloseRequest(c -> {
			System.exit(0);
		});

		// Initialize Scenes
		loginScene.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
		chatScene.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
		stage.setScene(loginScene.getScene());

		// Show
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
