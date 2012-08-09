/*
 * Actuated_Control.java
 *
 * Actuated Signal Control Design main screen.
 * 
 * Created on May 17, 2006, 12:08 PM
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
import java.applet.*;
import java.net.URL ;
import java.awt.image.*;
import java.io.*;
import java.io.FilenameFilter;
import com.sun.image.codec.jpeg.*;

public class Actuated_Control extends Applet 
{
    //static int xx[] = new int[1024];
    //static int yy[] = new int[1024];
    //int n = 0;
    String URLParam ;
    // Java GUI
    myWindow frmActuatedControl ;        // pp screen for actuated control settings
    myWindow frmActuatedIntersection ;        // screen for intersection geometry design
    myWindow frmAbout ;                 // help about screen
    intscDrawArea intscDesign = new intscDrawArea();
    

    // Link Para
    public LinkPanel NB, SB, EB, WB ;           // GUI parameters
    public Checkbox mainEW, mainNS ;
    imagePanel IP ;                     // center image panel
    // Shared variables
    SHARED myShared = new SHARED() ;
    
    // class initialization
    public void init()
    {
        //intscDesign.myDB = myShared ;
        //intscDesign.actuatedScreen = this ;
        frmAbout = new myWindow() ;
        frmActuatedControl = new myWindow() ;
        frmActuatedIntersection = new myWindow() ;
        
        Button btnOK =  new Button("Continue to Intersection Settings") ;
        //Button btnOK =  new Button("Proceed to Traffic Signal Controller") ;
        setLayout(new BorderLayout(0,0));
        Panel textboxp = new Panel();
        textboxp.setLayout(new BorderLayout(0,0));
        textboxp.add("Center", new aboutTextbox()); 
        
        Panel startup = new Panel();
        startup.setBackground(Color.white);
	startup.setLayout(new BorderLayout(1,1));
	startup.add("Center", textboxp);
	startup.add("South", btnOK);

        
        // handle event on OK nutton
        ActionListener btnOKListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // temperatory disabled for virtual controller
                popActuatedControlSettings("Actuated Signal Control - Settings");
                
                // 1/19/07 chenfu
                //intscDesign.init() ;
                //intscDesign.popSignalController() ;
                
                //popActuatedIntersectionSim() ; //comment out
            }
        } ;
        btnOK.addActionListener(btnOKListener) ;
        
        // broders
	add("West", new border(2, Color.black));
	add("East", new border(2, Color.black));
	add("North", new border(2, Color.black));
	add("South", new border(2, Color.black));
        add("Center",startup);

    }
    
    public void popActuatedControlSettings(String _title) {
        if (frmActuatedControl.isShowing()==false) 
        {
            Button btnNext =  new Button("Click Here to Continue") ;
            frmActuatedControl = new myWindow(_title) ;//"Simulation Statistics") ;
            frmActuatedControl.setSize(580, 520) ;
            //frmActuatedControl.setLocation(150,100) ;
           
            // create settings for 4-leg approaches
            NB = new LinkPanel("North Bound", "NB") ;
            SB = new LinkPanel("South Bound", "SB") ;
            EB = new LinkPanel("East Bound", "EB") ;
            WB = new LinkPanel("West Bound", "WB") ;
            // make sure EW or NS has the same # of lane
            NB.approach = SB ;
            SB.approach = NB ;
            EB.approach = WB ;
            WB.approach = EB ;
            
            // image panel
            IP = new imagePanel("intscNS") ;
            
            // MAIN street selection
            mainNS = new Checkbox("North-South") ;  // 3/14/07 added
            mainEW = new Checkbox("East-West") ;  // 3/14/07 added
            if (myShared.mainStreetNS) {
                mainNS.setState(true) ; 
            } else {
                mainEW.setState(true) ;
            }
            Panel mainSt = new Panel() ;
            //mainSt.setLayout(new BorderLayout(0, 0));
            //mainSt.add("Center",mainEW);
            //mainSt.add("West",mainNS);
            mainSt.setBackground(Color.lightGray) ;     //new Color(121,217,157));
            mainSt.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL ;
            c.insets = new Insets(1,40,0,40) ;
            c.gridx = 0; c.gridy = 0;
            mainSt.add(new Label("Main Street:"), c);
            c.gridx = 1;
            mainSt.add(mainNS, c);
            c.gridx = 2;
            mainSt.add(mainEW, c);
            // main street
            ItemListener mainEW_listener = new ItemListener() {
                public void itemStateChanged(ItemEvent ie) {
                    boolean state = mainEW.getState() ;
                    if (state) {
                        mainNS.setState(!state) ;
                        IP.changeImagePanel("intscEW") ;
                    }   // if
                }   // itemStateChanged
            } ;
            mainEW.addItemListener(mainEW_listener) ;        
            ItemListener mainNS_listener = new ItemListener() {
                public void itemStateChanged(ItemEvent ie) {
                    boolean state = mainNS.getState() ;
                    if (state) {
                        mainEW.setState(!state) ;
                        IP.changeImagePanel("intscNS") ;
                    }   // if
                }   // itemStateChanged
            } ;
            mainNS.addItemListener(mainNS_listener) ;    
            
            Panel setup = new Panel();
            //setup.setBackground(Color.lightGray);
            setup.setLayout(new BorderLayout(0,0));
            setup.add("North",NB);
            setup.add("South",SB);
            setup.add("East",EB);
            setup.add("West",WB);
            setup.add("Center", IP) ;
            
            // handle event on continue button
            ActionListener btnNextListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // save settings and create intersectin geometry here
                    myShared.mainStreetNS = mainNS.getState() ;
                    myShared.EB_data = new LinkData(EB.getSpeed(), EB.getVolume(), 
                            EB.hasExclusiveLeftTurn(), EB.getLaneSize()) ;
                    //System.out.println("EB spd limit="+EB.getSpeed()) ;
                    //System.out.println("EB spd limit="+myShared.EB_data.getSpeedLimit()) ;
                    
                    myShared.WB_data = new LinkData(WB.getSpeed(), WB.getVolume(), 
                            WB.hasExclusiveLeftTurn(), WB.getLaneSize()) ;
                    myShared.NB_data = new LinkData(NB.getSpeed(), NB.getVolume(), 
                            NB.hasExclusiveLeftTurn(), NB.getLaneSize()) ;
                    myShared.SB_data = new LinkData(SB.getSpeed(), SB.getVolume(), 
                            SB.hasExclusiveLeftTurn(), SB.getLaneSize()) ;
                  // display actuated intersection gui
                    popActuatedIntersectionSim("Signalized Intersection") ;
                }
            } ;
            btnNext.addActionListener(btnNextListener) ;
                        
            frmActuatedControl.setLayout(new BorderLayout(0,0));
            frmActuatedControl.add("North", mainSt) ;
            frmActuatedControl.add("Center", setup) ;
            frmActuatedControl.add("South", btnNext) ;
            frmActuatedControl.setCenter();
            frmActuatedControl.validate() ;
            frmActuatedControl.setVisible(true) ;
            frmActuatedControl.show() ;
            
        }
    }
    
    // Show Horizontal Geometric Design Panel
    public void popActuatedIntersectionSim(String _title) {
        if (frmActuatedIntersection.isShowing()==false) 
        {
            frmActuatedIntersection = new myWindow(_title) ;//"Simulation Statistics") ;
            frmActuatedIntersection.setSize(800, 640) ;
            frmActuatedIntersection.setResizable(false) ;
            //frmActuatedIntersection.setLocation(150,100) ;
            frmActuatedIntersection.setCenter();
            frmActuatedIntersection.validate() ; 
            frmActuatedIntersection.setVisible(true) ;
            frmActuatedIntersection.show() ;

            // file menu
            MenuBar menu_bar = new MenuBar() ;
            Menu menu_file = new Menu("File") ;
            MenuItem file_cntlr_open = new MenuItem("Load Controller Settings") ;
            MenuItem file_cntlr_save = new MenuItem("Save Controller Settings") ;
            
            MenuItem file_open = new MenuItem("Load Signal Timing") ;
            MenuItem file_save = new MenuItem("Save Signal Timing") ;
            MenuItem separator = new MenuItem("-") ;
       //     MenuItem file_close = new MenuItem("Close Design") ;
       //     MenuItem file_import = new MenuItem("Import Contour") ;
      //      MenuItem file_pagesetup = new MenuItem("Page Setup") ;
            MenuItem file_print = new MenuItem("Print") ;
            MenuItem file_close = new MenuItem("Close") ;
            // file menu items
            menu_file.add(file_cntlr_open) ;   // add menu items
            menu_file.add(file_cntlr_save) ;   // add menu items
            menu_file.addSeparator() ;
            menu_file.add(file_open) ;   // add menu items
            menu_file.add(file_save) ;   // add menu items
            menu_file.addSeparator() ;
            
      //      menu_file.add(file_close) ;   // add menu items
      //      menu_file.addSeparator() ;
      //      menu_file.add(file_import) ;
//            menu_file.addSeparator() ;
      //      menu_file.add(file_pagesetup) ;
//            menu_file.add(file_print) ;
//            menu_file.addSeparator() ;
            menu_file.add(file_close) ;
            /*
            // edit menu
            Menu menu_edit = new Menu("Edit") ;
//            edit_undo = new MenuItem("Undo") ;
//            edit_redo = new MenuItem("Redo") ;
//            edit_delete = new MenuItem("Delete") ;
            MenuItem edit_clearLandmarks = new MenuItem("Clear Landmarks") ;
            MenuItem edit_clearAll = new MenuItem("Clear All") ;
            MenuItem edit_unselect = new MenuItem("Unselect All") ;
//            menu_edit.add(edit_undo) ;
//            menu_edit.add(edit_redo) ;
//            menu_edit.add(edit_delete) ;
            menu_edit.addSeparator();
            menu_edit.add(edit_clearLandmarks) ;
            menu_edit.add(edit_clearAll) ;
            menu_edit.add(edit_unselect) ;
            // view menu
            Menu menu_view = new Menu("View") ;
            MenuItem view_reset = new MenuItem("Reset (1:1)") ;
            MenuItem view_zoomin = new MenuItem("Zoom In") ;
            MenuItem view_zoomout = new MenuItem("Zoom Out") ;
            MenuItem view_landmarks = new MenuItem("Station Landmarks");
            //view_landmarks.setEnabled(false);
            
            menu_view.add(view_reset) ;
            menu_view.add(view_zoomin) ;
            menu_view.add(view_zoomout) ;
            menu_view.addSeparator();
            menu_view.add(view_landmarks) ;
            */
            // settings menu
            Menu menu_settings = new Menu("Settings") ;
            MenuItem settings_volume = new MenuItem("Traffic Volume") ;
            MenuItem settings_actuation = new MenuItem("Actuation Type") ;
            MenuItem settings_controller = new MenuItem("Controller Settings") ;
            menu_settings.add(settings_volume) ;
            menu_settings.add(settings_actuation) ;
            menu_settings.add(settings_controller) ;
            // tool menu
            Menu menu_option = new Menu("Tool") ;
            MenuItem option_update = new MenuItem("Update Controller Settings") ;
            MenuItem option_view = new MenuItem("View Signal Timing") ;
            MenuItem option_time = new MenuItem("Set Global Time") ;
            MenuItem option_jump2time = new MenuItem("Jump Back to Time") ;
            menu_option.add(option_update) ;
            menu_option.add(option_view) ;
            menu_option.add(option_time) ;
            menu_option.add(option_jump2time) ;
            
            // help menu
            Menu menu_help = new Menu("Help") ;
            //MenuItem help_manual = new MenuItem("Instructions") ;
            MenuItem help_web_contents = new MenuItem("Web Contents") ;
            MenuItem help_about = new MenuItem("About") ;
            menu_help.add(help_web_contents) ;
            //menu_help.add(help_manual) ;
            menu_help.add(help_about) ;
            // ===========================================
            menu_bar.add(menu_file) ;     // add menu
       //     menu_bar.add(menu_edit) ;     // add menu
       //     menu_bar.add(menu_view) ;     // add menu
            menu_bar.add(menu_settings) ;     // add menu
            menu_bar.add(menu_option) ;     // add menu
            menu_bar.add(menu_help) ;     // add menu
            frmActuatedIntersection.setMenuBar(menu_bar) ;

            toolbar tb = new toolbar();
            statusbar sb = new statusbar() ;
            Panel cm = new Panel();
            Panel cc = new Panel();
            frmActuatedIntersection.setLayout(new BorderLayout(0,0));

            //Scrollbar ss = new Scrollbar(Scrollbar.HORIZONTAL);
            cc.setLayout(new BorderLayout(0,0));
            //cc.add("South",new Scrollbar(Scrollbar.HORIZONTAL));
            //cc.add("East",new Scrollbar(Scrollbar.VERTICAL));
            intscDesign = new intscDrawArea(tb, sb);    // simulation display area
            intscDesign.myApplet = this;
            intscDesign.myDB = myShared ;
            //intscDesign.frameParent = frmActuatedIntersection ;
            intscDesign.init() ; 
            cc.add("Center",intscDesign); 

            cm.setBackground(Color.black);
            cm.setLayout(new BorderLayout(1,1));
            cm.add("North",tb);
            cm.add("Center",cc);
            cm.add("South",sb);

            frmActuatedIntersection.add("West", new border(2, Color.black));
            frmActuatedIntersection.add("East", new border(2, Color.black));
            frmActuatedIntersection.add("North", new border(2, Color.black));
            frmActuatedIntersection.add("South", new border(2, Color.black));
            frmActuatedIntersection.add("Center",cm);
            frmActuatedIntersection.invalidate() ;
            frmActuatedIntersection.show() ;

            help_web_contents.addActionListener( 
                new ActionListener() {
                    public void actionPerformed(ActionEvent aev) {
                        try
                        {
                            AppletContext a = getAppletContext();
                            URL u = new URL(SHARED.CONTENTS_PATH);  
                            a.showDocument(u,"_blank");
                            //_blank to open page in new window		
                        }
                        catch (Exception e){
                                //do nothing
                            intscDesign.popMessageBox("Help - Web Content", "Error:"+e.toString()) ;
                        } // try
                    } // actionPerformed
                } // ActionListener
            ) ; // help_web_contents
