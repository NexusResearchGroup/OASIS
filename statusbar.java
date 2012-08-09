/*
 * statusbar.java
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
import java.net.URL;

public class statusbar extends Panel
{
    public String statusStr = "Status:" ;
    public String errorStr = "Error:" ;
    public String posStr = "(X,Y):";
    public String sigInfoStr = "";
    //Color cstat = new Color(200,200,200);
    intscDrawArea parent;
    int[] panelWidth = {200,200, 100,70} ;
    int panelHeight=20 ;


    statusbar()
    {
	setBackground(Color.lightGray);
    }

    public void paint(Graphics g) 
    {
	Rectangle r = bounds();

        int start = 0;
	for(int i=0;i<4;i++)
	{
            g.setColor(Color.lightGray);
            g.fillRect(start, 0,start,panelHeight);

	    g.setColor(Color.black);
	    g.drawLine(start, 0,start, panelHeight);
            switch (i) {
            case 0:
                g.drawString(statusStr, start+2, panelHeight-4);
                break ;
            case 1: // error
                g.setColor(Color.red) ;
                g.drawString(errorStr, start+2, panelHeight-4);
                break ;
            case 2:
                g.drawString(posStr, start+2, panelHeight-4);
                break ;
            case 3:
                g.drawString(sigInfoStr, start+2, panelHeight-4);
                break ;
             }
            start += panelWidth[i] ;
	}   // i
        
    }

    public Dimension preferredSize()
    {
	return(new Dimension(panelWidth[0],panelHeight));
    }

    public boolean mouseDown(Event e,int x,int y)
    {
	return(true);
    }
    public boolean setStatusBarText(int index, String str)
    {
        switch (index) {
        case 0:
            statusStr="Status:" + str;
            break ;
        case 1:
            errorStr= str;
            break ;
        case 2:
            posStr="(X,Y):" + str;
            break ;
        case 3:
            sigInfoStr= str;
            break ;
        case 4:
            errorStr=str;
            break ;
         }
         repaint();
	 return(true);
    }
}
