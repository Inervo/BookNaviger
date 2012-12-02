/*
 */

package booknaviger.macworld;

import booknaviger.BookNavigerReadView;
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
    BookNavigerReadView bnrv = null;

    public TrackPadAdapter(BookNavigerReadView bnrv) {
        this.bnrv = bnrv;
    }

    public void addListenerOn(JComponent jc) {
        GestureUtilities.addGestureListenerTo(jc, this);
    }

    public void removeListenerFrom(JComponent jc) {
        GestureUtilities.removeGestureListenerFrom(jc, this);
    }

    @Override
    public void gestureBegan(GesturePhaseEvent gpe) {
        value = 0;
        actionPerformed = false;
    }

    @Override
    public void magnify(MagnificationEvent me) {
        if (actionPerformed)
            return;
        value += me.getMagnification();
        if (value >= 0.7) {
            actionPerformed = true;
            bnrv.increaseZoom();
        }
        if (value <= -0.7) {
            actionPerformed = true;
            bnrv.decreaseZoom();
        }
    }

    @Override
    public void rotate(RotationEvent re) {
        if (actionPerformed)
            return;
        value += re.getRotation();
        if (value >= 60) {
            actionPerformed = true;
            bnrv.getReadComponent().rotateCCW();
        }
        if (value <= -60) {
            actionPerformed = true;
            bnrv.getReadComponent().rotateCW();
        }
    }

    @Override
    public void swipedLeft(SwipeEvent se) {
        bnrv.goNextPage();
    }

    @Override
    public void swipedRight(SwipeEvent se) {
        bnrv.goPreviousPage();
    }

}
