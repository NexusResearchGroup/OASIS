/*
 * LTPanel.java
 * Left turn panel, permissive or protected
 * Created on July 5, 2006, 11:48 AM
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

public class LTPanel extends Panel {
    
    int panelWidth=200 ;    // default panel size
    int panelHeight=100 ;
    Label title = new Label();
    CheckboxGroup cbg = new CheckboxGroup();
    Checkbox permissive = new Checkbox("Permissive", cbg, false) ;
    Checkbox protective = new Checkbox("Protective", cbg, true) ;

    /** Creates a new instance of LTPanel */
    public LTPanel() {
    }
    
    public LTPanel(String _titleStr) {
        title.setText(_titleStr) ;
	setBackground(new Color(166,210,169));
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
        c.gridwidth = 2 ;
        add(title, c);
        c.gridwidth = 1 ;
        c.gridy = 1;
        add(permissive, c);
        c.gridy = 2;
        add(protective, c);      
        permissive.setCheckboxGroup(cbg) ;
        protective.setCheckboxGroup(cbg) ;
    }
    
    public void setPermissive(boolean state) {
        permissive.setState(state) ;
    }
    public void setProtective(boolean state) {
        protective.setState(state) ;
        protective.setEnabled(state) ;
    }
    public boolean isPermissive() {
        return permissive.getState() ;
    }
    public boolean isProtective() {
        return protective.getState() ;
    }
}
