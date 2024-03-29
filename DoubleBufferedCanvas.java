/*
 * DoubleBufferedCanvas.java
 *
 * Created on January 23, 2004, 2:22 PM
 */

/**
 * @author  Chen-Fu Liao
 * Sr. Systems Engineer
 * ITS Institute, ITS Laboratory
 * Center For Transportation Studies
 * University of Minnesota
 * 200 Transportation and Safety Building
 * 511 Washington Ave. SE
 * Minneapolis, MN 55455
 */

import java.awt.* ;

class DoubleBufferedCanvas extends Canvas {
    
    /** Creates a new instance of DoubleBufferedCanvas */
    public DoubleBufferedCanvas() {
    }

    public void update(Graphics g) {
	Graphics offgc;
	Image offscreen = null;
	Dimension d = size();

	// create the offscreen buffer and associated Graphics
	offscreen = createImage(d.width, d.height);
	offgc = offscreen.getGraphics();
	// clear the exposed area
	offgc.setColor(getBackground());
	offgc.fillRect(0, 0, d.width, d.height);
	offgc.setColor(getForeground());
	// do normal redraw
	paint(offgc);
	// transfer offscreen to window
	g.drawImage(offscreen, 0, 0, this);
    }
}    
    

