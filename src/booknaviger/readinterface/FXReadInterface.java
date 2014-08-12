/*
 */

package booknaviger.readinterface;

import booknaviger.MainInterface;
import booknaviger.osbasics.OSBasics;
import booknaviger.picturehandler.AbstractImageHandler;
import java.awt.KeyboardFocusManager;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Initializer class for the {@link ReadInterface} now with Javafx
 * @author Inervo
 */
public class FXReadInterface extends Application {
    final private SwingNode swingNode = new SwingNode();
    private AbstractImageHandler imageHandler;
    private ReadInterface readInterface = null;
    private Stage stage = null;
    public static FXReadInterface INSTANCE = null;
    private static boolean rotationPerformed = false;

    /**
     * Basic constructor with a reference to a unique instance as static
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public FXReadInterface() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "FXReadInterface");
        INSTANCE = this;
        Platform.setImplicitExit(false);
        Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "FXReadInterface");
    }
    
    /**
     * Set the {@link AbstractImageHandler} for the {@link ReadComponent}
     * @param imageHandler the {@link AbstractImageHandler} image handler
     */
    public void setImageHandler(AbstractImageHandler imageHandler) {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "setImageHandler", imageHandler);
        this.imageHandler = imageHandler;
        Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "setImageHandler");
    }
    
    /**
     * Start method used by Application.launch()
     * @param stage the primary stage of javaFX
     */
    @Override
    public void start(Stage stage) {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "start", stage);
        Logger.getLogger(ReadComponent.class.getName()).log(Level.INFO, "JavaFX interface is being prepared");
        StackPane pane = new StackPane();
        pane.getChildren().add(swingNode);
        
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        if (!OSBasics.isMac()) {
            stage.setMaximized(true);
        }
        
        Scene scene = new Scene(pane);
        registerEvents(scene);
        
        this.stage = stage;
        stage.setTitle(ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        stage.setScene(scene);
        Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "start");
    }
    
    /**
     * Register the gestures events
     * @param scene the scene to which bind the gestures events
     */
    private void registerEvents(Scene scene) {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "registerEvents", scene);
        Logger.getLogger(ReadComponent.class.getName()).log(Level.CONFIG, "Gestures events are being registered");
        scene.setOnZoom((ZoomEvent event) -> {
            readInterface.getReadComponent().setZoomValue(event.getZoomFactor());
        });
        scene.setOnSwipeRight((SwipeEvent event) -> {
            readInterface.goNextImage();
        });
        scene.setOnSwipeLeft((SwipeEvent event) -> {
            readInterface.goPreviousImage();
        });
        scene.setOnRotate((RotateEvent event) -> {
            if (rotationPerformed) {
                return;
            }
            if (event.getTotalAngle() > 80) {
                rotationPerformed = true;
                readInterface.getReadComponent().rotateImage(readInterface.getReadComponent().getCurrentOrientation() + 90);
            } else if (event.getTotalAngle() < -80) {
                rotationPerformed = true;
                readInterface.getReadComponent().rotateImage(readInterface.getReadComponent().getCurrentOrientation() - 90);
            }
        });
        scene.setOnRotationFinished((RotateEvent event) -> {
            rotationPerformed = false;
        });
        Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "registerEvents");
    }
    
    /**
     * Show the javaFX interface
     */
    public void showTime() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "showTime");
        Platform.runLater(() -> {
            readInterface.setFocusable(true);
            readInterface.setVisible(true);
            stage.show();
            readInterface.requestFocus();
            Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "showTime");
        });
    }
    
    /**
     * Close the JavaFX interface and give focus back to MainInterface
     */
    public void close() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "close");
        Platform.runLater(() -> {
            readInterface.setFocusable(false);
            readInterface.setVisible(false);
            stage.close();
            new Thread(() -> {
                while (!MainInterface.getInstance().getFocusOnAlbumsTable()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXReadInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
            Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "close");
        });
    }
    
    /**
     * Hide JavaFX stage
     */
    public void hide() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "close");
        Platform.runLater(() -> {
            stage.close();
            Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "close");
        });
    }

    /**
     * Initialise a new {@link ReadInterface} and link it to JavaFX
     */
    public void createAndSetSwingContent() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "createAndSetSwingContent");
        Logger.getLogger(ReadComponent.class.getName()).log(Level.CONFIG, "new ReadInterface is being linked with the JavaFX scene");
            readInterface = new ReadInterface(imageHandler);
            if (OSBasics.isMac()) {
                RepaintManager.currentManager(readInterface).setDoubleBufferingEnabled(false);
            }
            readInterface.setPreferredSize(null);
            SwingUtilities.invokeLater(() -> {
                swingNode.setContent(readInterface);
                Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "createAndSetSwingContent");
            });
    }

    /**
     * Getter for {@link ReadInterface} 
     * @return The {@link ReadInterface} in memory
     */
    public ReadInterface getReadInterface() {
        Logger.getLogger(FXReadInterface.class.getName()).entering(FXReadInterface.class.getName(), "getReadInterface");
        Logger.getLogger(FXReadInterface.class.getName()).exiting(FXReadInterface.class.getName(), "getReadInterface", readInterface);
        return readInterface;
    }
}
