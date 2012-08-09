/*
 * toolbar.java
 * Horizontal curve design toolbar.
 *
 * Created on March 16, 2006, 8:25 PM
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
import java.applet.*;
import java.net.URL;
import java.io.*;
//import javax.swing.*;

public class toolbar extends Panel
{
    private static Font dialogFontB12 = new Font("Dialog", Font.BOLD | Font.PLAIN, 12) ;
    private final int NUM_ICONS = 9 ;
    public int status = -1;
    intscDrawArea parent;
//    JToolTip myToolTip = new JToolTip();
    
    // class construction
    toolbar()
    {
	setBackground(Color.lightGray);
    }

    public void paint(Graphics g) 
    {
	Rectangle r = bounds();


	for(int i=0;i<NUM_ICONS;i++)
	{
	    if(i==status)
	    {
		g.setColor(new Color(192,192,192));
		g.fillRect(i*33, 0,32,32);
	    }
	    //g.setColor(Color.black);
	    //g.drawLine(i*33+32, 0,i*33+32, 32);
            URL url = null;
            switch (i) {
            case 0:
                //url = getClass().getResource("Arrow.png");
                url = getImageResource("volume");
                break ;
            case 1:
                //url = getClass().getResource("ZoomIn.png");
                url = getImageResource("actuation");   // actuation
                break ;
            case 2:
                //url = getClass().getResource("ZoomIn.png");
                url = getImageResource("controller");   // controller settings
                break ;
            case 3:
                //url = getClass().getResource("ZoomOut.png");
                url = getImageResource("simstart");
                break ;
            case 4:
                //url = getClass().getResource("Move.png");
                url = getImageResource("simstep");
                break ;
            case 5:
                //url = getClass().getResource("Move.png");
                url = getImageResource("simpause");
                break ;
            case 6:
                //url = getClass().getResource("Move.png");
                url = getImageResource("simstop");
                break ;
            case 7:
                //url = getClass().getResource("Move.png");
                url = getImageResource("simstepback");
                break ;
            case 8:
                //url = getClass().getResource("Move.png");
                url = getImageResource("help");
                break ;
            }
            Image img = Toolkit.getDefaultToolkit().getImage(url);
            g.drawImage(img, i*33,0,this) ;
            if (i!=status) {
                // button boundary depressed
                g.setColor(Color.white);
                g.drawLine(i*33+1, 1,(i+1)*33-2, 1);
                g.drawLine(i*33+1, 1,i*33+1, 30);
                g.setColor(Color.black);
                g.drawLine(i*33+2, 30,(i+1)*33-2, 30);
                g.drawLine((i+1)*33-2, 2,(i+1)*33-2, 30);
            } else {
                // button pressed
                // button boundary pressed
                g.setColor(Color.black);
                g.drawLine(i*33+1, 1,(i+1)*33-2, 1);
                g.drawLine(i*33+1, 1,i*33+1, 30);
                g.setColor(Color.white);
                g.drawLine(i*33+2, 30,(i+1)*33-2, 30);
                g.drawLine((i+1)*33-2, 2,(i+1)*33-2, 30);
            }
	}   // i
        g.setColor(new Color(126,20,1));
        g.setFont(dialogFontB12) ;
        g.drawString("MTO, CE, ITS Institute, TEL, University of Minnesota", 350, 20) ;
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
	return(new Dimension(32,32));
    }

    public boolean mouseDown(Event e, int x, int y)
    {
	if(x<NUM_ICONS*33)
	{
	    int oldstatus = status;
	    status = x/33;
	    if(status<0) status = 0;
	    if(status>9) status = 9;
	    //if(oldstatus!=status)
	    //{
                String str = "" ;
                switch (status) {
                    case 0:
                        parent.SetVol_flag = true ;
                        
                        //myToolTip.setToolTipText("Pointer tool");
                        break ;
                    case 1:  
                        parent.SetActuationType_flag = true ;
                        break ;
                    case 2:
                        parent.SetTiming_flag = true ;
                        break ;
                    case 3:
                        str = " Start Simulation" ;
                        parent.newstatus(status, str);
                        break ;
                    case 4:
                        str = " Step Simulation" ;
                        parent.newstatus(status, str);
                        break ;
                    case 5:
                        str = " Pause Simulation" ;
                        parent.newstatus(status, str);
                        break ;
                    case 6:
                        str = " Terminate Simulation" ;
                        parent.newstatus(status, str);
                        break ;
                    case 7:
                        str = " Step Back Simulation" ;
                        parent.newstatus(status, str);
                        break ;
                    case 8:
                        str = " Help" ;
                        parent.newstatus(status, str);
                        break ;

                } // switch
		//parent.newstatus(status, str);
		repaint();
	    //}
	}
	return(true);
    }   //mouseDown
}
