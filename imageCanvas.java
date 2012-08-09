/*
 * imageCanvas.java
 *
 * Created on July 13, 2005, 12:21 PM
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
import java.io.* ;
import javax.imageio.* ;

public class imageCanvas extends Canvas {
    
    Image myImage = null ;
    
    /** Creates a new instance of imageCanvas */
    public imageCanvas() {
    }
    
    public imageCanvas(Image inputImage) {
        myImage = inputImage ;
    }
    
    public void paint(Graphics g) {
        g.drawImage(myImage, 0, 0, this) ;

    }
}
