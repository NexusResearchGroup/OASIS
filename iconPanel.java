/*
 * iconPanel.java
 *
 * Created on June 6, 2006, 8:25 PM
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

import java.awt.*;
import java.net.URL;
import java.io.*;
import java.awt.image.*;

public class iconPanel extends Panel
{
    String iconFilename = "" ;
    int iconW = 0, iconH=0 ;
    Image img ;

    // class construction
    iconPanel(String name)
    {
        iconFilename = name ;
	setBackground(new Color(121,217,157));
        
        URL url = getImageResource(iconFilename);
        img = Toolkit.getDefaultToolkit().getImage(url);
        PixelGrabber pg=new PixelGrabber(img, 0,0,-1,-1,true);
        try{
            if(pg.grabPixels()){
                iconW = pg.getWidth();
                iconH = pg.getHeight();
            }
        } catch(InterruptedException ie){
            System.out.println("Error: "+ie.toString()) ;
        }    }

    public void paint(Graphics g) 
    {
	Rectangle r = bounds();
        //URL url = getImageResource(iconFilename);
        //Image img = Toolkit.getDefaultToolkit().getImage(url);
        g.drawImage(img, (r.width-iconW)/2, (r.height-iconH)/2, this) ;

    }   // paint

    public URL getImageResource(String img) {
        URL url = null;
        //try {
            url = getClass().getResource(img+".png");
            if (url==null) {
                url = getClass().getResource(img+".PNG");
            }
        //} catch (IOException ioe) {
        //    System.out.println(url.toString());
        //}
        return url ;
    }   //getImageResource
    
    public Dimension preferredSize()
    {
	return(new Dimension(28,28));
    }

    public boolean mouseDown(Event e, int x, int y)
    {
	return(true);
    }   //mouseDown
}
