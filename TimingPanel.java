/*
 * TimingPanel.java
 * Display panel for signal timing
 * Created on July 5, 2006, 11:25 AM
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
import java.awt.event.* ;        // doesn//t automatically load with java.awt.*

public class TimingPanel extends Panel {
    
    int panelWidth=200 ;    // default panel size
    int panelHeight=100 ;
    Label title = new Label();
    TextField left_time = new TextField("0") ;
    TextField thru_time = new TextField("10") ;
    Label leftonly = new Label() ;
    
    /** Creates a new instance of TimingPanel */
    public TimingPanel() {
    }
    
    public TimingPanel(String _titleStr, String _left, String _thru) {
        title.setText(_titleStr) ;
	setBackground(new Color(166, 210, 169));
        setLayout(new BorderLayout(2,2));                
        title.setForeground(Color.black) ;
        title.setAlignment(title.CENTER) ;
        title.setFont(Font.getFont("TimesRoman-BOLD-15")) ;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0,0,0,0) ;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2 ;
        add(title, c);
        c.gridwidth = 1 ;
        c.gridy = 1;
        add(new Label("Ph"+_thru+": Thru (sec)"), c);   // thru movement
        c.gridy = 2;
        leftonly.setText("Ph"+_left+": Left (sec)") ;   // left turn 
        add(leftonly, c);
        
        c.gridx = 1;
        c.gridy = 1;
        add(thru_time, c);      
        c.gridy = 2;
        add(left_time, c);
    }

    public float getLeftTime()
    {
         float _lt_time = new Float(left_time.getText()).floatValue() ;
         return _lt_time ;
    }
    public float getThruTime()
    {
         float _thru_time = new Float(thru_time.getText()).floatValue() ;
         return _thru_time ;
    }
    public void setLeftTime(float _left) {
        left_time.setText(new Float(_left).toString()) ;
    }
    public void setThruTime(float _thru) {
        thru_time.setText(new Float(_thru).toString()) ;
    }
    public void setProtective(boolean state) {
        left_time.setEnabled(state) ;
        leftonly.setEnabled(state) ;
    }

}
