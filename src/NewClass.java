
import java.awt.Color;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;

/*
 */

/**
 * @author Inervo
 *
 */
public class NewClass extends Stage {

    public NewClass() {
//
//        final JFXPanel fxPanel = new JFXPanel();
//        add(fxPanel);
//        setSize(100, 100);
//        fxPanel.setSize(100, 100);
//        fxPanel.setForeground(Color.red);
        final Pane pane = new Pane();
//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
                
                Scene scene = new Scene(pane, 100, 100);
                    scene.addEventHandler(Event.ANY, new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        if (!(event.getEventType().toString().startsWith("MOUSE_") || event.getEventType().toString().startsWith("SCROLL"))) {
                            System.out.println(event.getEventType());
                        }
                        event.consume();
                    }
                });
                setScene(scene);
//            }
//        });
    }
    
    

}
