import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.input.RotateEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 *
 * Sample that shows how gesture events are generated. The UI consists of
 * two shapes and a log. The shapes respond to scroll, zoom, rotate and
 * swipe events. The log contains information for the last 50 events that
 * were generated and captured for the rectangle and ellipse object. 
 */
public class GestureEvents extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {

        AnchorPane root = new AnchorPane();
        final SwingNode swingnode = new SwingNode();
        createAndSetSwingContent(swingnode);
        StackPane pane = new StackPane();
        pane.getChildren().add(swingnode);
        
        Scene scene = new Scene(pane, 500, 500);
        scene.setOnRotate((RotateEvent event) -> {
            System.out.println("coucou, ca tourne");
            event.consume();
        });
        this.primaryStage = primaryStage;

        primaryStage.setScene(scene);
        
    }
    
    private void createAndSetSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(new JButton("Click me!"));
            Platform.runLater(() -> {
                primaryStage.show();
            });
        });
    }
}
    
