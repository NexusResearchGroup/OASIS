/*
 * LinkPanel.java
 * Display panel for an intersection link
 * Created on June 6, 2006, 11:35 PM
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

public class LinkPanel extends Panel
{
    public LinkPanel approach ;
    
    int panelWidth=200 ;    // default panel size
    int panelHeight=150 ;
    Label title = new Label();
    Checkbox exclLeft = new Checkbox("Exclusive Left Turn");
    TextField speed = new TextField("35") ;
    public TextField volume = new TextField("450") ;
    Choice laneSize = new Choice() ;
    Checkbox mainStreet = new Checkbox("Main") ;  // 3/14/07 added
    
    LinkPanel(String _titleStr, String _iconStr)
    {
        title.setText(_titleStr) ;
	setBackground(new Color(121,217,157));
        setLayout(new BorderLayout(3,1));                
        title.setForeground(Color.black) ;
        title.setAlignment(title.CENTER) ;
        title.setFont(Font.getFont("TimesRoman-BOLD-15")) ;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0,0,0,0) ;
        c.gridx = 0;
        c.gridy = 0;
        add(title, c);
        c.gridy = 1;
        add(new Label("Speed (mph)"), c);
        c.gridy = 2;
        add(new Label("Volume (vph)"), c);
        c.gridy = 3;
        add(new Label("# of Lanes"), c);
        
        c.gridx = 1;
        c.gridy = 0;
        add(new iconPanel(_iconStr), c);
        c.gridy = 1;
        add(speed, c);
        c.gridy = 2;
        add(volume, c);
        c.gridy = 3;
        add(laneSize, c);
        laneSize.add("2") ;
        laneSize.add("4") ;
        laneSize.add("6") ;
        
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2 ;
        add(exclLeft, c);

        //c.gridy = 5; ;
        //add(mainStreet, c);

        // temperary set
        laneSize.select(1) ;            // 3/7/07, 4 lanes
        exclLeft.setState(true) ;       // 3/7/07
        laneSize.setEnabled(false) ;    // not allow for changes
        exclLeft.setEnabled(false) ;    // disabled
        
        // lane sixe
        ItemListener laneSize_listener = new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                int index = laneSize.getSelectedIndex();
                approach.setLaneSize(index) ;
                
            }
        } ;
        laneSize.addItemListener(laneSize_listener) ;
        // main street
        ItemListener mainSt_listener = new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                approach.setMainStreet(mainStreet.getState()) ;
            }
        } ;
        mainStreet.addItemListener(mainSt_listener) ;        
    }

    public void setMainStreet(boolean state) {
        mainStreet.setState(state) ;
    }
    
    public void setLaneSize(int _laneSize)
    {
        laneSize.select(_laneSize) ;
    }
    
    public float getSpeed() 
    {
        // return entered speed
        float _spd = new Float(speed.getText()).floatValue() ;
        //System.out.println("speed="+_spd) ;
        return _spd ;
    }
    public float getVolume() 
    {
        // return entered volume
        float _vol = 0f ;
        _vol = new Float(volume.getText()).floatValue() ;
        return _vol ;
    }
    public boolean hasExclusiveLeftTurn() 
    {
        // return checked uxclusive left turn setting
        return exclLeft.getState() ;
    }
    public int getLaneSize()
    {
        int data = new Integer(laneSize.getSelectedItem()).intValue() ;
        return data ;
    }

    public void paint(Graphics g) 
    {
  
    }

    public Dimension preferredSize()
    {
	return(new Dimension(panelWidth,panelHeight));
    }

    public boolean mouseDown(Event e,int x,int y)
    {
	return(true);
    }

}
