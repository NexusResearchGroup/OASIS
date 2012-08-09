/*
 * intscDrawArea.java
 * Intersection geometry design design class.
 *
 * Created on May 21, 2006, 4:12 PM
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
import java.applet.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import java.awt.print.* ;
import java.net.URL ;
import java.io.* ;
import java.io.FilenameFilter ;
import javax.swing.* ;
import java.util.* ;
import javax.imageio.* ;


public class intscDrawArea extends DoubleBufferedPanel
    implements MouseListener,  MouseMotionListener
{
    toolbar tb;                     // toolbar
    statusbar sb;                   // status bar
    final int grid = 8;             // drawarea grid size
    final int link_length = 300 ;   // pixel
    final int MAX_VEH_SIZE = 40 ;   // max vehicles at a link
    final int STOPBAR_WIDTH = 20 ;  // stop bar width in pixel
    final int DET_PERIOD = 200 ;    // 50 ms
    private final int KEY_CLEAR = 77 ;  // Controller clear key

    // approach vs pahse assignment, assume N-S main street, 8 phases
    // ***** DO NOT CHANGE THE PHASE ASSIGNMENT TO INTERSECTION GEOMETRY *****
    final int[] LEFT_ASSIGN_PH = {7,3,5,1} ;    // E, W, N, S, left turn
    final int[] THRU_ASSIGN_PH = {4,8,2,6} ;    // E, W, N, S, thru
    // ***** DO NOT CHANGE THE PHASE ASSIGNMENT TO INTERSECTION GEOMETRY *****
    
    //vDrawArea vDesign = new vDrawArea();
    Applet myApplet ;       // applet pointer from parent, Geometry_Design

    // variables here
    public SHARED myDB = new SHARED();
//    public Actuated_Control actuatedScreen = new Actuated_Control();
    int toolbarIndex = 0 ;
    mPoint e0, e1 ;	//= DBNull
    boolean line_started = false ;
    boolean curve_started = false ;
    boolean modification_started = false ;

    public Image image ;
    public int imageW = 0, imageH=0 ;
    //Graphics g ;

    mPoint translate = new mPoint(0, 0);
    mPoint scaledxlate = new mPoint(0, 0);
    mPoint translate_delta = new mPoint(0, 0);
    mPoint scaledxlate_delta = new mPoint(0, 0);
    boolean mouseHoldDown = false ;
    float draw_scale  = 1.0f ;
    int dataSelIndex = -1 ;
    boolean controller_configured = false ;
    
    // Horizontal geometry DB
    static final int NUM_LINK = 4 ;   // num of links
    static final int INTSC_SIZE = 1 ;

    // other variables
    int myAlpha = 255 ; // declare a Alpha variable
    // intersectin deometry info
    int intsc_left, intsc_right, intsc_top, intsc_bottom ;
    int intsc_width, intsc_height ;
    Point center ;
    private float [][][] traffic_demand ; // [intsc id][approach id][demand + turning %]
    routeDB[] route_NB ;    // NB route DB
    routeDB[] route_SB ;    // SB route DB
    routeDB[] route_EB ;    // EB route DB
    routeDB[] route_WB ;    // WB route DB
    float[] vHeadway = new float[4] ;         // E, W, N, S, duration (sec) per vehicle 
    float[] timeElapsed = new float[4] ;
    long tLast = 0 ;        // last system time, thread2, vehicle generation
    long tLast1 = 0 ;       // system time for thread3, vehicle collision avoidance
    int[] veh_index = new int[4] ;          // generated vehicle index
    int veh_ref_index = 0 ;
    Random random = new Random(CInt(Math.IEEEremainder(System.currentTimeMillis(), 1967))) ;
    int[] _lane_size = new int[4] ;
    float[] _speed_limit = new float[4] ;
    
    // vehciles
    //Vehicle2D myCar ;
    Vehicle2D[][] myVehicles = new Vehicle2D[4][MAX_VEH_SIZE] ;   // 4 links, 40 vehicles max in each approach
    VehPointer[] myVehRef = new VehPointer[4*MAX_VEH_SIZE] ;
    
    // signal timing control algorithm
    SignalTiming mySignalControl = new SignalTiming() ;
    TimingPanel timeEB = new TimingPanel("EB Timing", "7", "4") ; 
    TimingPanel timeWB = new TimingPanel("WB Timing", "3", "8") ; 
    TimingPanel timeNB = new TimingPanel("NB Timing", "5", "2") ; 
    TimingPanel timeSB = new TimingPanel("SB Timing", "1", "6") ; 
    LTPanel leftturn_EB = new LTPanel("EB Left Turn") ;
    LTPanel leftturn_WB = new LTPanel("WB Left Turn") ;
    LTPanel leftturn_NB = new LTPanel("NB Left Turn") ;
    LTPanel leftturn_SB = new LTPanel("SB Left Turn") ;
    
    // window frame =================
    //public myWindow frameParent = new myWindow();
    myWindow frmAbout ;
    myWindow frame_msgbox, frame_msgboxYesNo ;
    myWindow frame_demand = new myWindow("Intersection Traffic Demand") ;       // volume & turning setting screen
    myWindow frame_controlType = new myWindow("Control Type") ;       // volume & turning setting screen
    myWindow frame_timing = new myWindow("Intersection Signal Timing") ;        // signal timing plan window
    myWindow frame_controller = new myWindow("Intersection Signal Controller") ; // signal controller
    
    Image myControllerImage = null ;
    Image myPhaseAssgn = null ;
    imageCanvas controlPanel = null ;
    
    private Color LCD_BG_COLOR = new Color(207, 216, 35) ;  // LCD screen background color
    private int NUM_LCD_PAGES ;
    private int NUM_LCD_FORMATS ;
    private LCD_PAGE[] myLCD_PAGES = new LCD_PAGE[164] ;    // 162 pages
    private LCD_DATA_ENTRY[] myLCD_Formats = new LCD_DATA_ENTRY[100] ;
    private int lcd_page_index = 0 ;
    private int lcd_row_index = 0 ;
    private int lcd_col_index = 0 ;
    private int lcd_page_ID = 389 ;             // LCD page ID, starting page
    private int lcd_page_ID_last = 0 ;          // LCD last page ID
    private int lcd_menu_layer = 0 ;
    private int lcd_num_key = -99 ;             // LCD numerical key
    private boolean data_entry_mode = false ;   // LCD data entry mode or option select mode
    private boolean next_screen_sel = false ;   // next screen key is pressed
    private boolean next_page_sel = false ;     // next page key is pressed
    private int old_page_index = -1 ;
    private int blink_flag = -1 ;               // blinking toggle flag
    private boolean LCD_READY = false ;
    //private int paint_refresh = 0 ;             // paint refresh 
    
    // ============================
    PrintUtilities hd_pu, vd_pu ;
    
    // Java GUI
    Stroke myDashStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float [] {9}, 0) ;
    Stroke myLineStroke = new BasicStroke(1) ;

    TextField [] txt_demand = new TextField[NUM_LINK] ;
    TextField [] txt_turnL = new TextField[NUM_LINK] ;
    TextField [] txt_turnT = new TextField[NUM_LINK] ;
    TextField [] txt_turnR = new TextField[NUM_LINK] ;
    Label [] lbl_turnTotal = new Label[NUM_LINK] ;
    
    Runnable cursorThread = null ;	// cursor blinking thread, 1/9/07 added
    Runnable runThread0 = null ;    // runs newstatus(0,xx)
    Runnable runThread1 = null ;    // draw area repaint
    Runnable runThread2 = null ;    // vehicle generation
    Runnable runThread3 = null ;    // collisin avoidance
    Runnable runThread4 = null ;    // runs newstatus(1,xx)
    
    Runnable runThreadNBdet = null ;    // NB detection
    Runnable runThreadSBdet = null ;    // SB detection
    Runnable runThreadEBdet = null ;    // EB detection
    Runnable runThreadWBdet = null ;    // WB detection
    Runnable runThreadSTP_NB = null ;    // stop on red light
    Runnable runThreadSTP_SB = null ;    // stop on red light
    Runnable runThreadSTP_EB = null ;    // stop on red light
    Runnable runThreadSTP_WB = null ;    // stop on red light
    
    public Thread tCursor ;         // cursor thread, 1/9/07
    public Thread tSetVol ;
    public Thread tSetTiming ;
    public Thread tRepaint ;        // draw area refresh
    public Thread tGenVeh ;         // vehicle generation thread
    public Thread tCollisionAvoid ; // collision avoidance thread
    public Thread tDetectionNB ;      // detector thread
    public Thread tDetectionSB ;      // detector thread
    public Thread tDetectionEB ;      // detector thread
    public Thread tDetectionWB ;      // detector thread
    public Thread tStopOnRedNB ;      // stop on red light
    public Thread tStopOnRedSB ;      // stop on red light
    public Thread tStopOnRedEB ;      // stop on red light
    public Thread tStopOnRedWB ;      // stop on red light
    public boolean SetVol_flag = false ;
    public boolean SetActuationType_flag = false ;
    public boolean SetTiming_flag = false ;
    public boolean msgBox_flag = false ;
    private String msgBox_title = "" ;
    private String msgBox_body = "" ;
    boolean sim_flag = false ;
    boolean sim_alreadyStarted = false ;
    
    Checkbox fixed, actuated ;
    //PageFormat printPageFormat = new PageFormat() ;
    
    //==================================================================
    // class initialization
    intscDrawArea()
    {   // initialization code here...
        // included in init sub routine
    }
    
    intscDrawArea(toolbar t, statusbar s)
    {
	tb = t;
        sb = s;

	setBackground(Color.lightGray);
	t.parent = this;
        s.parent = this;
    }

    // object initialization
    public void init() {
        addMouseListener(this);
        addMouseMotionListener(this);
        frmAbout = new myWindow();
        
        //mySignalControl.setRing1Seq(1,2,3,4) ;  // ring 1 phase sequence
        //mySignalControl.setRing2Seq(5,6,7,8) ;  // ring 2 phase sequence
        
        // =======================================================================
        // process intersection geometry layout
        // =======================================================================
        Rectangle r = new Rectangle(800, 530) ; // frmGeometryDesign.setSize(800, 600) ;
        
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            intsc_width = (myDB.NB_data.getLaneSize()+1)*myDB.LANE_WIDTH ;
        } else {
            intsc_width = myDB.NB_data.getLaneSize()*myDB.LANE_WIDTH ;
        }
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            intsc_height = (myDB.EB_data.getLaneSize()+1)*myDB.LANE_WIDTH ;
        } else {
            intsc_height = myDB.EB_data.getLaneSize()*myDB.LANE_WIDTH ;
        }
        intsc_left = (r.width-intsc_width)/2 ;
        intsc_right = intsc_left+intsc_width ;
        intsc_top = (r.height-intsc_height)/2 ;
        intsc_bottom = intsc_top+intsc_height ;
        int intsc_centerX = (intsc_left+intsc_right)/2 ;
        int intsc_centerY = (intsc_top+intsc_bottom)/2 ;
        center = new Point((intsc_left+intsc_right)/2,(intsc_top+intsc_bottom)/2) ;

        // =======================================================================
        // intersection stopbar locations
        // =======================================================================
        myDB.EB_data.stopbar_UL = new Point(intsc_left-STOPBAR_WIDTH, intsc_centerY) ;
        myDB.EB_data.stopbar_LR = new Point(intsc_left, intsc_bottom) ;
        myDB.WB_data.stopbar_UL = new Point(intsc_right, intsc_top) ;
        myDB.WB_data.stopbar_LR = new Point(intsc_right+STOPBAR_WIDTH, intsc_centerY) ;
        myDB.NB_data.stopbar_UL = new Point(intsc_centerX, intsc_bottom) ;
        myDB.NB_data.stopbar_LR = new Point(intsc_right, intsc_bottom+STOPBAR_WIDTH) ;
        myDB.SB_data.stopbar_UL = new Point(intsc_left, intsc_top-STOPBAR_WIDTH) ;
        myDB.SB_data.stopbar_LR = new Point(intsc_centerX, intsc_top) ;
        
//        System.out.println("left="+intsc_left) ;
//        System.out.println("right="+intsc_right) ;
//        System.out.println("top="+intsc_top) ;
//        System.out.println("bottom="+intsc_bottom) ;
        
        sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
        //processImage();
         traffic_demand = new float [INTSC_SIZE][NUM_LINK][4] ;
        
        // =======================================================================
        // default demand & turning proportions
        // =======================================================================
        // default demand, East
        //traffic_demand[0][0][0] = 280 ; // veh/h
        traffic_demand[0][0][1] = 10 ;  // 10% left turn
        traffic_demand[0][0][2] = 75 ;  // 75% thru
        traffic_demand[0][0][3] = 15 ;  // 15% right turn
        // default demand, West
        //traffic_demand[0][1][0] = 250 ; // veh/h
        traffic_demand[0][1][1] = 10 ;  // 10% left turn
        traffic_demand[0][1][2] = 75 ;  // 75% thru
        traffic_demand[0][1][3] = 15 ;  // 15% right turn
        // default demand, North
        //traffic_demand[0][2][0] = 450 ; // veh/h
        traffic_demand[0][2][1] = 19 ;  // 10% left turn
        traffic_demand[0][2][2] = 70 ;  // 75% thru
        traffic_demand[0][2][3] = 11 ;  // 15% right turn
        // default demand, South
        //traffic_demand[0][3][0] = 400 ; // veh/h
        traffic_demand[0][3][1] = 18 ;  // 10% left turn
        traffic_demand[0][3][2] = 70 ;  // 75% thru
        traffic_demand[0][3][3] = 12 ;  // 15% right turn
       
        traffic_demand[0][0][0] = myDB.EB_data.getVolume() ;
        traffic_demand[0][1][0] = myDB.WB_data.getVolume() ;
        traffic_demand[0][2][0] = myDB.NB_data.getVolume() ;
        traffic_demand[0][3][0] = myDB.SB_data.getVolume() ;

        // init routeDB
        int nb_routeSize = myDB.NB_data.getLaneSize()/2+2 ;
        int sb_routeSize = myDB.SB_data.getLaneSize()/2+2 ;
        int eb_routeSize = myDB.EB_data.getLaneSize()/2+2 ;
        int wb_routeSize = myDB.WB_data.getLaneSize()/2+2 ;
        route_NB = new routeDB[nb_routeSize] ;
        route_SB = new routeDB[sb_routeSize] ;
        route_EB = new routeDB[eb_routeSize] ;
        route_WB = new routeDB[wb_routeSize] ;
        int i ;
        for (i=0; i<4; i++) {
            switch (i) {
                case 0: 
                    _lane_size[0] = myDB.EB_data.getLaneSize()/2 ;
                    _speed_limit[0] = myDB.EB_data.getSpeedLimit() ;
                    break ;
                case 1: 
                    _lane_size[1] = myDB.WB_data.getLaneSize()/2 ;
                    _speed_limit[1] = myDB.WB_data.getSpeedLimit() ;
                    break ;
                case 2: 
                    _lane_size[2] = myDB.NB_data.getLaneSize()/2 ;
                    _speed_limit[2] = myDB.NB_data.getSpeedLimit() ;
                    break ;
                case 3: 
                    _lane_size[3] = myDB.SB_data.getLaneSize()/2 ;
                    _speed_limit[3] = myDB.SB_data.getSpeedLimit() ;
                    break ;
            }// switch i     
            veh_index[i]=0 ;
        }
        // =======================================================================
        // NB routes
        // =======================================================================
        for (i=0; i<nb_routeSize; i++) {
            // initialize NB route DB
            switch (i) {
                case 0: // left
                    // check exclusive left turn?
                    if (myDB.NB_data.leftTurnLaneExists()) {
                        // left turn lane exists
                        route_NB[0] = new routeDB(8) ;
                        route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length)) ;
                        route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length/2-5) ) ;
                        route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, intsc_bottom+link_length/2-15)) ;
                        route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, intsc_bottom) ) ; // 22.5 deg
                        route_NB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 45 deg
                        route_NB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 67.5 deg
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_NB[0].setRouteDBi(6, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH) ) ; // 
                            route_NB[0].setRouteDBi(7, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH)) ; 
                        } else {
                            route_NB[0].setRouteDBi(6, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                            route_NB[0].setRouteDBi(7, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2)) ; 
                            
                        }
                        // signal strips, NB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[2]-1].sigbar_UL = 
                            new Point(center.x-myDB.LANE_WIDTH/2, intsc_bottom+STOPBAR_WIDTH-5) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[2]-1].sigbar_LR = 
                            new Point(center.x+myDB.LANE_WIDTH/2, intsc_bottom+STOPBAR_WIDTH-2) ;
                        mySignalControl.protected_left[2] = true ;
                        
                        //route_NB[0].print() ; 
                    } else {
                        route_NB[0] = new routeDB(6) ;
                        if (myDB.SB_data.leftTurnLaneExists()) {
                            route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length)) ;
                            route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom) ) ; // 
                            route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 
                            route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                        } else {
                            route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ;
                            route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_bottom) ) ; // 
                            route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 
                            route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; //                          
                        }
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_NB[0].setRouteDBi(4, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH) ) ; // 90 deg
                            route_NB[0].setRouteDBi(5, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH)) ; 
                        } else {
                            route_NB[0].setRouteDBi(4, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 90 deg
                            route_NB[0].setRouteDBi(5, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2)) ; 
                        }                        
                        //route_NB[0].print() ; 
                        // signal strips, NB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[2]-1].sigbar_UL = 
                            new Point(-99, -99) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[2]-1].sigbar_LR = 
                            new Point(-99, -99) ;
                        //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[2])) {
                            // ring 2
                        mySignalControl.setRing2Seq_i(0, 6) ; 
                        //} else {
                            // ring 1
                        //    mySignalControl.setRing1Seq_iThru(LEFT_ASSIGN_PH[2]-1) ;
                        //}
                        mySignalControl.protected_left[2] = false ; ;
                    }
                    break ;
                case 1: // right
                    route_NB[1] = new routeDB(5) ;
                    route_NB[1].setRouteDBi(0, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ;
                    route_NB[1].setRouteDBi(1, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_bottom) ) ; // 
                    route_NB[1].setRouteDBi(2, new Point(intsc_right-myDB.LANE_WIDTH/4, intsc_bottom-myDB.LANE_WIDTH/2) ) ; // 
                    route_NB[1].setRouteDBi(3, new Point(intsc_right, intsc_bottom-myDB.LANE_WIDTH/2) ) ; // 
                    route_NB[1].setRouteDBi(4, new Point(intsc_right+link_length, intsc_bottom-myDB.LANE_WIDTH/2)) ; 
                    
                    //route_NB[1].print() ; 
                    break ;
                default: // straight thru
                    route_NB[i] = new routeDB(2) ;  // linear path
                    if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                        route_NB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_bottom+link_length)) ;
                        //route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_bottom)) ;
                        //route_NB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_top)) ;
                        route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_top-link_length)) ;  
                        // signal strips, NB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_UL = 
                            new Point(center.x+myDB.LANE_WIDTH/2, intsc_bottom+STOPBAR_WIDTH-5) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_LR = 
                            new Point(intsc_right, intsc_bottom+STOPBAR_WIDTH-2) ;
                    } else {
                        route_NB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ;
                        //route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_bottom)) ;
                        //route_NB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_top)) ;
                        route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_top-link_length)) ;                  
                        // signal strips, NB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_UL = 
                            new Point(center.x, intsc_bottom+STOPBAR_WIDTH-5) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_LR = 
                            new Point(intsc_right, intsc_bottom+STOPBAR_WIDTH-2) ;
                    }                    
                    break ;
            }
        }   // end of NB routes
        
        // =======================================================================
        // South bound routes
        // =======================================================================
        for (i=0; i<sb_routeSize; i++) {
            // initialize SB route DB
            switch (i) {
                case 0: // left
                    // check excl left turn?
                    if (myDB.SB_data.leftTurnLaneExists()) {
                        // left turn lane exists
                        route_SB[0] = new routeDB(8) ;
                        route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length)) ;
                        route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length/2+5) ) ;
                        route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, intsc_top-link_length/2+15)) ;
                        route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, intsc_top) ) ; // 22.5 deg
                        route_SB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 45 deg
                        route_SB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 67.5 deg
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_SB[0].setRouteDBi(6, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH) ) ; // 
                            route_SB[0].setRouteDBi(7, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH)) ; 
                        } else {
                            route_SB[0].setRouteDBi(6, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                            route_SB[0].setRouteDBi(7, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2)) ; 
                            
                        }
                        // signal strips, SB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[3]-1].sigbar_UL = 
                            new Point(center.x-myDB.LANE_WIDTH/2, intsc_top-STOPBAR_WIDTH+2) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[3]-1].sigbar_LR = 
                            new Point(center.x+myDB.LANE_WIDTH/2, intsc_top-STOPBAR_WIDTH+5) ;
                        mySignalControl.protected_left[3] = true ;
                        //route_SB[0].print() ; 
                    } else {
                        route_SB[0] = new routeDB(6) ;
                        if (myDB.NB_data.leftTurnLaneExists()) {
                            route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length)) ;
                            route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top) ) ; // 
                            route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 
                            route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                        } else {
                            route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_top-link_length)) ;
                            route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_top) ) ; // 
                            route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; // 
                            route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; //                          
                        }
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_SB[0].setRouteDBi(4, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH) ) ; // 90 deg
                            route_SB[0].setRouteDBi(5, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH)) ; 
                        } else {
                            route_SB[0].setRouteDBi(4, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 90 deg
                            route_SB[0].setRouteDBi(5, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2)) ; 
                        }                        
                        //route_SB[0].print() ; 
                        // signal strips, SB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[3]-1].sigbar_UL = 
                            new Point(-99, -99) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[3]-1].sigbar_LR = 
                            new Point(-99, -99) ;
                        //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[3])) {
                            // ring 2
                        //    mySignalControl.setRing2Seq_iThru(LEFT_ASSIGN_PH[3]-5) ; 
                        //} else {
                            // ring 1
                        mySignalControl.setRing1Seq_i(0, 2) ;
                        //}  
                        mySignalControl.protected_left[3] = false ;
                    }
                    break ;
                case 1: // right
                    route_SB[1] = new routeDB(5) ;
                    route_SB[1].setRouteDBi(0, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_top-link_length)) ;
                    route_SB[1].setRouteDBi(1, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_top) ) ; // 
                    route_SB[1].setRouteDBi(2, new Point(intsc_left+myDB.LANE_WIDTH/4, intsc_top+myDB.LANE_WIDTH/2) ) ; // 
                    route_SB[1].setRouteDBi(3, new Point(intsc_left, intsc_top+myDB.LANE_WIDTH/2) ) ; // 
                    route_SB[1].setRouteDBi(4, new Point(intsc_left-link_length, intsc_top+myDB.LANE_WIDTH/2)) ; 
                    
                    //route_SB[1].print() ; 
                    break ;
                default: // straight thru
                    route_SB[i] = new routeDB(2) ;  // linear path
                    if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                        route_SB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_top-link_length)) ;
                        //route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_top)) ;
                        //route_SB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_bottom)) ;
                        route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_bottom+link_length)) ;  
                        // signal strips, SB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_UL = 
                            new Point(intsc_left, intsc_top-STOPBAR_WIDTH+2) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_LR = 
                            new Point(center.x-myDB.LANE_WIDTH/2, intsc_top-STOPBAR_WIDTH+5) ;
                    } else {
                        route_SB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_top-link_length)) ;
                        //route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_top)) ;
                        //route_SB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_bottom)) ;
                        route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ;                  
                        // signal strips, SB THRU, no medium
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_UL = 
                            new Point(intsc_left, intsc_top-STOPBAR_WIDTH+2) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_LR = 
                            new Point(center.x, intsc_top-STOPBAR_WIDTH+5) ;
                    }
                    break ;
            }
        }   // end of SB routes
        
        // =======================================================================
        // EB routes
        // =======================================================================
        for (i=0; i<eb_routeSize; i++) {
            // initialize EB route DB
            switch (i) {
                case 0: // left
                    // check excl left turn?
                    if (myDB.EB_data.leftTurnLaneExists()) {
                        // left turn lane exists
                        route_EB[0] = new routeDB(8) ;
                        route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH)) ;
                        route_EB[0].setRouteDBi(1, new Point(intsc_left-link_length/2+5, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH) ) ;
                        route_EB[0].setRouteDBi(2, new Point(intsc_left-link_length/2+15, (intsc_top+intsc_bottom)/2)) ;
                        route_EB[0].setRouteDBi(3, new Point(intsc_left, (intsc_top+intsc_bottom)/2 ) ) ; // 
                        route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2)) ; // 
                        route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH)) ; // 
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_EB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top) ) ; // 
                            route_EB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top-link_length)) ; 
                        } else {
                            route_EB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top) ) ; // 
                            route_EB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top-link_length)) ; 
                                      
                        }
                        // signal strips, EB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[0]-1].sigbar_UL = 
                            new Point(intsc_left-STOPBAR_WIDTH+2, center.y-myDB.LANE_WIDTH/2) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[0]-1].sigbar_LR = 
                            new Point(intsc_left-STOPBAR_WIDTH+5, center.y+myDB.LANE_WIDTH/2) ;
                        mySignalControl.protected_left[0] = true ;
                        //route_EB[0].print() ; 
                    } else {
                        route_EB[0] = new routeDB(6) ;
                        if (myDB.WB_data.leftTurnLaneExists()) {
                            route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH)) ;
                            route_EB[0].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH) ) ; // 
                            route_EB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                            route_EB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                        } else {
                            route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2 )) ;
                            route_EB[0].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                            route_EB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                            route_EB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; //                          
                        }
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top) ) ; // 90 deg
                            route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top-link_length )) ; 
                        } else {
                            route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top) ) ; // 90 deg
                            route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top-link_length )) ; 
                        }                        
                        //route_EB[0].print() ; 
                        // signal strips, EB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[0]-1].sigbar_UL = 
                            new Point(-99, -99) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[0]-1].sigbar_LR = 
                            new Point(-99, -99) ;
                        //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[0])) {
                            // ring 2
                            mySignalControl.setRing2Seq_i(2, 8) ; 
                        //} else {
                            // ring 1
                        //    mySignalControl.setRing1Seq_iThru(LEFT_ASSIGN_PH[0]-1) ;
                        //}
                        mySignalControl.protected_left[0] = false ;
                    }
                    break ;
                case 1: // right
                    route_EB[1] = new routeDB(5) ;
                    route_EB[1].setRouteDBi(0, new Point(intsc_left-link_length, intsc_bottom-myDB.LANE_WIDTH/2)) ;
                    route_EB[1].setRouteDBi(1, new Point(intsc_left, intsc_bottom-myDB.LANE_WIDTH/2) ) ; // 
                    route_EB[1].setRouteDBi(2, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom-myDB.LANE_WIDTH/4) ) ; // 
                    route_EB[1].setRouteDBi(3, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom) ) ; // 
                    route_EB[1].setRouteDBi(4, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ; 
                    
                    //route_EB[1].print() ; 
                    break ;
                default: // straight thru
                    route_EB[i] = new routeDB(2) ;  // linear path
                    if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                        route_EB[i].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH)) ;
                        //route_EB[i].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH)) ;
                        //route_EB[i].setRouteDBi(2, new Point(intsc_right, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH)) ;
                        route_EB[i].setRouteDBi(1, new Point(intsc_right+link_length+50, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH)) ;  
                        // signal strips, EB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_UL = 
                            new Point(intsc_left-STOPBAR_WIDTH+2, center.y+myDB.LANE_WIDTH/2) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_LR = 
                            new Point(intsc_left-STOPBAR_WIDTH+5, intsc_bottom) ;
                    } else {
                        route_EB[i].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2)) ;
                        //route_EB[i].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2)) ;
                        //route_EB[i].setRouteDBi(2, new Point(intsc_right, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2)) ;
                        route_EB[i].setRouteDBi(1, new Point(intsc_right+link_length+50, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2)) ;                  
                        // signal strips, EB THRU, no medium
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_UL = 
                            new Point(intsc_left-STOPBAR_WIDTH+2, center.y) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_LR = 
                            new Point(intsc_left-STOPBAR_WIDTH+5, intsc_bottom) ;
                    }
                    break ;
            }
        }   // end of EB routes
        
        // =======================================================================
        // WB routes
        // =======================================================================
        for (i=0; i<wb_routeSize; i++) {
            // initialize WB route DB
            switch (i) {
                case 0: // left
                    // check excl left turn?
                    if (myDB.WB_data.leftTurnLaneExists()) {
                        // left turn lane exists
                        route_WB[0] = new routeDB(8) ;
                        route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH)) ;
                        route_WB[0].setRouteDBi(1, new Point(intsc_right+link_length/2-5, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH) ) ;
                        route_WB[0].setRouteDBi(2, new Point(intsc_right+link_length/2-15, (intsc_top+intsc_bottom)/2)) ;
                        route_WB[0].setRouteDBi(3, new Point(intsc_right, (intsc_top+intsc_bottom)/2 ) ) ; // 
                        route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2)) ; // 
                        route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH)) ; // 
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_WB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom) ) ; // 
                            route_WB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom+link_length)) ; 
                        } else {
                            route_WB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom) ) ; // 
                            route_WB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom+link_length)) ; 
                                      
                        }
                        // signal strips, WB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[1]-1].sigbar_UL = 
                            new Point(intsc_right+STOPBAR_WIDTH-5, center.y-myDB.LANE_WIDTH/2) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[1]-1].sigbar_LR = 
                            new Point(intsc_right+STOPBAR_WIDTH-2, center.y+myDB.LANE_WIDTH/2) ;
                        mySignalControl.protected_left[1] = true ;
                        //route_WB[0].print() ; 
                    } else {
                        route_WB[0] = new routeDB(6) ;
                        if (myDB.EB_data.leftTurnLaneExists()) {
                            route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH)) ;
                            route_WB[0].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH) ) ; // 
                            route_WB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                            route_WB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2) ) ; // 
                        } else {
                            route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2 )) ;
                            route_WB[0].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                            route_WB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2) ) ; // 
                            route_WB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2) ) ; //                          
                        }
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom) ) ; // 90 deg
                            route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom+link_length )) ; 
                        } else {
                            route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom) ) ; // 90 deg
                            route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom+link_length )) ; 
                        }                        
                        //route_WB[0].print() ; 
                        // signal strips, WB LF
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[1]-1].sigbar_UL = 
                            new Point(-99, -99) ;
                        mySignalControl.myPhases[LEFT_ASSIGN_PH[1]-1].sigbar_LR = 
                            new Point(-99, -99) ;                        
                        //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[1])) {
                            // ring 2
                        //    mySignalControl.setRing2Seq_iThru(LEFT_ASSIGN_PH[1]-5) ; 
                        //} else {
                            // ring 1
                            mySignalControl.setRing1Seq_i(2, 4) ;
                        //}
                        mySignalControl.protected_left[1] = false ;
                    }
                    break ;
                case 1: // right
                    route_WB[1] = new routeDB(5) ;
                    route_WB[1].setRouteDBi(0, new Point(intsc_right+link_length, intsc_top+myDB.LANE_WIDTH/2)) ;
                    route_WB[1].setRouteDBi(1, new Point(intsc_right, intsc_top+myDB.LANE_WIDTH/2) ) ; // 
                    route_WB[1].setRouteDBi(2, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top+myDB.LANE_WIDTH/4) ) ; // 
                    route_WB[1].setRouteDBi(3, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top) ) ; // 
                    route_WB[1].setRouteDBi(4, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top-link_length)) ; 
                    
                    //route_WB[1].print() ; 
                    break ;
                default: // straight thru
                    route_WB[i] = new routeDB(2) ;  // linear path
                    if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                        route_WB[i].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH)) ;
                        //route_WB[i].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH)) ;
                        //route_WB[i].setRouteDBi(2, new Point(intsc_left, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH)) ;
                        route_WB[i].setRouteDBi(1, new Point(intsc_left-link_length-50, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH)) ;  
                        // signal strips, WB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_UL = 
                            new Point(intsc_right+STOPBAR_WIDTH-5, intsc_top) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_LR = 
                            new Point(intsc_right+STOPBAR_WIDTH-2, center.y-myDB.LANE_WIDTH/2) ;
                    } else {
                        route_WB[i].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2)) ;
                        //route_WB[i].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2)) ;
                        //route_WB[i].setRouteDBi(2, new Point(intsc_left, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2)) ;
                        route_WB[i].setRouteDBi(1, new Point(intsc_left-link_length-50, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2)) ;                  
                        // signal strips, WB THRU, no medium
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_UL = 
                            new Point(intsc_right+STOPBAR_WIDTH-5, intsc_top) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_LR = 
                            new Point(intsc_right+STOPBAR_WIDTH-2, center.y) ;
                    }
                    break ;
            }
        }   // end of WB routes
        
        // create a new vehicle
        //myCar = new Vehicle2D(0, 1, route_EB[1], 30f) ; // dir, route id, route, speed
        
        // =======================================================================
        // loop detector layout
        // =======================================================================
        int N ; 
        int ij=0 ;
        // loop detector
        // EB
        N = myDB.EB_data.getLaneSize()/2 ;
        if (myDB.EB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.EB_data.detectorUL[0] = new Point(intsc_left-myDB.EB_data.detector_dist, center.y-myDB.LOOP_DET_WIDTH/2) ;
        } else {
            // left turn does not exist
            myDB.EB_data.detectorUL[0] = new Point(-99, -99) ;
        }
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.EB_data.detectorUL[ij] = new Point(intsc_left-myDB.EB_data.detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.EB_data.detectorUL[ij] = new Point(intsc_left-myDB.EB_data.detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
            }            
        }
        // WB
        N = myDB.WB_data.getLaneSize()/2 ;
        if (myDB.WB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.WB_data.detectorUL[0] = new Point(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2) ;
        } else {
            // left turn does not exist
            myDB.WB_data.detectorUL[0] = new Point(-99, -99) ;
        }
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.WB_data.detectorUL[ij] = new Point(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.WB_data.detectorUL[ij] = new Point(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
            }            
        }        
        // NB
        N = myDB.NB_data.getLaneSize()/2 ;
        if (myDB.NB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.NB_data.detectorUL[0] = new Point(center.x-myDB.LOOP_DET_WIDTH/2, intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH) ;
        } else {
            // left turn does not exist
            myDB.NB_data.detectorUL[0] = new Point(-99, -99) ;
        }
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.NB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.NB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH) ;
            }            
        }   
        // SB
        N = myDB.SB_data.getLaneSize()/2 ;
        if (myDB.SB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.SB_data.detectorUL[0] = new Point(center.x-myDB.LOOP_DET_WIDTH/2, intsc_top-myDB.SB_data.detector_dist) ;
        } else {
            // left turn does not exist
            myDB.SB_data.detectorUL[0] = new Point(-99, -99) ;
        }
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.SB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.detector_dist) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.SB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 -(ij-1)*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.detector_dist) ;
            }            
        }   
        
        // =======================================================================
        // cursor blinking thread
        // =====================================================================
        cursorThread = new Runnable() {
            public void run() {
                while (true) {
                    if (frame_controller==null){
                        tCursor.yield();
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;
                    } else {    // not null
                        if (frame_controller.isShowing()==false){
                            // not showing
                            tCursor.yield();
                            try {Thread.sleep(1000) ;}
                            catch (InterruptedException ie) {} ;
                        } else if (LCD_READY) {
                            // check if data entry
                            int idx = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
                            if (idx>=0) {
                                // in data entry mode
                                myLCD_Formats[idx].cursorInit() ;   // set starting pos
                                blinkCursor(idx) ;
                                try {Thread.sleep(300) ;}
                                catch (InterruptedException ie) {} ;
                            }   // if idx
                        }   // if showing?
                    }   // if
                }   // while
             }   // void run
        } ; // runThread 0
        tCursor = new Thread(cursorThread, "cursorBlinking") ;
        tCursor.start() ;

        // =======================================================================
        // bring traffic volume to top display thread
        // =====================================================================
        runThread0 = new Runnable() {
            public void run() {
                while (true) {
                    if (SetVol_flag){
                        newstatus(0, " Traffic Volume");
                        SetVol_flag = false ;
                    } else if (SetActuationType_flag){
                        newstatus(1, " Actuation Type");
                        SetActuationType_flag = false ;
                    } else if (msgBox_flag) {
                        popMessageBox(msgBox_title, msgBox_body) ;
                        msgBox_flag = false ;
                    } else {
                        tSetVol.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }
                }
             }   // void run
        } ; // runThread 0
        tSetVol = new Thread(runThread0, "Settings") ;
        tSetVol.start() ;

        //======================================================================
        // refresh drawing area to update vehicle location
        // =====================================================================
        runThread1 = new Runnable() {
            public void run() {
                while (true) {
                    repaint() ;
                    if (sim_flag) {
                        //tRepaint.yield() ;
                        try {Thread.sleep(DET_PERIOD-1) ;}   // <= DET_PERIOD
                        catch (InterruptedException ie) {} ;
                    } else {
                        tRepaint.yield() ;
                        try {Thread.sleep(200) ;}   
                        catch (InterruptedException ie) {} ;                        
                    }   // end if sim_flag
                }   // while loop
             }   // void run
        } ; // runThread 1
        tRepaint = new Thread(runThread1, "Repaint") ;
        tRepaint.start() ;
        
        //======================================================================
        // vehicle generation thread, based on input demands
        // =====================================================================
        runThread2 = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        long tNow = System.currentTimeMillis() ;
                        float timeElapsedSec = ( tNow - tLast)/1000f ;   // sec
                        for (int i=0; i<4; i++) {
                            timeElapsed[i] += timeElapsedSec ;
                            // headway variation +/- 1 sec
                            vHeadway[i] = 3600f / traffic_demand[0][i][0] - 1f + 2f*random.nextFloat();
                            //System.out.println("timeElapsed="+timeElapsed[i]+",headway="+vHeadway[i]) ;
                            if (timeElapsed[i] >= vHeadway[i]) {
                                timeElapsed[i] -= vHeadway[i] ;
                                // generate a random value
                                float frac = random.nextFloat()*100f ;  // turning proportion in percentage
                                int route_id ;
                                // System.out.println("frac="+frac+", demand="+traffic_demand[0][i][1]) ;
                                // determine vehicle turning left, right or going thru
                                if (frac<=traffic_demand[0][i][1]) {
                                    // left turn
                                    route_id = 0 ;
                                } else if (frac>(traffic_demand[0][i][1]+traffic_demand[0][i][2])) {
                                    // right
                                    route_id = 1 ;
                                } else {
                                    // thru
                                    route_id = 2 + new Double(Math.floor(random.nextFloat()*_lane_size[i])).intValue() ;
                                }
                                //System.out.println("route_id="+route_id) ;
                                // generate speed variation, +/- 5 MPH
                                float _spd = _speed_limit[i] - 5.0f + random.nextFloat()*10f ;
                                //System.out.println("i="+i+",speed limit="+_speed_limit[i]+", lane_size="+_lane_size[i]) ;
                                // generate a vehicle
                                //System.out.println("link="+i+",vid="+veh_index[i]+",speed="+_spd) ;
                                switch (i) {
                                    case 0: // EB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(i, route_id, route_EB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 1:     // WB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(i, route_id, route_WB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 2:     // NB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(i, route_id, route_NB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 3: // SB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(i, route_id, route_SB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                
                                }
                                Color myColor ;
                                int r, g, b ;
                                r = random.nextInt(200) ;
                                g = random.nextInt(200) ;
                                b = random.nextInt(200) ;
                                if (nearby(r, 192, 5) & nearby(g, 192, 5) & nearby(b, 192, 5)) {
                                    myColor = Color.black ;
                                } else {
                                    myColor = new Color(r, g, b) ;
                                }
                                myVehicles[i][veh_index[i]].setColor(myColor) ;
                   //             myVehicles[i][veh_index[i]].vehicleStart() ;
                                
                                if (veh_index[i] >= MAX_VEH_SIZE-1) {
                                    veh_index[i] = 0 ;
                                } else {
                                    veh_index[i] = veh_index[i]+1 ;
                                }
                                
                            }   // if exceed headway
                        }   // for
                        tLast = tNow ;
                        //tGenVeh.yield() ;
                    }   // if sim_flag
                    tGenVeh.yield() ;
                    try {Thread.sleep(1000) ;}
                    catch (InterruptedException ie) {} ;
                }   // while
             }   // void run
        } ; // runThread 2
        tGenVeh = new Thread(runThread2, "VehicleGeneration") ;
        tGenVeh.start() ;
        
        // =====================================================================
        // vehicles collision avoidance thread
        // =====================================================================
        runThread3 = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        veh_ref_index = 0 ;
                        long tNow = System.currentTimeMillis() ;
                        float timeElapsedSec = ( tNow - tLast1)/1000f ;   // sec
                        //System.out.println("Time Elapsed (sec)= "+timeElapsedSec) ; around 0.25
                        // update vehicle location
                        for (int i=0; i<4; i++) {   // 4 approaches
                            // each approach 
                            for (int j=0; j<MAX_VEH_SIZE; j++) {
                                if ( myVehicles[i][j] != null ) {
                                    // store veh info to a buffer for car-following & collision avoidance analysis
                                    myVehRef[veh_ref_index] = new VehPointer(i, j) ; 
                                    veh_ref_index++ ; 
                                }   // end if not null
                            }   // for j
                        }   // for i
                        
                        for (int m=0; m<veh_ref_index; m++) {
                            int mi = myVehRef[m].approach ;
                            int mj = myVehRef[m].veh_index ;
                            int m_rid = myVehicles[mi][mj].getRouteID() ;
                            myVehicles[mi][mj].saveCurRouteIndex() ;
                            myVehicles[mi][mj].forecastNextPosition(timeElapsedSec) ;
                            Point pos_m = myVehicles[mi][mj].getNextForecastedPos() ;
                            Point pos_mc = myVehicles[mi][mj].getPosition() ;
                            // proposed travel distance
                            double forecasted_dist = calcDist(pos_m, pos_mc) ;
                            //boolean veh_update_flag = true ;
                            for (int n=m+1; n<veh_ref_index; n++){
                                int ni = myVehRef[n].approach ;
                                int nj = myVehRef[n].veh_index ;
                                int n_rid = myVehicles[ni][nj].getRouteID() ;
                                Point pos_n = myVehicles[ni][nj].getPosition() ;
                                double veh_dist = calcDist(pos_m, pos_n) ;   // veh distance
                                float min_dist = 0.5f*(myVehicles[mi][mj].length+myVehicles[ni][nj].length)+2f ;
                                if (veh_dist<=min_dist) {
                                    if ((mi==ni) && (n_rid==m_rid)) { 
                                        // veh on the same route & same link
                                        float ratio = new Double(1.0-min_dist/forecasted_dist).floatValue() ;
                                        myVehicles[mi][mj].restorePrevRouteIndex() ;
                                        myVehicles[mi][mj].forecastNextPosition(ratio*timeElapsedSec) ;
                                        pos_m = myVehicles[mi][mj].getNextForecastedPos() ;
                                        //veh_update_flag = false ;
                                    }
                                }   // if veh_dist
                            }   // for n
                            
                            //if (veh_update_flag) {  // if OK to update new position
                                // check signal stop -------------------------------
                                Point stop_UL=new Point(0,0) ;
                                Point stop_LR=new Point(0,0) ; ;
                                Point stopbar_p1=new Point(0,0) ;
                                Point stopbar_p2=new Point(0,0) ;
                                switch (mi) {
                                    case 0: // EB
                                        stop_UL = myDB.EB_data.stopbar_UL ;
                                        stop_LR = myDB.EB_data.stopbar_LR ;
                                        stopbar_p1 = stop_UL ;
                                        stopbar_p2 = new Point(
                                            myDB.EB_data.stopbar_UL.x,
                                            myDB.EB_data.stopbar_LR.y) ;
                                        break ;
                                    case 1: //WB
                                        stop_UL = myDB.WB_data.stopbar_UL ;
                                        stop_LR = myDB.WB_data.stopbar_LR ;
                                        stopbar_p1 = new Point(
                                            myDB.WB_data.stopbar_LR.x,
                                            myDB.WB_data.stopbar_UL.y) ;
                                        stopbar_p2 = stop_LR ;
                                        break ;
                                    case 2: //NB
                                        stop_UL = myDB.NB_data.stopbar_UL ;
                                        stop_LR = myDB.NB_data.stopbar_LR ;
                                        stopbar_p1 = new Point(
                                            myDB.NB_data.stopbar_UL.x,
                                            myDB.NB_data.stopbar_LR.y) ;
                                        stopbar_p2 = stop_LR ;
                                        break ;
                                    case 3: //SB
                                        stop_UL = myDB.SB_data.stopbar_UL ;
                                        stop_LR = myDB.SB_data.stopbar_LR ;
                                        stopbar_p1 = stop_UL ;
                                        stopbar_p2 = new Point(
                                            myDB.SB_data.stopbar_LR.x,
                                            myDB.SB_data.stopbar_UL.y) ;
                                        break ;
                                }   // end switch

                                int phase_id = 0 ;
                                if (myVehicles[mi][mj].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[mi] ;
                                } else {
                                    // thru or right turn traffic
                                    phase_id = THRU_ASSIGN_PH[mi] ;
                                }        
                                // distance to stop bar
                                double dist = calcDist2Line(pos_m, stopbar_p1, stopbar_p2) ;
                                // check if veh approaching stop bar
                                mPointF v1 = myVehicles[mi][mj].headingVector() ;
                                mPointF vs = new mPointF(stop_UL.x-pos_m.x, stop_UL.y-pos_m.y) ;
                                float dotVal = vs.dot(v1) ;
                                if (dotVal>=0) { // approach stop bar
                                    // check if green 
                                    if (!mySignalControl.phaseStatus[phase_id-1]) {
                                        // red or yellow, stop
                                        System.out.println("phase "+phase_id+", red/yel") ;
                                        if (forecasted_dist>dist) {
                                            // will go over
                                            float ratio = new Double(dist/forecasted_dist).floatValue() ;
                                            myVehicles[mi][mj].restorePrevRouteIndex() ;
                                            myVehicles[mi][mj].forecastNextPosition(ratio*timeElapsedSec) ;
                                        }
                                    }
                                }   // enf if (dotVal>=0)
                                myVehicles[mi][mj].updatePos() ;
/*
                                if (mySignalControl.phaseStatus[phase_id-1]) {
                                    // green
                                    float time_remain = 0 ;
                                    // get phase timer count
                                    if (phase_id<=4) {
                                        // ring 1 timer
                                        if (mySignalControl.control_type==0) {
                                            time_remain = mySignalControl.fixedTimer1.greenTimer.getCount() ;
                                        } else if (mySignalControl.control_type==1) {
                                            time_remain = mySignalControl.actuatedTimer1.greenTimer.getCount() ;
                                        }
                                    } else {
                                        // ring 2 timer
                                        if (mySignalControl.control_type==0) {
                                            time_remain = mySignalControl.fixedTimer2.greenTimer.getCount() ;
                                        } else if (mySignalControl.control_type==1) {
                                            time_remain= mySignalControl.actuatedTimer2.greenTimer.getCount() ;
                                        }
                                    }
                                    //System.out.println("EB dotVal="+dotVal) ;

                                } else {
                                    // red signal
                                    if (stop_flag) {        // signal red
                                        myVehicles[i][j].stop() ;
                                    } else if (dist<=myVehicles[i][j].length/4) {
                                        // red
                                        myVehicles[i][j].slowdown(0.65f) ;
                                    }   // stop_flag
                                } // if state
*/                                
                            //}   // end if (veh_update_flag)
                            /*
                            if (veh_update_flag) {
                                // OK to update m position
                               myVehicles[mi][mj].updatePos() ;
                            } else {
                                // restore, do not update due to collisiion
                                myVehicles[mi][mj].restorePrevRouteIndex() ;
                            }
                             */
                        }   // for m
                        
                        // process veh collision avoidance calculation
