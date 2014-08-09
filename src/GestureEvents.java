import booknaviger.readinterface.ReadComponent;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JScrollPane;
import javax.swing.RepaintManager;
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
        RepaintManager.currentManager(null).setDoubleBufferingEnabled(false);
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        final SwingNode swingnode = new SwingNode();
//        swingnode.setCache(true);
//        swingnode.setCacheHint(CacheHint.SPEED);
        createAndSetSwingContent(swingnode);
//        Group group = new Group();
//        group.getChildren().add(swingnode);
        StackPane pane = new StackPane();
        pane.getChildren().add(swingnode);
        
        Scene scene = new Scene(pane, 500, 500);
//        scene.setOnRotate((RotateEvent event) -> {
//            System.out.println("coucou, ca tourne");
//            event.consume();
//        });

        primaryStage.setScene(scene);
        primaryStage.show();        
    }
    
    private void createAndSetSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(new JScrollPane(new ReadComponent()));
        });
    }
}
    