/*
            help_manual.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aev) {
                        try
                        {
                            AppletContext a = getAppletContext();
                            URL u = new URL(SHARED.MANUAL_PATH);
                            a.showDocument(u,"_blank");
                            //_blank to open page in new window		
                        }
                        catch (Exception e){
                                //do nothing
                            intscDesign.popMessageBox("Help - Instructions", "Error:"+e.toString()) ;
                        } // try
                    } // actionPerformed
                } // ActionListener
            ) ; // help_manual
*/
            help_about.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aev) {
                        if (frmAbout.isShowing()==false) {
                            frmAbout = new myWindow("About Intersection Signal Control") ;
                            frmAbout.setSize(300, 140) ;
                            frmAbout.setResizable(false);
                            frmAbout.setLocation(100,100) ;
                            frmAbout.show() ;

                            frmAbout.setLayout(new BorderLayout(0,0));
                            Panel textboxp = new Panel();
                            textboxp.setLayout(new BorderLayout(0,0));
                            textboxp.add("Center",new aboutTextbox()); 

                            Panel about = new Panel();
                            about.setBackground(Color.white);
                            about.setLayout(new BorderLayout(1,1));
                            about.add("Center",textboxp);
                            frmAbout.add(about);
                            frmAbout.invalidate() ;
                            frmAbout.setVisible(true) ;
                        }
                        else {
                            frmAbout.show();
                        }
                        
                   } // actionPerformed
                } // ActionListener
            ) ; // help_about
            /*
            file_import.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            try
                            {
                            }
                            catch (Exception e){
                                    //do nothing
                            } // try
                        } // actionPerformed
                    } // ActionListener
             ) ; // file import
             */
            file_cntlr_save.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.saveLCDPages() ;
                        } // actionPerformed
                    } // ActionListener
             ) ; // file controller open
            file_cntlr_open.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.openLCDPages() ;
                            intscDesign.parseSignalTimingFromVirtualController() ;
                        } // actionPerformed
                    } // ActionListener
             ) ; // file controller close
             
            file_open.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            // load control timing
                            FileInputStream fis=null;
                            DataInputStream br=null;
                            String dir = "", filename = "" ;

                            try
                            {
                                FileDialog fd=new FileDialog(new Frame(),"Open Timing", FileDialog.LOAD);
                                fd.setFile("*.act");
                                fd.show();
                                dir = fd.getDirectory() ;
                                filename = fd.getFile() ;
                                String fullpath = dir + filename ;
                                fd.dispose();

                                if(filename != null && dir != null) {
                                    //System.out.println("Open filename="+fullpath);
                                   
                                    fis = new FileInputStream(fullpath);
                                    br = new DataInputStream( new BufferedInputStream(fis,512)); 
                                    
                                    intscDesign.parseSignalTimingFromVirtualController() ;
                                    intscDesign.sim_step_once = true ;
                                    try {Thread.sleep(1000) ;}
                                    catch (InterruptedException ie) {} ;
                                    
                                    // read data from file
                                    intscDesign.myGlobalTime = br.readFloat() ;
                                    intscDesign.mySignalControl.control_type = br.readInt() ;
                                    intscDesign.mySignalControl.ring1ptr = br.readInt() ;
                                    intscDesign.mySignalControl.ring2ptr = br.readInt() ;
                                    
                                    // fixed timer 1
                                    intscDesign.mySignalControl.fixedTimer1.phase_stage = br.readInt() ;
                                    intscDesign.mySignalControl.fixedTimer1.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.setTimesup(br.readBoolean()) ; 
                                    intscDesign.mySignalControl.fixedTimer1.setStepSim(br.readBoolean()) ;
                                    // fixed timer 1 GREEN
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer1.greenTimer.elapsedTimeSec = br.readFloat() ;
                                    //  fixed timer 1 YELLOW
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer1.yellowTimer.elapsedTimeSec = br.readFloat() ;
                                    // fixed timer 1 RED
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer1.redTimer.elapsedTimeSec =br.readFloat() ;

                                    // fixed timer 2
                                    intscDesign.mySignalControl.fixedTimer2.phase_stage = br.readInt() ;
                                    intscDesign.mySignalControl.fixedTimer2.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.setStepSim(br.readBoolean()) ;
                                    // fixed timer 2 GREEN
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer2.greenTimer.elapsedTimeSec = br.readFloat() ;
                                    // fixed timer 2 YELLOW
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer2.yellowTimer.elapsedTimeSec = br.readFloat() ;
                                    // fixed timer 2 RED
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.fixedTimer2.redTimer.elapsedTimeSec = br.readFloat() ;
   
                                    // actuated timer 1
                                    intscDesign.mySignalControl.actuatedTimer1.phase_stage = br.readInt() ;
                                    intscDesign.mySignalControl.actuatedTimer1.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.setTimesup(br.readBoolean()) ; 
                                    intscDesign.mySignalControl.actuatedTimer1.setStepSim(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.ok_to_terminate = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer1.timeup_flag = br.readInt() ;
                                    
                                    // actuated timer 1 GREEN
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.setMaxOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.setGapOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.setInitOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_initial(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_extension(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_maximum(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_lastinitial(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_lastextension(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_lastmaximum(br.readFloat()) ;  
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_setting_EXT(br.readFloat()) ; 
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_setting_INI(br.readFloat()) ; 
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_setting_MAX(br.readFloat()) ;  
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_EXT_start(br.readBoolean()) ; 
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_hold_recall_flag(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.set_first_start(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.greenTimer.elapsedTimeSec = br.readFloat() ; 
                                    //  actuated timer 1 YELLOW
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer1.yellowTimer.elapsedTimeSec = br.readFloat() ;
                                    // actuated timer 1 RED
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer1.redTimer.elapsedTimeSec =br.readFloat() ;
                                    
                                    // actuated timer 2
                                    intscDesign.mySignalControl.actuatedTimer2.phase_stage = br.readInt() ;
                                    intscDesign.mySignalControl.actuatedTimer2.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.setTimesup(br.readBoolean()) ; 
                                    intscDesign.mySignalControl.actuatedTimer2.setStepSim(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.ok_to_terminate = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer2.timeup_flag = br.readInt() ;
                                    
                                    // actuated timer 2 GREEN
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.setMaxOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.setGapOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.setInitOut(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_initial(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_extension(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_maximum(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_lastinitial(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_lastextension(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_lastmaximum(br.readFloat()) ;  
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_setting_EXT(br.readFloat()) ; 
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_setting_INI(br.readFloat()) ; 
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_setting_MAX(br.readFloat()) ;  
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_EXT_start(br.readBoolean()) ; 
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_hold_recall_flag(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.set_first_start(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.greenTimer.elapsedTimeSec = br.readFloat() ; 
                                    //  actuated timer 2 YELLOW
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer2.yellowTimer.elapsedTimeSec = br.readFloat() ;
                                    // actuated timer 2 RED
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setFlag(br.readInt()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setCount(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setCountValue(br.readFloat()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setTimerPaused(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setTimesup(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.setFirstStart(br.readBoolean()) ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.stepSim = br.readBoolean() ;
                                    intscDesign.mySignalControl.actuatedTimer2.redTimer.elapsedTimeSec =br.readFloat() ;
   
                                    // ================= added 4/2/08
                                    for (int i=0; i<intscDesign.mySignalControl.NUM_PHASES; i++){
                                        intscDesign.mySignalControl.phaseStatus[i] = br.readBoolean() ;
                                        intscDesign.mySignalControl.myPresenceRegisters[i] = br.readBoolean() ; 
                                        intscDesign.mySignalControl.myExtRegisters[i] = br.readBoolean() ; 
                                    }
                                    // LOOP DETECTORS STATUS
                                    int N = 0 ;
                                    // EB
                                    N = intscDesign.myDB.EB_data.getLaneSize()/2 ;
                                    for (int j=0; j<=N; j++) {
                                        intscDesign.myDB.EB_data.setLoopOccupied(j, br.readBoolean()) ;
                                        intscDesign.myDB.EB_data.setPresenceLoopOccupied(j, br.readBoolean()) ;
                                    }
                                    // WB
                                    N = intscDesign.myDB.WB_data.getLaneSize()/2 ;
                                    for (int j=0; j<=N; j++) {
                                        intscDesign.myDB.WB_data.setLoopOccupied(j, br.readBoolean()) ;
                                        intscDesign.myDB.WB_data.setPresenceLoopOccupied(j, br.readBoolean()) ;
                                    }
                                    // NB
                                    N = intscDesign.myDB.NB_data.getLaneSize()/2 ;
                                    for (int j=0; j<=N; j++) {
                                        intscDesign.myDB.NB_data.setLoopOccupied(j, br.readBoolean()) ;
                                        intscDesign.myDB.NB_data.setPresenceLoopOccupied(j, br.readBoolean()) ;
                                    }
                                    // SB
                                    N = intscDesign.myDB.SB_data.getLaneSize()/2 ;
                                    for (int j=0; j<=N; j++) {
                                        intscDesign.myDB.SB_data.setLoopOccupied(j, br.readBoolean()) ;
                                        intscDesign.myDB.SB_data.setPresenceLoopOccupied(j, br.readBoolean()) ;
                                    }

                                    intscDesign.sb.sigInfoStr = br.readUTF() ;
                                    intscDesign.sb.errorStr = br.readUTF() ;
                                    
                                    // =========================================
                                    br.close();
                                    fis.close();
                                    
                                }   // end if
                            }   // end try
                            catch (Exception e){
                                    //do nothing
                                System.out.println("Open Timing File:"+e.toString());
                                System.out.println("File directory = "+dir+", filename = "+filename);
                            } // try
                        } // actionPerformed
                    } // ActionListener
             ) ; // file open
             
             file_save.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            FileOutputStream fos=null;
                            DataOutputStream w=null;
 
                            try
                            {
                                FileDialog fd=new FileDialog(new Frame(),"Save Timing", FileDialog.SAVE);
                                fd.setFile("*.act");
                     /*            fd.setFilenameFilter(new FilenameFilter(){
                                    public boolean accept(File dir, String name){
                                      return (name.endsWith(".act")) ;  // || name.endsWith(".gif"));
                                      }
                                });
                      */
                                fd.show();
                                String fullpath=fd.getDirectory()+fd.getFile();
                                fd.dispose();
//System.out.println("filepath="+fullpath);
                                if(fullpath!=null) {
                                    fos = new FileOutputStream(fullpath);
                                    w = new DataOutputStream( new BufferedOutputStream(fos,512)); 
                                    
                                    // save data to file
                                    w.writeFloat(intscDesign.myGlobalTime) ;
                                    w.writeInt(intscDesign.mySignalControl.control_type) ;
                                    w.writeInt(intscDesign.mySignalControl.ring1ptr) ;
                                    w.writeInt(intscDesign.mySignalControl.ring2ptr) ;
                                    
                                    // fixed timer 1
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer1.phase_stage) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.getTimesup()) ; 
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.getStepSim()) ;
                                    // fixed timer 1 GREEN
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer1.greenTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.greenTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.greenTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.greenTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.greenTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.greenTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.greenTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.greenTimer.elapsedTimeSec) ;
                                    //  fixed timer 1 YELLOW
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.yellowTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.yellowTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.yellowTimer.elapsedTimeSec) ;
                                    // fixed timer 1 RED
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer1.redTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.redTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.redTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.redTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.redTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.redTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer1.redTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer1.redTimer.elapsedTimeSec) ;

                                    // fixed timer 2
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer2.phase_stage) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.getTimesup()) ; 
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.getStepSim()) ;
                                    // fixed timer 2 GREEN
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer2.greenTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.greenTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.greenTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.greenTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.greenTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.greenTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.greenTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.greenTimer.elapsedTimeSec) ;
                                    // fixed timer 2 YELLOW
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.yellowTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.yellowTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.yellowTimer.elapsedTimeSec) ;
                                    // fixed timer 2 RED
                                    w.writeInt(intscDesign.mySignalControl.fixedTimer2.redTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.redTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.redTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.redTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.redTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.redTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.fixedTimer2.redTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.fixedTimer2.redTimer.elapsedTimeSec) ;

                                    // ====================================================================================
                                    // actuated timer 1
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer1.phase_stage) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.getTimesup()) ; 
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.getStepSim()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.ok_to_terminate) ;
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer1.timeup_flag) ;
                                    
                                    // actuated timer 1 GREEN
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.getMaxOut()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.getGapOut()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.getInitOut()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_initial()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_extension()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_maximum()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_lastinitial()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_lastextension()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_lastmaximum()) ;  
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_setting_EXT()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_setting_INI()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_setting_MAX()) ;  
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_EXT_start()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_hold_recall_flag()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.stepSim) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.greenTimer.get_first_start()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.greenTimer.elapsedTimeSec) ;
                                    //  actuated timer 1 YELLOW
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.yellowTimer.elapsedTimeSec) ;
                                    // actuated timer 1 RED
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer1.redTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.redTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.redTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.redTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.redTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.redTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer1.redTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer1.redTimer.elapsedTimeSec) ;
                                    
                                    // actuated timer 2
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer2.phase_stage) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.getTimesup()) ; 
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.getStepSim()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.ok_to_terminate) ;
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer2.timeup_flag) ;
                                    
                                    // actuated timer 2 GREEN
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.getMaxOut()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.getGapOut()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.getInitOut()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_initial()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_extension()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_maximum()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_lastinitial()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_lastextension()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_lastmaximum()) ;  
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_setting_EXT()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_setting_INI()) ; 
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_setting_MAX()) ;  
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_EXT_start()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_hold_recall_flag()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.stepSim) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.greenTimer.get_first_start()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.greenTimer.elapsedTimeSec) ;
                                    //  actuated timer 2 YELLOW
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.yellowTimer.elapsedTimeSec) ;
                                    // actuated timer 2 RED
                                    w.writeInt(intscDesign.mySignalControl.actuatedTimer2.redTimer.getFlag()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.redTimer.getCount()) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.redTimer.getCountValue()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.redTimer.getTimerPaused()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.redTimer.getTimesup()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.redTimer.getFirstStart()) ;
                                    w.writeBoolean(intscDesign.mySignalControl.actuatedTimer2.redTimer.stepSim) ;
                                    w.writeFloat(intscDesign.mySignalControl.actuatedTimer2.redTimer.elapsedTimeSec) ;
                                    
                                    // ================= added 4/2/08
                                    for (int i=0; i<intscDesign.mySignalControl.NUM_PHASES; i++){
                                        w.writeBoolean(intscDesign.mySignalControl.phaseStatus[i]) ;
                                        w.writeBoolean(intscDesign.mySignalControl.myPresenceRegisters[i]) ; 
                                        w.writeBoolean(intscDesign.mySignalControl.myExtRegisters[i]) ; 
                                    }
                                    for (int i=0; i<4; i++) {
                                        LinkData myLink = new LinkData() ;
                                        switch (i) {
                                            case 0: // EB
                                                myLink = intscDesign.myDB.EB_data ;
                                                break ;
                                            case 1: // WB
                                                myLink = intscDesign.myDB.WB_data ;
                                                break ;
                                            case 2: // NB
                                                myLink = intscDesign.myDB.NB_data ;
                                                break ;
                                            case 3: // SB
                                                myLink = intscDesign.myDB.SB_data ;
                                                break ;
                                        }   // switch
                                        int N = myLink.getLaneSize()/2 ;
                                        for (int j=0; j<=N; j++) {
                                            w.writeBoolean(myLink.isLoopOccupied(j)) ;
                                            w.writeBoolean(myLink.isPresenceLoopOccupied(j)) ;
                                        }   // j
                                    }
                                    w.writeUTF(intscDesign.sb.sigInfoStr) ;
                                    w.writeUTF(intscDesign.sb.errorStr) ;
                                    // end of 4/2/08 added
                                    
                                    // ===============
                                    w.flush();
                                    w.close();
                                }
                                fos.close();
                            }
                            catch (Exception e){
                                    //do nothing
                                System.out.println("Save Control Timing:"+e.toString());
                            } // try
                        } // actionPerformed
                    } // ActionListener
             ) ; // file save

             file_print.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.print();
                            //PrintUtilities.printComponent(intscDesign);
                        } // actionPerformed
                    } // ActionListener
             ) ; // file Print
             file_close.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            frmActuatedIntersection.dispose();
                        } // actionPerformed
                    } // ActionListener
             ) ; // file Exit
             /*
             view_zoomin.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                        //    intscDesign.changeDrawScale(0.1f);
                        } // actionPerformed
                    } // ActionListener
             ) ; // view zoom in
             view_zoomout.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                        //    intscDesign.changeDrawScale(-0.1f);
                        } // actionPerformed
                    } // ActionListener
             ) ; // view zoom out
              */
             settings_volume.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.SetVol_flag = true ;
                        } // actionPerformed
                    } // ActionListener
             ) ; // volume settings
             settings_actuation.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.SetActuationType_flag = true ;
                        } // actionPerformed
                    } // ActionListener
             ) ; // control type
             settings_controller.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            intscDesign.SetTiming_flag = true ;
                        } // actionPerformed
                    } // ActionListener
             ) ; // control type
             option_update.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            //intscDesign.load_LCD_Screens() ;    // read LCD screen database
                            // load LCD formats
                            //intscDesign.load_LCD_Data_Format() ;

                           intscDesign.parseSignalTimingFromVirtualController() ;
                            
                              
                        } // actionPerformed
                    } // ActionListener
             ) ; // load default controller settings
             option_view.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {
                            //intscDesign.load_LCD_Screens() ;    // read LCD screen database
                            // load LCD formats
                            //intscDesign.load_LCD_Data_Format() ;

                           // intscDesign.parseSignalTimingFromVirtualController() ;
                            intscDesign.popSignalTimingData() ;
                              
                        } // actionPerformed
                    } // ActionListener
             ) ; // view default controller settings
 
             option_time.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {

                            intscDesign.popSetGlobalTime() ;
                            
                        } // actionPerformed
                    } // ActionListener
             ) ; // load default global time settings  
             
             option_jump2time.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent aev) {

                            intscDesign.popJump2Time() ; 
                            
                        } // actionPerformed
                    } // ActionListener
             ) ; // load default global time settings       
             
            //=============================
            frmActuatedIntersection.invalidate() ;
            frmActuatedIntersection.setVisible(true) ;
        }
        else {  // frmActuatedIntersection already displayed
            frmActuatedIntersection.show() ;
        }
        

    } // popActuatedIntersectionSim
    
   
   
}   // Actuated_Control