/*                        for (int k=0; k<veh_ref_index-1; k++) {
                            int i1 = myVehRef[k].approach ;
                            int j1 = myVehRef[k].veh_index ;
                            float speed_v1 = myVehicles[i1][j1].getSpeed() ;
                            Point pos1 = myVehicles[i1][j1].getPosition() ;
                            int routeID_v1 = myVehicles[i1][j1].getRouteID() ;
                            mPointF headingV1 = myVehicles[i1][j1].headingVector() ;
                            
                            double min_dist1 = 9999 ;   // minimum head-on distance
                            //double min_headway = 9999 ;
                            Point min_dist1_index = new Point(-1, -1) ;
                            //Point min_headway_index = new Point(-1, -1) ;
                            float min_dist1_speed_v2 = 0f;
                            double min_gap = 0 ;
                            for (int m=k+1; m<veh_ref_index; m++) {
                                int i2 = myVehRef[m].approach ;
                                int j2 = myVehRef[m].veh_index ;
                                
                                Point pos2 = myVehicles[i2][j2].getPosition() ;
                                mPointF veh_vector = vector(pos1, pos2) ;
                                int routeID_v2 = myVehicles[i2][j2].getRouteID() ;
                                mPointF headingV2 = myVehicles[i2][j2].headingVector() ;
                                float veh_vec_dot = headingV1.dot(headingV2) ;  // dot of 2 veh heading
                                float veh_loc_dot = veh_vector.dot(headingV1) ; // veh relative location, >0 if V2 in front of V1
                                float speed_v2 = myVehicles[i2][j2].getSpeed() ;
                                double veh_dist1 = calcDist(pos1, pos2) ;   // veh distance
                                // vehicle distance at t+1 sec
                                double veh_dist2 = calcDistf(
                                                        pos1.x+speed_v1*headingV1.X,  
                                                        pos1.y+speed_v1*headingV1.Y,
                                                        pos2.x+speed_v2*headingV2.X, 
                                                        pos2.y+speed_v2*headingV2.Y) ;
                                // check if vehicle is stopped
                                if ( (min_dist1>veh_dist1) && (veh_vec_dot<=-0.01f) && (veh_loc_dot>0.5f) ) {
                                    // veh stopped, check head on?
                                    min_dist1 = veh_dist1 ;
                                    min_dist1_index = new Point(i2, j2) ;
                                    min_dist1_speed_v2 = speed_v2 ;
                                    if (speed_v1>speed_v2) {
                                        min_gap = veh_dist1/speed_v1 ;  // sec
                                    } else {
                                        min_gap = veh_dist1/speed_v2 ;  // sec
                                    }
                                //System.out.println("min gap = "+min_gap) ;
                                }
                                if (veh_dist1 < 60.0f) {   // ~30 ft  
                                    if ( (veh_dist2<veh_dist1) || speed_v1 <0.01f || speed_v2<.01f) {
                                        // veh getting closer
                                        if ((veh_vec_dot>=0.99) && Math.abs(veh_loc_dot)>=0.99) {
                                            //System.out.println("d1="+veh_dist1+",d2="+veh_dist2) ;
                                            
                                            // veh moving in about the same direction & same lane
                                            //if ((routeID_v1==routeID_v2) && 
                                            if((veh_dist1 <= 0.5f*(myVehicles[i1][j1].length+myVehicles[i2][j2].length)+5.0f)) {   // jam or stop
                                                // determine which veh is in front
                                                if (veh_loc_dot>0) {
                                                    // vehicle 2 in front
                                                     myVehicles[i1][j1].stop() ;
                                                } else {
                                                    // vehicle 1 in front
                                                    myVehicles[i2][j2].stop() ; 
                                                }
                                            } else { // distance greater than 1 vehicle length
                                                // determine which veh is in front
                                                if (veh_loc_dot>0) {
                                                    // vehicle 2 in front, slow down veh 1
                                                     myVehicles[i1][j1].slowdown() ; //, myVehicles[i2][j2].getPosition()) ; 
                                                } else {
                                                    // vehicle 1 in front, slow down veh 2
                                                    myVehicles[i2][j2].slowdown() ; //, myVehicles[i1][j1].getPosition()) ;   
                                                }
                                            }
                                        } else if ((veh_vec_dot<=-0.01f) && (veh_loc_dot>0.5f)) {
                                            // veh moving in about the opposit direction, heads-on
                                            if (routeID_v1<routeID_v2) {
                                                if (myVehicles[i2][j2].getSpeed()>0f) {
                                                    myVehicles[i1][j1].stop() ;
                                                } else {
                                                    myVehicles[i1][j1].speedup() ;
                                                }
                                            } else if (routeID_v1>routeID_v2) {
                                                if (myVehicles[i1][j1].getSpeed()>0f) {
                                                    myVehicles[i2][j2].stop() ;
                                                } else {
                                                    myVehicles[i2][j2].speedup() ;
                                                }
                                            }
                                        //} else {
                                            // about perpendicular movement
                                        }   // compare heading vector dot value
                                    }   // dist2 < dist1
                                }  // in vicinity ?
                            }   // next m
                            
                            if (min_gap>6 && min_dist1_index.x>=0) {
                                int i2 = min_dist1_index.x ;
                                int j2 = min_dist1_index.y ;
                                if (myVehicles[i1][j1].getSpeed()<0.01f) {
                                    myVehicles[i1][j1].speed_control = 1.0f ;
                                } else if (myVehicles[i2][j2].getSpeed()<0.01f) {
                                    myVehicles[i2][j2].speed_control = 1.0f ;
                                }
                            }   // min distance exists with head-on scenario
*/                            
                            /*
                            // check if headway is greater than 60 pixels       
                            int i2 = min_headway_index.x ;
                            int j2 = min_headway_index.y ;
                            if (min_headway>60f && min_headway_index.x>=0) {
                                int phase_id = 0 ;
                                if (myVehicles[i2][j2].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[i2] ;
                                } else {
                                    // thru or right turn traffic
                                    phase_id = THRU_ASSIGN_PH[i2] ;
                                } 
                                if (mySignalControl.phaseStatus[phase_id-1]) {
                                    myVehicles[i2][j2].speedup() ;
                                } else {    // red
                                }
                            }        //if headway is greater than 60 pixels
                            else {
                                myVehicles[i2][j2].slowdown(0.45f) ;
                            }
                            */           
