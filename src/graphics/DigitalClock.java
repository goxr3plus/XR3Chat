package graphics;

import application.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A Digital Clock which represents the Time in format HH:MM:SS
 * 
 * @author SuperGoliath
 *
 */
public class DigitalClock extends StackPane {

	Thread thread;

	// Canvas
	Canvas canvas = new Canvas();
	GraphicsContext gc = canvas.getGraphicsContext2D();

	// variables
	private StringProperty timeNow = new SimpleStringProperty("30:00");
	private int countDown;

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public DigitalClock(int width, int height) {

		// initialize
		setWidth(width);
		setHeight(height);

		canvas.setWidth(width);
		canvas.setHeight(height);

		// Text
		Text text = new Text();
		text.setFill(Color.WHITE);
		text.setFont(Font.font("null", FontWeight.BOLD, 15));
		text.textProperty().bind(timeNow);

		getChildren().addAll(canvas, text);
		repaint();
	}

	/**
	 * Starts the digitalClock
	 */
	public void startClock(int seconds) {
		if (thread == null) {
			countDown = seconds;
			timeNow.set(getTimeEdited(countDown));
			repaint();
			// Start the Thread
			thread = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// +1 Second every 1000 milli
					while (thread != null) {
						repaint();
						Thread.sleep(1000);
						timeNow.set(getTimeEdited(--countDown));
						if (countDown == 0) {
							// System.out.println("Sented message QUIT");
							Main.chatScene.chatClient.sentMessage("QUIT");
						}
					}
					return null;
				}
			});
			thread.start();
		} else {
			System.out.println("Can't start Clock");
		}
	}

	/**
	 * Stops the digitalClock
	 */
	public void stopClock() {
		thread = null;
	}

	/**
	 * Returns the seconds in format HH:MM:SS
	 * 
	 * @param seconds
	 * @return
	 */
	public String getTimeEdited(int seconds) {

		// Is more than one hour>60
		if (seconds / 60 > 60)
			return String.format("%02d:%02d:%02d", (seconds / 60) / 60, (seconds / 60) % 60, seconds % 60);
		// Is less than one hour<60
		else
			return String.format("%02d:%02d", (seconds / 60) % 60, seconds % 60);

	}

	/**
	 * Repaints the Clock
	 */
	private void repaint() {

		// System.out.println("Repainting..." + timeNow.get());

		// Clear Rect
		gc.clearRect(0, 0, getWidth(), getHeight());

		gc.setFill(Color.FIREBRICK);
		gc.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
	}

}
