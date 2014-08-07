/*
 */

package booknaviger.macworld;

import booknaviger.readinterface.ReadInterface;
import com.apple.eawt.event.GestureAdapter;
import com.apple.eawt.event.GesturePhaseEvent;
import com.apple.eawt.event.GestureUtilities;
import com.apple.eawt.event.MagnificationEvent;
import com.apple.eawt.event.RotationEvent;
import com.apple.eawt.event.SwipeEvent;
import javax.swing.JComponent;

/**
 * @author Inervo
 *
 */
public class TrackPadAdapter extends GestureAdapter {

    double value = 0;
    boolean actionPerformed = false;
    ReadInterface readInterface = null;

    /**
     *
     * @param readInterface
     */
    public TrackPadAdapter(ReadInterface readInterface) {
        this.readInterface = readInterface;
    }

    /**
     *
     * @param jc
     */
    public void addListenerOn(JComponent jc) {
        GestureUtilities.addGestureListenerTo(jc, this);
    }

    /**
     *
     * @param jc
     */
    public void removeListenerFrom(JComponent jc) {
        GestureUtilities.removeGestureListenerFrom(jc, this);
    }

    /**
     *
     * @param gpe
     */
    public void gestureBegan(GesturePhaseEvent gpe) {
        System.out.println("gesture began");
        value = 0;
        actionPerformed = false;
    }

    /**
     *
     * @param me
     */
    public void magnify(MagnificationEvent me) {
        System.out.println("magnify");
        if (actionPerformed) {
            return;
        }
        value += me.getMagnification();
        if (value >= 0.7) {
            actionPerformed = true;
            readInterface.getReadComponent().zoomIn();
        }
        if (value <= -0.7) {
            actionPerformed = true;
            readInterface.getReadComponent().zoomOut();
        }
    }

    /**
     *
     * @param re
     */
    public void rotate(RotationEvent re) {
        System.out.println("rotate");
        if (actionPerformed) {
            return;
        }
        value += re.getRotation();
        if (value >= 60) {
            actionPerformed = true;
            readInterface.getReadComponent().rotateImage(readInterface.getReadComponent().getCurrentOrientation() - 90);
        }
        if (value <= -60) {
            actionPerformed = true;
            readInterface.getReadComponent().rotateImage(readInterface.getReadComponent().getCurrentOrientation() + 90);
        }
    }

    /**
     *
     * @param se
     */
    public void swipedLeft(SwipeEvent se) {
        System.out.println("swipe left");
        readInterface.goNextImage();
    }

    /**
     *
     * @param se
     */
    public void swipedRight(SwipeEvent se) {
        System.out.println("swipe right");
        readInterface.goPreviousImage();
    }

}