//                        }   // next k
                        tLast1 = tNow ;
                        tCollisionAvoid.yield() ;
                        try {Thread.sleep(100) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tCollisionAvoid.yield() ;
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread 3
        tCollisionAvoid = new Thread(runThread3, "Simulation") ;
        tCollisionAvoid.setPriority(Thread.NORM_PRIORITY) ;   
        tCollisionAvoid.start() ;
        
        // =======================================================================
        // bring signal timing to top display thread
        // =====================================================================
        runThread4 = new Runnable() {
            public void run() {
                while (true) {
                    if (SetTiming_flag){
                        newstatus(2, " Signal Controller");
                        SetTiming_flag = false ; 
                    } else {
                        tSetTiming.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }
                }
             }   // void run
        } ; // runThread 4
        tSetTiming = new Thread(runThread4, "ControllerSettings") ;
        tSetTiming.start() ;
        
        // =====================================================================
        // vehicles stop on red light thread
        // =======================================================================
        runThreadSTP_EB = new Runnable() {
            public void run() {
                Point stopbar_p2 = new Point(
                    myDB.EB_data.stopbar_UL.x,
                    myDB.EB_data.stopbar_LR.y) ;
                while (true) {
                    int i=0 ;   // EB
                    if (sim_flag) {
                        //double min_dist2stopbar = 9999 ;
                        //int min_dist_index = -1 ;
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            if ( myVehicles[i][j] != null ) {
                                // find the leading veh in front of stopbar
                                /*----------- 
                                double dist = calcDist2Line(myVehicles[i][j].getFrontBumperCenter(), myDB.EB_data.stopbar_UL, stopbar_p2) ;
                                if (dist<min_dist2stopbar) {
                                    mPointF v1 = myVehicles[i][j].headingVector() ;
                                    Point pos_veh = myVehicles[i][j].getPosition() ;
                                    mPointF vs = new mPointF(myDB.EB_data.stopbar_UL.x-pos_veh.x, myDB.EB_data.stopbar_UL.y-pos_veh.y) ;
                                    float dotVal = vs.dot(v1) ;
                                    if (dotVal>=0) { // approach stop bar
                                        min_dist2stopbar = dist ;
                                        min_dist_index = j ;
                                    }   // if dotVal
                                }   // if dist???
                            }   // if veh != null
                        }   // for j
                        // check phase state
                        int phase_id = 0 ;
                        if (myVehicles[i][min_dist_index].getRouteID()==0 ) {
                            // left turn
                            phase_id = LEFT_ASSIGN_PH[i] ;
                        } else {
                            // thru or right turn traffic
                            phase_id = THRU_ASSIGN_PH[i] ;
                        }        
                        */
                               
                                // -----------------
                                // check if vehicle on RED bar
                                boolean stop_flag = false ;
                                if (isOverStopBar(myDB.EB_data.stopbar_UL,
                                    myDB.EB_data.stopbar_LR, 
                                    myVehicles[i][j].getPosition(),
                                    myVehicles[i][j].getFrontBumperCenter(),
                                    myVehicles[i][j].getRearBumperCenter()
                                    )) {
                                        stop_flag = true ;
                                }   // if over stop bar
                                //Point stopbar_p2 = new Point(
                                //    myDB.EB_data.stopbar_UL.x,
                                //    myDB.EB_data.stopbar_LR.y) ;

                                int phase_id = 0 ;
                                if (myVehicles[i][j].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[i] ;
                                } else {
                                    // thru or right turn traffic
                                    phase_id = THRU_ASSIGN_PH[i] ;
                                }        
                                double dist = calcDist2Line(myVehicles[i][j].getFrontBumperCenter(), myDB.EB_data.stopbar_UL, stopbar_p2) ;
                          /*      if (stop_flag) {        // over stop bar
                                    // compare with signal phase state
                                    if (!mySignalControl.phaseStatus[phase_id-1]) {
                                        // red light
                                        myVehicles[i][j].stop() ;
                                    }  else {
                                        // green phase
                                        myVehicles[i][j].speedup() ;
                                    }   // if phase_id     
                                } // if stop_flag
                                else {  */
                                     if (mySignalControl.phaseStatus[phase_id-1]) {
                                         // green
                                         // check if veh approaching stop bar
                                         mPointF v1 = myVehicles[i][j].headingVector() ;
                                         Point pos_veh = myVehicles[i][j].getPosition() ;
                                         mPointF vs = new mPointF(myDB.EB_data.stopbar_UL.x-pos_veh.x, myDB.EB_data.stopbar_UL.y-pos_veh.y) ;
                                         float dotVal = vs.dot(v1) ;
                                         //System.out.println("EB dotVal="+dotVal) ;
                                         
                                         if (dotVal>=0) { // approach stop bar
                                             float time_count = 0 ;
                                             // get phase timer count
                                             if (phase_id<=4) {
                                                 // ring 1 timer
                                                 time_count = mySignalControl.fixedTimer1.greenTimer.getCount() ;
                                             } else {
                                                 // ring 2 timer
                                                 time_count = mySignalControl.fixedTimer2.greenTimer.getCount() ;
                                             }
                                       //      System.out.println("EB: ph="+phase_id+", time="+time_count+", dist="+dist+", stop_flag="+myVehicles[i][j].veh_stop_flag) ;
                                             // check if green is ending
                                             if ((time_count<=1.5f)&&(dist<20)) {
                                                 myVehicles[i][j].slowdown() ;
                                             } else {
                                                myVehicles[i][j].speedup() ;
                                             }
                                             // check if veh speed =0
                                             if (myVehicles[i][j].getSpeed()<=0.1f) {
                                                 int k = myVehicles[i][j].getRouteID() ;
                                                 System.out.println("VID:("+i+","+j+","+k+")") ;
                                             }
                                             
                                         } else {
                                             // veh already pass stop bar 
                                             myVehicles[i][j].speedup() ;
                                         }
                                     } else {
                                         // red signal
                                         if (stop_flag) {        // signal red
                                             myVehicles[i][j].stop() ;
                                         }
                                         else if (dist<=myVehicles[i][j].length/4) {
                                             // red
                                             myVehicles[i][j].slowdown(0.65f) ;
                                         }
                                     } // if state
                            //    }   // if over stop zone
                            }   // if vehicle not null
                        }   // next j
                       
                        //tStopOnRedEB.yield() ;
                        try {Thread.sleep(10) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tStopOnRedEB.yield() ;
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread EB
        tStopOnRedEB = new Thread(runThreadSTP_EB, "StopOnRed_EB") ;
        //tStopOnRedEB.setPriority(Thread.NORM_PRIORITY) ;
//        tStopOnRedEB.start() ;
        
        // =======================================================================
        // vehicles stop on red lit thread
        // =======================================================================
        runThreadSTP_WB = new Runnable() {
            public void run() {
                Point stopbar_p1 = new Point(
                    myDB.WB_data.stopbar_LR.x,
                    myDB.WB_data.stopbar_UL.y) ;
                while (true) {
                    int i=1 ;   // WB
                    if (sim_flag) {
                        // each approach
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            if ( myVehicles[i][j] != null ) {
                                // check if vehicle on RED bar
                                boolean stop_flag = false ;

                                if (isOverStopBar(myDB.WB_data.stopbar_UL,
                                    myDB.WB_data.stopbar_LR, 
                                    myVehicles[i][j].getPosition(),
                                    myVehicles[i][j].getFrontBumperCenter(),
                                    myVehicles[i][j].getRearBumperCenter()
                                    )) {
                                       stop_flag = true ;
                                }   // if over stop bar
                                //Point stopbar_p1 = new Point(
                                //    myDB.WB_data.stopbar_LR.x,
                                //    myDB.WB_data.stopbar_UL.y) ;
                                int phase_id = 0 ;
                                double dist = calcDist2Line(myVehicles[i][j].getFrontBumperCenter(), stopbar_p1, myDB.WB_data.stopbar_LR) ;
                                if (myVehicles[i][j].getRouteID()==0 )  {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[i] ;
                                } else {
                                    // thru traffic
                                    phase_id = THRU_ASSIGN_PH[i] ;
                                }                                    
                           /*     if (stop_flag) {        // signal red
                                    // compare with signal phase state
                                    if ( !mySignalControl.phaseStatus[phase_id-1]) {
                                        myVehicles[i][j].stop() ;
                                    } else {
                                        // green phase
                                        myVehicles[i][j].speedup() ;
                                    }   // if phase_id      
                                } // if stop_flag
                                else {  */
                                     if (mySignalControl.phaseStatus[phase_id-1]) {
                                         // green
                                         // check if veh approaching stop bar
                                         mPointF v1 = myVehicles[i][j].headingVector() ;
                                         Point pos_veh = myVehicles[i][j].getPosition() ;
                                         mPointF vs = new mPointF(myDB.WB_data.stopbar_LR.x-pos_veh.x, myDB.WB_data.stopbar_UL.y-pos_veh.y) ;
                                         if (vs.dot(v1)>=0) { // approach stop bar
                                             float time_count = 0 ;
                                             if (phase_id<=4) {
                                                 // ring 1 timer
                                                 time_count = mySignalControl.fixedTimer1.greenTimer.getCount() ;
                                             } else {
                                                 // ring 2 timer
                                                 time_count = mySignalControl.fixedTimer2.greenTimer.getCount() ;
                                             }
                                             // check if green is ending
                                             if ((time_count<=1.5f)&&(dist<20f)) {
                                                 myVehicles[i][j].slowdown() ;
                                             } else {
                                                myVehicles[i][j].speedup() ;
                                             }
                                         } else {
                                             myVehicles[i][j].speedup() ;
                                         }
                                     } else {
                                         if (stop_flag) {        // signal red
                                             myVehicles[i][j].stop() ;
                                         }
                                         else if (dist<=myVehicles[i][j].length/4) {
                                            myVehicles[i][j].slowdown(0.65f) ;
                                         }
                                     }
                            //    }   // if over stop zone

                            }   // if vehicle not null
                        }   // next j
                        //}   // next i
                        //tStopOnRedWB.yield() ;
                        try {Thread.sleep(10) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tStopOnRedWB.yield() ; 
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread WB
        tStopOnRedWB = new Thread(runThreadSTP_WB, "StopOnRed_WB") ;
        //tStopOnRedWB.setPriority(Thread.NORM_PRIORITY) ;
//        tStopOnRedWB.start() ;

        // =======================================================================
        // vehicles stop on red lit thread
        // =======================================================================
        runThreadSTP_NB = new Runnable() {
            public void run() {
                Point stopbar_p1 = new Point(
                        myDB.NB_data.stopbar_UL.x,
                        myDB.NB_data.stopbar_LR.y) ;
                while (true) {
                    int i=2 ;   // NB
                    if (sim_flag) {                        
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            if ( myVehicles[i][j] != null ) {
                                // check if vehicle on RED bar
                                boolean stop_flag = false ;

                                if (isOverStopBar(myDB.NB_data.stopbar_UL,
                                    myDB.NB_data.stopbar_LR, 
                                    myVehicles[i][j].getPosition(),
                                    myVehicles[i][j].getFrontBumperCenter(),
                                    myVehicles[i][j].getRearBumperCenter()
                                    )) {
                                       stop_flag = true ;
                                }   // if over stop bar
                                //Point stopbar_p1 = new Point(
                                //        myDB.NB_data.stopbar_UL.x,
                                //        myDB.NB_data.stopbar_LR.y) ;
                                int phase_id = 0 ;
                                double dist = calcDist2Line(myVehicles[i][j].getFrontBumperCenter(), stopbar_p1, myDB.NB_data.stopbar_LR) ;
                                if (myVehicles[i][j].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[i] ;                                
                                } else {
                                    // thru traffic
                                    phase_id = THRU_ASSIGN_PH[i] ;
                                }                                    
                           /*     if (stop_flag) {        // signal red
                                    // compare with signal phase state
                                    if ( !mySignalControl.phaseStatus[phase_id-1]) {
                                        myVehicles[i][j].stop() ;
                                    }  else {
                                        // green phase
                                        myVehicles[i][j].speedup() ;
                                    }   // if phase_id     
                                } // if stop_flag
                                else {  */
                                     if (mySignalControl.phaseStatus[phase_id-1]) {
                            //System.out.println("left turn, i="+i+"phaseId="+phase_id) ;
                                         // green
                                         // check if veh approaching stop bar
                                         mPointF v1 = myVehicles[i][j].headingVector() ;
                                         Point pos_veh = myVehicles[i][j].getPosition() ;
                                         mPointF vs = new mPointF(myDB.NB_data.stopbar_UL.x-pos_veh.x, myDB.NB_data.stopbar_LR.y-pos_veh.y) ;
                                         if (vs.dot(v1)>=0) { // approach stop bar
                                             float time_count = 0 ;
                                             if (phase_id<=4) {
                                                 // ring 1 timer
                                                 time_count = mySignalControl.fixedTimer1.greenTimer.getCount() ;
                                             } else {
                                                 // ring 2 timer
                                                 time_count = mySignalControl.fixedTimer2.greenTimer.getCount() ;
                                             }
                             //System.out.println("dot>=0,phaseId="+phase_id+", t="+time_count) ;
                                             // check if green is ending
                                             if ((time_count<=1.5f)&&(dist<20)) {
                                                 myVehicles[i][j].slowdown() ;
                                             } else {
                                                myVehicles[i][j].speedup() ;
                                             }
                                         } else {
                                             myVehicles[i][j].speedup() ;
                                         }
                                     } else {
                                         // red
                                         if (stop_flag) { 
                                             myVehicles[i][j].stop() ;
                                         }
                                         else if (dist<=myVehicles[i][j].length/4) {
                                            myVehicles[i][j].slowdown(0.65f) ;
                                         }
                                     }
                            //    }   // if over stop zone

                            }   // if vehicle not null
                        }   // next j
                       
                        //tStopOnRedNB.yield() ;
                        try {Thread.sleep(10) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tStopOnRedNB.yield() ;
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread NB
        tStopOnRedNB = new Thread(runThreadSTP_NB, "StopOnRed_NB") ;
        //tStopOnRedNB.setPriority(Thread.NORM_PRIORITY) ;
//        tStopOnRedNB.start() ;
        
        // =======================================================================
        // vehicles stop on red lit thread
        // =======================================================================
        runThreadSTP_SB = new Runnable() {
            Point stopbar_p2 = new Point(
                myDB.SB_data.stopbar_LR.x,
                myDB.SB_data.stopbar_UL.y) ;
            public void run() {
                while (true) {
                    int i=3 ;   // SB
                    if (sim_flag) {
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            if ( myVehicles[i][j] != null ) {
                                // check if vehicle on RED bar
                                boolean stop_flag = false ;
                                
                                if (isOverStopBar(myDB.SB_data.stopbar_UL,
                                    myDB.SB_data.stopbar_LR, 
                                    myVehicles[i][j].getPosition(),
                                    myVehicles[i][j].getFrontBumperCenter(),
                                    myVehicles[i][j].getRearBumperCenter()
                                    )) {
                                       stop_flag = true ;
                                }   // if over stop bar
                               
                                //Point stopbar_p2 = new Point(
                                //    myDB.SB_data.stopbar_LR.x,
                                //    myDB.SB_data.stopbar_UL.y) ;

                                int phase_id = 0 ;
                                double dist = calcDist2Line(myVehicles[i][j].getFrontBumperCenter(), myDB.SB_data.stopbar_UL, stopbar_p2) ;
                                if (myVehicles[i][j].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[i] ;
                                } else {
                                    // thru traffic
                                    phase_id = THRU_ASSIGN_PH[i] ;
                                }    
                        /*        if (stop_flag) {                                      
                                    // compare with signal phase state
                                    if (!mySignalControl.phaseStatus[phase_id-1]) {
                                        // signal red  
                                        myVehicles[i][j].stop() ;
                                    }  else {
                                        // green phase
                                        myVehicles[i][j].speedup() ;
                                    }   // if phase_id       
                                } // if stop_flag
                                else { */
                                     if (mySignalControl.phaseStatus[phase_id-1]) {
                                         // green
                                         // check if veh approaching stop bar
                                         mPointF v1 = myVehicles[i][j].headingVector() ;
                                         Point pos_veh = myVehicles[i][j].getPosition() ;
                                         mPointF vs = new mPointF(myDB.SB_data.stopbar_LR.x-pos_veh.x, myDB.SB_data.stopbar_UL.y-pos_veh.y) ;
                                         if (vs.dot(v1)>=0) { // approach stop bar
                                             float time_count = 0 ;
                                             if (phase_id<=4) {
                                                 // ring 1 timer
                                                 time_count = mySignalControl.fixedTimer1.greenTimer.getCount() ;
                                             } else {
                                                 // ring 2 timer
                                                 time_count = mySignalControl.fixedTimer2.greenTimer.getCount() ;
                                             }
                                             // check if green is ending
                                             if ((time_count<=1.5f)&& (dist<20)) {
                                                 myVehicles[i][j].slowdown() ;
                                             } else {
                                                myVehicles[i][j].speedup() ;
                                             }
                                         } else {
                                             myVehicles[i][j].speedup() ;
                                         }
                                     } else {
                                         // red
                                         if (stop_flag) {
                                            // signal red  
                                            myVehicles[i][j].stop() ;
                                         }
                                         else if (dist<=myVehicles[i][j].length/4) {
                                            myVehicles[i][j].slowdown(0.65f) ;
                                         }
                                     }  // green or red phase
                            //    }   // if over stop zone

                            }   // if vehicle not null
                        }   // next j
                        //}   // next i
                        //tStopOnRedSB.yield() ;
                        try {Thread.sleep(10) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tStopOnRedSB.yield() ;
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread EB
        tStopOnRedSB = new Thread(runThreadSTP_SB, "StopOnRed_SB") ;
        //tStopOnRedSB.setPriority(Thread.NORM_PRIORITY) ;
//        tStopOnRedSB.start() ;
        
        // =====================================================================
        // vehicle detection thread EB
        // =======================================================================
        runThreadEBdet = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        // EB
                        int i=0 ;
                        int N=0 ;
                        int ij, k ; 
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.EB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.EB_data.loopDetCount[k]=0 ;
                                }   // next k
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.EB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_LENGTH, myDB.LOOP_DET_WIDTH)) {
                                        myDB.EB_data.loopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.EB_data.loopDetCount[ij]>0) {
                                myDB.EB_data.setLoopOccupied(ij, true) ; 
                                // ====================================
                                if (mySignalControl.control_type==1) {
                                    // actuated control
                                    if (ij==0) {
                                        // left turn, phase 7
                                        mySignalControl.myDetectors[6] = true ;
                                    } else {
                                        // thru movement, phase 4
                                        mySignalControl.myDetectors[3] = true ;
                                    }
                                }   // actuated control
                                // ====================================
                            } else {
                                myDB.EB_data.setLoopOccupied(ij, false) ; 
                            }
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread EB
        tDetectionEB = new Thread(runThreadEBdet, "DetectionEB") ;
        tDetectionEB.start() ;
        
        // =======================================================================
        // detection thread WB
        // =======================================================================
        runThreadWBdet = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        // WB
                        int i=1 ;
                        int N=0 ;
                        int ij, k ; 
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.WB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.WB_data.loopDetCount[k]=0 ;
                                }   // next k
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.WB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_LENGTH, myDB.LOOP_DET_WIDTH)) {
                                        myDB.WB_data.loopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.WB_data.loopDetCount[ij]>0) {
                                myDB.WB_data.setLoopOccupied(ij, true) ; 
                                // ====================================
                                if (mySignalControl.control_type==1) {
                                    // actuated control
                                    if (ij==0) {
                                        // left turn, phase 3
                                        mySignalControl.myDetectors[2] = true ;
                                    } else {
                                        // thru movement, phase 8
                                        mySignalControl.myDetectors[7] = true ;
                                    }
                                }   // actuated control
                                // ====================================
                            } else {
                                myDB.WB_data.setLoopOccupied(ij, false) ; 
                            }
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread 
        tDetectionWB = new Thread(runThreadWBdet, "DetectionWB") ;
        tDetectionWB.start() ;
        
        // =======================================================================
        // detection thread NB
        // =======================================================================
        runThreadNBdet = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        // NB
                        int i=2 ;
                        int N=0 ;
                        int ij, k ; 
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.NB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.NB_data.loopDetCount[k]=0 ;
                                }   // next k
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.NB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_LENGTH, myDB.LOOP_DET_WIDTH)) {
                                        myDB.NB_data.loopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.NB_data.loopDetCount[ij]>0) {
                                myDB.NB_data.setLoopOccupied(ij, true) ; 
                                // ====================================
                                if (mySignalControl.control_type==1) {
                                    // actuated control
                                    if (ij==0) {
                                        // left turn, phase 5
                                        mySignalControl.myDetectors[4] = true ;
                                    } else {
                                        // thru movement, phase 2
                                        mySignalControl.myDetectors[1] = true ;
                                    }
                                }   // actuated control
                                // ====================================
                            } else {
                                myDB.NB_data.setLoopOccupied(ij, false) ; 
                            }
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread 
        tDetectionNB = new Thread(runThreadNBdet, "DetectionNB") ;
        tDetectionNB.start() ;
        
        // =======================================================================
        // detection thread SB
        // =======================================================================
        runThreadSBdet = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag) {
                        // SB
                        int i=3 ;
                        int N=0 ;
                        int ij, k ; 
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.SB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.SB_data.loopDetCount[k]=0 ;
                                }   // next k
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.SB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_LENGTH, myDB.LOOP_DET_WIDTH)) {
                                        myDB.SB_data.loopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.SB_data.loopDetCount[ij]>0) {
                                myDB.SB_data.setLoopOccupied(ij, true) ; 
                                // ====================================
                                if (mySignalControl.control_type==1) {
                                    // actuated control
                                    if (ij==0) {
                                        // left turn, phase 1
                                        mySignalControl.myDetectors[0] = true ;
                                    } else {
                                        // thru movement, phase 6
                                        mySignalControl.myDetectors[5] = true ;
                                    }
                                }   // actuated control
                                // ====================================
                            } else {
                                myDB.SB_data.setLoopOccupied(ij, false) ; 
                            }
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        try {Thread.sleep(1000) ;}
                        catch (InterruptedException ie) {} ;                        
                    }   // end if
                }
             }   // void run
        } ; // runThread 
        tDetectionSB = new Thread(runThreadSBdet, "DetectionSB") ;
        tDetectionSB.start() ;
        
        // =======================================================================
        //myCar.vehicleStart() ;
        // test
        //System.out.println(mySignalControl.toStrSeq1()) ;
        //System.out.println(mySignalControl.toStrSeq2()) ;
        
        try {
            // read from JAR file
            InputStream is = getClass().getResourceAsStream("phase_assgn.png") ;
            myPhaseAssgn = ImageIO.read(is) ;
        } catch (IOException e) {
            System.out.println("Failed to read phase_assgn.png image file!") ;
        }

    }   // end of startup initialization
    
    public int dist2Intsc(int _approach, int _veh_index) {
        int posx = myVehicles[_approach][_veh_index].getPosition().x ;
        int posy = myVehicles[_approach][_veh_index].getPosition().y ;
        int dx = posx - (intsc_left+intsc_right)/2 ;
        int dy = posy - (intsc_top+intsc_bottom)/2 ;
        double dist = Math.sqrt(dx*dx+dy*dy) ;
        return CInt(dist) ;
    }
    
    private boolean enteredIntsc(int _approach, int _veh_index) {
        int posx = myVehicles[_approach][_veh_index].getPosition().x ;
        int posy = myVehicles[_approach][_veh_index].getPosition().y ;
        //int buff = myVehicles[_approach][_veh_index].length ;
        int buff = 0 ;
        if ((posx>=intsc_left-buff) && (posx<=intsc_right+buff) &&
            (posy>=intsc_top-buff) && (posy<=intsc_bottom+buff)) {
            return true ;
        } else {
            return false ;
        }
    }
    private double calcDist2Line(Point p0, Point p1, Point p2) {
        int x0 = p0.x ;
        int y0 = p0.y ;
        int x1 = p1.x ;
        int y1 = p1.y ;
        int x2 = p2.x ;
        int y2 = p2.y ;
        int dx = x2-x1 ;
        int dy = y2-y1 ;
        double len = Math.sqrt(dx*dx+dy*dy) ;
        return Math.abs(dx*(y1-y0)-(x1-x0)*dy)/len ;
    }
    
    private double calcDist(Point pt1, Point pt2) {
        double dx = pt1.x - pt2.x ;
        double dy = pt1.y - pt2.y ;
        return Math.sqrt(dx*dx+dy*dy) ;
    }
    private double calcDist(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2 ;
        double dy = y1 - y2 ;
        return Math.sqrt(dx*dx+dy*dy) ;
    }
    private double calcDistf(float x1, float y1, float x2, float y2) {
        double dx = x1 - x2 ;
        double dy = y1 - y2 ;
        return Math.sqrt(dx*dx+dy*dy) ;
    }    
    
    private boolean nearby(int v1, int v2, int dv) {
        if (Math.abs(v1-v2) <= dv) {
            return true ;
        } else {
            return false ;
        }
    }    
    // process image file
    public void processImage() {
            PixelGrabber pg=new PixelGrabber(image,0,0,-1,-1,true);
            try{
                if(pg.grabPixels()){
                    imageW=pg.getWidth();
                    imageH=pg.getHeight();
//                    int[] op=(int[]) pg.getPixels();
//                    int[] np=(int[]) new int[w*w];
//                    g.drawImage(image,0,0,this);
                }
            } catch(InterruptedException ie){
                sb.setStatusBarText(1, "Error: "+ie.toString()) ;
            }
    }
    // methods
    
    // start simulation
    public void startSim() {
        for (int i=0; i<4; i++) {
            //veh_index[i] = 0 ;
            vHeadway[i] = 3600f / traffic_demand[0][i][0] ;
            timeElapsed[i] = random.nextFloat()*vHeadway[i] ;
        }
        tLast = System.currentTimeMillis();
        tLast1 = tLast ;
        sim_flag = true ;
        if (mySignalControl.control_type==0) {
            // fixed control
            if (sim_alreadyStarted) {
                mySignalControl.continueFixedTime() ; 
            } else {
                mySignalControl.doFixedTime() ; 
            } 
        } else if (mySignalControl.control_type==1) {
            // actuated control
            if (sim_alreadyStarted) {
                mySignalControl.continueActuatedTime() ;
            } else {
                mySignalControl.doActuatedTime() ;
            }
        }
        setVehiclesEnabled(true) ;  // enable all vehicles
        sim_alreadyStarted = true ;
    }
    
    // pause simulation
    public void pauseSim() {
        sim_flag = false ;
        int N = myDB.EB_data.MAX_LANE_SIZE ;
        setVehiclesEnabled(false) ; // pause all vehicles
        for (int k=0; k<N; k++) {
            myDB.EB_data.loopDetCount[k]=0 ;
            myDB.WB_data.loopDetCount[k]=0 ;
            myDB.NB_data.loopDetCount[k]=0 ; 
            myDB.SB_data.loopDetCount[k]=0 ;
            myDB.EB_data.setLoopOccupied(k, false) ;
            myDB.WB_data.setLoopOccupied(k, false) ;
            myDB.NB_data.setLoopOccupied(k, false) ;
            myDB.SB_data.setLoopOccupied(k, false) ;
        }   // next k
        if (mySignalControl.control_type==0) {
            mySignalControl.pauseFixedTime() ;
        } else if (mySignalControl.control_type==1) {
            mySignalControl.pauseActuatedTime() ; 
        }
        //System.out.println("--- " + mySignalControl.fixedTimer1.getCount()) ; 
        /*
         for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
            if (mySignalControl.phaseStatus[i]) {
                // green
                System.out.println("phase "+(i+1)+" green") ;
            } else {
                System.out.println("phase "+(i+1)+" red") ;
            }
        }   // for
        */
    }
    
    public void setVehiclesEnabled(boolean state) {
        for (int i=0; i<4; i++) {
            for (int j=0; j<MAX_VEH_SIZE; j++) {
                if (myVehicles[i][j] != null ) {
                    if (state) {    // resue
                        myVehicles[i][j].vehResume() ;
                    } else {
                        myVehicles[i][j].vehPause() ;
                    }
                }   // veh null ??
            }   // next j
        }   // next i
    }   // func
    
    private void showTimer(Graphics2D g, int ref_x, int ref_y, int phase, int ring) {
        g.setStroke(new BasicStroke(2)) ;
        String[] str = new String[4] ;
        str[0] = "Phase: "+phase ;  // display phase #
        str[1] = "INI" ;    // initial green
        str[2] = "EXT" ;    // green extension
        str[3] = "MAX" ;    // maximum green
        for (int i=1; i<=3; i++) {
            g.setColor(Color.blue);
            g.drawRect(ref_x, ref_y-10+i*30, 80, 30) ;
            g.setColor(Color.yellow);
            g.fillRect(ref_x+5, ref_y-5+i*30, 70, 20) ;
        }
        g.setColor(Color.black);
        //g.setStroke(new BasicStroke(14)) ;
        g.drawString(str[0], ref_x, ref_y+12) ;
        String time_stamp = "" ;
        float timer_val = 0f ;
        for (int i=1; i<=3; i++) {
            g.drawString(str[i], ref_x-33, ref_y+10+i*30) ;
            time_stamp = "00.00" ;
            switch (i) {
                case 1:     // INITIAL
                    if (ring==1) {
                        timer_val = mySignalControl.actuatedTimer1.greenTimer.get_initial() ;
                    } else if (ring==2) {
                        timer_val = mySignalControl.actuatedTimer2.greenTimer.get_initial() ;
                    }
                    //time_stamp = CStr2(mySignalControl.myPhases[phase-1].getInitial()) ;
                    
                    break ;
                case 2:     // EXTENSION
                    if (ring==1) {
                        timer_val = mySignalControl.actuatedTimer1.greenTimer.get_extension() ;
                    } else if (ring==2) {
                        timer_val = mySignalControl.actuatedTimer2.greenTimer.get_extension() ;
                    }
                    //time_stamp = CStr2(mySignalControl.myPhases[phase-1].getExtension()) ;
                    break ;
                case 3:     // MAXIMUM TIMER
                    if (ring==1) {
                        timer_val = mySignalControl.actuatedTimer1.greenTimer.get_maximum() ;
                    } else if (ring==2) {
                        timer_val = mySignalControl.actuatedTimer2.greenTimer.get_maximum() ;
                    }
                    //time_stamp = CStr2(mySignalControl.myPhases[phase-1].getMax1()) ;
                    break ;
            }   // switch
            time_stamp = CStr2(timer_val) ;
            
            //g.drawString("00:00", ref_x+25, ref_y+10+i*30) ;
            g.drawString(time_stamp, ref_x+25, ref_y+10+i*30) ;
        }   // end of for
        //g.setColor(Color.blue);
        g.drawRect(ref_x-40, ref_y-5, 125, 120) ;
        
    }
    public void paint(Graphics gr) 
    {
        Graphics2D g = (Graphics2D)gr ;
        
        if (mySignalControl.control_type==1) {
            // actuated signal control
            /*
            int[] active_phase = new int[2] ;   //2 rings, 2 active phases
            int index = 0 ;
            for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
                if (mySignalControl.phaseStatus[i]) {
                    active_phase[index] = i ;
                    index++ ;
                }   // if
            }   // for
            */
            // plot timer display
            // barrier 1
            if (mySignalControl.phaseStatus[0]) {   // phase 1 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showTimer(g, center.x-175, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                    showTimer(g, center.x+130, 360, 1, 1) ; // show phase 1 @ phase 2, 5 timer location
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showTimer(g, center.x-175, 60, 1, 1) ; // for 6-lane roadway, phase 6, 1
                    showTimer(g, center.x+130, 360, 5, 2) ; // for 6-lane roadway, phase 2, 5
                }
            } else if (mySignalControl.phaseStatus[1]) {   // phase 2 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showTimer(g, center.x+130, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showTimer(g, center.x-175, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showTimer(g, center.x+130, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showTimer(g, center.x-175, 60, 5, 2) ; // show phase 5 @ phase 6, 1 timer location
                }
            }
            
            // barrier 2
            if (mySignalControl.phaseStatus[2]) {
                // phase 3 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showTimer(g, center.x+130, 60, 8, 2) ; // for 6-lane roadway, phase 8, 3
                    showTimer(g, center.x-175, 360, 3, 1) ; // phase 3 at  phase 4, 7
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showTimer(g, center.x+130, 60, 3, 1) ; // for 6-lane roadway, phase 8, 3
                    showTimer(g, center.x-175, 360, 7, 2) ; // for 6-lane roadway, phase 4, 7
                }
            } else if (mySignalControl.phaseStatus[3]) {
                // phase 4 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showTimer(g, center.x-175, 360, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showTimer(g, center.x+130, 60, 8, 2) ; // for 6-lane roadway, phase 8, 3
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showTimer(g, center.x-175, 360, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showTimer(g, center.x+130, 60, 7, 2) ; // phase 7 at timer phase 8, 3
                }
            }
        }   // end if sim running and actuated signal control
        
        g.drawImage(myPhaseAssgn, 700, 0, this) ;  // phase assignment graph, 2/25/07 added
        
        // ---------------------------------------------
        g.setColor(Color.lightGray);
        // intersection
        g.setStroke(new BasicStroke(2)) ;   // curbs
        g.drawRect(intsc_left, intsc_top, intsc_width, intsc_height) ;
        // links
        g.setColor(Color.black);
        g.drawLine(intsc_left, intsc_top, intsc_left, intsc_top-link_length) ;
        g.drawLine(intsc_left, intsc_top, intsc_left-link_length, intsc_top) ;

        g.drawLine(intsc_left, intsc_bottom, intsc_left, intsc_top+intsc_height+link_length) ;
        g.drawLine(intsc_left, intsc_bottom, intsc_left-link_length, intsc_top+intsc_height) ;
        
        g.drawLine(intsc_right, intsc_top, intsc_right, intsc_top-link_length) ;
        g.drawLine(intsc_right, intsc_top, intsc_right+link_length, intsc_top) ;

        g.drawLine(intsc_right, intsc_bottom, intsc_right, intsc_top+intsc_height+link_length) ;
        g.drawLine(intsc_right, intsc_bottom, intsc_right+link_length, intsc_top+intsc_height) ;
        
        //Point center = new Point((intsc_left+intsc_right)/2,(intsc_top+intsc_bottom)/2) ;
        
        // medium island, check exclusive left turn
        g.setColor(new Color(255, 255, 0)) ;    // dark yellow
        g.setStroke(myLineStroke) ;
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            // northern link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(myLineStroke) ;
            int _x1 = center.x+myDB.LANE_WIDTH/2 ;
            int _x2 = center.x-myDB.LANE_WIDTH/2 ;
            g.drawLine(_x1, intsc_top, _x1, intsc_top-link_length) ;
            if (myDB.SB_data.leftTurnLaneExists()) {
                g.drawLine(_x2, intsc_top-link_length/2, _x2, intsc_top-link_length) ;
                g.drawLine(_x2, intsc_top-link_length/2, _x1, intsc_top-link_length/2+10) ;
                g.setColor(Color.white) ;    // white strips
                g.setStroke(myDashStroke) ;
                g.drawLine(_x2, intsc_top-STOPBAR_WIDTH, _x2, intsc_top-link_length/2+10) ;
                // loop detector
                g.setStroke(myLineStroke) ;
                if (myDB.SB_data.isLoopOccupied(0)) {
                    // left turn lane veh detected
                    g.setColor(Color.red) ; 
                } else {
                    // left turn lane veh not detected
                    g.setColor(Color.black) ;
                }
                g.drawRect(center.x-myDB.LOOP_DET_WIDTH/2, 
                            intsc_top-myDB.SB_data.detector_dist, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LOOP_DET_LENGTH
                          ) ;
            } else {
                // no exclusive left turn
                g.drawLine(_x2, intsc_top, _x2, intsc_top-link_length) ;
                g.drawLine(_x2, intsc_top, _x1, intsc_top) ;
            }
            // southern link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(myLineStroke) ;
            g.drawLine(_x2, intsc_bottom, _x2, intsc_bottom+link_length) ;
            if (myDB.NB_data.leftTurnLaneExists()) {
                g.drawLine(_x1, intsc_bottom+link_length/2, _x1, intsc_bottom+link_length) ;
                g.drawLine(_x1, intsc_bottom+link_length/2, _x2, intsc_bottom+link_length/2-10) ;
                g.setColor(Color.white) ;    // white strips
                g.setStroke(myDashStroke) ;
                g.drawLine(_x1, intsc_bottom+STOPBAR_WIDTH, _x1, intsc_bottom+link_length/2-10) ;
                // loop detector
                g.setStroke(myLineStroke) ;
                if (myDB.NB_data.isLoopOccupied(0)) {
                    // left turn lane veh detected
                    g.setColor(Color.red) ;
                } else {
                    // left turn lane veh not detected
                    g.setColor(Color.black) ;
                }
                g.drawRect(center.x-myDB.LOOP_DET_WIDTH/2, 
                            intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LOOP_DET_LENGTH
                          ) ;
            } else {
                // no exclusive left turn
                g.drawLine(_x1, intsc_bottom, _x1, intsc_bottom+link_length) ;
                g.drawLine(_x2, intsc_bottom, _x1, intsc_bottom) ;
            }
        } else {
            // no exclusive left turns
            g.drawLine(center.x, intsc_top, center.x, intsc_top-link_length) ;
            g.drawLine(center.x, intsc_bottom, center.x, intsc_bottom+link_length) ;
            
        }
        
        g.setColor(new Color(255, 255, 0)) ;    // dark yellow
        g.setStroke(myLineStroke) ;
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            // easthern link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(myLineStroke) ;
            int _y1 = center.y+myDB.LANE_WIDTH/2 ;
            int _y2 = center.y-myDB.LANE_WIDTH/2 ;
            g.drawLine(intsc_right, _y1, intsc_right+link_length, _y1) ;
            if (myDB.WB_data.leftTurnLaneExists()) {
                g.drawLine(intsc_right+link_length/2, _y2, intsc_right+link_length, _y2) ;
                g.drawLine(intsc_right+link_length/2, _y2, intsc_right+link_length/2-10, _y1) ;
                g.setColor(Color.white) ;    // white strips
                g.setStroke(myDashStroke) ;
                g.drawLine(intsc_right+STOPBAR_WIDTH, _y2, intsc_right+link_length/2-10, _y2) ;
                // loop detector
                g.setStroke(myLineStroke) ;
                if (myDB.WB_data.isLoopOccupied(0)) {
                    // left turn lane veh detected
                    g.setColor(Color.red) ;
                } else {
                    // left turn lane veh not detected
                    g.setColor(Color.black) ;
                }
                g.drawRect(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, 
                            center.y-myDB.LOOP_DET_WIDTH/2, 
                            myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH
                          ) ;
            } else {
                // no exclusive left turn
                g.drawLine(intsc_right, _y2, intsc_right+link_length, _y2) ;
                g.drawLine(intsc_right, _y1, intsc_right, _y2) ;
            }
            // western link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(myLineStroke) ;
            g.drawLine(intsc_left, _y2, intsc_left-link_length, _y2) ;
            if (myDB.EB_data.leftTurnLaneExists()) {
                g.drawLine(intsc_left-link_length/2, _y1, intsc_left-link_length, _y1) ;
                g.drawLine(intsc_left-link_length/2, _y1, intsc_left-link_length/2+10, _y2) ;
                g.setColor(Color.white) ;    // white strips
                g.setStroke(myDashStroke) ;
                g.drawLine(intsc_left-STOPBAR_WIDTH, _y1, intsc_left-link_length/2+10, _y1) ;
                // loop detector
                g.setStroke(myLineStroke) ;
                if (myDB.EB_data.isLoopOccupied(0)) {
                    // left turn lane veh detected
                    g.setColor(Color.red) ;
                } else {
                    // left turn lane veh not detected
                    g.setColor(Color.black) ;
                }
                g.drawRect(intsc_left-myDB.EB_data.detector_dist, 
                            center.y-myDB.LOOP_DET_WIDTH/2, 
                            myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH
                          ) ;
            } else {
                // no exclusive left turn
                g.drawLine(intsc_left, _y1, intsc_left-link_length, _y1) ;
                g.drawLine(intsc_left, _y1, intsc_left, _y2) ;
            }
        } else {
            // no exclusive left turn lanes
            g.drawLine(intsc_right, center.y, intsc_right+link_length, center.y) ;
            g.drawLine(intsc_left, center.y, intsc_left-link_length, center.y) ;
        }
        
        // ============================================================
        // handle more lane 2 lanes in each bound
        g.setColor(Color.white) ;    // white strips
        g.setStroke(myDashStroke) ;
        if (myDB.NB_data.getLaneSize()>2 ) {
            // draw white dash lines
            int _x1, _x2 ;
            int _x11, _x22 ;
            if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                _x1 = center.x+myDB.LANE_WIDTH+myDB.LANE_WIDTH/2 ;
                _x2 = center.x-myDB.LANE_WIDTH-myDB.LANE_WIDTH/2 ;
            } else {
                _x1 = center.x+myDB.LANE_WIDTH ;
                _x2 = center.x-myDB.LANE_WIDTH ;
            }
            switch (myDB.NB_data.getLaneSize()) {
                case 4:
                    g.drawLine(_x1, intsc_top-STOPBAR_WIDTH, _x1, intsc_top-link_length) ;
                    g.drawLine(_x2, intsc_top-STOPBAR_WIDTH, _x2, intsc_top-link_length) ;
                    break ;
                case 6:
                    g.drawLine(_x1, intsc_top-STOPBAR_WIDTH, _x1, intsc_top-link_length) ;
                    g.drawLine(_x2, intsc_top-STOPBAR_WIDTH, _x2, intsc_top-link_length) ;
                    _x11 = _x1+myDB.LANE_WIDTH ;
                    _x22 = _x2-myDB.LANE_WIDTH ;
                    g.drawLine(_x11, intsc_top-STOPBAR_WIDTH, _x11, intsc_top-link_length) ;
                    g.drawLine(_x22, intsc_top-STOPBAR_WIDTH, _x22, intsc_top-link_length) ;
                    break ;
            }   // end switch
            switch (myDB.SB_data.getLaneSize()) {
                case 4:
                    g.drawLine(_x1, intsc_bottom+STOPBAR_WIDTH, _x1, intsc_bottom+link_length) ;
                    g.drawLine(_x2, intsc_bottom+STOPBAR_WIDTH, _x2, intsc_bottom+link_length) ;
                    break ;
                case 6:
                    g.drawLine(_x1, intsc_bottom+STOPBAR_WIDTH, _x1, intsc_bottom+link_length) ;
                    g.drawLine(_x2, intsc_bottom+STOPBAR_WIDTH, _x2, intsc_bottom+link_length) ;
                    _x11 = _x1+myDB.LANE_WIDTH ;
                    _x22 = _x2-myDB.LANE_WIDTH ;
                    g.drawLine(_x11, intsc_bottom+STOPBAR_WIDTH, _x11, intsc_bottom+link_length) ;
                    g.drawLine(_x22, intsc_bottom+STOPBAR_WIDTH, _x22, intsc_bottom+link_length) ;
                    break ;
            }   // end switch
        }
        if (myDB.EB_data.getLaneSize()>2 ) {
            // draw white dash lines
            int _y1, _y2 ; 
            int _y11, _y22 ;
            if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                _y1 = center.y+myDB.LANE_WIDTH+myDB.LANE_WIDTH/2 ;
                _y2 = center.y-myDB.LANE_WIDTH-myDB.LANE_WIDTH/2 ;
            } else {
                _y1 = center.y+myDB.LANE_WIDTH ;
                _y2 = center.y-myDB.LANE_WIDTH ;                
            }
            switch (myDB.EB_data.getLaneSize()) {
                case 4:
                    g.drawLine(intsc_right+STOPBAR_WIDTH, _y1, intsc_right+link_length, _y1) ;
                    g.drawLine(intsc_right+STOPBAR_WIDTH, _y2, intsc_right+link_length, _y2) ;
                    break ;
                case 6:
                    g.drawLine(intsc_right+STOPBAR_WIDTH, _y1, intsc_right+link_length, _y1) ;
                    g.drawLine(intsc_right+STOPBAR_WIDTH, _y2, intsc_right+link_length, _y2) ;
                    _y11 = _y1 + myDB.LANE_WIDTH ; 
                    _y22 = _y2 - myDB.LANE_WIDTH ; 
                    g.drawLine(intsc_right, _y11, intsc_right+link_length, _y11) ;
                    g.drawLine(intsc_right, _y22, intsc_right+link_length, _y22) ;
                    break ;
            }   // end switch
            switch (myDB.WB_data.getLaneSize()) {
                case 4:
                    g.drawLine(intsc_left-STOPBAR_WIDTH, _y1, intsc_left-link_length, _y1) ;
                    g.drawLine(intsc_left-STOPBAR_WIDTH, _y2, intsc_left-link_length, _y2) ;
                    break ;
                case 6:
                    g.drawLine(intsc_left-STOPBAR_WIDTH, _y1, intsc_left-link_length, _y1) ;
                    g.drawLine(intsc_left-STOPBAR_WIDTH, _y2, intsc_left-link_length, _y2) ;
                    _y11 = _y1 + myDB.LANE_WIDTH ; 
                    _y22 = _y2 - myDB.LANE_WIDTH ; 
                    g.drawLine(intsc_left, _y11, intsc_left-link_length, _y11) ;
                    g.drawLine(intsc_left, _y22, intsc_left-link_length, _y22) ;
                    break ;
            }   // end switch
        }
  

        // plot loop detectors
        int N ; 
        int ij=0 ;
        // loop detector
        g.setStroke(myLineStroke) ;
        // EB
        N = myDB.EB_data.getLaneSize()/2 ;
        for (ij=0; ij<=N; ij++) {
            if (myDB.EB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            g.drawRect(myDB.EB_data.detectorUL[ij].x, 
                        myDB.EB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH
                      ) ;
        }
        // WB
        N = myDB.WB_data.getLaneSize()/2 ;
        for (ij=0; ij<=N; ij++) {
            if (myDB.WB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            g.drawRect(myDB.WB_data.detectorUL[ij].x, 
                        myDB.WB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH
                      ) ;
        }
        // NB
        N = myDB.NB_data.getLaneSize()/2 ;
        for (ij=0; ij<=N; ij++) {
            if (myDB.NB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            g.drawRect(myDB.NB_data.detectorUL[ij].x, 
                        myDB.NB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.LOOP_DET_LENGTH
                      ) ;
        }
        // SB
        N = myDB.SB_data.getLaneSize()/2 ;
        for (ij=0; ij<=N; ij++) {
            if (myDB.SB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            g.drawRect(myDB.SB_data.detectorUL[ij].x, 
                        myDB.SB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.LOOP_DET_LENGTH
                      ) ;
        }
        // stopbar
        /*
        g.setColor(Color.white) ;
        g.drawRect( myDB.EB_data.stopbar_UL.x, 
                    myDB.EB_data.stopbar_UL.y,
                    myDB.EB_data.stopbar_LR.x - myDB.EB_data.stopbar_UL.x,
                    myDB.EB_data.stopbar_LR.y - myDB.EB_data.stopbar_UL.y) ;
        g.drawRect( myDB.WB_data.stopbar_UL.x, 
                    myDB.WB_data.stopbar_UL.y,
                    myDB.WB_data.stopbar_LR.x - myDB.WB_data.stopbar_UL.x,
                    myDB.WB_data.stopbar_LR.y - myDB.WB_data.stopbar_UL.y) ;
        g.drawRect( myDB.NB_data.stopbar_UL.x, 
                    myDB.NB_data.stopbar_UL.y,
                    myDB.NB_data.stopbar_LR.x - myDB.NB_data.stopbar_UL.x,
                    myDB.NB_data.stopbar_LR.y - myDB.NB_data.stopbar_UL.y) ;
        g.drawRect( myDB.SB_data.stopbar_UL.x, 
                    myDB.SB_data.stopbar_UL.y,
                    myDB.SB_data.stopbar_LR.x - myDB.SB_data.stopbar_UL.x,
                    myDB.SB_data.stopbar_LR.y - myDB.SB_data.stopbar_UL.y) ;
      */
         
        // *********************************************************************
        // plot signals
        g.setStroke(myLineStroke) ;
        for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
            if (mySignalControl.myPhases[i].sigbar_UL.x>0 && mySignalControl.myPhases[i].sigbar_UL.y>0) {
                if (mySignalControl.phaseStatus[i]) {
                    // green or yellow or red
                    g.setColor(Color.green) ;
                    if (sim_flag && mySignalControl.control_type==0) {
                        // fixed timing
                        //System.out.println("i="+i) ;
                        //System.out.println("Fixed-R1-phaseID="+mySignalControl.getR1PhaseID()+", stage="+mySignalControl.fixedTimer1.phase_stage) ;
                        //System.out.println("Fixed-R2-phaseID="+mySignalControl.getR2PhaseID()+", stage="+mySignalControl.fixedTimer2.phase_stage) ;
                        
                        if (i==mySignalControl.getR1PhaseID()) {
                            switch (mySignalControl.fixedTimer1.phase_stage) {
                                case 1: // green
                                    g.setColor(Color.green) ;
                                    break ;
                                case 2: // yellow
                                    g.setColor(Color.yellow) ;
                                    break ;
                                case 3: // red
                                    g.setColor(Color.red) ;
                                    break ;
                            }   // switch
                            //System.out.println("Fixed-R1-phase="+(i+1)+", stage="+mySignalControl.fixedTimer1.phase_stage) ;
                        } else if (i==mySignalControl.getR2PhaseID()) {
                            switch (mySignalControl.fixedTimer2.phase_stage) {
                                case 1: // green
                                    g.setColor(Color.green) ;
                                    break ;
                                case 2: // yellow
                                    g.setColor(Color.yellow) ;
                                    break ;
                                case 3: // red
                                    g.setColor(Color.red) ;
                                    break ;
                            }   // switch
                            //System.out.println("Fixed-R2-phase="+(i+1)+", stage="+mySignalControl.fixedTimer2.phase_stage) ;
                        }   // end if active phase in ring 1 or 2
                    } else if (sim_flag && mySignalControl.control_type==1) {
                        // actuated
                        if (i==mySignalControl.getR1PhaseID()) {
                            switch (mySignalControl.actuatedTimer1.phase_stage) {
                                case 1: // green
                                    g.setColor(Color.green) ;
                                    break ;
                                case 2: // yellow
                                    g.setColor(Color.yellow) ;
                                    break ;
                                case 3: // red
                                    g.setColor(Color.red) ;
                                    break ;
                            }   // switch
                        } else if (i==mySignalControl.getR2PhaseID()) {
                            switch (mySignalControl.actuatedTimer2.phase_stage) {
                                case 1: // green
                                    g.setColor(Color.green) ;
                                    break ;
                                case 2: // yellow
                                    g.setColor(Color.yellow) ;
                                    break ;
                                case 3: // red
                                    g.setColor(Color.red) ;
                                    break ;
                            }   // switch
                        }   // if active phase in ring 1 or 2
                    }   // end if control-type
                } else {
                    // red or inactive phase
                    g.setColor(Color.red) ;
                }   // if green phase
                
                g.fillRect(mySignalControl.myPhases[i].sigbar_UL.x,
                           mySignalControl.myPhases[i].sigbar_UL.y, 
                           Math.abs(mySignalControl.myPhases[i].sigbar_UL.x-mySignalControl.myPhases[i].sigbar_LR.x), 
                           Math.abs(mySignalControl.myPhases[i].sigbar_UL.y-mySignalControl.myPhases[i].sigbar_LR.y)
                ) ; // fill rectangle
            }
        }
        
        //======================================================================
        // plot vehicles
        //System.out.println("repaint") ;
        for (int i=0; i<4; i++) {
            for (int j=0; j<MAX_VEH_SIZE; j++) {
                if (myVehicles[i][j] != null) {
                    if (myVehicles[i][j].finished) {
                        myVehicles[i][j] = null ;
                    } else {
                       //System.out.println("repaint-1") ;
                        g.setColor(myVehicles[i][j].myColor) ;
                        g.fillPolygon(myVehicles[i][j].getXpoints(), myVehicles[i][j].getYpoints(), 4) ;
                    }
                }   // if finished
            }   // j
        }   // i
    
/*
        // temporary plot route lines
        g.setColor(Color.red) ;    // red lines
        g.setStroke(myLineStroke) ;
        int route_size ;
        
        route_size = route_NB.length ;
        for (int i=0; i<route_size; i++){
            int pts = route_NB[i].getDataSize() ;
            int x1, x2, y1, y2 ;
            System.out.println("pts="+pts);
            x1 = route_NB[i].getPosition(0).x ;
            y1 = route_NB[i].getPosition(0).y ;
            for (int j=1; j<pts; j++){
                x2 = route_NB[i].getPosition(j).x ;
                y2 = route_NB[i].getPosition(j).y ;
                g.drawLine(x1, y1, x2, y2) ;
                x1 = x2 ;
                y1 = y2 ;
            }   // j
        }   // i

        route_size = route_SB.length ;
        for (int i=0; i<route_size; i++){
            int pts = route_SB[i].getDataSize() ;
            int x1, x2, y1, y2 ;
            System.out.println("pts="+pts);
            x1 = route_SB[i].getPosition(0).x ;
            y1 = route_SB[i].getPosition(0).y ;
            for (int j=1; j<pts; j++){
                x2 = route_SB[i].getPosition(j).x ;
                y2 = route_SB[i].getPosition(j).y ;
                g.drawLine(x1, y1, x2, y2) ;
                x1 = x2 ;
                y1 = y2 ;
            }   // j
        }   // i
        
        route_size = route_EB.length ;
        for (int i=0; i<route_size; i++){
            int pts = route_EB[i].getDataSize() ;
            int x1, x2, y1, y2 ;
            System.out.println("pts="+pts);
            x1 = route_EB[i].getPosition(0).x ;
            y1 = route_EB[i].getPosition(0).y ;
            for (int j=1; j<pts; j++){
                x2 = route_EB[i].getPosition(j).x ;
                y2 = route_EB[i].getPosition(j).y ;
                g.drawLine(x1, y1, x2, y2) ;
                x1 = x2 ;
                y1 = y2 ;
            }   // j
        }   // i
         
        route_size = route_WB.length ;
        for (int i=0; i<route_size; i++){
            int pts = route_WB[i].getDataSize() ;
            int x1, x2, y1, y2 ;
            System.out.println("pts="+pts);
            x1 = route_WB[i].getPosition(0).x ;
            y1 = route_WB[i].getPosition(0).y ;
            for (int j=1; j<pts; j++){
                x2 = route_WB[i].getPosition(j).x ;
                y2 = route_WB[i].getPosition(j).y ;
                g.drawLine(x1, y1, x2, y2) ;
                x1 = x2 ;
                y1 = y2 ;
            }   // j
        }   // i
*/        
        //System.out.println("left="+intsc_left) ;
        //System.out.println("top="+intsc_top) ;
        //System.out.println("width="+intsc_width) ;
        //System.out.println("height="+intsc_height) ;
        
    }   // end of paint

    public boolean keyDown(Event e,int k)
    {
	return(true);
    }

    // update toolbar index
    public void newstatus(int index, String str)
    {
        sb.setStatusBarText(0, str) ;
        toolbarIndex = index ;

        switch (toolbarIndex) {
            case 0: // pop volume & turning screen
                
                if (frame_demand==null){
                    popTrafficDemand();
                } else {    // not null
                    if (frame_demand.isShowing()==false){
                        //System.out.println("showing=false") ;
                        popTrafficDemand();
                    } else {
                        frame_demand.toFront();
                        //System.out.println("showing=true") ;
                    }
                }
                
                //repaint();
                //Window win = new Window(frame_demand) ;
                //win.toFront() ;
                //frameParent.toBack() ;
               break ;
            case 1: // pop control type screen
                
                if (frame_controlType==null){
                    popControlType();
                } else {    // not null
                    if (frame_controlType.isShowing()==false){
                        //System.out.println("showing=false") ;
                        popControlType();
                    } else {
                        frame_controlType.toFront();
                        //System.out.println("showing=true") ;
                    }
                }
                break ;
            case 2: // signal control
                // called from runThread4
                 if (frame_controller==null){
                    popSignalController();
                 } else {    // not null
                    if (frame_controller.isShowing()==false){
                        //System.out.println("showing=false") ;
                        popSignalController();
                    } else {
                        frame_controller.toFront();
                        //System.out.println("showing=true") ;
                    }
                 }
                 controller_configured = true ;
                /*
                 if (frame_timing==null){
                    popSignalTiming();
                } else {    // not null
                    if (frame_timing.isShowing()==false){
                        //System.out.println("showing=false") ;
                        popSignalTiming();
                    } else {
                        frame_timing.toFront();
                        //System.out.println("showing=true") ;
                    }
                }
                */
                //try {Thread.sleep(1000) ;}
                //catch (InterruptedException ie) {} ;

                //LCD_update() ;
                break ;
            case 3: // start simulation
                parsePhaseSequence() ;
                if (controller_configured) {
                    startSim() ;
                } else {
                    msgBox_title = "Controller Configuration" ;
                    msgBox_body = "Please configure signal controller first!" ;
                    msgBox_flag = true ;
                }
                break ;
            case 4: // stop simulation
                pauseSim() ;
                // debug only
                //parsePhaseSequence() ;
                //parseSignalTimingFromVirtualController() ;
                break ;
        }
	//repaint();
    }
    
    public void changeDrawScale(float scale) {
        draw_scale += scale ;
        if (draw_scale > 5.0f) {
            draw_scale = 5.0f;
        }
        else if (draw_scale < 0.1f) { 
            draw_scale = 0.1f ;
        }
        sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
        imageResize() ;
    }
    public void imageResize() {
        Rectangle r = bounds();
        scaledxlate.X = CInt(0.5f * r.width * (1 - draw_scale) + draw_scale * translate.X);
        scaledxlate.Y = CInt(0.5f * r.height * (1 - draw_scale) + draw_scale * translate.Y);
        repaint();
        //PictureBox1.Invalidate()
    }


    //public boolean mouseDown(Event e, int x, int y)
    public boolean mouseDown(int x, int y)
    {
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;
    
        switch (toolbarIndex) {
            case 3:  // move
                e0 = new mPoint(x, y) ;
                mouseHoldDown = true ;
                break ;

        }   // end switch
        return(true);
    }
    

    // transform mouse click position on the screen to pixel location on the digital map
    public mPointF transform(mPoint input) {
        mPointF ptf = new mPointF(0f,0f);
        if (draw_scale == 1f) {
            ptf.X = (input.X - translate.X);
            ptf.Y = (input.Y - translate.Y);
        } else {
            ptf.X = (input.X - scaledxlate.X) / draw_scale;
            ptf.Y = (input.Y - scaledxlate.Y) / draw_scale;
        }
        return ptf;
    }
    
    // transform location saved onthe DB to relative position on screen
    public mPoint drawTransform(mPointF input) {
        mPoint ptf = new mPoint(0,0);
        if (draw_scale == 1) {
            ptf.X = CInt(input.X + translate.X + translate_delta.X);
            ptf.Y = CInt(input.Y + translate.Y + translate_delta.Y);
        } else {
            ptf.X = CInt(input.X * draw_scale + scaledxlate.X + scaledxlate_delta.X);
            ptf.Y = CInt(input.Y * draw_scale + scaledxlate.Y + scaledxlate_delta.Y);
        }

        return ptf;
    }

    //public boolean mouseDrag(Event e, int x, int y)
    public boolean mouseLeftDrag(int x, int y)
    {
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;

        switch (toolbarIndex){
            case 3: // 
   
                
        } // end switch
        return (true);
    }
    
    //public boolean mouseUp(Event e, int x, int y)
    public boolean mouseLeftUp(int x, int y)
    {
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;
        if (sim_flag == false) {
            // paused
            int i, j ;
            for (i=0; i<4; i++) {
                for (j=0; j<MAX_VEH_SIZE; j++) {
                    if (myVehicles[i][j]!=null) {
                        if (myVehicles[i][j].isClicked(x, y)) {
                            // pop vehicle info screen
                            int k = myVehicles[i][j].getRouteID() ;
                            sb.setStatusBarText(3, "VID:("+i+","+j+","+k+"), POS="+
                                myVehicles[i][j].getPosition().x+","+myVehicles[i][j].getPosition().y+",heading="+ myVehicles[i][j].headingVector().toStr()) ;
                            i=4 ;
                            j=MAX_VEH_SIZE ;
                            break ;
                        }   // if
                    }   // if not null
                }   // for j
            }   // for i
        }   //if
        /*
        switch (toolbarIndex){
            case 0: // // pointer, select
                repaint();
                break;


        }   // switch
        */
        return (true) ;
    }
    public void print(){
        // print current frame
        //PrintUtilities.printComponent(this) ;    //, printPageFormat); 
        //PrintUtilities pu = new PrintUtilities(this) ;
        hd_pu = new PrintUtilities(this) ;
        hd_pu.print();
    }   // print
    
    public void printPageSetup(){
        // print current frame
//        PrintUtilities.printPageSetup() ;   
//        PrintUtilities pu = new PrintUtilities(this) ;
        hd_pu = new PrintUtilities(this) ;
        hd_pu.printPageSetup();
    }   // printPageSetup
    
    public float calc_turning_proportion(int link_index) {
        float pTot ;
        pTot = Float.valueOf(txt_turnL[link_index].getText()).floatValue() +
            Float.valueOf(txt_turnT[link_index].getText()).floatValue() +
            Float.valueOf(txt_turnR[link_index].getText()).floatValue()  ;
        return pTot ;
    }

 
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }   // end function

    
    public float distanceOf(mPointF p1 , mPointF p2 ) {
        float dist, dx, dy ;
        dx = p1.X - p2.X;
        dy = p1.Y - p2.Y;
        dist = new Double(Math.sqrt(dx*dx + dy*dy)).floatValue();
        return dist;
    }

    public int CInt(double val){
        return new Double(val).intValue();
    }    
    
    public int CInt(float val){
        return new Float(val).intValue();
    }
    
    public int CInt(String str){
    //    int index = str.indexOf('-') ;
    //    System.out.println("CInt Str="+str) ;
    //    if (index >0) {
    //        int data = new Integer(str.substring(index+1)).intValue();
    //        return -data ;
    //    } else {
        
            return new Integer(str.trim()).intValue();
    //    }
    }
    public float CFloat(String str){
        return new Float(str.trim()).floatValue();
    }  
    public float CFloat(double val){
        return new Double(val).floatValue();
    }   
    public String CStr(double val){
        return new Double(val).toString();
    }   
    public String CStr(float val){
        return new Float(val).toString();
    }    
    public String CStr2(float val){
        float val2 = CInt(val*100)/100 ;
        return new Float(val2).toString();
    }    
    public String CStr(int val){
        return new Integer(val).toString();
    }   
 
    // -----------------------------------------------------------------------------
    /** Pop up a window to display intersection traffic demand data */    
    // -----------------------------------------------------------------------------
    // pop signal control type screen
    public void popControlType() {
        
        // open a frame
        frame_controlType = new myWindow("Actuation Type") ;
        frame_controlType.setLocation(200,0) ;
        frame_controlType.setSize(150, 150) ;
        MenuBar mb = new MenuBar() ; 
        Menu m = new Menu("File") ;
        MenuItem exitItem = new MenuItem("Close") ;

        m.add(exitItem) ;

        mb.add(m) ;     // add menu
        frame_controlType.setMenuBar(mb) ;

        exitItem.setShortcut(new MenuShortcut(KeyEvent.VK_X)) ;
        ActionListener frame_controlType_exit_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                // save control type selection
                if (fixed.getState()) {
                    // fixed
                    mySignalControl.control_type = 0 ;
                } else if (actuated.getState()) {
                    // actuated
                    mySignalControl.control_type = 1 ;
                }
                frame_controlType.dispose() ;
            }
        } ;
        exitItem.addActionListener(frame_controlType_exit_listener) ;
        frame_controlType.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1 ; c.gridheight = 1 ;
        c.insets = new Insets(5,5,5,5) ; // 5-pixel margins on all sides
        CheckboxGroup cbg = new CheckboxGroup();
        fixed = new Checkbox("Fixed Timing", cbg, false) ;
        actuated = new Checkbox("Actuated", cbg, true) ;
        frame_controlType.add(fixed, c) ;
        c.gridy = 1;
        frame_controlType.add(actuated, c) ;
        c.gridy = 2 ;
        Button btnOK = new Button("OK") ;
        frame_controlType.add(btnOK, c) ;
        btnOK.addActionListener(frame_controlType_exit_listener) ;
        
        if (mySignalControl.control_type==0) {
            // fixed
            fixed.setState(true) ;
        } else if (mySignalControl.control_type==1) {
            // actuated
            actuated.setState(true) ;
        }
        frame_controlType.invalidate() ;
        //frame_controlType.repaint() ;
        frame_controlType.setVisible(true) ;
        frame_controlType.setResizable(false) ;
        frame_controlType.show() ;
    }
    
    // pop traffic demand screen
    public void popTrafficDemand() {

        // get demands from main screen inputs, E, W, N & S
        traffic_demand[0][0][0] = myDB.EB_data.getVolume() ;
        traffic_demand[0][1][0] = myDB.WB_data.getVolume() ;
        traffic_demand[0][2][0] = myDB.NB_data.getVolume() ;
        traffic_demand[0][3][0] = myDB.SB_data.getVolume() ;
        
        // open a frame
        frame_demand = new myWindow("Intersection Traffic Demand") ;
        frame_demand.setLocation(300,0) ;
        frame_demand.setSize(500,280) ;
        frame_demand.validate() ;
        frame_demand.setVisible(true) ;
        //frame_demand.setResizable(false) ;
        //frame_demand.show() ;

        MenuBar mb = new MenuBar() ; 
        Menu m = new Menu("File") ;
        MenuItem exitItem = new MenuItem("Close") ;

        m.add(exitItem) ;

        mb.add(m) ;     // add menu
        frame_demand.setMenuBar(mb) ;

        exitItem.setShortcut(new MenuShortcut(KeyEvent.VK_X)) ;
        ActionListener frame_demand_exit_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                // save turning porpotions
                for (int i=0; i<NUM_LINK; i++) {
                    traffic_demand[0][i][0] = Float.valueOf(txt_demand[i].getText()).floatValue() ;
                    traffic_demand[0][i][1] = Float.valueOf(txt_turnL[i].getText()).floatValue() ;
                    traffic_demand[0][i][2] = Float.valueOf(txt_turnT[i].getText()).floatValue() ;
                    traffic_demand[0][i][3] = Float.valueOf(txt_turnR[i].getText()).floatValue() ;
                    // sync linkData volume
                    // E, W, N & S, 6/14/06
                    myDB.EB_data.setVolume(traffic_demand[0][0][0]) ;
                    myDB.WB_data.setVolume(traffic_demand[0][1][0]) ;
                    myDB.NB_data.setVolume(traffic_demand[0][2][0]) ;
                    myDB.SB_data.setVolume(traffic_demand[0][3][0]) ;
                    // sync actuated_control volume inputs
                    //actuatedScreen.EB.approach.volume.setText(String.valueOf(myDB.EB_data.getVolume())) ;
                    //actuatedScreen.WB.approach.volume.setText(String.valueOf(myDB.WB_data.getVolume())) ;
                    //actuatedScreen.NB.approach.volume.setText(String.valueOf(myDB.NB_data.getVolume())) ;
                    //actuatedScreen.SB.approach.volume.setText(String.valueOf(myDB.SB_data.getVolume())) ;
                }
                frame_demand.dispose() ;
            }
        } ;
        exitItem.addActionListener(frame_demand_exit_listener) ;

        // handle event on password key ENTER
        KeyListener turningKey = new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            } 
            public void keyReleased(KeyEvent ke) {
            }
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER ) {
                    for (int i=0; i<4; i++) {
                        float turning_total = calc_turning_proportion(i) ;
                        lbl_turnTotal[i].setText(
                            String.valueOf(turning_total)) ;
                       // if (turning_total != 100f ) {
                       //     messageBox("")
                       // }
                    }
                }                
            }
        } ;
                            
        ActionListener calc_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                for (int i=0; i<4; i++) {
                        float turning_total = calc_turning_proportion(i) ;
                        lbl_turnTotal[i].setText(
                            String.valueOf(turning_total)) ;
                        if (turning_total != 100f ) {
                            messageBox("Turning proportion", "Link "+String.valueOf(i+1)+": Total turning proportion not equal 100%") ;
                        }
                    }
            }
        } ;
 
        frame_demand.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        
        c.gridx = 0 ; c.gridy = 0; c.gridwidth = 6 ; c.gridheight = 1 ;
        c.insets = new Insets(1,50,1,0) ; // 5-pixel margins on all sides
        Label myTitle = new Label("Intersection Traffic Demand & Turning Proportion") ;
        myTitle.setFont(new Font("SansSerif", Font.BOLD , 16)) ;
        myTitle.setForeground(new Color(0,0,218)) ;
        frame_demand.setBackground(new Color(200, 200, 200)) ;
        frame_demand.add(myTitle, c) ;

        c.gridx = 0 ;
        c.gridy = 1; c.gridwidth = 1 ; c.gridheight = 1 ;
        c.insets = new Insets(3,3,0,3) ; // 5-pixel margins on all sides
        frame_demand.add(new Label(" "), c) ;
        c.gridy = 2;
        c.insets = new Insets(0,3,3,3) ;
        frame_demand.add(new Label("Approach",Label.CENTER), c) ;
        c.gridy = 3; c.insets = new Insets(3,3,3,3) ;
        frame_demand.add(new Label("East Bound"), c) ;
        c.gridy = 4;
        frame_demand.add(new Label("West Bound"), c) ;
        c.gridy = 5;
        frame_demand.add(new Label("North Bound"), c) ;
        c.gridy = 6;
        frame_demand.add(new Label("South Bound"), c) ;

        c.gridx = 1 ;
        c.gridy = 1; c.gridwidth = 1 ; c.gridheight = 1 ;
        //c.anchor = GridBagConstraints.CENTER ; 
        c.insets = new Insets(3,3,0,3) ; // 5-pixel margins on all sides
        frame_demand.add(new Label("Traffic"), c) ;
        c.gridy = 2; c.insets = new Insets(0,3,3,3) ;
        frame_demand.add(new Label("Demand (veh/h)"), c) ;
        c.insets = new Insets(3,3,3,3) ;
        for (int i=0; i<NUM_LINK; i++) {
            txt_demand[i] = new TextField("", 6) ;
            c.gridy = 3+i ;
            frame_demand.add(txt_demand[i], c) ;
        }
        c.gridx = 2 ;
        c.gridy = 1; c.gridwidth = 3 ; c.gridheight = 1 ;
        c.insets = new Insets(3,3,0,3) ; // 5-pixel margins on all sides
        frame_demand.add(new Label("Vehicle Turning Proportion (%)"), c) ;
        c.gridx = 2 ; c.gridy = 2; c.gridwidth = 1 ;
        c.insets = new Insets(0,3,3,3) ;
        frame_demand.add(new Label("Left",Label.CENTER), c) ;
        c.gridx = 3 ; 
        frame_demand.add(new Label("Thru",Label.CENTER), c) ;
        c.gridx = 4 ; 
        frame_demand.add(new Label("Right",Label.CENTER), c) ;
        c.gridx = 5 ; 
        frame_demand.add(new Label("    Total (%)"), c) ;
        c.insets = new Insets(3,3,3,3) ;
        c.gridx = 2 ; c.gridy = 3; c.gridwidth = 1 ;
        for (int i=0; i<NUM_LINK; i++) {
            txt_turnL[i] = new TextField("25", 3) ;
            c.gridy = 3+i ;
            frame_demand.add(txt_turnL[i], c) ;
            txt_turnL[i].addKeyListener(turningKey) ;
        }
        c.gridx = 3 ; c.gridy = 3; c.gridwidth = 1 ;
        for (int i=0; i<NUM_LINK; i++) {
            txt_turnT[i] = new TextField("50", 3) ;
            c.gridy = 3+i ;
            frame_demand.add(txt_turnT[i], c) ;
            txt_turnT[i].addKeyListener(turningKey) ;
        }
        c.gridx = 4 ; c.gridy = 3; c.gridwidth = 1 ;
        for (int i=0; i<NUM_LINK; i++) {
            txt_turnR[i] = new TextField("25", 3) ;
            c.gridy = 3+i ;
            frame_demand.add(txt_turnR[i], c) ;
            txt_turnR[i].addKeyListener(turningKey) ;
        }
        c.gridx = 5 ; c.gridy = 3; c.gridwidth = 1 ;
        for (int i=0; i<NUM_LINK; i++) {
            lbl_turnTotal[i] = new Label("100.0",Label.CENTER) ;
            c.gridy = 3+i ;
            frame_demand.add(lbl_turnTotal[i], c) ;
        }
        
        c.gridx = 2 ;
        c.gridy = 7 ; c.gridwidth = 3 ;
        Button close_button = new Button("Save & Close") ;
        frame_demand.add(close_button, c) ;
        close_button.addActionListener(frame_demand_exit_listener) ;
        c.gridx = 5 ;
        c.gridy = 7 ; c.gridwidth = 1 ;
        Button calc_button = new Button("Calculate") ;
        frame_demand.add(calc_button, c) ;
        calc_button.addActionListener(calc_listener) ;
        
        // restore default or existing data
        for (int i=0; i<NUM_LINK; i++) {
            txt_demand[i].setText(String.valueOf(traffic_demand[0][i][0])) ;
            txt_turnL[i].setText(String.valueOf(traffic_demand[0][i][1])) ;
            txt_turnT[i].setText(String.valueOf(traffic_demand[0][i][2])) ;
            txt_turnR[i].setText(String.valueOf(traffic_demand[0][i][3])) ;
        }

        frame_demand.setLocation(300,0) ;
        //frame_demand.setSize(600, 250) ;
        frame_demand.invalidate() ;
        //frame_demand.repaint() ;
        frame_demand.setVisible(true) ;
        frame_demand.setResizable(false) ;
        frame_demand.show() ;
        // frame_demand.reshape(100, 50, 600, 300) ;
        
        //if (!beginNewSim_flag) {
        //    disableDemandEntry() ;
        //}
    }


    // -----------------------------------------------------------------------------
    /** Pop up a window to display intersection traffic signal controller settings */    
    // -----------------------------------------------------------------------------
    public void popSignalController() {
        
        // open a frame
        frame_controller = new myWindow("Traffic Signal Controller") ;
        //frame_controller.setLocation(0,0) ;
        frame_controller.setSize(1024,605) ;    // image size=1024x574
        frame_controller.validate() ;
        frame_controller.setVisible(true) ;
        //frame_controller.setResizable(false) ;
        //frame_controller.show() ;

        load_LCD_Screens() ;    // read LCD screen database
        // load LCD formats
        load_LCD_Data_Format() ;
        
        try {
            // read from JAR file
            InputStream is = getClass().getResourceAsStream("Econolite5.png") ;
            myControllerImage = ImageIO.read(is) ;
        } catch (IOException e) {
            System.out.println("popSignalTiming: Failed to read controller image file!") ;
        }
        controlPanel = new imageCanvas(myControllerImage) ; 
        controlPanel.setSize(controlPanel.getWidth(), controlPanel.getHeight()) ;
        
        MouseAdapter myMouseAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //System.out.println("(x,y)=("+e.getX()+","+e.getY()+")") ;
                int x = e.getX() ;
                int y = e.getY() ;
                String _char = "" ;
                boolean valid_click = true ;
                if (x>=556 && x<=598) {
                    // Functin 1, 3, 5, 7
                    lcd_menu_layer = 0 ;    // reset menu layer
                    if (y>=104 && y<=129) {
                        // key F1, main menu
                        //_char = "A" ;
                        //System.out.println("Key F1") ;
                        lcd_page_ID_last = 1 ;  
                        lcd_page_ID = 1 ;                        
                    } else if (y>=155 && y<=180) {
                        // key F3
                        //lcd_page_ID = 3 ;
                        if (lcd_page_ID>1) {
                            // back 1 level per F3 key pressed
                            lcd_page_ID = lcd_page_ID/16 ;
                        }
                        //_char = "B" ;
                        //System.out.println("Key F3") ;
                    } else if (y>=207 && y<=231) {
                        // key F5
                        //lcd_page_ID = 5 ;
                        //_char = "C" ;
                        //System.out.println("Key F5") ;
                    } else if (y>=258 && y<=283) {
                        // key F7, status display
                        lcd_page_ID = 7 ;
                        //_char = "D" ;
                        //System.out.println("Key F7") ;
                    }   // if y
                } else if (x>=618 && x<=658) {
                    // Function 2, 4, 6, 8
                    lcd_menu_layer = 0 ;    // reset menu layer
                    if (y>=104 && y<=129) {
                        // key F2, next screen
                        //lcd_page_ID = 2 ;
                        //_char = "E" ;
                        //System.out.println("Key F2") ;
                        /*
                        if (lcd_page_ID-lcd_page_ID_last <=1) {
                            
                            lcd_page_ID_last = lcd_page_ID ;
                            lcd_page_ID++ ;
                        } else {
                            lcd_page_ID_last = lcd_page_ID ;
                            lcd_page_ID = lcd_page_ID*16 ;
                        }
                         **/
                        // next screen
                        next_screen_sel = true ;
                        next_page_sel = false ;
                        old_page_index = lcd_page_index ;
                        lcd_page_ID = 36 ; // prompt sel message
                        //if (myLCD_PAGES[lcd_page_index].rightPage>0) {
                        //    lcd_page_ID = myLCD_PAGES[lcd_page_index].rightPage ;
                        //}
                    } else if (y>=155 && y<=180) {
                        // key F4
                        //lcd_page_ID = 4 ;
                        //_char = "F" ;
                        //System.out.println("Key F4") ;
                        // next data
                    } else if (y>=207 && y<=231) {
                        // key F6
                        //lcd_page_ID = 6 ;
                        //_char = "G" ;
                        //System.out.println("Key F6") ;
                        // next page
                        old_page_index = lcd_page_index ;
                        lcd_page_ID = 36 ;
                        next_page_sel = true ;
                        next_screen_sel = false ;
                        //if (myLCD_PAGES[lcd_page_index].nextPage>0) {
                        //    lcd_page_ID = myLCD_PAGES[lcd_page_index].nextPage ;
                        //}
                    } else if (y>=258 && y<=283) {
                        // key F8
                        //lcd_page_ID = 8 ;
                        //_char = "H" ;
                        //System.out.println("Key F8") ;
                    }   // if y
                } else if (x>=690 && x<=775 && y>=244 && y<=283) {
                    // enter key
                    //_char = "I" ;
                    //System.out.println("Key ENTER") ;
                    
                } else if (x>=683 && x<=715 && y>=125 && y<=157) {
                    // left arrow
                    //_char = "J" ;
                    //System.out.println("Key LEFT") ;
                    //int old_page_index = myLCD_PAGES[lcd_page_index].previousPage ;
                    
                    if (old_page_index>0) {
                        if (next_screen_sel && myLCD_PAGES[old_page_index].leftPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].leftPage ;
                            next_screen_sel = false ;
                        }
                        if (next_page_sel && myLCD_PAGES[old_page_index].previousPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].previousPage ;
                            next_page_sel = false ;
                        }
                        //old_page_index=-1 ;
                    }
                    int idx = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
                    if (myLCD_PAGES[lcd_page_index].dataEntryIndex>=0) {
                        // data entry mode
                        myLCD_Formats[idx].moveCursorLeft() ;
                    }
                } else if (x>=748 && x<=781 && y>=125 && y<=157) {
                    // right arrow
                    //_char = "K" ;
                    //System.out.println("Key RIGHT") ;
                    //int old_page_index = myLCD_PAGES[lcd_page_index].previousPage ;
                    if (old_page_index>0) {
                        if (next_screen_sel && myLCD_PAGES[old_page_index].rightPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].rightPage ;
                            next_screen_sel = false ;
                        }
                        if (next_page_sel && myLCD_PAGES[old_page_index].nextPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].nextPage ;
                            next_page_sel = false ;
                        }
                        //old_page_index=-1 ;
                    }
                    int idx = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
                    if (idx>=0) {
                        // data entry mode
                        myLCD_Formats[idx].moveCursorRight() ;
                    }
                } else if (x>=716 && x<=748 && y>=92 && y<=124) {
                    // up arrow
                    //_char = "L" ;
                    //System.out.println("Key UP") ;
                    if (lcd_page_ID == 36 && (next_page_sel || next_screen_sel)) {
                        if (next_screen_sel && myLCD_PAGES[old_page_index].previousPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].previousPage ;
                            next_screen_sel = false ;
                        }
                        if (next_page_sel && myLCD_PAGES[old_page_index].previousPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].previousPage ;
                            next_page_sel = false ;
                        }
                    } else {
                        // move cursor
                        int idx = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
                        if (idx>=0) {
                            // data entry mode
                            myLCD_Formats[idx].moveCursorUp() ;
                        }
                    }
                } else if (x>=716 && x<=748 && y>=158 && y<=190) {
                    // down arrow
                    //_char = "M" ;
                    //System.out.println("Key DOWN") ;
                    
                    if (lcd_page_ID == 36 && (next_page_sel || next_screen_sel)) {
                        //System.out.println("Down, lcd_ID, lcd_old_idx="+lcd_page_ID +","+old_page_index) ;
                        
                        if (next_screen_sel && myLCD_PAGES[old_page_index].nextPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].nextPage ;
                            next_screen_sel = false ;
                        }
                        if (next_page_sel && myLCD_PAGES[old_page_index].nextPage>0) {
                            lcd_page_ID = myLCD_PAGES[old_page_index].nextPage ;
                            next_page_sel = false ;
                        }
                    } else {
                        // move cursor
                        int idx = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
                        if (idx>=0) {
                            // data entry mode
                            myLCD_Formats[idx].moveCursorDown() ;
                        }
                    }   // if lcd_page_ID == 36
                } else if (x>=802 && x<=844) {
                // #########################################################
                    // # key 1, 4, 7, spec function
                    if (y>=101 && y<=127) {
                        // key 1
                        //_char = "1" ;
                        //System.out.println("Key #1") ;
                        lcd_num_key = 1 ;
                        //processKeyEntered(1) ;
                    } else if (y>=153 && y<=178) {
                        // key 4
                        //_char = "4" ;
                        //System.out.println("Key #4") ;
                        lcd_num_key = 4 ;
                        //processKeyEntered(4) ;
                    } else if (y>=204 && y<=229) {
                        // key 7
                        //_char = "7" ;
                        //System.out.println("Key #7") ;
                        lcd_num_key = 7 ;
                        //processKeyEntered(7) ;
                    } else if (y>=255 && y<=281) {
                        // key special function
                        //_char = "N" ;
                        //System.out.println("Key SPEC FUNC") ;
                        lcd_num_key = 99 ;  // special function
                    }   // if y
                    
                    if (myLCD_PAGES[lcd_page_index].dataEntryIndex>=0) {
                        data_entry_mode = true ;
                        processKeyEntered(lcd_num_key) ;
                    } else {
                        data_entry_mode = false ;
                        if (lcd_num_key>=0) {
                            lcd_menu_layer++ ;
                            lcd_page_ID_last = lcd_page_ID ;
                            lcd_page_ID = lcd_page_ID*16 + lcd_num_key ;
                        }   // if lcd_num_key
                    }   // if myLCD
                //**************************************************************
                } else if (x>=854 && x<=896) {
                    // # key 2, 5, 8, 0/toggle
                    if (y>=101 && y<=127) {
                        // key 2
                        //_char = "2" ;
                        //System.out.println("Key #2") ;
                        lcd_num_key = 2 ;
                        //processKeyEntered(2) ;
                    } else if (y>=153 && y<=178) {
                        // key 5
                        //_char = "5" ;
                        //System.out.println("Key #5") ;
                        lcd_num_key = 5 ;
                        //processKeyEntered(5) ;
                    } else if (y>=204 && y<=229) {
                        // key 8
                        //_char = "8" ;
                        //System.out.println("Key #8") ;
                        lcd_num_key = 8 ;
                        //processKeyEntered(8) ;
                    } else if (y>=255 && y<=281) {
                        // key 0 / toggle
                        //_char = "0" ;
                        //System.out.println("Key #0") ;
                        lcd_num_key = 0 ;
                        //processKeyEntered(0) ; // 0 or toggle
                    }   // if y
                    
                    if (myLCD_PAGES[lcd_page_index].dataEntryIndex>=0) {
                        data_entry_mode = true ;
                        processKeyEntered(lcd_num_key) ;
                    } else {
                        data_entry_mode = false ;
                        if (lcd_num_key>=0) {
                            lcd_menu_layer++ ;
                            lcd_page_ID_last = lcd_page_ID ;
                            lcd_page_ID = lcd_page_ID*16 + lcd_num_key ;
                        }   // if lcd_num_key
                    }   // if myLCD
                //**************************************************************
                } else if (x>=905 && x<=947) {
                    // # key 3, 6, 9, clear
                    if (y>=101 && y<=127) {
                        // key 3
                        //_char = "3" ;
                        //System.out.println("Key #3") ;
                        lcd_num_key = 3 ;
                        //processKeyEntered(3) ;
                    } else if (y>=153 && y<=178) {
                        // key 6
                        //_char = "6" ;
                        //System.out.println("Key #6") ;
                        lcd_num_key = 6 ;
                        //processKeyEntered(6) ;
                    } else if (y>=204 && y<=229) {
                        // key 9
                        //_char = "9" ;
                        //System.out.println("Key #9") ;
                        lcd_num_key = 9 ;
                        //processKeyEntered(9) ;
                    } else if (y>=255 && y<=281) {
                        // key clear
                        //_char = "Q" ;
                        //System.out.println("Key CLEAR") ;
                        lcd_num_key = KEY_CLEAR ;  // clear data field
                    }   // if y
                    
                    if (myLCD_PAGES[lcd_page_index].dataEntryIndex>=0) {
                        data_entry_mode = true ;
                        processKeyEntered(lcd_num_key) ;
                    } else {
                        data_entry_mode = false ;
                        if (lcd_num_key>=0) {
                            lcd_menu_layer++ ;
                            lcd_page_ID_last = lcd_page_ID ;
                            lcd_page_ID = lcd_page_ID*16 + lcd_num_key ;
                        }   // if lcd_num_key
                    }   // if myLCD

                // #########################################################
                } else {
                    valid_click = false ;
                }   // if x 
                
                if (valid_click) {
                    System.out.println("page_ID="+lcd_page_ID) ;
                    //LCD_repaint(_char) ;
                    if (lcd_page_ID==36) {
                        // cursor sel key prompt
                        //old_page_index = myLCD_PAGES[lcd_page_index].previousPage ;
                        System.out.println("old index="+old_page_index) ;
                    }
                }   // if valis click ?
                LCD_update() ;  // 2/9/2007
            }   // mouse clicked
            
            public void mouseEntered(MouseEvent e) {
                //LCD_repaint("a") ; 
                LCD_update() ;
            }
        } ;
        
        controlPanel.addMouseListener(myMouseAdapter) ;
        
        frame_controller.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0 ; c.gridy = 0; c.gridwidth = 1 ; c.gridheight = 1 ;
        frame_controller.add(controlPanel, c) ;
        
        frame_controller.invalidate() ;
        frame_controller.setVisible(true) ;
        frame_controller.setResizable(true) ;
        //frame_controller.setCenter() ;
        frame_controller.show() ;
        // at end of sub
        LCD_READY = true ;
    }   // popSignalController


    /*
    public void LCD_repaint(String _char) { /// obsoleted
        Graphics g = controlPanel.getGraphics() ;

        g.setFont(new Font("Courier", Font.PLAIN, 13)) ;

        int x_offset = 8 ;
        int y_offset = 6+10 ;
        g.setColor(new Color(0, 221, 111)) ;
        g.fillRect(143+x_offset, 115, 282, 162) ;
        g.setColor(Color.black) ;
        for (int i=0; i<40; i++) {
            for (int j=0; j<16; j++) {
                g.drawString(_char, 143+x_offset+i*7, 109+y_offset+j*10) ; 
            }   // for j
        }   // for i
        
    }   // LCD_repaint
    */
    public void parsePhaseSequence() {
        int index = getLCDPageIndex(273) ; //  # F1-1-1 MAIN 0X111 #273
        if (index >=0) {
            int data_size=2 ;   // 4 char data field
            int phase = 0 ;
            int col = 3 ;
            for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
                // row 6 ring 1
                phase = CInt(myLCD_PAGES[index].parseData(5,  col+i*(data_size+1), data_size)) ;
                mySignalControl.setRing1Seq_i(i, phase) ;
                
                // row 7 ring 2
                phase = CInt(myLCD_PAGES[index].parseData(6,  col+i*(data_size+1), data_size)) ;
                mySignalControl.setRing2Seq_i(i, phase) ;    
            }
            // Barrier
            char key ;
            for (int j=0; j<mySignalControl.NUM_PHASES; j++) {
                col=5+j*3 ;
                key = myLCD_PAGES[index].PAGE_CHARS[7][col] ;
                if (key=='^') {
                    mySignalControl.CG[j] = true ;
                } else {
                    mySignalControl.CG[j] = false ;
                }
            }
            /* debug
            System.out.println("phase sequence") ;
            System.out.println(" ring 1:"+mySignalControl.toStrSeq1()) ;
            System.out.println(" ring 2:"+mySignalControl.toStrSeq2()) ;
            System.out.println("Barrier:"+mySignalControl.toStrCG()) ;
             **/
        } else {
            System.out.println("parsePhaseSequence(): Page 273 not found!") ;
            System.out.println("Ring 1: "+mySignalControl.toStrSeq1()) ;
            System.out.println("Ring 2: "+mySignalControl.toStrSeq2()) ;
           
        }

    }   // parsePhaseSequence
      
    public void parseSignalTimingFromVirtualController() {
        int index1 = getLCDPageIndex(289) ; // page # F1-2-1 MAIN 0X121 #289
        int index2 = getLCDPageIndex(4624) ; // page # F1-2-1.0 MAIN 0X1210 #4624
        int index3 = getLCDPageIndex(303) ; // page # F1-2-1R MAIN 0X12F #303
        int index4 = getLCDPageIndex(4639) ; // page # F1-2-1.0R MAIN 0X121F #4639
        // phase 1-8
        if (index1>=0 && index2>=0 && index3>=0 && index4>=0) {
            int j=0 ;
            int col=8 ;  //init data column index
            int data_size=4 ;   // 4 char data field
            float data = 0f ;
            for (j=0; j<mySignalControl.NUM_PHASES; j++) {
                int i, index ;
                if (j<8) {
                    index = index1 ;    // page # F1-2-1 MAIN 0X121 #289 
                    i=j ;
                } else {
                    index = index3 ;    // page # F1-2-1R MAIN 0X12F #303
                    i=j-8 ;
                }
                
                // row 3 min green
                data = CFloat(myLCD_PAGES[index].parseData(2,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setInitial(data) ;
                // row 6 walk
                data = CFloat(myLCD_PAGES[index].parseData(5,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setWalk(data) ;
                // row 7 ped clear
                data = CFloat(myLCD_PAGES[index].parseData(6,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setPedClear(data) ;
                // row 8, extension
                data = CFloat(myLCD_PAGES[index].parseData(7,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setExtension(data) ;
                // row 11, max1 green
                data = CFloat(myLCD_PAGES[index].parseData(10,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setMax1(data) ;
                // row 12, max2 green
                data = CFloat(myLCD_PAGES[index].parseData(11,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setMax2(data) ;
                
                if (i<8) {
                    index = index2 ;    // page # F1-2-1.0 MAIN 0X1210 #4624
                } else {
                    index = index4 ;    // page # F1-2-1.0R MAIN 0X121F #4639
                }
                
                // row 3 yellow
                data = CFloat(myLCD_PAGES[index].parseData(2,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setYellow(data) ;
                // row 4 red
                data = CFloat(myLCD_PAGES[index].parseData(3,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setRed(data) ;
                // row 9 max init
                data = CFloat(myLCD_PAGES[index].parseData(8,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setMaxInit(data) ;
                // row 13 min gap
                data = CFloat(myLCD_PAGES[index].parseData(12,  col+i*data_size, data_size)) ;
                mySignalControl.myPhases[j].setMinGap(data) ;
                
                // debug
                /*
                System.out.println("phase"+(j+1)) ;
                System.out.println("MIN_GRN, WALK, PED_CLR, EXT, MAX1, MAX2, YEL, RED, MAX_INIT, MIN_GAP") ;
                System.out.println( mySignalControl.myPhases[j].getInitial() + ", " +
                                    mySignalControl.myPhases[j].getWalk() + ", " +
                                    mySignalControl.myPhases[j].getPedClear() + ", " +
                                    mySignalControl.myPhases[j].getExtension() + ", " +
                                    mySignalControl.myPhases[j].getMax1() + ", " +
                                    mySignalControl.myPhases[j].getMax2() + ", " + 
                                    mySignalControl.myPhases[j].getYellow() + ", " +
                                    mySignalControl.myPhases[j].getRed() + ", " +
                                    mySignalControl.myPhases[j].getMaxInit() + ", " +
                                    mySignalControl.myPhases[j].getMinGap() ) ;
                 */
            }   // for loop
        } else {
            System.out.println("parseSignalTimingfromVirtualController(): Page 289/303/4624/4639 not found!") ;
        }
    }   // parseSignalTimingfromVirtualController
    
    public void processKeyEntered(int keyVal) {
        int index = myLCD_PAGES[lcd_page_index].dataEntryIndex ;
        int row = myLCD_Formats[index].row_index ;
        int col = myLCD_Formats[index].col_index ;
        String str = CStr(keyVal) ;
        
        if (keyVal<=9 && keyVal>=0) {
            System.out.println("Data mode="+myLCD_Formats[index].data_mode) ;
            switch (myLCD_Formats[index].data_mode) {
                case 1: // *, 2-digit numerical data
                    char c = str.charAt(0) ;
                    //System.out.println("num key pressed="+myLCD_Formats[index].num_key_pressed) ;
                    if (myLCD_Formats[index].num_key_pressed==0) {
                        // key pressed 1st time at current field
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                        System.out.println(" * data key="+c);
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1]=='0') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                        }   // if leading zero '0' -> blank
                    }
                    break ;
                case 2: // T, text
                    if (keyVal==0) {
                        processToggleKey(index, row, col) ;
                    }   // if keyVal==0 toggle key
                    break ;
                case 3: // X, toggle between X and .
                    if (keyVal==0) {
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]=='.') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='X' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='.';
                        }
                    }   // if keyVal==0 toggle key
                    break ;
                case 4: // ^ special for controller sequence page
                    if (keyVal==0) {
                        if (lcd_page_ID==273) {
                            // F1-1-1
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]=='.') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='^' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row-1][col]='|' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row-2][col]='|' ;
                            } else {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='.';
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row-1][col]=' ' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row-2][col]=' ' ;
                            }
                        }
                    }   // if keyVal==0 toggle key
                    break ;
                case 5: // &, 3-digit numerical data
                    c = str.charAt(0) ;
                    //System.out.println("num key pressed="+myLCD_Formats[index].num_key_pressed) ;
                    if (myLCD_Formats[index].num_key_pressed==0) {
                        // key pressed 1st time at current field
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        System.out.println(" & data key="+c);
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2]=='0') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1]=='0') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                            }
                        }   // if leading zero '0' -> blank
                    }
                    break ;
                case 6:     // % sign, float ##.# data
                    c = str.charAt(0) ;
                    //System.out.println("num key pressed="+myLCD_Formats[index].num_key_pressed) ;
                    if (myLCD_Formats[index].num_key_pressed==0) {
                        // key pressed 1st time at current field
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '0' ;
                        System.out.println("% data key="+c);
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] = c ;
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3]=='0') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3]=' ' ;
                        }   // if
                    }   // if
                    
            }   // switch
            myLCD_Formats[index].num_key_pressed++ ;
        }   // if keyVal 0~9
        else if ( keyVal == KEY_CLEAR ) {
            // clear data field
            switch (myLCD_Formats[index].data_mode) {
                case 1: // *, 2-digit numerical data
                    // clear
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]= '0' ;
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                    break ;
                case 5: // &, 3-digit numerical data
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='0' ;
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                    break ;
                case 6:     // % sign, float ##.# data
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]='0' ;
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '0' ;
                    myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = ' ' ;
                    break ;
            }   // switch
            // clear data field
            myLCD_Formats[index].num_key_pressed = 0 ;
        }   // end if
    }   // processKeyEntered
    
    // process LCD toggle key '0'
    private void processToggleKey(int index, int row, int col) {
        
        switch (lcd_page_ID) {
            case 277:   // port 2 configuration
                switch (row) {
                    case 2: // TERMINAL / NTCIP
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'L') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = ' ' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'T' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'C' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'I' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'P' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = 'T' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'R' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'M' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'L' ;                            
                        }
                        break ;
                    case 3: // YES/NO
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                        }
                        break ;
                    case 5: // 1200/2400/4800/9600/19.2k/38.4k/57.6k/115.2k
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == '0') {
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '2') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '2' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '4' ;
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '4') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '4' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '8' ;                                
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '8') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '9' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '6' ;                                
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '6') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '9' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '.' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '2' ;  
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'k' ;
                            }
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'k') {
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '9') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '3' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '8' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '4' ;  
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '8') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '5' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '7' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '6' ;                            
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '7') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '5' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '2' ;                            
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '5') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = ' ' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = ' ' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '2' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '0' ; 
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = '0' ; 
                            }
                        }   // 0 or k
                        break ;
                    case 6: //7, E, 1/8, N, 1
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == 'N') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] = '7' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'E' ;
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == 'E') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] = '8' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'N' ; 
                        }
                        break ;
                    case 11: // YES/NO
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                        }
                        break ;
                }   // switch row
                break ; // page 277
            case 4432:   // port 2 configuration, next page
                if (row==2 || row==5) {
                    // YES/NO
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                    }
                }   // if row=2 or 5
                break ; // page 4432
            case 278:   // port 3 configuration
                switch (row) {
                    case 2: // TELEM / NTCIP
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'M') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'T' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'C' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'I' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'P' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = 'T' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'L' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'M' ;                            
                        }
                        break ;
                    case 3: case 4: // YES/NO
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                        }
                        break ;
                    case 8: // HALF/FULL
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'L') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'H' ;                                
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'A' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'F' ;
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'F') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'F' ;                                
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'U' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'L' ;
                        }   // FULL OR HALF DUPLEX
                        break ;
                    case 9: // 1200/2400/4800/9600/19.2k/38.4k/57.6k/115.2k
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == '0') {
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '2') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '2' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '4' ;
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '4') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '4' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '8' ;                                
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '8') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '9' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '6' ;                                
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] == '6') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '9' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '.' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '2' ;  
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'k' ;
                            }
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'k') {
                            if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '9') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '3' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '8' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '4' ;  
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '8') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '5' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '7' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '6' ;                            
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '7') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '5' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '2' ;                            
                            } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '5') {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-5] = ' ' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-4] = ' ' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '1' ;
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = '2' ;                                
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = '0' ; 
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = '0' ; 
                            }
                        }   // 0 or k
                        break ;
                    case 10: //8, 0, 1/8, E, 1/7, E, 1/8, N, 1
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == '0') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] = '8' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'E' ;
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == 'E' && 
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] == '8') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] = '7' ;
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == 'E' && 
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] == '7') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-6] = '8' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = 'N' ;
                        } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] == 'N') {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-3] = '0' ;
                        }
                        break ;
                }   // switch row
                break ; // page 278
            case 4448:   // port 3 configuration, next page
                if (row==5 || row==8) {
                    // YES/NO
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                    }
                }   // if row=8 or 5
                break ; // page 4448
            case 280:   // options
                if (row==7 || row==9 || row==11) {
                    // YES/NO
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                    } else {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                    }
                }   // if row
                break ; // page 280
            case 294:   // START FLASH DATA
                if (row==10 || row==11) {
                    // YELLOW/RED/GREEN-WALK/GREEN
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'Y') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'R' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'D' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = ' ' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'R') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'G' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'R' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = '-' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = 'W' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = 'A' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = 'K' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'G') {
                        if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] == '-') {
                            for (int i=5; i<10; i++) {
                                myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+i] = ' ' ;
                            }
                        } else {
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'Y' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'E' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'L' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'L' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'O' ;
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'W' ;                            
                        }
                   }
                }   // if row
                break ; // page 294
            case 4752:   // CONTROLLER option DATA
                if (row>0 || row<15) {
                    // ON/OFF
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'N') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'F' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'F' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'F') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'N' ;                            
                    }
                }   // if row
                break ; // page 4752
            case 305:   // COORDINATOR OPTIONS
                if (row==2 || row==3) {   // Split unit
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] == '%') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'C' ;
                        updateCoordDisplay(row, 's') ;   // PAGE 370
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'S') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = '%' ;    
                        updateCoordDisplay(row, '%') ;   // PAGE 370
                    }
                } else if (row==4) {
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'T') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'D' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'S') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'P' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'A' ;  
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'N' ;  
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'P') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = '2' ;  
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = ' ' ;  
                    }
                } else if (row==5) {    // INTERCNT SRC.
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'H') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'I' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'C' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'N') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'M' ;  
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'T') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'H' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'D' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'W' ;  
                    }
                } else if (row==7) {    // TRANISITION
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'S') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'A' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'D' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'D' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'Y' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] == 'Y') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'D' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'W' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = ' ' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] == 'W') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'M' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'H' ;
                    }
                }   // IF
                break ; // PAGE 305
            case 306:   // COORD MANUAL AND SPLIT DEMAND
                if (row==2) {
                    // ON/OFF
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'N') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'F' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'F' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'F') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'N' ;                            
                    }                    
                }   // if
                break ; // PAGE 306
            case 321:   // PRIORITY PREEMPTOR 1 to 6
            case 322: case 323: case 324: case 325: case 326:
                if (row>=10 && row<=14) {
                    // YES/NO
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'O') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = 'Y' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'S' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'S') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-2] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col-1] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'O' ;                            
                    }
                }   // if row
                break ;     // PAGE 321 to 326
            case 337:   // NIC/TOD CLOCK/CALENDAR DATA
                if (row==10) {
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] == 'Y') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'R' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'F' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'R' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = 'C' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+10]= 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+11]= 'I' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+12]= 'M' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+13]= 'E' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] == 'N') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'A' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = 'V' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+10]= ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+11]= ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+12]= ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+13]= ' ' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] == 'V') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'A' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = 'Y' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = 'C' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = ' ' ;
                    }                    
                }   // if row
                break ; // page 337
            case 340:   // NIC/TOD HOLIDAY PROGRAM
            case 5440:  case 5441:
                if (row>=3 || row<=14) {
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] == 'L') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'I' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'X' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'D' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] == 'I') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'L' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'O' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'A' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'T' ;                        
                    }
                }   // if
                break ; // page 340
            case 357:   //SPEED DETECTORS
            case 367:   case 5887:  case 94207:
                if (row==14) {
                    if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'I') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'C' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'I' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'M' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = 'T' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = 'R' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+10]= 'S' ;
                    } else if (myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col] == 'C') {
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col]   = 'I' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+1] = 'N' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+2] = 'C' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+3] = 'H' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+4] = 'E' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+5] = 'S' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+6] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+7] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+8] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+9] = ' ' ;
                        myLCD_PAGES[lcd_page_index].PAGE_CHARS[row][col+10]= ' ' ;
                    }
                }   // if
                break ;     // page 357
        }   // switch lcd_page_ID
        
    }   // procecssToggle Key
    
    private void updateCoordDisplay(int row, char ch) {
        int idx = getLCDPageIndex(370) ;
        switch (row) {
            case 2: // SPLIT
                myLCD_PAGES[idx].PAGE_CHARS[9][34]= ch ;
                myLCD_PAGES[idx].PAGE_CHARS[9][39]= ch ;
                break ;
            case 3: // OFFSET
                for (int i=6; i<=9; i++) {
                    myLCD_PAGES[idx].PAGE_CHARS[i][15]= ch ;
                }   // for     
                break ;
        }   // switch
    }   // upadte PAGE 370
    
    public void blinkCursor(int index) {
        Graphics g = controlPanel.getGraphics() ;
        g.setFont(new Font("Courier", Font.PLAIN, 13)) ;
        int x_offset = 8 ;
        int y_offset = 6 ;
        int i = myLCD_Formats[index].row_index ;
        int j = myLCD_Formats[index].col_index  ;
        g.setColor(LCD_BG_COLOR) ;
        g.fillRect(143+x_offset+j*7, 109+y_offset+i*10, 7, 10) ;
        
        if (blink_flag>0) {
            // normal display
            g.setColor(Color.black) ;
            g.drawString(new Character(myLCD_PAGES[lcd_page_index].PAGE_CHARS[i][j]).toString(), 
                143+x_offset+j*7, 109+y_offset+(i+1)*10) ; 
        } else {
            // display darkgray rectangle cursor
            g.setColor(Color.darkGray) ;
            g.fillRect(143+x_offset+j*7, 109+y_offset+i*10, 7, 10) ;
        }   // if
        blink_flag *= -1 ;
        //System.out.println("blink="+blink_flag+"i,j="+i+","+j);
    } // blink cursor
    
    public void LCD_update() {
        Graphics g = controlPanel.getGraphics() ;

        g.setFont(new Font("Courier", Font.PLAIN, 13)) ;

        int x_offset = 8 ;
        int y_offset = 6+10 ;
        g.setColor(LCD_BG_COLOR) ;
        g.fillRect(143+x_offset, 115, 282, 162) ;
        g.setColor(Color.black) ;
        if (!isLCDPageExists()) {
            // LCD page not found
            lcd_page_ID = lcd_page_ID_last ;
        }
        if (lcd_page_ID==36) {
            // cursor sel key prompt
            int i, j ;
            for (i=0; i<5; i++) {
                for (j=0; j<40; j++) {
                    g.drawString(new Character(myLCD_PAGES[old_page_index].PAGE_CHARS[i][j]).toString(), 143+x_offset+j*7, 109+y_offset+i*10) ; 
                }   // for j
            }   // for i
            for (i=5; i<9; i++) {
                for (j=0; j<40; j++) {
                    g.drawString(new Character(myLCD_PAGES[lcd_page_index].PAGE_CHARS[i][j]).toString(), 143+x_offset+j*7, 109+y_offset+i*10) ; 
                }   // for j
            }   // for i
            for (i=9; i<16; i++) {
                for (j=0; j<40; j++) {
                    g.drawString(new Character(myLCD_PAGES[old_page_index].PAGE_CHARS[i][j]).toString(), 143+x_offset+j*7, 109+y_offset+i*10) ; 
                }   // for j
            }   // for i
        } else {
            for (int i=0; i<16; i++) {
                for (int j=0; j<40; j++) {
                    g.drawString(new Character(myLCD_PAGES[lcd_page_index].PAGE_CHARS[i][j]).toString(), 143+x_offset+j*7, 109+y_offset+i*10) ; 
                }   // for j
            }   // for i
        }
    }   // LCD_update

    public boolean isLCDPageExists() {
        int i ;
        boolean page_found = false ;
        //int lcd_page_index = 0 ;
        for (i=0; i<NUM_LCD_PAGES; i++) {
            //System.out.println("page ID="+myLCD_PAGES[i].page_ID) ;
            //System.out.println("sel ID="+lcd_page_ID) ;
            if (myLCD_PAGES[i].page_ID == lcd_page_ID) {
                /*
                if (lcd_page_ID == 36 && (next_page_sel || next_screen_sel)) {
                    //store current page index to myLCD_PAGES[i].previousPage
                    //myLCD_PAGES[i].previousPage = lcd_page_index ;
                    old_page_index = lcd_page_index ;
                    System.out.println("once") ;
                } 
                 */
                lcd_page_index = i ;
                page_found = true ;
                break ;
            }
        }
        //System.out.println("page ID 22="+myLCD_PAGES[22].page_ID) ;
        //System.out.println("page ID="+lcd_page_ID+", exist="+page_found) ;
        return page_found ;
    }
    
    public int getLCDPageIndex(int _page_id) {
        int index = -1 ;
        for (int i=0; i<NUM_LCD_PAGES; i++) {
            if (myLCD_PAGES[i].page_ID == _page_id) {
                index = i ;
                break ;
            }
            //System.out.println("page ID="+myLCD_PAGES[i].page_ID) ;
        }
        return index ;
    }
    
    public int getLCDFormatIndex(int _page_id) {
        int index = -1 ;
        for (int i=0; i<NUM_LCD_FORMATS; i++) {
            if (myLCD_Formats[i].page_ID == _page_id) {
                index = i ;
                break ;
            }
        }
        return index ;
    }
    
    public void load_LCD_Screens() {
        InputStream is = getClass().getResourceAsStream("LCD_SCRNS.txt") ;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null ;
        lcd_page_index = -1 ;
        try {
            while (( line = br.readLine()) != null){
                //System.out.println(line) ;
                if (line.length()==0) {
                    lcd_row_index ++ ;
                    lcd_col_index=0 ;   // reset column index                
                } else if (line.charAt(0)!='#') {
                    // not comment line
                    int len = line.length() ;
                    if (len>40) { len=40 ;}
                    //System.out.println("len="+len) ;
                    for (int i=0; i<len; i++) {  // 16 line x 40 char LCD 
                            myLCD_PAGES[lcd_page_index].PAGE_CHARS[lcd_row_index][lcd_col_index] = line.charAt(i) ;
                        lcd_col_index++ ;
                        //System.out.println("col index="+lcd_col_index) ;
                    }
                    lcd_row_index ++ ;
                    lcd_col_index=0 ;   // reset column index
                } else {
                    // comment
                    lcd_row_index = 0 ;
                    lcd_col_index = 0 ;
                    lcd_page_index ++ ;
                    myLCD_PAGES[lcd_page_index] = new LCD_PAGE() ;
                    int index = line.indexOf('#', 2) ;
                    int end_idx = line.indexOf(',', index) ;
                    if (end_idx>0) {
                        // additional page info exists
                        myLCD_PAGES[lcd_page_index].page_ID = CInt(line.substring(index+1, end_idx)) ;
                        index = end_idx+1 ;
                        end_idx = line.indexOf(',', index) ;
                        if (end_idx>0) {
                            //System.out.println(line) ;
                            //System.out.println("start and end index"+index+","+end_idx) ;
                            //System.out.println("substring="+line.substring(index, end_idx)) ;
                            myLCD_PAGES[lcd_page_index].previousPage = CInt(line.substring(index, end_idx)) ;
                            index = end_idx+1 ;
                            end_idx = line.indexOf(',', index) ;
                            if (end_idx>0) {
                                myLCD_PAGES[lcd_page_index].nextPage = CInt(line.substring(index, end_idx)) ;
                                index = end_idx+1 ;
                                end_idx = line.indexOf(',', index) ;
                                if (end_idx>0) {
                                    myLCD_PAGES[lcd_page_index].leftPage = CInt(line.substring(index, end_idx)) ;
                                    index = end_idx+1 ;
                                    end_idx = line.indexOf(',', index) ;
                                    if (end_idx>0) {
                                        myLCD_PAGES[lcd_page_index].rightPage = CInt(line.substring(index, end_idx)) ;
                                    } else {
                                        end_idx = line.indexOf('@', index) ;    // data entry
                                        if (end_idx>0) {
                                            // data entry format exists
                                            myLCD_PAGES[lcd_page_index].rightPage = CInt(line.substring(index, end_idx)) ;
                                            // extract data entry info
                                            extractDataEntryInfo(line.substring(end_idx+1)) ;
                                        } else {
                                            myLCD_PAGES[lcd_page_index].rightPage = CInt(line.substring(index)) ;
                                        } 
                                    }
                                } else {
                                    end_idx = line.indexOf('@', index) ;    // data entry
                                    if (end_idx>0) {
                                        // data entry format exists
                                        myLCD_PAGES[lcd_page_index].leftPage = CInt(line.substring(index, end_idx)) ;
                                        // extract data entry info
                                        extractDataEntryInfo(line.substring(end_idx+1)) ;
                                    } else {
                                        myLCD_PAGES[lcd_page_index].leftPage = CInt(line.substring(index)) ;
                                    }
                                }
                            } else {
                                end_idx = line.indexOf('@', index) ;    // data entry
                                if (end_idx>0) {
                                    // data entry format exists
                                    myLCD_PAGES[lcd_page_index].nextPage = CInt(line.substring(index, end_idx)) ;
                                    // extract data entry info
                                    extractDataEntryInfo(line.substring(end_idx+1)) ;
                                } else {
                                    myLCD_PAGES[lcd_page_index].nextPage = CInt(line.substring(index)) ;
                                }                        
                            }
                        } else {
                            end_idx = line.indexOf('@', index) ;    // data entry
                            if (end_idx>0) {
                                // data entry format exists
                                myLCD_PAGES[lcd_page_index].previousPage = CInt(line.substring(index, end_idx)) ;
                                // extract data entry info
                                extractDataEntryInfo(line.substring(end_idx+1)) ;
                            } else {
                                myLCD_PAGES[lcd_page_index].previousPage = CInt(line.substring(index)) ;
                            }
                        }
                    } else {
                        end_idx = line.indexOf('@', index) ;    // data entry
                        if (end_idx>0) {
                            // data entry format exists
                            myLCD_PAGES[lcd_page_index].page_ID = CInt(line.substring(index+1, end_idx)) ;
                            // extract data entry info
                            extractDataEntryInfo(line.substring(end_idx+1)) ;
                        } else {
                            myLCD_PAGES[lcd_page_index].page_ID = CInt(line.substring(index+1)) ;
                        }
                    }
                    //System.out.println("page ID="+myLCD_PAGES[lcd_page_index].page_ID) ;
                }   // if
            }   // while
            NUM_LCD_PAGES = lcd_page_index ;
        } catch (IOException ioe) {
            System.out.println(ioe.toString()) ;
        }
    }   // read LCD screens
    
    // read LCD DATA ENTRY field format
    public void load_LCD_Data_Format() {
        InputStream is = getClass().getResourceAsStream("LCD_SCRNS_DATA.txt") ;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null ;
        int lcd_format_row_index = -1 ;
        //int lcd_format_col_index = -1 ;
        int lcd_format_index = -1 ;
        boolean data_valid = false ;
        try {
            while (( line = br.readLine()) != null){
                if (line.charAt(0)=='#') {
                    // comment line, get id # and else
                    data_valid = false ;
                    lcd_format_index ++ ;
                    myLCD_Formats[lcd_format_index] = new LCD_DATA_ENTRY() ;
                    int index = line.indexOf('#', 2) ; // beginning of LCD ID
                    int end_idx = line.indexOf('@', index) ; // beginning of row start index
                    if (end_idx>0) {
                        int myPage_ID = CInt(line.substring(index+1, end_idx)) ;
                        myLCD_Formats[lcd_format_index].page_ID = myPage_ID ;
                        int page_index = getLCDPageIndex(myPage_ID) ;
                        if (page_index>0) {
                            // page exists
                            myLCD_Formats[lcd_format_index].lcd_page_index = page_index ;
                            myLCD_PAGES[page_index].dataEntryIndex = lcd_format_index ; 
                            // get starting row & len
                            index = end_idx+1 ;
                            end_idx = line.indexOf('/', index) ; // data separator
                            myLCD_Formats[lcd_format_index].startRow = CInt(line.substring(index, end_idx))-1 ;
                            lcd_format_row_index = myLCD_Formats[lcd_format_index].startRow ;

                            data_valid = true ;
                        } else {
                            System.out.println("Read LCD data format error.") ;
                        } // if page_index >0
                    }   // if end_idx > 0 
                } else if (data_valid) {
                    // read data foeld format info
                    int len = line.length() ;
                    if (len>40) { len=40 ;}
                    //System.out.println(line) ;
                    //System.out.println("len="+len) ;
                    //lcd_format_col_index=0 ;   // reset column index
                    for (int i=0; i<len; i++) {  // 16 line x 40 char LCD 
                        myLCD_Formats[lcd_format_index].PAGE_CHARS[lcd_format_row_index][i] = line.charAt(i) ;
                        //lcd_format_col_index++ ;
                        //System.out.println("col index="+lcd_col_index) ;
                    }
                    lcd_format_row_index ++ ;
                    
                    // update row length
                    myLCD_Formats[lcd_format_index].rowLength = lcd_format_row_index - myLCD_Formats[lcd_format_index].startRow ;
                }
            }   // while
            NUM_LCD_FORMATS = lcd_format_index ;
            
            // check data format read
            /*
            for (int i=0; i<NUM_LCD_FORMATS; i++) {
                int idx = myLCD_Formats[i].lcd_page_index ;
                System.out.println("index="+idx) ;
                System.out.println("page ID="+myLCD_PAGES[idx].page_ID) ;
                int stRow = myLCD_Formats[i].startRow ;
                System.out.println("start row="+stRow) ;
                int rowLen = myLCD_Formats[i].rowLength ;
                System.out.println("row length="+rowLen) ;
                for (int j=0; j<rowLen; j++) {
                    String data = "" ;
                    for (int k=0; k<40; k++) {
                        data += myLCD_Formats[i].PAGE_CHARS[j+stRow][k] ;
                    }
                    System.out.println(data) ;
                }
            }
            */
            //System.out.println("Format Pages = " + lcd_format_index);
        } catch (IOException ioe) {
            System.out.println("Data Field" + ioe.toString()) ;
        }
    }   // read LCD screens data format
    
    public void extractDataEntryInfo(String info) {
        // data string  xx/xx/xx, separated by /
        
    }
    
    public void saveTimingPlan() {
        // EB
        if (leftturn_EB.isProtective()) {
            // pertective phase
            //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[0])) {
                // ring 2
                mySignalControl.setRing2Seq_i(LEFT_ASSIGN_PH[0]-5, LEFT_ASSIGN_PH[0]) ;  
            //} else {
                // ring 1
            //    mySignalControl.setRing1Seq_iLeft(LEFT_ASSIGN_PH[0]-1) ;  
            //}
            mySignalControl.protected_left[0] = true ;
        } else if (leftturn_EB.isPermissive()) {
            // permissive phase
            //if (mySignalControl.inRing2(LEFT_ASSIGN_PH[0])) {
                // ring 2
                mySignalControl.setRing2Seq_i(2, 8) ; 
            //} else {
                // ring 1
            //    mySignalControl.setRing1Seq_iThru(LEFT_ASSIGN_PH[0]-1) ;
            //}
            mySignalControl.protected_left[0] = false ;
        }
        // WB
        if (leftturn_WB.isProtective()) {
            // pertective phase
            //if (LEFT_ASSIGN_PH[1]>4) {
                // ring 2
            //    mySignalControl.setRing2Seq_iLeft(LEFT_ASSIGN_PH[1]-5) ;  
            //} else {
                // ring 1
                mySignalControl.setRing1Seq_i(LEFT_ASSIGN_PH[1]-1, LEFT_ASSIGN_PH[1]) ;  
            //}
            mySignalControl.protected_left[1] = true ;
        } else if (leftturn_WB.isPermissive()) {
            // permissive phase
            //if (LEFT_ASSIGN_PH[1]>4) {
                // ring 2
            //    mySignalControl.setRing2Seq_iThru(LEFT_ASSIGN_PH[1]-5) ; 
            //} else {
                // ring 1
                mySignalControl.setRing1Seq_i(2, 4) ;
            //}
            mySignalControl.protected_left[1] = false ;
        }
        // NB
        if (leftturn_NB.isProtective()) {
            // pertective phase
            //if (LEFT_ASSIGN_PH[2]>4) {
                // ring 2
                mySignalControl.setRing2Seq_i(LEFT_ASSIGN_PH[2]-5, LEFT_ASSIGN_PH[2]) ;  
            //} else {
                // ring 1
            //    mySignalControl.setRing1Seq_iLeft(LEFT_ASSIGN_PH[2]-1) ;  
            //}
            mySignalControl.protected_left[2] = true ;
        } else if (leftturn_NB.isPermissive()) {
            // permissive phase
            //if (LEFT_ASSIGN_PH[2]>4) {
                // ring 2
                mySignalControl.setRing2Seq_i(0, 6) ; 
            //} else {
                // ring 1
            //    mySignalControl.setRing1Seq_iThru(LEFT_ASSIGN_PH[2]-1) ;
            //}
            mySignalControl.protected_left[2] = false ;
        }
        // SB
        if (leftturn_SB.isProtective()) {
            // pertective phase
            //if (LEFT_ASSIGN_PH[3]>4) {
                // ring 2
            //    mySignalControl.setRing2Seq_iLeft(LEFT_ASSIGN_PH[3]-5) ;  
            //} else {
                // ring 1
                mySignalControl.setRing1Seq_i(LEFT_ASSIGN_PH[3]-1, LEFT_ASSIGN_PH[3]) ;  
            //}
            mySignalControl.protected_left[3] = true ;
        } else if (leftturn_NB.isPermissive()) {
            // permissive phase
            //if (LEFT_ASSIGN_PH[3]>4) {
                // ring 2
            //    mySignalControl.setRing2Seq_iThru(LEFT_ASSIGN_PH[3]-5) ; 
            //} else {
                // ring 1
                mySignalControl.setRing1Seq_i(0, 2) ;
            //}
            mySignalControl.protected_left[3] = false ;
        }

        // FIXED TIMING PLAN
        mySignalControl.myPhases[LEFT_ASSIGN_PH[0]-1].setInitial(timeEB.getLeftTime()) ;
        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].setInitial(timeEB.getThruTime()) ;

        mySignalControl.myPhases[LEFT_ASSIGN_PH[1]-1].setInitial(timeWB.getLeftTime()) ;
        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].setInitial(timeWB.getThruTime()) ;

        mySignalControl.myPhases[LEFT_ASSIGN_PH[2]-1].setInitial(timeNB.getLeftTime()) ;
        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].setInitial(timeNB.getThruTime()) ;

        mySignalControl.myPhases[LEFT_ASSIGN_PH[3]-1].setInitial(timeSB.getLeftTime()) ;
        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].setInitial(timeSB.getThruTime()) ;


    //System.out.println(mySignalControl.toStrSeq1()) ;
    //System.out.println(mySignalControl.toStrSeq2()) ;
        
    }
    // -----------------------------------------------------------------------------
    /* Pop up a window to display message */    
    // -----------------------------------------------------------------------------
    public void popMessageBox(String caption, String message) {
        // open a frame
        frame_msgbox = new myWindow(caption) ;
        //frame_msgbox.setLocation(400,50) ;
        frame_msgbox.setSize(300,150) ;
        frame_msgbox.setCenter() ;
        frame_msgbox.validate() ;
        frame_msgbox.setVisible(true) ;
        frame_msgbox.setResizable(false);
        //frame_msgbox.show() ;
/*
        ActionListener frame_msgbox_ok_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                
                frame_msgbox.dispose() ;
            }
        } ;
*/
        frame_msgbox.setLayout(new BorderLayout(1,1)) ;
        TextArea myTitle = new TextArea(message, 3, 60) ;
        myTitle.setFont(new Font("SansSerif", Font.PLAIN , 12)) ;
        myTitle.setForeground(new Color(0,0,218)) ;
        frame_msgbox.setBackground(new Color(200, 200, 200)) ;
        frame_msgbox.add("Center",myTitle) ;
        
        //Button btn_ok = new Button(" OK ") ;
        //frame_msgbox.add("South",btn_ok) ;
        //btn_ok.addActionListener(frame_msgbox_ok_listener) ;
        frame_msgbox.invalidate();
        frame_msgbox.show() ;
        frame_msgbox.toFront() ;
    } // popMessageBox


    // -----------------------------------------------------------------------------
    public mPointF vector(Point p1, Point p2 ) 
    {
        mPointF _vec ;
        _vec = new mPointF(p2.x - p1.x, p2.y - p1.y) ;
        return _vec;
    }    
    
    public mPointF vector(mPointF p1, mPointF p2 ) 
    {
        mPointF _vec ;
        _vec = new mPointF(p2.X - p1.X, p2.Y - p1.Y) ;
        return _vec;
    }

    public void view_RESET() {
        translate.X = 0;
        translate.Y = 0;
        scaledxlate.X = 0;
        scaledxlate.Y = 0;
        draw_scale = 1f;
        sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
        repaint();
    }
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        mouseDown(x,y);
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK)
            == InputEvent.BUTTON1_MASK) {
            mouseLeftUp(x,y);    
        }
    }
    
    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK)
            == InputEvent.BUTTON1_MASK) {
            mouseLeftDrag(x,y);
        }
    }
    
    public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
    }
    
    // -----------------------------------------------------------------------------
    /** Pop up a window to display message */    
    public void messageBox(String caption, String message) {

        // open a frame
        frame_msgbox = new myWindow(caption) ;
        frame_msgbox.setLocation(400,100) ;
        frame_msgbox.setSize(300,120) ;
        frame_msgbox.validate() ;
        frame_msgbox.setVisible(true) ;

        ActionListener frame_msgbox_ok_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                
                frame_msgbox.dispose() ;
            }
        } ;

        frame_msgbox.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.NONE ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0 ; c.gridy = 0; c.gridwidth = 7 ; c.gridheight = 1 ;
        c.insets = new Insets(10,10,10,10) ; // 5-pixel margins on all sides
        Label myTitle = new Label(message) ;
        myTitle.setFont(new Font("SansSerif", Font.PLAIN , 12)) ;
        myTitle.setForeground(new Color(0,0,218)) ;
        frame_msgbox.setBackground(new Color(200, 200, 200)) ;
        frame_msgbox.add(myTitle, c) ;
        
        c.gridx = 3; c.gridy = 1 ; c.gridwidth = 1 ;
        Button btn_ok = new Button(" OK ") ;
        frame_msgbox.add(btn_ok, c) ;
        btn_ok.addActionListener(frame_msgbox_ok_listener) ;

        frame_msgbox.show() ;

    }


    public void popAbout(){
        if (frmAbout.isShowing()==false) {
            frmAbout = new myWindow("About Actuated Signal Control Software") ;
            frmAbout.setSize(300, 140) ;
            frmAbout.setResizable(false);
            frmAbout.setLocation(100,100) ;
            //frmAbout.show() ;

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
            frmAbout.show();
        }
        else {
            frmAbout.show();
        }
    }

    public boolean isOverStopBar(Point _ul, Point _lr, Point pos, Point front, Point rear) {
        // check if pos locates inside _ul & _lr rectanglar stop bar
        if (pos.x>=_ul.x && pos.x <= _lr.x && pos.y >= _ul.y && pos.y <=_lr.y) {
            return true ;
        } else if (front.x>=_ul.x && front.x <= _lr.x && front.y >= _ul.y && front.y <=_lr.y) {
            return true ;
        } else if (rear.x>=_ul.x && rear.x <= _lr.x && rear.y >= _ul.y && rear.y <=_lr.y) {
            return true ;
        } else {
            return false ;
        }
    }
    public boolean isOverDetector(Point _det, Point pos, Point front, Point rear, int width, int length) {
        // check if pos locates inside detector square box
        if (pos.x>=_det.x && pos.x <= _det.x+width && 
            pos.y >= _det.y && pos.y <=_det.y+length) {
            return true ;
        } else if (front.x>=_det.x && front.x <= _det.x+width && 
            front.y >= _det.y && front.y <=_det.y+length) {
            return true ;
        } else if (rear.x>=_det.x && rear.x <= _det.x+width && 
            rear.y >= _det.y && rear.y <=_det.y+length) {
            return true ;
        } else {
            return false ;
        }
    }

    public boolean enteredIntsc(Point front) {
        if (front.x> intsc_left && front.x < intsc_right && 
            front.y > intsc_top && front.y < intsc_bottom) {
            return true ;
        } else {
            return false ;
        }
    }
}   // intscDrawArea class
