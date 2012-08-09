/*
 * imagePanel.java
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

public class imagePanel extends Panel
{
    String imageFilename = "" ;
    int imageW = 0, imageH=0 ;
    Image img ;
    
    // class construction
    imagePanel(String name)
    {
        imageFilename = name ;
	setBackground(new Color(121,217,157));
        setSize(154, 150) ;
        URL url = getImageResource(imageFilename);
        img = Toolkit.getDefaultToolkit().getImage(url);
        PixelGrabber pg=new PixelGrabber(img, 0,0,-1,-1,true);
        try{
            if(pg.grabPixels()){
                imageW = pg.getWidth();
                imageH = pg.getHeight();
            }
        } catch(InterruptedException ie){
            System.out.println("Error: "+ie.toString()) ;
        }
    }

    public void changeImagePanel(String imageFilename)
    {       
        URL url = getImageResource(imageFilename);
        img = Toolkit.getDefaultToolkit().getImage(url);
        PixelGrabber pg=new PixelGrabber(img, 0,0,-1,-1,true);
        try{
            if(pg.grabPixels()){
                imageW = pg.getWidth();
                imageH = pg.getHeight();
            }
        } catch(InterruptedException ie){
            System.out.println("Error: "+ie.toString()) ;
        }
        repaint() ;
    }
    
    public void paint(Graphics g) 
    {
        
	Rectangle r = bounds();
        g.drawImage(img, (r.width-imageW)/2, (r.height-imageH)/2, this) ;
//        g.drawImage(img, (r.width-154)/2, (r.height-150)/2, this) ;
        //System.out.println("w="+imageW+",H=" + imageH) ;

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
	return(new Dimension(154,150));
    }

    public boolean mouseDown(Event e, int x, int y)
    {
	return(true);
    }   //mouseDown
}
