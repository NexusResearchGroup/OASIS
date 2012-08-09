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
import java.text.DecimalFormat ;
import javax.swing.table.* ;

public class intscDrawArea extends DoubleBufferedPanel
    implements MouseListener,  MouseMotionListener
{
    final int stepSignalTimeState_BUF_SIZE = 300 ;  // 5 minutes
    private stepSignalTimeState[] SavedSignalTime = new stepSignalTimeState[stepSignalTimeState_BUF_SIZE] ;
    private int savedSignalTime_steps = 0 ;
    private int savedSignalTime_ptr = 0 ;
    private boolean restoreLastSignalTiming_flag = false ;
    //private String statusBarMessage = "" ;
 
    private static Font monoFont = new Font("Monospaced", Font.BOLD, 14) ;
    private static Font sanSerifFont = new Font("SanSerif", Font.BOLD | Font.PLAIN, 14) ;
    private static Font serifFont = new Font("Serif", Font.BOLD, 14) ;
    private static Font dialogFontB14 = new Font("Dialog", Font.BOLD | Font.PLAIN, 14) ;
    private static Font dialogFontB12 = new Font("Dialog", Font.BOLD | Font.PLAIN, 12) ;
    private static Font dialogFont = new Font("Dialog", Font.PLAIN, 12) ;
    
    toolbar tb;                     // toolbar
    statusbar sb;                   // status bar
    final int grid = 8;             // drawarea grid size
    final int link_length = 300 ;   // pixel
    final int MAX_VEH_SIZE = 40 ;   // max vehicles at a link
    final int STOPBAR_WIDTH = 20 ;  // stop bar width in pixel
    final int DET_PERIOD = 100 ;    // 50 ms
    private final int KEY_CLEAR = 77 ;  // Controller clear key
    private DecimalFormat twoPlaces = new DecimalFormat("00");  // changed from "00.00" 8/20/07

    // approach vs pahse assignment, assume N-S main street, 8 phases
    // ***** DO NOT CHANGE THE PHASE ASSIGNMENT TO INTERSECTION GEOMETRY *****
    // N-S MAIN
    final int[] NS_LEFT_ASSIGN_PH = {7,3,5,1} ;    // E, W, N, S, left turn
    final int[] NS_THRU_ASSIGN_PH = {4,8,2,6} ;    // E, W, N, S, thru
    // E-W MAIN
    final int[] EW_LEFT_ASSIGN_PH = {5,1,3,7} ;    // E, W, N, S, left turn
    final int[] EW_THRU_ASSIGN_PH = {2,6,8,4} ;    // E, W, N, S, thru
    // ***** DO NOT CHANGE THE PHASE ASSIGNMENT TO INTERSECTION GEOMETRY *****
    
    private int[] LEFT_ASSIGN_PH = new int[4];  // E, W, N, S, left turn
    private int[] THRU_ASSIGN_PH = new int[4];  // E, W, N, S, thru
    
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
    public boolean controller_configured = false ;
    
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
    public float myGlobalTime = 0f ;
    
    // vehciles
    //Vehicle2D myCar ;
    Vehicle2D[][] myVehicles = new Vehicle2D[4][MAX_VEH_SIZE] ;   // 4 links, 40 vehicles max in each approach
    VehPointer[] myVehRef = new VehPointer[4*MAX_VEH_SIZE] ;
    
    // signal timing control algorithm
    SignalTiming mySignalControl = new SignalTiming() ;
    // N-S MAIN
    //TimingPanel timeEB = new TimingPanel("EB Timing", "7", "4") ; 
    //TimingPanel timeWB = new TimingPanel("WB Timing", "3", "8") ; 
    //TimingPanel timeNB = new TimingPanel("NB Timing", "5", "2") ; 
    //TimingPanel timeSB = new TimingPanel("SB Timing", "1", "6") ; 
    // E-W MAIN
    //TimingPanel timeEB = new TimingPanel("EB Timing", "5", "2") ; 
    //TimingPanel timeWB = new TimingPanel("WB Timing", "1", "6") ; 
    //TimingPanel timeNB = new TimingPanel("NB Timing", "3", "8") ; 
    //TimingPanel timeSB = new TimingPanel("SB Timing", "7", "4") ; 
    
    TimingPanel timeEB ; 
    TimingPanel timeWB ; 
    TimingPanel timeNB ; 
    TimingPanel timeSB ; 
 
    LTPanel leftturn_EB = new LTPanel("EB Left Turn") ;
    LTPanel leftturn_WB = new LTPanel("WB Left Turn") ;
    LTPanel leftturn_NB = new LTPanel("NB Left Turn") ;
    LTPanel leftturn_SB = new LTPanel("SB Left Turn") ;
    
    // window frame =================
    //public myWindow frameParent = new myWindow();
    myWindow frmAbout ;
    myWindow frame_msgbox, frame_msgboxYesNo ;
    myWindow frame_demand = new myWindow("Intersection Traffic Demand") ;       // volume & turning setting screen
    myWindow frame_controlType = new myWindow("Control Type") ;       // select control type
    myWindow frame_globalTime = new myWindow("Global Time") ;       // set global time
    myWindow frame_jumpTime = new myWindow("Jump Back to Time") ;       // set global time
    myWindow frame_timing = new myWindow("Intersection Signal Timing") ;        // signal timing plan window
    myWindow frame_controller = new myWindow("Intersection Signal Controller") ; // signal controller
    
    JFrame frmSignalTimingTable = new JFrame("View Signal Timing Data") ;
    private JTable stationTable = new JTable();

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
    Runnable runThreadToolbar = null ;    // runs toolbar newstatus(0,xx)
    Runnable runThreadRepaint = null ;    // draw area repaint
    Runnable runThreadVehGen = null ;    // vehicle generation
    Runnable runThreadVehSim = null ;    // collisin avoidance & simulation
    Runnable runThreadSignalControl = null ;    // run signal controller
    
    Runnable runThreadNBdet = null ;    // NB detection
    Runnable runThreadSBdet = null ;    // SB detection
    Runnable runThreadEBdet = null ;    // EB detection
    Runnable runThreadWBdet = null ;    // WB detection
    //Runnable runThreadSTP_NB = null ;    // stop on red light
    //Runnable runThreadSTP_SB = null ;    // stop on red light
    //Runnable runThreadSTP_EB = null ;    // stop on red light
    //Runnable runThreadSTP_WB = null ;    // stop on red light
    private Runnable runThread_gTimer ;
    private Thread t_gTimer ;
    private Runnable runThread_simStep ;
    private Thread t_simStep ;
    
    public Thread tCursor ;         // cursor thread, 1/9/07
    public Thread tSetVol ;
    public Thread tSetTiming ;
    public Thread tRepaint ;        // draw area refresh
    public Thread tGenVeh ;         // vehicle generation thread
    public Thread tVehicleSim ;     // vehicle simulation & collision avoidance thread
    public Thread tDetectionNB ;      // detector thread
    public Thread tDetectionSB ;      // detector thread
    public Thread tDetectionEB ;      // detector thread
    public Thread tDetectionWB ;      // detector thread
    //public Thread tStopOnRedNB ;      // stop on red light
    //public Thread tStopOnRedSB ;      // stop on red light
    //public Thread tStopOnRedEB ;      // stop on red light
    //public Thread tStopOnRedWB ;      // stop on red light
    public boolean SetVol_flag = false ;
    public boolean SetActuationType_flag = false ;
    public boolean SetTiming_flag = false ;
    public boolean msgBox_flag = false ;
    private String msgBox_title = "" ;
    private String msgBox_body = "" ; 
    boolean sim_flag = false ;          // simulation flag, true when running simulation
    boolean sim_step_toolbar = false ;  // step simulation used in new_status toolbar
    public boolean sim_step_once = false ;     // flag use to run simStep Thread once, 3/16/07
    boolean sim_alreadyStarted = false ;
    Random rdm = new Random(1) ;                 // a random class
    Checkbox fixed, actuated, actuated_m ;      // actuationi type checkbox
    TextField txt_gTime ;
    //PageFormat printPageFormat = new PageFormat() ;
    
    //private String msg_title = "" ;
    //private String msg_body = "" ;
    //private boolean msg_flag = false ;
    
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
        mySignalControl.sb = sb ;
        
        if (myDB.mainStreetNS) {
            // NS main street
            LEFT_ASSIGN_PH = NS_LEFT_ASSIGN_PH ;
            THRU_ASSIGN_PH = NS_THRU_ASSIGN_PH ;
            timeEB = new TimingPanel("EB Timing", "7", "4") ; 
            timeWB = new TimingPanel("WB Timing", "3", "8") ; 
            timeNB = new TimingPanel("NB Timing", "5", "2") ; 
            timeSB = new TimingPanel("SB Timing", "1", "6") ; 
        } else {
            // EW maiin street
            LEFT_ASSIGN_PH = EW_LEFT_ASSIGN_PH ;
            THRU_ASSIGN_PH = EW_THRU_ASSIGN_PH ;
            timeEB = new TimingPanel("EB Timing", "5", "2") ; 
            timeWB = new TimingPanel("WB Timing", "1", "6") ; 
            timeNB = new TimingPanel("NB Timing", "3", "8") ; 
            timeSB = new TimingPanel("SB Timing", "7", "4") ; 
        }
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
        
        //sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
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
                        route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length), 2) ;
                        route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length/2-5), 2) ;
                        route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, intsc_bottom+link_length/2-15), 2) ;
                        route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, intsc_bottom), 99) ; // 22.5 deg
                        route_NB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2), 99 ) ; // 45 deg
                        route_NB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2), 99) ; // 67.5 deg
                        if (myDB.EB_data.leftTurnLaneExists() || myDB.WB_data.leftTurnLaneExists()) {
                            route_NB[0].setRouteDBi(6, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH), 105) ; // 
                            route_NB[0].setRouteDBi(7, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),105) ; 
                        } else {
                            route_NB[0].setRouteDBi(6, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),105) ; // 
                            route_NB[0].setRouteDBi(7, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),105) ; 
                            
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
                            route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom+link_length),2) ;
                            route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_bottom),99) ; // 
                            route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; // 
                            route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                        } else {
                            route_NB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_bottom+link_length),2) ;
                            route_NB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_bottom),99 ) ; // 
                            route_NB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99 ) ; // 
                            route_NB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99 ) ; //                          
                        }
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_NB[0].setRouteDBi(4, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),105) ; // 90 deg
                            route_NB[0].setRouteDBi(5, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),105) ; 
                        } else {
                            route_NB[0].setRouteDBi(4, new Point(intsc_left, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),105) ; // 90 deg
                            route_NB[0].setRouteDBi(5, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),105) ; 
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
                    route_NB[1].setRouteDBi(0, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_bottom+link_length),2) ;
                    route_NB[1].setRouteDBi(1, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_bottom),99) ; // 
                    route_NB[1].setRouteDBi(2, new Point(intsc_right-myDB.LANE_WIDTH/4, intsc_bottom-myDB.LANE_WIDTH/2),99) ; // 
                    route_NB[1].setRouteDBi(3, new Point(intsc_right, intsc_bottom-myDB.LANE_WIDTH/2),104) ; // 
                    route_NB[1].setRouteDBi(4, new Point(intsc_right+link_length, intsc_bottom-myDB.LANE_WIDTH/2),104) ; 
                    
                    //route_NB[1].print() ; 
                    break ;
                default: // straight thru
                    route_NB[i] = new routeDB(4) ;  // linear path
                    if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                        route_NB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_bottom+link_length),2) ;
                        route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_bottom),99) ;
                        route_NB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_top),106) ;
                        route_NB[i].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+(i-1)*myDB.LANE_WIDTH, intsc_top-link_length),106) ;  
                        // signal strips, NB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_UL = 
                            new Point(center.x+myDB.LANE_WIDTH/2, intsc_bottom+STOPBAR_WIDTH-5) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[2]-1].sigbar_LR = 
                            new Point(intsc_right, intsc_bottom+STOPBAR_WIDTH-2) ;
                    } else {
                        route_NB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_bottom+link_length),2) ;
                        route_NB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_bottom),99) ;
                        route_NB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_top),106) ;
                        route_NB[i].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2, intsc_top-link_length),106) ;                  
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
                        route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length),3) ;
                        route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length/2+5),3) ;
                        route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, intsc_top-link_length/2+15),3) ;
                        route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, intsc_top),99) ; // 22.5 deg
                        route_SB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; // 45 deg
                        route_SB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 67.5 deg
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_SB[0].setRouteDBi(6, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),104) ; // 
                            route_SB[0].setRouteDBi(7, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),104) ; 
                        } else {
                            route_SB[0].setRouteDBi(6, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),104) ; // 
                            route_SB[0].setRouteDBi(7, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),104) ; 
                            
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
                            route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top-link_length),3) ;
                            route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_top),99) ; // 
                            route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; // 
                            route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                        } else {
                            route_SB[0].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_top-link_length),3) ;
                            route_SB[0].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_top),99) ; // 
                            route_SB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; // 
                            route_SB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; //                          
                        }
                        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                            route_SB[0].setRouteDBi(4, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),104) ; // 90 deg
                            route_SB[0].setRouteDBi(5, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),104) ; 
                        } else {
                            route_SB[0].setRouteDBi(4, new Point(intsc_right, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),104) ; // 90 deg
                            route_SB[0].setRouteDBi(5, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),104) ; 
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
                    route_SB[1].setRouteDBi(0, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_top-link_length),3) ;
                    route_SB[1].setRouteDBi(1, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_top),99) ; // 
                    route_SB[1].setRouteDBi(2, new Point(intsc_left+myDB.LANE_WIDTH/4, intsc_top+myDB.LANE_WIDTH/2),99) ; // 
                    route_SB[1].setRouteDBi(3, new Point(intsc_left, intsc_top+myDB.LANE_WIDTH/2),105) ; // 
                    route_SB[1].setRouteDBi(4, new Point(intsc_left-link_length, intsc_top+myDB.LANE_WIDTH/2),105) ; 
                    
                    //route_SB[1].print() ; 
                    break ;
                default: // straight thru
                    route_SB[i] = new routeDB(4) ;  // linear path
                    if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                        route_SB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_top-link_length),3) ;
                        route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_top),99) ;
                        route_SB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_bottom),107) ;
                        route_SB[i].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-(i-1)*myDB.LANE_WIDTH, intsc_bottom+link_length),107) ;  
                        // signal strips, SB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_UL = 
                            new Point(intsc_left, intsc_top-STOPBAR_WIDTH+2) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[3]-1].sigbar_LR = 
                            new Point(center.x-myDB.LANE_WIDTH/2, intsc_top-STOPBAR_WIDTH+5) ;
                    } else {
                        route_SB[i].setRouteDBi(0, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_top-link_length),3) ;
                        route_SB[i].setRouteDBi(1, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_top),99) ;
                        route_SB[i].setRouteDBi(2, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_bottom),107) ;
                        route_SB[i].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2, intsc_bottom+link_length),107) ;                  
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
                        route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),0) ;
                        route_EB[0].setRouteDBi(1, new Point(intsc_left-link_length/2+5, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),0) ;
                        route_EB[0].setRouteDBi(2, new Point(intsc_left-link_length/2+15, (intsc_top+intsc_bottom)/2),0) ;
                        route_EB[0].setRouteDBi(3, new Point(intsc_left, (intsc_top+intsc_bottom)/2 ),99) ; // 
                        route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                        route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),99) ; // 
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_EB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top),106) ; // 
                            route_EB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top-link_length),106) ; 
                        } else {
                            route_EB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top),106) ; // 
                            route_EB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top-link_length),106) ; 
                                      
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
                            route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),0) ;
                            route_EB[0].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),99) ; // 
                            route_EB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                            route_EB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                        } else {
                            route_EB[0].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2 ),0) ;
                            route_EB[0].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                            route_EB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                            route_EB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; //                          
                        }
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top),106) ; // 90 deg
                            route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH, intsc_top-link_length ),106) ; 
                        } else {
                            route_EB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top),106) ; // 90 deg
                            route_EB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2+myDB.LANE_WIDTH/2, intsc_top-link_length ),106) ; 
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
                    route_EB[1].setRouteDBi(0, new Point(intsc_left-link_length, intsc_bottom-myDB.LANE_WIDTH/2),0) ;
                    route_EB[1].setRouteDBi(1, new Point(intsc_left, intsc_bottom-myDB.LANE_WIDTH/2),99) ; // 
                    route_EB[1].setRouteDBi(2, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom-myDB.LANE_WIDTH/4),99) ; // 
                    route_EB[1].setRouteDBi(3, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom),107) ; // 
                    route_EB[1].setRouteDBi(4, new Point(intsc_left+myDB.LANE_WIDTH/2, intsc_bottom+link_length),107) ; 
                    
                    //route_EB[1].print() ; 
                    break ;
                default: // straight thru
                    route_EB[i] = new routeDB(4) ;  // linear path
                    if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                        route_EB[i].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH),0) ;
                        route_EB[i].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH),99) ;
                        route_EB[i].setRouteDBi(2, new Point(intsc_right, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH),104) ;
                        route_EB[i].setRouteDBi(3, new Point(intsc_right+link_length+50, (intsc_top+intsc_bottom)/2+(i-1)*myDB.LANE_WIDTH),104) ;  
                        // signal strips, EB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_UL = 
                            new Point(intsc_left-STOPBAR_WIDTH+2, center.y+myDB.LANE_WIDTH/2) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[0]-1].sigbar_LR = 
                            new Point(intsc_left-STOPBAR_WIDTH+5, intsc_bottom) ;
                    } else {
                        route_EB[i].setRouteDBi(0, new Point(intsc_left-link_length, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2),0) ;
                        route_EB[i].setRouteDBi(1, new Point(intsc_left, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2),99) ;
                        route_EB[i].setRouteDBi(2, new Point(intsc_right, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2),104) ;
                        route_EB[i].setRouteDBi(3, new Point(intsc_right+link_length+50, (intsc_top+intsc_bottom)/2+(i-2)*myDB.LANE_WIDTH+myDB.LANE_WIDTH/2),104) ;                  
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
                        route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),1) ;
                        route_WB[0].setRouteDBi(1, new Point(intsc_right+link_length/2-5, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),1) ;
                        route_WB[0].setRouteDBi(2, new Point(intsc_right+link_length/2-15, (intsc_top+intsc_bottom)/2),1) ;
                        route_WB[0].setRouteDBi(3, new Point(intsc_right, (intsc_top+intsc_bottom)/2 ),99) ; // 
                        route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                        route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH),99) ; // 
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_WB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom),107) ; // 
                            route_WB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom+link_length),107) ; 
                        } else {
                            route_WB[0].setRouteDBi(6, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom),107) ; // 
                            route_WB[0].setRouteDBi(7, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom+link_length),107) ; 
                                      
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
                            route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),1) ;
                            route_WB[0].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH),99) ; // 
                            route_WB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                            route_WB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2+myDB.LANE_WIDTH/2),99) ; // 
                        } else {
                            route_WB[0].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2 ),1) ;
                            route_WB[0].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                            route_WB[0].setRouteDBi(2, new Point((intsc_left+intsc_right)/2, (intsc_top+intsc_bottom)/2-myDB.LANE_WIDTH/2),99) ; // 
                            route_WB[0].setRouteDBi(3, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, (intsc_top+intsc_bottom)/2),99) ; //                          
                        }
                        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
                            route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom),107) ; // 90 deg
                            route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH, intsc_bottom+link_length ),107) ; 
                        } else {
                            route_WB[0].setRouteDBi(4, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom),107) ; // 90 deg
                            route_WB[0].setRouteDBi(5, new Point((intsc_left+intsc_right)/2-myDB.LANE_WIDTH/2, intsc_bottom+link_length ),107) ; 
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
                    route_WB[1].setRouteDBi(0, new Point(intsc_right+link_length, intsc_top+myDB.LANE_WIDTH/2),1) ;
                    route_WB[1].setRouteDBi(1, new Point(intsc_right, intsc_top+myDB.LANE_WIDTH/2),99) ; // 
                    route_WB[1].setRouteDBi(2, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top+myDB.LANE_WIDTH/4),99) ; // 
                    route_WB[1].setRouteDBi(3, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top),106) ; // 
                    route_WB[1].setRouteDBi(4, new Point(intsc_right-myDB.LANE_WIDTH/2, intsc_top-link_length),106) ; 
                    
                    //route_WB[1].print() ; 
                    break ;
                default: // straight thru
                    route_WB[i] = new routeDB(4) ;  // linear path
                    if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
                        route_WB[i].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH),1) ;
                        route_WB[i].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH),99) ;
                        route_WB[i].setRouteDBi(2, new Point(intsc_left, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH),105) ;
                        route_WB[i].setRouteDBi(3, new Point(intsc_left-link_length-50, (intsc_top+intsc_bottom)/2-(i-1)*myDB.LANE_WIDTH),105) ;  
                        // signal strips, WB THRU
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_UL = 
                            new Point(intsc_right+STOPBAR_WIDTH-5, intsc_top) ;
                        mySignalControl.myPhases[THRU_ASSIGN_PH[1]-1].sigbar_LR = 
                            new Point(intsc_right+STOPBAR_WIDTH-2, center.y-myDB.LANE_WIDTH/2) ;
                    } else {
                        route_WB[i].setRouteDBi(0, new Point(intsc_right+link_length, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2),1) ;
                        route_WB[i].setRouteDBi(1, new Point(intsc_right, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2),99) ;
                        route_WB[i].setRouteDBi(2, new Point(intsc_left, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2),105) ;
                        route_WB[i].setRouteDBi(3, new Point(intsc_left-link_length-50, (intsc_top+intsc_bottom)/2-(i-2)*myDB.LANE_WIDTH-myDB.LANE_WIDTH/2),105) ;                  
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
        int signal_head_offset = (myDB.LANE_WIDTH-myDB.SIGNAL_HEAD_SIZE)/2 ;
        // loop detector
        // EB
        N = myDB.EB_data.getLaneSize()/2 ;
        if (myDB.EB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.EB_data.detectorUL[0] = new Point(intsc_left-myDB.EB_data.LT_detector_dist, center.y-myDB.LOOP_DET_WIDTH/2) ;
            myDB.EB_data.signalLit_UL[0] = new Point(intsc_left-myDB.SIGNAL_HEAD_SIZE, center.y-myDB.LANE_WIDTH/2+signal_head_offset) ;
            myDB.EB_data.presence_detectorUL[0] = myDB.EB_data.detectorUL[0] ;
        } else {
            // left turn does not exist
            myDB.EB_data.detectorUL[0] = new Point(-99, -99) ;
            myDB.EB_data.presence_detectorUL[0] = new Point(-99, -99) ;
            myDB.EB_data.signalLit_UL[0] = new Point(-99, -99) ;
        }
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.EB_data.detectorUL[ij] = new Point(intsc_left-myDB.EB_data.detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH) ;
                myDB.EB_data.presence_detectorUL[ij] = new Point(intsc_left-myDB.EB_data.presence_detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH) ;
                myDB.EB_data.signalLit_UL[ij] = new Point(intsc_left-myDB.SIGNAL_HEAD_SIZE, center.y-myDB.LANE_WIDTH/2 + signal_head_offset + ij*myDB.LANE_WIDTH) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.EB_data.detectorUL[ij] = new Point(intsc_left-myDB.EB_data.detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
                myDB.EB_data.presence_detectorUL[ij] = new Point(intsc_left-myDB.EB_data.presence_detector_dist, center.y-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
                myDB.EB_data.signalLit_UL[ij] = new Point(intsc_left-myDB.SIGNAL_HEAD_SIZE, center.y-myDB.LANE_WIDTH/2 + signal_head_offset+myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
            }            
        }

        // WB
        N = myDB.WB_data.getLaneSize()/2 ;
        if (myDB.WB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.WB_data.detectorUL[0] = new Point(intsc_right+myDB.WB_data.LT_detector_dist-myDB.LT_LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2) ;
            myDB.WB_data.signalLit_UL[0] = new Point(intsc_right, center.y-myDB.LANE_WIDTH/2+signal_head_offset) ;
            myDB.WB_data.presence_detectorUL[0] = myDB.WB_data.detectorUL[0] ;
        } else {
            // left turn does not exist
            myDB.WB_data.detectorUL[0] = new Point(-99, -99) ;
            myDB.WB_data.presence_detectorUL[0] = new Point(-99, -99) ;
            myDB.WB_data.signalLit_UL[0] = new Point(-99, -99) ;
        }
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.WB_data.detectorUL[ij] = new Point(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH) ;
                myDB.WB_data.presence_detectorUL[ij] = new Point(intsc_right+myDB.WB_data.presence_detector_dist-myDB.PRESENCE_LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH) ;
                myDB.WB_data.signalLit_UL[ij] = new Point(intsc_right, center.y-myDB.LANE_WIDTH/2 + signal_head_offset- ij*myDB.LANE_WIDTH) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.WB_data.detectorUL[ij] = new Point(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
                myDB.WB_data.presence_detectorUL[ij] = new Point(intsc_right+myDB.WB_data.presence_detector_dist-myDB.PRESENCE_LOOP_DET_LENGTH, center.y-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
                myDB.WB_data.signalLit_UL[ij] = new Point(intsc_right, center.y-myDB.LANE_WIDTH/2 + signal_head_offset- myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH) ;
            }            
        }        
        // NB
        N = myDB.NB_data.getLaneSize()/2 ;
        if (myDB.NB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.NB_data.detectorUL[0] = new Point(center.x-myDB.LOOP_DET_WIDTH/2, intsc_bottom+myDB.NB_data.LT_detector_dist-myDB.LT_LOOP_DET_LENGTH) ;
            myDB.NB_data.signalLit_UL[0] = new Point(center.x-myDB.LANE_WIDTH/2+signal_head_offset, intsc_bottom) ;
            myDB.NB_data.presence_detectorUL[0] = myDB.NB_data.detectorUL[0] ;
        } else {
            // left turn does not exist
            myDB.NB_data.detectorUL[0] = new Point(-99, -99) ;
            myDB.NB_data.presence_detectorUL[0] = new Point(-99, -99) ;
            myDB.NB_data.signalLit_UL[0] = new Point(-99, -99) ;
        }
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.NB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH) ;
                myDB.NB_data.presence_detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + ij*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.presence_detector_dist-myDB.PRESENCE_LOOP_DET_LENGTH) ;
                myDB.NB_data.signalLit_UL[ij] = new Point(center.x-myDB.LANE_WIDTH/2 +signal_head_offset+ ij*myDB.LANE_WIDTH, intsc_bottom) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.NB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH) ;
                myDB.NB_data.presence_detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 + myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH, intsc_bottom+myDB.NB_data.presence_detector_dist-myDB.PRESENCE_LOOP_DET_LENGTH) ;
                myDB.NB_data.signalLit_UL[ij] = new Point(center.x-myDB.LANE_WIDTH/2 + signal_head_offset+ myDB.LANE_WIDTH/2 +(ij-1)*myDB.LANE_WIDTH, intsc_bottom) ;
            }            
        }   
        // SB
        N = myDB.SB_data.getLaneSize()/2 ;
        if (myDB.SB_data.leftTurnLaneExists()) {
            // left turn exists
            myDB.SB_data.detectorUL[0] = new Point(center.x-myDB.LOOP_DET_WIDTH/2, intsc_top-myDB.SB_data.LT_detector_dist) ;
            myDB.SB_data.signalLit_UL[0] = new Point(center.x-myDB.LANE_WIDTH/2 + signal_head_offset, intsc_top-myDB.SIGNAL_HEAD_SIZE) ;
            myDB.SB_data.presence_detectorUL[0] = myDB.SB_data.detectorUL[0] ;
        } else {
            // left turn does not exist
            myDB.SB_data.detectorUL[0] = new Point(-99, -99) ;
            myDB.SB_data.presence_detectorUL[0] = new Point(-99, -99) ;
            myDB.SB_data.signalLit_UL[0] = new Point(-99, -99) ;
        }
        if (myDB.NB_data.leftTurnLaneExists() | myDB.SB_data.leftTurnLaneExists()) {
            for (ij=1; ij<=N; ij++) {
                myDB.SB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.detector_dist) ;
                myDB.SB_data.presence_detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - ij*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.presence_detector_dist) ;
                myDB.SB_data.signalLit_UL[ij] = new Point(center.x-myDB.LANE_WIDTH/2 + signal_head_offset- ij*myDB.LANE_WIDTH, intsc_top-myDB.SIGNAL_HEAD_SIZE) ;
            }
        } else {
            for (ij=1; ij<=N; ij++) {
                myDB.SB_data.detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 -(ij-1)*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.detector_dist) ;
                myDB.SB_data.presence_detectorUL[ij] = new Point(center.x-myDB.LOOP_DET_WIDTH/2 - myDB.LANE_WIDTH/2 -(ij-1)*myDB.LANE_WIDTH, intsc_top-myDB.SB_data.presence_detector_dist) ;
                myDB.SB_data.signalLit_UL[ij] = new Point(center.x-myDB.LANE_WIDTH/2 + signal_head_offset- myDB.LANE_WIDTH/2 -(ij-1)*myDB.LANE_WIDTH, intsc_top-myDB.SIGNAL_HEAD_SIZE) ;
            }            
        }   
        
        // =======================================================================
        // sim step thread
        runThread_simStep = new Runnable() {
            public void run() { 
                while (true) {
                    if (sim_step_once) {
                        if (controller_configured) {
                            if (myGlobalTime>0) {
                                // save a copy of current signal timing
                                saveStepSignalTiming() ;
                            }
                            sim_step_toolbar = true ; 
                            startSim() ;

                            tLast = System.currentTimeMillis();
                            //myGlobalTime += 1f ;    // update global time by 1 sec.
                    //System.out.println("1 Global Time="+myGlobalTime) ;
                    
                            do {    
                                try {Thread.sleep(100) ;}   // 600 too much
                                catch (InterruptedException ie) {} ;
                            } while (!mySignalControl.step_finished) ;
                            myGlobalTime += 1f ;    // update global time by 1 sec.
                            
                            pauseSim() ;
                            if (mySignalControl.control_type==2 && sim_step_toolbar) {
                                // actuated by mouse 
                                //System.out.println("step 1") ;
                                // clear extension detectors
                                statusBar_Message(mySignalControl.getRegisteredPhases()) ; 
                                resetDetectors(1) ; // clear extension loop detectors
                                mySignalControl.resetEXTRegisters() ;  // clear controller registers

                            //    repaint() ; // run before clearPresencedetectors & Registers
                           /*     for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
                                    if (mySignalControl.phaseStatus[i]) {
                                        System.out.println("Phase "+(i+1)+" ON") ;
                                        System.out.println("Lit = "+mySignalControl.phaseLitColor[i]) ;
                                    }
                                }
                            */ 
                                clearPresenceDetectorsOnGreen() ;
                                mySignalControl.clearPresenceRegistersOnGreen() ;
                                //repaint() ;
                            //    System.out.println("R1 phase stage="+mySignalControl.actuatedTimer1.phase_stage) ;
                            }
                            //repaint() ;   // use repaint thread
                        } else {
                            msgBox_title = "Controller Configuration" ;
                            msgBox_body = "Please configure signal controller and \ndownload controller settings first!" ;
                            msgBox_flag = true ;
                        }   // if controller configurred
                        // reset button 3/29/07
                        tb.status = -1 ;
                        tb.repaint() ;
                        sim_step_once = false ;
                        try {Thread.sleep(100) ;}   // 
                        catch (InterruptedException ie) {} ;
                    } else {    // 
                        //t_simStep.yield() ;
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }   // if sim_step_once
                }   // while
            }   // run
        }   ; //runThread_simStep
        t_simStep = new Thread(runThread_simStep, "Step Simulation") ;
        t_simStep.start() ;
        
        // =======================================================================
        // global timer thread
        runThread_gTimer = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag && !sim_step_toolbar) {
                        // Get elapsed time in milliseconds
                        long tNow = System.currentTimeMillis() ;
                        long elapsedTimeMillis = tNow-tLast;
                        // Get elapsed time in seconds
                        float elapsedTimeSec = elapsedTimeMillis/1000f;   
                        //int wait = 200 ;
                        //if (sim_step_toolbar) { // step by second
                        //    elapsedTimeSec = 1f ;
                        //    wait = 1000 ;
                        //}
                        myGlobalTime += elapsedTimeSec ;
                   // System.out.println("2 Global Time="+myGlobalTime) ;
                        tLast = tNow ;
                        
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    } else {    // paused
                        //t_gTimer.yield() ;
                        try {Thread.sleep(100) ;}
                        catch (InterruptedException ie) {} ;
                    }   // if
                }   // while
            }   // run
        }   ; //runThread_gTimer
        t_gTimer = new Thread(runThread_gTimer, "Global Timer") ;
        t_gTimer.start() ;
        
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
                        if (!frame_controller.isShowing()){
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
                            }   // if idx
                            try {Thread.sleep(200) ;}
                            catch (InterruptedException ie) {} ;
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
        runThreadToolbar = new Runnable() {
            public void run() {
                while (true) {
                    if (SetVol_flag){
                        newstatus(0, " Set Traffic Volume");
                        SetVol_flag = false ;
                    } else if (SetActuationType_flag){
                        newstatus(1, " Set Actuation Type");
                        SetActuationType_flag = false ;
                    } else if (msgBox_flag) {
                        popMessageBox(msgBox_title, msgBox_body) ;
                        msgBox_flag = false ; 
                    } else if (restoreLastSignalTiming_flag) {
                        // restore last signal timing step
                        if (controller_configured) {
                            restoreLastSignalTiming() ;
                            // reset button 3/29/07
                            tb.status = -1 ;
                            tb.repaint() ;
                        } else {
                            msgBox_title = "Controller Configuration" ;
                            msgBox_body = "Please configure signal controller and \ndownload controller settings first!" ;
                            msgBox_flag = true ;
                        }                
                        restoreLastSignalTiming_flag = false ;
                    } else {
                        //tSetVol.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }
                }
             }   // void run
        } ; // runThread 0
        tSetVol = new Thread(runThreadToolbar, "Settings") ;
        tSetVol.start() ;

        //======================================================================
        // refresh drawing area to update vehicle location
        // =====================================================================
        
        runThreadRepaint = new Runnable() {
            public void run() {
                int wait_time = 0 ;
                while (true) {
                    
                    if (sim_flag) {
                        repaint() ;
                        //tRepaint.yield() ;
                        if (mySignalControl.control_type==2) {
                            wait_time = 200 ;
                        } else {
                            wait_time = DET_PERIOD-1 ;
                        }
                        try {Thread.sleep(wait_time) ;}   // <= DET_PERIOD
                        catch (InterruptedException ie) {} ;
                    } else {
                        //tRepaint.yield() ;
                        repaint() ;
                        statusBar_Message(mySignalControl.getRegisteredPhases()) ; 
                        try {Thread.sleep(500) ;}   
                        catch (InterruptedException ie) {} ;                        
                    }   // end if sim_flag
                    
                    // pop message box
                    //if (msg_flag) {
                    //    msg_flag = false ;
                    //    popMessageBox(msg_title, msg_body) ;
                    //}
                    
                }   // while loop
             }   // void run
        } ; // runThread 1
        tRepaint = new Thread(runThreadRepaint, "Refresh Screen") ;
        tRepaint.start() ;
        
        //======================================================================
        // vehicle generation thread, based on input demands
        // =====================================================================
        runThreadVehGen = new Runnable() {
            public void run() {
                /*
                int[] LaneNum = new int[4] ;
                LaneNum[0] = myDB.EB_data.getLaneSize()/2 ;
                LaneNum[1] = myDB.WB_data.getLaneSize()/2 ;
                LaneNum[2] = myDB.NB_data.getLaneSize()/2 ;
                LaneNum[3] = myDB.SB_data.getLaneSize()/2 ;
                */
                while (true) {
                    if (sim_flag && mySignalControl.control_type!=2) {
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
                                            new Vehicle2D(0, route_id, route_EB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 1:     // WB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(1, route_id, route_WB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 2:     // NB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(2, route_id, route_NB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                    case 3: // SB
                                        myVehicles[i][veh_index[i]] = 
                                            new Vehicle2D(3, route_id, route_SB[route_id], _spd) ; // dir, route id, route, speed
                                        break ;
                                
                                }
                                myVehicles[i][veh_index[i]].setColor(
                                    rdm.nextInt(128),
                                    rdm.nextInt(128),
                                    rdm.nextInt(128) ) ;
                                
                                if (veh_index[i] >= MAX_VEH_SIZE-1) {
                                    veh_index[i] = 0 ;
                                } else {
                                    veh_index[i] = veh_index[i]+1 ;
                                }
                                
                            }   // if exceed headway
                        }   // for
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                        tLast = tNow ;
                        //tGenVeh.yield() ;
                    }   // if sim_flag
                    tGenVeh.yield() ;
                    try {Thread.sleep(1000) ;}
                    catch (InterruptedException ie) {} ;
                }   // while
             }   // void run
        } ; // runThread 2
        tGenVeh = new Thread(runThreadVehGen, "Vehicle Generation") ;
        //tGenVeh.setPriority(Thread.NORM_PRIORITY+1) ;
        tGenVeh.start() ;
        
        // =====================================================================
        // vehicles simulation & collision avoidance thread
        // =====================================================================
        runThreadVehSim = new Runnable() {
            public void run() {
                Point stop_UL = new Point(0,0) ;
                Point stop_LR = new Point(0,0) ; ;
                Point stopbar_p1 = new Point(0,0) ;
                Point stopbar_p2 = new Point(0,0) ;
                //Point intsc_UL = new Point(intsc_left, intsc_top) ;
                //Point intsc_LR = new Point(intsc_right, intsc_bottom) ;
                
            while (true) {  // infinite loop
                if (sim_flag && mySignalControl.control_type!=2) {
                    long tNow = System.currentTimeMillis() ;
                    float timeElapsedSec = ( tNow - tLast1)/1000f ;   // sec
                    //System.out.println("Time Elapsed (sec)= "+timeElapsedSec) ; around 0.25
                    // find # of active vehicles in the network
                    veh_ref_index = 0 ;
                    for (int i=0; i<4; i++) {   // 4 approaches
                        // each approach 
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            if ( myVehicles[i][j] != null ) {
                                if (myVehicles[i][j].getRoute().getIndex()==myVehicles[i][j].getRoute().getDataSize()-1) {
                                    // end of animation
                                    myVehicles[i][j] = null ;
                                }   // end if
                                // store veh info to a buffer for car-following & collision avoidance analysis
                                myVehRef[veh_ref_index] = new VehPointer(i, j) ; 
                                veh_ref_index++ ; 
                            }   // end if not null
                        }   // for j
                    }   // for i
                    int mi, mj, m_rid, m_link ;
                    // =====================================================
                    for (int m=0; m<veh_ref_index; m++) {
                        mi = myVehRef[m].approach ;
                        mj = myVehRef[m].veh_index ;
                        if (myVehicles[mi][mj] != null) {
                            //myVehicles[mi][mj].saveCurRouteIndex() ;
                            myVehicles[mi][mj].forecastNextPosition(timeElapsedSec) ;
                            Point pos_mf = myVehicles[mi][mj].getNextForecastedPos() ;   // forecasted position
                            Point pos_mc = myVehicles[mi][mj].getPosition() ;           // current position
                            m_rid = myVehicles[mi][mj].getRouteID() ;  
                            m_link = myVehicles[mi][mj].getVehAtLink() ;  

                            //System.out.println("veh ["+mi+", "+mj+"] pos["+pos_mc.x+", "+pos_mc.y+"]") ;
                            // proposed travel distance
                            double forecasted_dist = calcDist(pos_mf, pos_mc) ;
                            boolean veh_update_flag = true ;
                            // check for position collision with other vehicles
                            int ni, nj, n_rid, n_link ;
                            for (int n=0; n<veh_ref_index; n++){
                                if (m != n) { // different vehicle
                                    ni = myVehRef[n].approach ;
                                    nj = myVehRef[n].veh_index ;
                                    //if (ni<0 || nj<0) {
                                    //System.out.println("veh ni, nj =["+ni+", "+nj+"]") ;
                                    //}
                                    if (myVehicles[ni][nj]!=null) {
                                        //System.out.println("veh ni, nj =["+ni+", "+nj+"] = null") ;
                                        Point pos_nc = myVehicles[ni][nj].getPosition() ;
                                        n_rid = myVehicles[ni][nj].getRouteID() ;
                                        n_link = myVehicles[ni][nj].getVehAtLink() ;
                                        
                                        double veh_distf = calcDist(pos_mf, pos_nc) ;   // veh travel distance
                                        float min_dist = 0.5f*(myVehicles[mi][mj].length+myVehicles[ni][nj].length)+10f ;

                                        mPointF vmc_n = new mPointF(pos_mc.x-pos_nc.x, pos_mc.y-pos_nc.y) ;
                                        mPointF vmf_n = new mPointF(pos_mf.x-pos_nc.x, pos_mf.y-pos_nc.y) ;
                                        float vec_dot = vmc_n.dot(vmf_n) ;
                                        

                                        if (veh_distf<=min_dist) {
                                            System.out.println("Veh ID"+myVehicles[mi][mj].VIDStr(mj)) ;  
                                            
                                            if (m_link == n_link ) {
                                                // veh in the same link
                                                if (vec_dot<-0.9) { // passing
                                                    veh_update_flag = false ;
                                                    System.out.println("veh passing "+mi+"-"+ni) ;
                                                } else if (vec_dot >0.9) {
                                                    veh_update_flag = false ; 
                                                    System.out.println("veh in front "+mi+"-"+ni) ;
                                                }
                                            } /*
                                            else if (m_link == 99 && n_link==99) {
                                                // both veh in intersection
                                                if (m_rid==0 && n_rid>0) {
                                                    veh_update_flag = false ;   // left turn inside intersection
                                                }
                                                System.out.println("inside intersection m:"+myVehicles[mi][mj].VIDStr(mj)) ;
                                            }   // if veh_dot
                                               */
                                        }   // if dist <= min_dist
                                    }   // if myVehicles[ni][nj] not null
                                }   // end if m != n
                            }   // for n

                            if (veh_update_flag) {  // if OK to update new position
                                // check signal stop -------------------------------
                                int phase_id = 0 ;
                                if (myVehicles[mi][mj].getRouteID()==0 ) {
                                    // left turn
                                    phase_id = LEFT_ASSIGN_PH[mi] ;
                                } else {
                                    // thru or right turn traffic
                                    phase_id = THRU_ASSIGN_PH[mi] ;
                                }        
                                stop_UL = mySignalControl.myPhases[phase_id-1].sigbar_UL ;
                                stop_LR = mySignalControl.myPhases[phase_id-1].sigbar_LR ;
                                switch (mi) {
                                    case 0: // EB
                                        //stop_UL = myDB.EB_data.stopbar_UL ;
                                        //stop_LR = myDB.EB_data.stopbar_LR ;
                                        stopbar_p1 = stop_UL ;
                                        stopbar_p2 = new Point(stop_UL.x,stop_LR.y) ;
                                        break ;
                                    case 1: //WB
                                        //stop_UL = myDB.WB_data.stopbar_UL ;
                                        //stop_LR = myDB.WB_data.stopbar_LR ;
                                        stopbar_p1 = new Point(stop_LR.x,stop_UL.y) ;
                                        stopbar_p2 = stop_LR ;
                                        break ;
                                    case 2: //NB
                                        //stop_UL = myDB.NB_data.stopbar_UL ;
                                        //stop_LR = myDB.NB_data.stopbar_LR ;
                                        stopbar_p1 = new Point(stop_UL.x,stop_LR.y) ;
                                        stopbar_p2 = stop_LR ;
                                        break ;
                                    case 3: //SB
                                        //stop_UL = myDB.SB_data.stopbar_UL ;
                                        //stop_LR = myDB.SB_data.stopbar_LR ;
                                        stopbar_p1 = stop_UL ;
                                        stopbar_p2 = new Point(stop_LR.x,stop_UL.y) ;
                                        break ;
                                }   // end switch
                                /* debug
                                System.out.println("link="+mi) ;
                                System.out.println("UL="+stop_UL.x+", "+stop_UL.y) ;
                                System.out.println("LR="+stop_LR.x+", "+stop_LR.y) ;
                                System.out.println("P1="+stopbar_p1.x+", "+stopbar_p1.y) ;
                                System.out.println("P2="+stopbar_p2.x+", "+stopbar_p2.y) ;
                                */
                                // debug

                                // distance from forecasted point to stop bar
                                double distf = calcDist2Line(pos_mf, stopbar_p1, stopbar_p2) ;
                                // distance from current point to stop bar
                                double distc = calcDist2Line(pos_mc, stopbar_p1, stopbar_p2) ;

                                //System.out.println("link="+mi+", phase_id="+phase_id+", dist="+dist) ;
                                // check if veh approaching stop bar
                                //mPointF v1 = myVehicles[mi][mj].headingVector() ;
                                int mid_x = (stopbar_p1.x+stopbar_p2.x)/2 ;
                                int mid_y = (stopbar_p1.y+stopbar_p2.y)/2 ;
                                mPointF vsf = new mPointF(pos_mf.x-mid_x, pos_mf.y-mid_y) ;
                                mPointF vsc = new mPointF(pos_mc.x-mid_x, pos_mc.y-mid_y) ;
                                float dotVal = vsf.dot(vsc) ;
                       //         System.out.println("link="+mi+", dotVal="+dotVal) ;
                                if (dotVal>0) { // cur & forecasted position on the same of stop bar
                                    // check if vehicle approaching stop bar
                                    if (distf<distc) {
                                        // vehicle approach stop bar
                                    //    System.out.println("distf="+distf+", distc="+distc) ;
                                        // check if green 
                           //             System.out.println("1: "+myVehicles[mi][mj].VIDStr(mj)+" phase="+phase_id+", state="+mySignalControl.phaseStatus[phase_id-1]) ;
                                        if (!mySignalControl.phaseStatus[phase_id-1]) {
                                            // red or yellow, stop
                                            if (distf <= 0.5*myVehicles[mi][mj].length+12) {
                                                //System.out.println("link="+mi+", phase_id="+phase_id) ;
                                                //System.out.println("dist to stop="+dist) ;
                                                //System.out.println("phase "+phase_id+", red/yel") ;
                            //                    System.out.println("cur pos="+myVehicles[mi][mj].getPosition2Str()) ;
                                                
                                                double dist = distc-0.5*myVehicles[mi][mj].length-15 ;
                                                // do not go over RED
                                                float ratio = new Double(dist/forecasted_dist).floatValue() ;
                            //                    System.out.println("1: "+myVehicles[mi][mj].VIDStr(mj)+", link="+mi+", dist="+dist+", ratio="+ratio) ;

                                                myVehicles[mi][mj].restorePrevRouteIndex() ;
                                                if (ratio>0) {
                                                    myVehicles[mi][mj].forecastNextPosition(ratio*timeElapsedSec) ;
                                                    myVehicles[mi][mj].updatePos() ;
                            //                    System.out.println("old pos="+myVehicles[mi][mj].getPosition2Str()) ;
                            //                    System.out.println("new pos="+myVehicles[mi][mj].getNextForecastedPos2Str()) ;
                                                }
                                                myVehicles[mi][mj].stop() ;
                           //                     System.out.println("Veh front bumper="+myVehicles[mi][mj].getFrontBumperCenter2Str()) ;
                                            //} else if (distf<2*myVehicles[mi][mj].length) {
                                            //   // slowdown ??
                                            //    myVehicles[mi][mj].slowdown() ;
                                            //    myVehicles[mi][mj].updatePos() ;
                                            //    System.out.println("slowdown: distf="+distf) ;
                                            } else {
                            //                    System.out.println("distf="+distf) ;
                                                myVehicles[mi][mj].updatePos() ;
                                            }
                                        } else {    // signal green
                                            myVehicles[mi][mj].updatePos() ;
                                            myVehicles[mi][mj].vehResume() ; 
                             //               System.out.println("grn 1: link="+mi+", route="+m_rid+", VID="+mj) ;
                                        }   // enf if phase status, green or red
                                    } else {    // distf >= distc && 
                                        if (!mySignalControl.phaseStatus[phase_id-1] && myVehicles[mi][mj].isVehStopped()) {
                                            // red & veh already stopped
                                            myVehicles[mi][mj].restorePrevRouteIndex() ;
                                            //System.out.println("Veh already stopped at"+myVehicles[mi][mj].getPosition2Str()) ;
                                        } else {
                                            // if signal state green or not stopped
                                            // vehicle leaving stop bar
                                            myVehicles[mi][mj].updatePos() ;
                                            myVehicles[mi][mj].vehResume() ;
                                            //System.out.println("1: Veh leaving stop bar "+myVehicles[mi][mj].VIDStr(mj)) ;  
                                        }   // end if signal state
                                    }   // if distf < distc
                                } else  { // dotVal<0 cur & forecasted position are on opposite side of stop bar
                                    // forecasted vehicle will cross stop bar
                            //        System.out.println("2: link="+mi+", dotVal="+dotVal) ;
                                    // ========================================
                                    if (!mySignalControl.phaseStatus[phase_id-1] ) {
                                        // red or yellow, stop
                            //           System.out.println("2: "+myVehicles[mi][mj].VIDStr(mj)+", RED") ;
                                        if (!myVehicles[mi][mj].isVehStopped()) {
                                            // if vehicle not stopped
                                            double dist = distc-0.5*myVehicles[mi][mj].length-15 ;
                                            // do not go over RED

                                            float ratio = new Double(dist/forecasted_dist).floatValue() ;
                           //                 System.out.println("2: "+myVehicles[mi][mj].VIDStr(mj)+", link="+mi+", dist="+dist+", ratio="+ratio) ;
                                            
                                            myVehicles[mi][mj].restorePrevRouteIndex() ;
                                            if (ratio>0) {
                                                myVehicles[mi][mj].forecastNextPosition(ratio*timeElapsedSec) ;
                                                myVehicles[mi][mj].updatePos() ;
                                            }
                                            myVehicles[mi][mj].stop() ;
                                        } else {    // veh stopped
                                            myVehicles[mi][mj].restorePrevRouteIndex() ;
                          //                  System.out.println("2: "+myVehicles[mi][mj].VIDStr(mj)+", already stopped") ;
                                        }
                                    } else {    // green
                                        myVehicles[mi][mj].updatePos() ;
                                        myVehicles[mi][mj].vehResume() ;
                          //              System.out.println("grn 2: link="+mi+", route="+m_rid+", VID="+mj+"\n") ;
                                    }
                                }   // enf if dotVal
                            } else {
                                // do not update due to collision
                                myVehicles[mi][mj].restorePrevRouteIndex() ;
                            }   // end if veh_update_flag
                        //System.out.println("\n") ;
                        }   // if myVehicles[mi][mj] not null
                    }   // for m
                    //repaint() ;
                    //tLast1 = tNow ;
                    tLast1 = System.currentTimeMillis() ;
                    
                    
                    //tVehicleSim.yield() ;
                    try {Thread.sleep(100) ;}
                    catch (InterruptedException ie) {} ;
                } else {
                    tVehicleSim.yield() ;
                    try {Thread.sleep(1000) ;}
                    catch (InterruptedException ie) {} ;                        
                }   // end if
            }   // end while loop
                
            }   // void run
        } ; // runThread 3
        tVehicleSim = new Thread(runThreadVehSim, "Simulation") ;
        //tVehicleSim.setPriority(Thread.NORM_PRIORITY) ;   
        tVehicleSim.start() ;
        
        // =======================================================================
        // bring signal control to top display thread
        // =====================================================================
        runThreadSignalControl = new Runnable() {
            public void run() {
                while (true) {
                    if (SetTiming_flag){
                        newstatus(2, " Signal Controller");
                        SetTiming_flag = false ; 
                    } else {
                        tSetTiming.yield();
                        try {Thread.sleep(500) ;}
                        catch (InterruptedException ie) {} ;
                    }
                }
             }   // void run
        } ; // runThread 4
        tSetTiming = new Thread(runThreadSignalControl, "ControllerSettings") ;
        tSetTiming.start() ;
        
        // =====================================================================
        // vehicle detection thread EB
        // =======================================================================
        runThreadEBdet = new Runnable() {
            public void run() {
                while (true) {
                    if (sim_flag && mySignalControl.control_type!=2) {
                        // EB
                        int i=0, N=0 ;
                        int ij, k ; 
                        int det_length ;
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.EB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.EB_data.loopDetCount[k]=0 ;
                                    myDB.EB_data.presenceLoopDetCount[k]=0 ;
                                }   // next k
                                det_length = myDB.LT_LOOP_DET_LENGTH ;
                            } else {
                                det_length = myDB.LOOP_DET_LENGTH ;
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.EB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            det_length, myDB.LOOP_DET_WIDTH)) {
                                        myDB.EB_data.loopDetCount[ij]++ ;
                                    } 
                                    if (isOverDetector(myDB.EB_data.presence_detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            det_length, myDB.LOOP_DET_WIDTH)) {
                                        myDB.EB_data.presenceLoopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        boolean myDetState, myPresenceDetState ;
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.EB_data.loopDetCount[ij]>0) {
                                myDetState = true ;
                            } else {    
                                myDetState = false ;
                            }
                            if (myDB.EB_data.presenceLoopDetCount[ij]>0) {
                                myPresenceDetState = true ;
                            } else {    
                                myPresenceDetState = false ;
                            }                      
                            myDB.EB_data.setLoopOccupied(ij, myDetState) ; 
                            myDB.EB_data.setPresenceLoopOccupied(ij, myPresenceDetState) ; 
                            // ====================================
                            if (mySignalControl.control_type==1) {
                                // actuated control
                                if (ij==0) {
                                    // left turn, phase 7
                                    mySignalControl.myExtRegisters[6] = myDetState ;
                                    mySignalControl.myPresenceRegisters[6] = myDetState ;
                                } else {
                                    // thru movement, phase 4
                                    mySignalControl.myExtRegisters[3] = myDetState ;
                                    mySignalControl.myPresenceRegisters[3] = myDetState ;
                                }
                            }   // actuated control
                            // ====================================
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tDetectionEB.yield() ;
                        try {Thread.sleep(200) ;}
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
                    if (sim_flag && mySignalControl.control_type!=2) {
                        // WB
                        int i=1, N=0 ;
                        int ij, k ; 
                        int det_length ;
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.WB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.WB_data.loopDetCount[k]=0 ;
                                }   // next k
                                det_length = myDB.LT_LOOP_DET_LENGTH ;
                            } else {
                                det_length = myDB.LOOP_DET_LENGTH ;
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.WB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            det_length, myDB.LOOP_DET_WIDTH)) {
                                        myDB.WB_data.loopDetCount[ij]++ ;
                                    } 
                                    if (isOverDetector(myDB.WB_data.presence_detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            det_length, myDB.LOOP_DET_WIDTH)) {
                                        myDB.WB_data.presenceLoopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        boolean myDetState, myPresenceDetState ;
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.WB_data.loopDetCount[ij]>0) {
                                myDetState = true ; 
                            } else {
                                myDetState = false ;
                            }
                            if (myDB.WB_data.presenceLoopDetCount[ij]>0) {
                                myPresenceDetState = true ; 
                            } else {
                                myPresenceDetState = false ;
                            }
                            myDB.WB_data.setLoopOccupied(ij, myDetState) ; 
                            myDB.WB_data.setPresenceLoopOccupied(ij, myPresenceDetState) ; 
                            // ====================================
                            if (mySignalControl.control_type==1) {
                                // actuated control
                                if (ij==0) {
                                    // left turn, phase 3
                                    mySignalControl.myExtRegisters[2] = myDetState ;
                                    mySignalControl.myPresenceRegisters[2] = myPresenceDetState ;
                                } else {
                                    // thru movement, phase 8
                                    mySignalControl.myExtRegisters[7] = myDetState ;
                                    mySignalControl.myPresenceRegisters[7] = myPresenceDetState ;
                                }
                            }   // actuated control
                            // ====================================
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tDetectionWB.yield() ;
                        try {Thread.sleep(200) ;}
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
                    if (sim_flag && mySignalControl.control_type!=2) {
                        // NB
                        int i=2, N=0 ;
                        int ij, k ; 
                        int det_width ;
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.NB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.NB_data.loopDetCount[k]=0 ;
                                }   // next k
                                det_width = myDB.LT_LOOP_DET_LENGTH ;
                            } else {
                                det_width = myDB.LOOP_DET_LENGTH ;
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.NB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_WIDTH, det_width)) {
                                        myDB.NB_data.loopDetCount[ij]++ ;
                                    } 
                                    if (isOverDetector(myDB.NB_data.presence_detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_WIDTH, det_width)) {
                                        myDB.NB_data.presenceLoopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        boolean myDetState, myPresenceDetState ;
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.NB_data.loopDetCount[ij]>0) {
                                myDetState = true ; 
                            } else {
                                myDetState = false ;
                            }
                            if (myDB.NB_data.presenceLoopDetCount[ij]>0) {
                                myPresenceDetState = true ; 
                            } else {
                                myPresenceDetState = false ;
                            }
                            myDB.NB_data.setPresenceLoopOccupied(ij, myPresenceDetState) ; 
                            // ====================================
                            if (mySignalControl.control_type==1) {
                                // actuated control
                                if (ij==0) {
                                    // left turn, phase 5
                                    mySignalControl.myExtRegisters[4] = myDetState ;
                                    mySignalControl.myPresenceRegisters[4] = myPresenceDetState ;
                                } else {
                                    // thru movement, phase 2
                                    mySignalControl.myExtRegisters[1] = myDetState ;
                                    mySignalControl.myPresenceRegisters[1] = myPresenceDetState ;
                                }
                            }   // actuated control
                            // ====================================
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tDetectionNB.yield() ;
                        try {Thread.sleep(200) ;}
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
                    if (sim_flag && mySignalControl.control_type!=2) {
                        // SB
                        int i=3, N=0 ;
                        int ij, k ; 
                        int det_width ;
                        for (int j=0; j<MAX_VEH_SIZE; j++) {
                            // vehicle loop detector detection
                            N = myDB.SB_data.getLaneSize()/2 ;
                            if (j==0) { // reset loopDetCount
                                for (k=0; k<=N; k++) {
                                    myDB.SB_data.loopDetCount[k]=0 ;
                                }   // next k
                                det_width = myDB.LT_LOOP_DET_LENGTH ;
                            } else {
                                det_width = myDB.LOOP_DET_LENGTH ;
                            }   // if j
                            if ( myVehicles[i][j] != null ) {
                                for (ij=0; ij<=N; ij++) {
                                    if (isOverDetector(myDB.SB_data.detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_WIDTH, det_width)) {
                                        myDB.SB_data.loopDetCount[ij]++ ;
                                    } 
                                    if (isOverDetector(myDB.SB_data.presence_detectorUL[ij], 
                                            myVehicles[i][j].getPosition(), 
                                            myVehicles[i][j].getFrontBumperCenter(),
                                            myVehicles[i][j].getRearBumperCenter(), 
                                            myDB.LOOP_DET_WIDTH, det_width)) {
                                        myDB.SB_data.presenceLoopDetCount[ij]++ ;
                                    } 
                                }   // next ij
                            }   // if not null
                        }   // next j
                        boolean myDetState, myPresenceDetState ;
                        for (ij=0; ij<=N; ij++) {
                            if (myDB.SB_data.loopDetCount[ij]>0) {
                                myDetState = true ; 
                            } else {
                                myDetState = false ;
                            }
                            if (myDB.SB_data.presenceLoopDetCount[ij]>0) {
                                myPresenceDetState = true ; 
                            } else {
                                myPresenceDetState = false ;
                            }
                            myDB.SB_data.setLoopOccupied(ij, myDetState) ; 
                            myDB.SB_data.setPresenceLoopOccupied(ij, myPresenceDetState) ; 
                            // ====================================
                            if (mySignalControl.control_type==1) {
                                // actuated control
                                if (ij==0) {
                                    // left turn, phase 1
                                    mySignalControl.myExtRegisters[0] = myDetState ;
                                    mySignalControl.myPresenceRegisters[0] = myPresenceDetState ;
                                } else {
                                    // thru movement, phase 6
                                    mySignalControl.myExtRegisters[5] = myDetState ;
                                    mySignalControl.myPresenceRegisters[5] = myPresenceDetState ;
                                }
                            }   // actuated control
                            // ====================================
                        }   // next ij
                        try {Thread.sleep(DET_PERIOD) ;}
                        catch (InterruptedException ie) {} ;
                    } else {
                        tDetectionSB.yield() ;
                        try {Thread.sleep(200) ;}
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
        
        String img_file = "phase_assgnEW.png" ;
        try {
            // read from JAR file
	    if (myDB.mainStreetNS) {
                img_file = "phase_assgnNS.png" ;
            }
            InputStream is = getClass().getResourceAsStream(img_file) ;
            myPhaseAssgn = ImageIO.read(is) ;
        } catch (IOException e) {
            System.out.println("Cannot read phase assignment image file, "+img_file) ;
        }
        // following are used for popSignalController
        load_LCD_Screens() ;    // read LCD screen database
        // load LCD formats
        load_LCD_Data_Format() ;
        
        try {
            // read from JAR file
            InputStream is = getClass().getResourceAsStream("Econolite5.png") ;
            myControllerImage = ImageIO.read(is) ;
        } catch (IOException e) {
            System.out.println("Failed to read controller image file!") ;
        }

        // added 03/10/08
        parseSignalTimingFromVirtualController() ;
        
        // 4/2/08 added
        for (i=0; i<stepSignalTimeState_BUF_SIZE; i++) {
            SavedSignalTime[i] = new stepSignalTimeState() ;
        }
        
    }   // end of startup initialization
    
    public boolean insideIntsc(Point pos) {
        boolean state = false ;
        if ((pos.x>intsc_left) && (pos.x<intsc_right) && (pos.y>intsc_top) && (pos.y<intsc_bottom)) {
            state = true ;
        }
        return state ;
    }
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
        if (controller_configured) {
            parsePhaseSequence() ;

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
                    mySignalControl.continueFixedTime(sim_step_toolbar) ;  
                } else {    // not started yet
                    mySignalControl.resetRingPtr() ;
                    mySignalControl.doFixedTime(sim_step_toolbar) ; 
                } 
            } else if (mySignalControl.control_type==1 || mySignalControl.control_type==2) {
                // actuated control
                if (sim_alreadyStarted) {
                    mySignalControl.continueActuatedTime(sim_step_toolbar) ;
                } else {    // not started yet
                    mySignalControl.resetRingPtr() ;
                    mySignalControl.doActuatedTime(sim_step_toolbar) ;
                }
            }
            if (mySignalControl.control_type!=2) { // if NOT mouse actuated signal control
                setVehiclesEnabled(true) ;  // enable all vehicles
            }
            sim_alreadyStarted = true ;
        } else {
            msgBox_title = "Controller Configuration" ;
            msgBox_body = "Please configure signal controller and \ndownload controller settings first!" ;
            msgBox_flag = true ;
        }
    }
    
    // pause simulation
    public void pauseSim() {
        sim_flag = false ;
        int N = myDB.EB_data.MAX_LANE_SIZE ;
        setVehiclesEnabled(false) ; // pause all vehicles
        /*  3/7/07 comment out
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
        */
        
        /* // comment out 4/2/08
        if (mySignalControl.control_type==0) {
            mySignalControl.pauseFixedTime() ;
        } else if (mySignalControl.control_type==1 || mySignalControl.control_type==2) {
            mySignalControl.pauseActuatedTime() ; 
        }
        */
        
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
    
    private void pauseSignalControl() {
        if (mySignalControl.control_type==0) {
            mySignalControl.pauseFixedTime() ;
        } else if (mySignalControl.control_type==1 || mySignalControl.control_type==2) {
            mySignalControl.pauseActuatedTime() ; 
        }
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
    
    private void showActTimer(Graphics2D g, int ref_x, int ref_y, int phase, int ring) {
        g.setColor(new Color(153, 204, 204));
        g.fillRoundRect(ref_x-40, ref_y-5, 105, 120, 8, 8) ;
        
        g.setStroke(new BasicStroke(2)) ;
        String[] str = new String[4] ;
        str[0] = "Phase: "+phase ;  // display phase #
        str[1] = "INIT" ;    // initial green
        str[2] = "EXT" ;    // green extension
        str[3] = "MAX" ;    // maximum green
        for (int i=1; i<=3; i++) {
            g.setColor(Color.blue);
            g.drawRoundRect(ref_x, ref_y-10+i*30+3, 52, 24, 8, 8) ;
            g.setColor(Color.yellow);
            g.fillRoundRect(ref_x+1, ref_y-6+i*30, 50, 22, 8, 8) ;
        }
        g.setColor(Color.black);
        //g.setStroke(new BasicStroke(14)) ;
        g.drawString(str[0], ref_x-15, ref_y+12) ;
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
                    time_stamp = CStr2(mySignalControl.myPhases[phase-1].getExtension()) ;
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
            g.drawString(time_stamp, ref_x+15, ref_y+10+i*30) ;
        }   // end of for
        g.setColor(Color.black);
        g.drawRoundRect(ref_x-40, ref_y-5, 105, 120, 8, 8) ;
        
    }
    public void paint(Graphics gr) 
    {
        Graphics2D g = (Graphics2D)gr ;
        Font myFont = g.getFont() ;
        
        //System.out.println(myFont.toString()) ;
        // plot global time
        //g.setFont(dialogFontB14);
        //FontMetrics fm = g.getFontMetrics();
        //g.drawString("Time: "+CStr2(myGlobalTime)+" sec", 490, 40) ;
        g.setColor(Color.blue) ;
        Point pos = new Point(490,20) ;
        // 3/8/07 added, display actuation type
        switch(mySignalControl.control_type) {
            case 0: // fixed
                g.drawString("No Actuation (Fixed Time)", pos.x, pos.y) ;
                break ;
            case 1: // veh actuation
                g.drawString("Actuated by Vehicle", pos.x, pos.y) ;
                break ;
            case 2: // mouse actuation
                g.drawString("Actuated by Mouse", pos.x, pos.y) ;
                break ;
        }
        g.setFont(myFont) ;
        
        if (mySignalControl.control_type==1 || mySignalControl.control_type==2) {
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
            plotActuationTimers(g) ;
        }   // end if sim running and actuated signal control
        
        g.drawImage(myPhaseAssgn, center.x-myPhaseAssgn.getWidth(this)/2, center.y-myPhaseAssgn.getHeight(this)/2, this) ;  // phase assignment graph, 2/25/07 added
        
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
            g.setStroke(new BasicStroke(2)) ;
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
      /*          g.drawRect(center.x-myDB.LOOP_DET_WIDTH/2, 
                            intsc_top-myDB.SB_data.detector_dist, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LOOP_DET_LENGTH
                          ) ; */
            } else {
                // no exclusive left turn
                g.drawLine(_x2, intsc_top, _x2, intsc_top-link_length) ;
                g.drawLine(_x2, intsc_top, _x1, intsc_top) ;
            }
            // southern link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(new BasicStroke(2)) ;
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
    /*            g.drawRect(center.x-myDB.LOOP_DET_WIDTH/2, 
                            intsc_bottom+myDB.NB_data.detector_dist-myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LOOP_DET_LENGTH
                          ) ; */
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
        g.setStroke(new BasicStroke(2)) ;
        if (myDB.EB_data.leftTurnLaneExists() | myDB.WB_data.leftTurnLaneExists()) {
            // easthern link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(new BasicStroke(2)) ;
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
    /*            g.drawRect(intsc_right+myDB.WB_data.detector_dist-myDB.LOOP_DET_LENGTH, 
                            center.y-myDB.LOOP_DET_WIDTH/2, 
                            myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH
                          ) ; */
            } else {
                // no exclusive left turn
                g.drawLine(intsc_right, _y2, intsc_right+link_length, _y2) ;
                g.drawLine(intsc_right, _y1, intsc_right, _y2) ;
            }
            // western link
            g.setColor(new Color(255, 255, 0)) ;    // dark yellow
            g.setStroke(new BasicStroke(2)) ;
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
     /*           g.drawRect(intsc_left-myDB.EB_data.detector_dist, 
                            center.y-myDB.LOOP_DET_WIDTH/2, 
                            myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH
                          ) ;   */
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
        plotDetectors(g) ;
        // plot signals
        plotSignals(g) ;
        // plot global time
        // plot global time
        g.setFont(dialogFontB14);
        g.setColor(Color.black) ;
        g.drawString("Time: "+CStr2(myGlobalTime)+" sec", 490, 40) ;
        
        // plot vehicles
        //plotVehicles(g) ;
        
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
     
    public void plotActuationTimers(Graphics2D g) {
        // plot timer display
        if (myDB.mainStreetNS) {    // main street is North-South
            // barrier 1
            if (mySignalControl.phaseStatus[0]) {   // phase 1 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showActTimer(g, center.x-270, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                    showActTimer(g, center.x-155, 60, 1, 1) ; // show phase 1 @ phase 2, 5 timer location
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showActTimer(g, center.x-155, 60, 1, 1) ; // for 6-lane roadway, phase 6, 1
                    showActTimer(g, center.x+130, 360, 5, 2) ; // for 6-lane roadway, phase 2, 5
                }
            } else if (mySignalControl.phaseStatus[1]) {   // phase 2 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showActTimer(g, center.x+130, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showActTimer(g, center.x-155, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showActTimer(g, center.x+245, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showActTimer(g, center.x+130, 360, 5, 2) ; // show phase 5 @ phase 6, 1 timer location
                }
            }   // id barrier 1
            
            // barrier 2
            if (mySignalControl.phaseStatus[2]) {
                // phase 3 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showActTimer(g, center.x+245, 60, 8, 2) ; // for 6-lane roadway, phase 8, 3
                    showActTimer(g, center.x+130, 60, 3, 1) ; // phase 3 at  phase 4, 7
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showActTimer(g, center.x+130, 60, 3, 1) ; // for 6-lane roadway, phase 8, 3
                    showActTimer(g, center.x-155, 360, 7, 2) ; // for 6-lane roadway, phase 4, 7
                }
            } else if (mySignalControl.phaseStatus[3]) {
                // phase 4 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showActTimer(g, center.x-155, 360, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showActTimer(g, center.x+130, 60, 8, 2) ; // for 6-lane roadway, phase 8, 3
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showActTimer(g, center.x-155, 360, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showActTimer(g, center.x-270, 360, 7, 2) ; // phase 7 at timer phase 8, 3
                }
            }   // if barrier 2
        } else {    // main street is EW =======================================================
            // barrier 1
            if (mySignalControl.phaseStatus[0]) {   // phase 1 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showActTimer(g, center.x+245, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                    showActTimer(g, center.x+130, 60, 1, 1) ; // show phase 1 @ phase 2, 5 timer location
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showActTimer(g, center.x+130, 60, 1, 1) ; // for 6-lane roadway, phase 6, 1
                    showActTimer(g, center.x-155, 360, 5, 2) ; // for 6-lane roadway, phase 2, 5
                }
            } else if (mySignalControl.phaseStatus[1]) {   // phase 2 ON
                if (mySignalControl.phaseStatus[5]) {
                    // phase 6 ON
                    showActTimer(g, center.x-155, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showActTimer(g, center.x+130, 60, 6, 2) ; // for 6-lane roadway, phase 6, 1
                } else if (mySignalControl.phaseStatus[4]) {
                    // phase 5 ON
                    showActTimer(g, center.x-155, 360, 2, 1) ; // for 6-lane roadway, phase 2, 5
                    showActTimer(g, center.x-270, 360, 5, 2) ; // show phase 5 @ phase 6, 1 timer location
                }
            }   // id barrier 1
            
            // barrier 2
            if (mySignalControl.phaseStatus[2]) {
                // phase 3 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showActTimer(g, center.x+245, 360, 8, 2) ; // for 6-lane roadway, phase 8, 3
                    showActTimer(g, center.x+130, 360, 3, 1) ; // phase 3 at  phase 4, 7
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showActTimer(g, center.x+130, 360, 3, 1) ; // for 6-lane roadway, phase 8, 3
                    showActTimer(g, center.x-155, 60, 7, 2) ; // for 6-lane roadway, phase 4, 7
                }
            } else if (mySignalControl.phaseStatus[3]) {
                // phase 4 ON
                if (mySignalControl.phaseStatus[7]) {
                    // phase 8 ON
                    showActTimer(g, center.x-155, 60, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showActTimer(g, center.x+130, 360, 8, 2) ; // for 6-lane roadway, phase 8, 3
                } else if (mySignalControl.phaseStatus[6]) {
                    // phase 7 ON
                    showActTimer(g, center.x-270, 60, 4, 1) ; // for 6-lane roadway, phase 4, 7
                    showActTimer(g, center.x-155, 60, 7, 2) ; // phase 7 at timer phase 8, 3
                }
            }   // if barrier 2             
        }   // if main street?
    }
    
    public void plotDetectors(Graphics2D g) {
        int N ; 
        int ij=0 ;
        // loop detector
        g.setStroke(myLineStroke) ;
        // EB
        N = myDB.EB_data.getLaneSize()/2 ;
        for (ij=1; ij<=N; ij++) {
            //System.out.println("EB isLoopOccupied="+myDB.EB_data.isLoopOccupied(ij)) ;
            if (myDB.EB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            
            if (ij==0) {
                g.drawRect(myDB.EB_data.detectorUL[ij].x, 
                            myDB.EB_data.detectorUL[ij].y, 
                            myDB.LT_LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH) ;                
            } else {
                g.drawRect(myDB.EB_data.detectorUL[ij].x, 
                            myDB.EB_data.detectorUL[ij].y, 
                            myDB.LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH) ;
            }   // if
            
        }   // for
        
        for (ij=0; ij<=N; ij++) {
            if (myDB.EB_data.isPresenceLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            
            if (ij==0) {
                g.drawRect(myDB.EB_data.presence_detectorUL[ij].x, 
                            myDB.EB_data.presence_detectorUL[ij].y, 
                            myDB.LT_LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH) ;                
            } else {
                /* comment out presence detector 8/20/07
                g.drawRect(myDB.EB_data.presence_detectorUL[ij].x, 
                            myDB.EB_data.presence_detectorUL[ij].y, 
                            myDB.PRESENCE_LOOP_DET_LENGTH, 
                            myDB.LOOP_DET_WIDTH) ;
                 */
            }
        }   // for
         
        // WB
        N = myDB.WB_data.getLaneSize()/2 ;
        for (ij=1; ij<=N; ij++) {
            if (myDB.WB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                g.drawRect(myDB.WB_data.detectorUL[ij].x, 
                        myDB.WB_data.detectorUL[ij].y, 
                        myDB.LT_LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH) ;                
            } else {
                g.drawRect(myDB.WB_data.detectorUL[ij].x, 
                        myDB.WB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH) ;
            }   // if
        }   // for
        
        for (ij=0; ij<=N; ij++) {
            if (myDB.WB_data.isPresenceLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                g.drawRect(myDB.WB_data.presence_detectorUL[ij].x, 
                        myDB.WB_data.presence_detectorUL[ij].y, 
                        myDB.LT_LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH) ;                
            } else {
                /* comment out presence detector 8/20/07
                g.drawRect(myDB.WB_data.presence_detectorUL[ij].x, 
                        myDB.WB_data.presence_detectorUL[ij].y, 
                        myDB.PRESENCE_LOOP_DET_LENGTH, 
                        myDB.LOOP_DET_WIDTH) ;
                 */
            }
        }   // for
         
        // NB
        N = myDB.NB_data.getLaneSize()/2 ;
        for (ij=1; ij<=N; ij++) {
            if (myDB.NB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                 g.drawRect(myDB.NB_data.detectorUL[ij].x, 
                            myDB.NB_data.detectorUL[ij].y, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LT_LOOP_DET_LENGTH) ;
            } else {
                g.drawRect(myDB.NB_data.detectorUL[ij].x, 
                            myDB.NB_data.detectorUL[ij].y, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LOOP_DET_LENGTH) ;
            } // if
        }   // for
        
        for (ij=0; ij<=N; ij++) {
            if (myDB.NB_data.isPresenceLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                 g.drawRect(myDB.NB_data.presence_detectorUL[ij].x, 
                            myDB.NB_data.presence_detectorUL[ij].y, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.LT_LOOP_DET_LENGTH) ;
            } else {
                /* comment out presence detector 8/20/07
                g.drawRect(myDB.NB_data.presence_detectorUL[ij].x, 
                            myDB.NB_data.presence_detectorUL[ij].y, 
                            myDB.LOOP_DET_WIDTH, 
                            myDB.PRESENCE_LOOP_DET_LENGTH) ;
                 */
            }
        }   // for
         
        // SB
        N = myDB.SB_data.getLaneSize()/2 ;
        for (ij=1; ij<=N; ij++) {
            if (myDB.SB_data.isLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                g.drawRect(myDB.SB_data.detectorUL[ij].x, 
                        myDB.SB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.LT_LOOP_DET_LENGTH) ;                
            } else {
                g.drawRect(myDB.SB_data.detectorUL[ij].x, 
                        myDB.SB_data.detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.LOOP_DET_LENGTH) ;
            }   // if
        }   // for
        
        for (ij=0; ij<=N; ij++) {
            if (myDB.SB_data.isPresenceLoopOccupied(ij)) {
                // thru lane veh detected
                g.setColor(Color.red) ;
            } else {
                // thru lane veh not detected
                g.setColor(Color.black) ;
            }
            if (ij==0) {
                g.drawRect(myDB.SB_data.presence_detectorUL[ij].x, 
                        myDB.SB_data.presence_detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.LT_LOOP_DET_LENGTH) ;                
            } else {
                /* comment out presence detector 8/20/07
                g.drawRect(myDB.SB_data.presence_detectorUL[ij].x, 
                        myDB.SB_data.presence_detectorUL[ij].y, 
                        myDB.LOOP_DET_WIDTH, 
                        myDB.PRESENCE_LOOP_DET_LENGTH) ;
                 */
            }
        }   // for
         
        
    }
    public void plotSignals(Graphics2D g) {
        String actuatedYelTime = "" ;
        g.setStroke(myLineStroke) ;
        Color[] phaseLit = new Color[mySignalControl.NUM_PHASES] ;
        for (int i=0; i<mySignalControl.NUM_PHASES; i++) {
            if (mySignalControl.myPhases[i].sigbar_UL.x>0 && mySignalControl.myPhases[i].sigbar_UL.y>0) {
                if (mySignalControl.phaseStatus[i]) {
                    // green or yellow
                    //g.setColor(Color.green) ; // 3/29/07 comment out
                    if ( (sim_step_toolbar && mySignalControl.control_type==0)
                        || (sim_flag && mySignalControl.control_type==0) ) {
                        // fixed timing ==============================
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
              //      } else if ( ( sim_step_toolbar && (mySignalControl.control_type==1 || mySignalControl.control_type==2))
              //          || (sim_flag && (mySignalControl.control_type==1 || mySignalControl.control_type==2)) ) {
                      } else if (mySignalControl.control_type==1 || mySignalControl.control_type==2) {
                        // actuated =====================
                        if (i==mySignalControl.getR1PhaseID()) {
                            //System.out.println("ph="+i+", stage="+mySignalControl.actuatedTimer1.phase_stage) ;
                            switch (mySignalControl.actuatedTimer1.phase_stage) {
                                case 1: // green
                                    g.setColor(Color.green) ; 
                                    break ;
                                case 2: // yellow
                                    g.setColor(Color.yellow) ;
                                    actuatedYelTime += " Phase "+(i+1)+" YEL="+mySignalControl.actuatedTimer1.yellowTimer.getCount()+";" ;
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
                                    actuatedYelTime += " Phase "+(i+1)+" YEL="+CInt(mySignalControl.actuatedTimer2.yellowTimer.getCount())+";" ;
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
                phaseLit[i] = g.getColor() ;
                mySignalControl.phaseLitColor[i] = phaseLit[i] ;
                g.setColor(new Color(210,210,210)) ;    // light light gray, stopbar
                
                g.fillRect(mySignalControl.myPhases[i].sigbar_UL.x,
                           mySignalControl.myPhases[i].sigbar_UL.y, 
                           Math.abs(mySignalControl.myPhases[i].sigbar_UL.x-mySignalControl.myPhases[i].sigbar_LR.x), 
                           Math.abs(mySignalControl.myPhases[i].sigbar_UL.y-mySignalControl.myPhases[i].sigbar_LR.y)
                ) ; // fill rectangle
                 
            }   // if
        }   // for i
        g.setColor(Color.red) ;
        if (mySignalControl.actuatedTimer1.phase_stage==3 || mySignalControl.actuatedTimer2.phase_stage==3) {
            g.drawString(" All Red: "+CInt(mySignalControl.actuatedTimer1.redTimer.getCount()), 502, 520) ;
        } else if (actuatedYelTime.length()>0) {
            g.drawString(actuatedYelTime, 502, 520) ;
        } 
        //else {
        //    statusBar_Message(" ") ;
        //}
        for (int m=0; m<4; m++) {
            int N=0 ;
            switch (m) {
                case 0: // EB
                    N = myDB.EB_data.getLaneSize()/2 ;
                    break ;
                case 1: //WB
                    N = myDB.WB_data.getLaneSize()/2 ;
                    break ;
                case 2: // NB
                    N = myDB.NB_data.getLaneSize()/2 ;
                    break ;
                case 3:     //SB
                    N = myDB.SB_data.getLaneSize()/2 ;
                    break ;
            }
            int phase_id = 0 ;
            for (int n=0; n<=N; n++) {
                if (n==0) {
                    phase_id = LEFT_ASSIGN_PH[m] ;
                } else {
                    phase_id = THRU_ASSIGN_PH[m] ;
                }   // if n==0
                g.setColor(phaseLit[phase_id-1]) ;
                Point pUL = new Point(-1, -1) ;
                switch (m) {
                    case 0: // EB
                        pUL = myDB.EB_data.signalLit_UL[n];
                        break ;
                    case 1: //WB
                        pUL = myDB.WB_data.signalLit_UL[n];
                        break ;
                    case 2: // NB
                        pUL = myDB.NB_data.signalLit_UL[n];
                        break ;
                    case 3:     //SB
                        pUL = myDB.SB_data.signalLit_UL[n];
                        break ;
                }   // switch m
                g.fillOval(pUL.x, pUL.y, myDB.SIGNAL_HEAD_SIZE, myDB.SIGNAL_HEAD_SIZE) ;
            }   // for n
        }   // for m, links
    }
    public void plotVehicles(Graphics2D g) {
        //======================================================================
        // plot vehicles
        //System.out.println("repaint") ;
        for (int i=0; i<4; i++) {
            for (int j=0; j<MAX_VEH_SIZE; j++) {
                if (myVehicles[i][j] != null) {
                   //System.out.println("repaint-1") ;
                    g.setColor(myVehicles[i][j].getVehColor()) ; 
                    g.fillPolygon(myVehicles[i][j].getXpoints(), myVehicles[i][j].getYpoints(), 4) ;
                }   // if finished
            }   // j
        }   // i
        
    }
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
            //==================================================================
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
            //==================================================================
            case 1: // pop control type screen
                
                if (frame_controlType==null){
                    popActuationType();
                } else {    // not null
                    if (frame_controlType.isShowing()==false){
                        //System.out.println("showing=false") ;
                        popActuationType();
                    } else {
                        frame_controlType.toFront();
                        //System.out.println("showing=true") ;
                    }
                }
                break ;
            //==================================================================
            case 2: // signal control
                // called from runThreadSignalControl
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
                 //controller_configured = true ;
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
             //==================================================================
           case 3: // start simulation
               if (!controller_configured) {
                    parseSignalTimingFromVirtualController() ;   // added 4/2/08
                    controller_configured = true ;
               }
               sim_step_toolbar = false ;
               //tLast = System.currentTimeMillis();
               startSim() ;
               break ;
            //==================================================================
            case 4:     // sim by 1 sec step
                if (!controller_configured) {
                    parseSignalTimingFromVirtualController() ; // 4/2/08 added
                    controller_configured = true ;
                }
                sim_step_once = true ;
                 
                break ;
            //==================================================================
            case 5: // pause simulation
                pauseSim() ;
                if (!controller_configured) {
                    parseSignalTimingFromVirtualController() ; // 4/2/08 added
                    controller_configured = true ;
                }
                sim_step_toolbar = false ;  
                sim_step_once = false ;
                pauseSignalControl() ;
                // debug only
                //parsePhaseSequence() ;
                //parseSignalTimingFromVirtualController() ;
                break ;
             //==================================================================
            case 6: // stop simulation & reset
                pauseSim() ;
                sim_alreadyStarted = false ;
                sim_step_toolbar = false ;
                // clear all detectors
                mySignalControl.resetAllRegisters() ;  // clear controller registers
                resetDetectors(3) ;                 // clear all loop detectors
                //reset ring1ptr & ring2ptr
                //mySignalControl.resetRingPtr() ;  // performed in startSim()
                
                pauseSignalControl() ;
                
                savedSignalTime_steps = 0 ;
                savedSignalTime_ptr = 0 ;
                repaint() ;
                break ; 
            //==================================================================
            case 7: // step back simulation 
                restoreLastSignalTiming_flag = true ;
                break ;
            //==================================================================
            case 8: // help doc
                popHelpDoc() ;  // pop java help document
                break ;
 
        }   // switch
	//repaint();  
    }
    
    private void popHelpDoc() {
        try
        {
            AppletContext a = myApplet.getAppletContext();
            URL u = new URL(SHARED.CONTENTS_PATH);  
            a.showDocument(u,"_blank");
            //_blank to open page in new window		
        }
        catch (Exception e){
                //do nothing
            popMessageBox("Help - Web Content", "Error:"+e.toString()) ;
        } // try
        
    }
    
    public void clearPresenceDetectorsOnGreen() {
        for (int i=0; i<4; i++) {
            LinkData myLink = new LinkData() ;
            switch (i) {
                case 0: // EB
                    myLink = myDB.EB_data ;
                    break ;
                case 1: // WB
                    myLink = myDB.WB_data ;
                    break ;
                case 2: // NB
                    myLink = myDB.NB_data ;
                    break ;
                case 3: // SB
                    myLink = myDB.SB_data ;
                    break ;
            }   // switch
            int N = myLink.getLaneSize()/2 ;
         
            
            if (mySignalControl.phaseLitColor[LEFT_ASSIGN_PH[i]-1]==Color.green) {
                //System.out.println("Phase="+LEFT_ASSIGN_PH[i]+ " green") ;
                    myLink.setLoopOccupied(0, false) ; // clear LT  presence detectors
                    myLink.setPresenceLoopOccupied(0, false) ; // clear LT  presence detectors
            }   // end if LEFT green
            if (mySignalControl.phaseLitColor[THRU_ASSIGN_PH[i]-1]==Color.green) {
                // green
                //System.out.println("Phase="+THRU_ASSIGN_PH[i]+ " green") ;
                for (int ij=1; ij<=N; ij++) {
                    myLink.setPresenceLoopOccupied(ij, false) ; // clear presence detectors
                }   // for ij
            }   // end if THRU green
            
            // 4/3/08 added
            if ((mySignalControl.getR1PhaseID()==LEFT_ASSIGN_PH[i]-1) && 
                (mySignalControl.phaseLitColor[LEFT_ASSIGN_PH[i]-1] == Color.green)) {
                mySignalControl.myPresenceRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
                mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
            }

            if ((mySignalControl.getR2PhaseID()==LEFT_ASSIGN_PH[i]-1) && 
                (mySignalControl.phaseLitColor[LEFT_ASSIGN_PH[i]-1] == Color.green)) {
                mySignalControl.myPresenceRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
                mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
            }            
        }   // for i
    }   // clearPresenceDetectorOnGreen
    
    public void resetDetectors(int choice) {
        for (int i=0; i<4; i++) {
            LinkData myLink = new LinkData() ;
            switch (i) {
                case 0: // EB
                    myLink = myDB.EB_data ;
                    break ;
                case 1: // WB
                    myLink = myDB.WB_data ;
                    break ;
                case 2: // NB
                    myLink = myDB.NB_data ;
                    break ;
                case 3: // SB
                    myLink = myDB.SB_data ;
                    break ;
            }
            switch (choice) {
                case 1: // reset EXT detectors only
                    myLink.resetExtLoops() ;
                    break ;
                case 2: // reset presence detectors only
                    myLink.resetPresenceLoops() ;
                    break ;
                case 3:     // reset all detectors
                    myLink.resetAllLoops() ;
                    break ;
            }   // switch
        }   // for loop
    }
    
    public void changeDrawScale(float scale) {
        draw_scale += scale ;
        if (draw_scale > 5.0f) {
            draw_scale = 5.0f;
        }
        else if (draw_scale < 0.1f) { 
            draw_scale = 0.1f ;
        }
        //sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
        imageResize() ;
    }
    public void imageResize() {
        Rectangle r = bounds();
        scaledxlate.X = CInt(0.5f * r.width * (1 - draw_scale) + draw_scale * translate.X);
        scaledxlate.Y = CInt(0.5f * r.height * (1 - draw_scale) + draw_scale * translate.Y);
        //repaint();
        //PictureBox1.Invalidate()
    }


    //public boolean mouseDown(Event e, int x, int y)
    public boolean mouseDown(int x, int y)
    {
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;

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

        return (true);
    }
    
    //public boolean mouseUp(Event e, int x, int y)
    public boolean mouseLeftUp(int x, int y)
    {
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;
        if (!sim_flag) {
            // paused
            int i, j ;
            for (i=0; i<4; i++) {
                for (j=0; j<MAX_VEH_SIZE; j++) {
                    if (myVehicles[i][j]!=null) {
                        if (myVehicles[i][j].isClicked(x, y)) {
                            // pop vehicle info screen
                            int k = myVehicles[i][j].getRouteID() ;
                            int posx = myVehicles[i][j].getPosition().x ;
                            int posy = myVehicles[i][j].getPosition().y ;
                            String heading = myVehicles[i][j].headingVector().toStr() ;
                            double headingDeg = myVehicles[i][j].getHeading()*180.0/Math.PI ;
                            sb.setStatusBarText(3, "VID: (" + i + "-" + j + "-" + k + "), POS=" +
                                posx + "," + posy + ",heading=" + heading) ;
                            msgBox_title= "Vehicle Information" ;
                            msgBox_body = "Veh Approach-ID: "+i +"-"+j +"\n"+"Route ID: "+k +"\n"+
                                          "Link ID: "+myVehicles[i][j].getVehAtLink()+"\n"+
                                          "Position [X, Y]: ["+x+", "+y+"]\n"+
                                          "Heading (Vx, Vy): "+heading +", "+CInt(headingDeg)+" deg.\n"+
                                          "Speed: "+CInt(myVehicles[i][j].getSpeed_MPH()) + " (MPH)\n" ;
                                          //"Stop Flag: "+myVehicles[i][j].veh_stop_flag ;
                            msgBox_flag = true ;
                            i=4 ;
                            j=MAX_VEH_SIZE ;
                            break ;
                        }   // if
                    }   // if not null
                }   // for j
            }   // for i
        }   //if
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
        int x = mouseEvent.getPoint().x ;
        int y = mouseEvent.getPoint().y ;
        sb.setStatusBarText(2, Integer.toString(x)+","+Integer.toString(y)) ;
        //System.out.println("X, Y="+x+", "+y) ;
        int N=0 ;
        boolean detector_found = false ;
        Point detector = new Point(0,0) ;
        LinkData my_link = new LinkData() ;
        int width=0, height=0 ;
        int p_width=0, p_height=0 ; // presence detector
        int LT_width=0, LT_height=0 ;
        for (int i=0; i<4; i++) {
            switch (i) {
                case 0:     // EB
                    my_link = myDB.EB_data ;
                    width = myDB.LOOP_DET_LENGTH ;
                    height = myDB.LOOP_DET_WIDTH ;
                    LT_width = myDB.LT_LOOP_DET_LENGTH ;
                    LT_height = height ;
                    p_width = myDB.PRESENCE_LOOP_DET_LENGTH ; 
                    p_height = height ;
                    break ;
                case 1:     // WB
                    my_link = myDB.WB_data ;
                    width = myDB.LOOP_DET_LENGTH ;
                    height = myDB.LOOP_DET_WIDTH ;
                    LT_width = myDB.LT_LOOP_DET_LENGTH ;
                    LT_height = height ;
                    p_width = myDB.PRESENCE_LOOP_DET_LENGTH ; 
                    p_height = height ;
                    break ;
                case 2:     // NB
                    my_link = myDB.NB_data ;
                    width = myDB.LOOP_DET_WIDTH ;
                    height = myDB.LOOP_DET_LENGTH ;
                    LT_width = width ;
                    LT_height = myDB.LT_LOOP_DET_LENGTH ;
                    p_width =  width ;
                    p_height = myDB.PRESENCE_LOOP_DET_LENGTH ;
                    break ;
                case 3:     // SB
                    my_link = myDB.SB_data ;
                    width = myDB.LOOP_DET_WIDTH ;
                    height = myDB.LOOP_DET_LENGTH ;
                    LT_width = width ;
                    LT_height = myDB.LT_LOOP_DET_LENGTH ;
                    p_width =  width ;
                    p_height = myDB.PRESENCE_LOOP_DET_LENGTH ;
                    break ;
            }   // end of switch
            N = my_link.getLaneSize()/2 ;   // number of lanes
            
            for (int ij=0; ij<=N; ij++) {
                boolean status=false, presence_status=false ;
                
                if (ij==0) {    // left turn
                    //status = isOverExtDetector1(my_link.detectorUL[ij], new Point(x,y), LT_width, LT_height) ;
                    presence_status = isOverExtDetector1(my_link.presence_detectorUL[ij], new Point(x,y), LT_width, LT_height) ;
                //System.out.println("status="+status+", presence="+presence_status) ;
                } else {    // thru 
                    status = isOverExtDetector1(my_link.detectorUL[ij], new Point(x,y), width, height) ;
                    presence_status = isOverExtDetector1(my_link.presence_detectorUL[ij], new Point(x,y), p_width, p_height) ;
                    if (status && !mySignalControl.phaseStatus[THRU_ASSIGN_PH[i]-1]) {
                        // extension detector actuated and phase is RED/YEL
                        // set presence detector ON
                        my_link.setPresenceLoopOccupied(ij, true) ;
                    }   // if
                }
                
                if (status) {   // extension detector
                    detector_found = true ;
                    boolean state = my_link.toggleLoopOccupied(ij) ; 
                    // tie extension detector togeteher added 8/20/07
                    for (int k=1; k<=N; k++) {
                        if (k!=ij) {
                            my_link.setLoopOccupied(k, state) ;
                        }
                    }
                    // end of 8/20 addition
                    
                    // actuated control by mouse
                    if (ij==0) {
                        // left turn, phase 7, 3, 5, 1
                        mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = state ;
                        //System.out.println("Left phase="+LEFT_ASSIGN_PH[i]) ;
                        //System.out.println("state="+state) ;

                    } else {
                        // thru movement, phase 4, 8, 2, 6
                        mySignalControl.myExtRegisters[THRU_ASSIGN_PH[i]-1] = state ;
                        //System.out.println("i="+(THRU_ASSIGN_PH[i]-1)+",phase status="+mySignalControl.phaseStatus[THRU_ASSIGN_PH[i]-1]) ;
                        
                        if (state && mySignalControl.phaseLitColor[THRU_ASSIGN_PH[i]-1]!=Color.green) {
                            // extension detector actuated and phase is RED or YELLOW
                            // set presence detector ON at controller registers
                            mySignalControl.myPresenceRegisters[THRU_ASSIGN_PH[i]-1] = true ;
                            //System.out.println("i="+(THRU_ASSIGN_PH[i]-1)+", presence="+mySignalControl.myPresenceRegisters[THRU_ASSIGN_PH[i]-1]) ;
                        }   // if 
                        // =====================================================
                        // 4/3/07 added to immediate RESET extension clock 
                        else if (sim_step_toolbar && state && 
                                mySignalControl.phaseLitColor[THRU_ASSIGN_PH[i]-1] == Color.green) {
                             //   mySignalControl.phaseStatus[THRU_ASSIGN_PH[i]-1]) {
                            // if 1-sec step simulation and extension detector actuated 
                            // and current phase is green
                                    
                            //sim_step_once = true ; comment out 8/10/07
                        //System.out.println("ring 1 ID="+mySignalControl.getR1PhaseID()) ;
                        //System.out.println("ring 2 ID="+mySignalControl.getR2PhaseID()) ;
                        //System.out.println("cur phase ID="+(THRU_ASSIGN_PH[i]-1)) ;
                            // added 8/10/07
                            if (mySignalControl.getR1PhaseID()==THRU_ASSIGN_PH[i]-1) {
                                mySignalControl.resetRing1Extension() ;
                                // added 3/31/08
                                mySignalControl.myExtRegisters[THRU_ASSIGN_PH[i]-1] = false ;
                            } else if (mySignalControl.getR2PhaseID()==THRU_ASSIGN_PH[i]-1) {
                                mySignalControl.resetRing2Extension() ;
                                // added 3/31/08
                                mySignalControl.myExtRegisters[THRU_ASSIGN_PH[i]-1] = false ;
                            }   
                            
                        }   // end if
                        // =====================================================
                    }   // if ij
                    //System.out.println("approach, detID="+i+", "+ij) ;
                    statusBar_Message("Approach "+CStr(i+1)+", EXT DET_ID="+ij) ;
                    i=4 ;
                    break ;
                }   // if over detector box
                
                else if (presence_status) { 
                    detector_found = true ;
                    boolean pstate = my_link.togglePresenceLoopOccupied(ij) ; 
                    // actuated control by mouse
                    if (ij==0) {
                        // left turn, phase 7, 3, 5, 1, EXT detector is same as presence detector
                        mySignalControl.myPresenceRegisters[LEFT_ASSIGN_PH[i]-1] = pstate ;
                        mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = pstate ;
                        //System.out.println("phase= "+(LEFT_ASSIGN_PH[i]-1)+"="+pstate) ;
                        // 4/3/08 added
                        if (sim_step_toolbar && 
                            mySignalControl.phaseLitColor[LEFT_ASSIGN_PH[i]-1] == Color.green) {
                            //   mySignalControl.phaseStatus[LEFT_ASSIGN_PH[i]-1]) {
                            // if 1-sec step simulation 
                            // and current phase is GREEN                       
                            // added  4/3/08
                            if (mySignalControl.getR1PhaseID()==LEFT_ASSIGN_PH[i]-1) {
                                mySignalControl.resetRing1Extension() ;
                                
                                mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
                            }   // added  4/3/08
                            else if (mySignalControl.getR2PhaseID()==LEFT_ASSIGN_PH[i]-1) {
                                mySignalControl.resetRing2Extension() ;
                               
                                mySignalControl.myExtRegisters[LEFT_ASSIGN_PH[i]-1] = false ;
                            }   // added  4/3/08
                        }
                    } else {
                    /* comment out thru movement present detector 8/20/07
                        // thru movement, phase 4, 8, 2, 6
                        mySignalControl.myPresenceRegisters[THRU_ASSIGN_PH[i]-1] = pstate ;
                        // added 3/29/07
                        mySignalControl.myExtRegisters[THRU_ASSIGN_PH[i]-1] = pstate ;
                    */
                    }
                    //System.out.println("approach, detID="+i+", "+ij) ;
                    statusBar_Message("Approach "+CStr(i+1)+", PRESENCE DET_ID="+ij) ;
                    i=4 ;
                    break ;
                }   // if over presence detector box
                
            }   // next ij
        }   // for i link
        if (!detector_found && mySignalControl.control_type!=2) {
            // check for click on vehicle
            mouseLeftUp(x, y) ;
        }
        repaint() ;   // or refresh thread
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
        //float val2 = Math.round(val*100f)/100f ;
        return twoPlaces.format(val) ;
    }    
    public String CStr(int val){
        return new Integer(val).toString();
    }   
 
    // -----------------------------------------------------------------------------
    /** Pop up a window to display intersection traffic demand data */    
    // -----------------------------------------------------------------------------
    // pop signal control type screen
    public void popActuationType() {
        
        // open a frame
        frame_controlType = new myWindow("Actuation Type") ;
        frame_controlType.setLocation(150,50) ;
        frame_controlType.setSize(200, 180) ;
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
                } else if (actuated_m.getState()) {
                    // actuated
                    mySignalControl.control_type = 2 ;
                }
                repaint() ;
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
        fixed = new Checkbox("No Actuation (Fixed Timing)", cbg, false) ;
        actuated = new Checkbox("Vehicle Actuated", cbg, false) ;
        actuated_m = new Checkbox("Mouse Actuated", cbg, true) ;
        frame_controlType.add(fixed, c) ;
        c.gridy = 1;
        frame_controlType.add(actuated, c) ;
        c.gridy = 2;
        frame_controlType.add(actuated_m, c) ;
        c.gridy = 3 ;
        Button btnOK = new Button("OK") ;
        frame_controlType.add(btnOK, c) ;
        btnOK.addActionListener(frame_controlType_exit_listener) ;
        
        // temporary disabled
        fixed.setEnabled(false) ;
        actuated.setEnabled(false) ;
        
        switch (mySignalControl.control_type) {
            case 0:
                // fixed
                fixed.setState(true) ;
                break ;
            case 1:
                // actuated
                actuated.setState(true) ;
                break ;
            case 2:
                // actuated by mouse
                actuated_m.setState(true) ;
                break ;
        }
        frame_controlType.invalidate() ;
        //frame_controlType.repaint() ;
        frame_controlType.setVisible(true) ;
        frame_controlType.setResizable(false) ;
        frame_controlType.show() ;
    }
    
    public void popSetGlobalTime() {
        
        // open a frame
        frame_globalTime = new myWindow("Set Global Time") ;
        frame_globalTime.setLocation(250,50) ;
        frame_globalTime.setSize(250, 120) ;
        MenuBar mb = new MenuBar() ; 
        Menu m = new Menu("File") ;
        MenuItem exitItem = new MenuItem("Close") ;

        m.add(exitItem) ;

        mb.add(m) ;     // add menu
        frame_globalTime.setMenuBar(mb) ;

        exitItem.setShortcut(new MenuShortcut(KeyEvent.VK_X)) ;
        ActionListener frame_globalTime_exit_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                // save control type selection, positive global time value
                myGlobalTime = Math.abs(new Float(txt_gTime.getText()).floatValue()) ;
                /*
                if (savedSignalTime_steps>0 ) {
                    int index = lookupSavedSignalTiming(myGlobalTime) ;
                    System.out.println("Saved Signal Timing Index="+index) ;
                    if (index>=0) {  // found data saved in the past
                        jumpToSavedSignalTiming(index) ;    // 4/2/07 added
                    }   // if index
                }   // if time_steps
                 */
                repaint() ;
                frame_globalTime.dispose() ;
                
            }
        } ;
        exitItem.addActionListener(frame_globalTime_exit_listener) ;
        frame_globalTime.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1 ; c.gridheight = 1 ;
        c.insets = new Insets(5,5,5,5) ; // 5-pixel margins on all sides
        
        frame_globalTime.add(new Label("Global Time (sec):"), c) ;
        c.gridx = 1; c.gridy = 0;
        txt_gTime = new TextField(CStr2(myGlobalTime)) ;
        frame_globalTime.add(txt_gTime, c) ;
        c.gridy = 1;
        Button btnOK = new Button("OK") ;
        frame_globalTime.add(btnOK, c) ;
        btnOK.addActionListener(frame_globalTime_exit_listener) ;
                
        frame_globalTime.invalidate() ;
        //frame_globalTime.repaint() ;
        frame_globalTime.setVisible(true) ;
        frame_globalTime.setResizable(false) ;
        frame_globalTime.show() ;
    }
    
    public void popJump2Time() {
        
        // open a frame
        frame_jumpTime = new myWindow("Jump Back to Time") ;
        frame_jumpTime.setLocation(250,50) ;
        frame_jumpTime.setSize(250, 120) ;
        MenuBar mb = new MenuBar() ; 
        Menu m = new Menu("File") ;
        MenuItem exitItem = new MenuItem("Close") ;

        m.add(exitItem) ;

        mb.add(m) ;     // add menu
        frame_jumpTime.setMenuBar(mb) ;

        exitItem.setShortcut(new MenuShortcut(KeyEvent.VK_X)) ;
        ActionListener frame_jumplTime_exit_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                // save control type selection, positive global time value
                float _time = Math.abs(new Float(txt_gTime.getText()).floatValue()) ;
                //if (savedSignalTime_steps>0 ) {
                    int index = lookupSavedSignalTiming(_time) ;
                    //System.out.println("Saved Signal Timing Index="+index) ;
                    if (index>=0) {  // found data saved in the past
                        jumpToSavedSignalTiming(index) ;    // 4/2/07 added
                        myGlobalTime = _time ;
                    }   // if index
                    else {
                        //msg_title = "Jump Back to Time" ;
                        //msg_body = "Specified time step not found in the recorded database!" ;
                        //msg_flag = true ;
                        //sb.sigInfoStr = "Specified time NOT found!" ;
                        //statusBar_Message("Specified time NOT found!") ;
                        popMessageBox("Jump Back to Time", "Specified time step was not found in the \nrecorded second by second database!") ;
                    }
                //}   // if time_steps
                //else {
                //    statusBar_Message("Specified time NOT found!") ;
                //}
                repaint() ;
                frame_jumpTime.dispose() ;
                
            }  
        } ;
        exitItem.addActionListener(frame_jumplTime_exit_listener) ;
        frame_jumpTime.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1 ; c.gridheight = 1 ;
        c.insets = new Insets(5,5,5,5) ; // 5-pixel margins on all sides
        
        frame_jumpTime.add(new Label("Jump Back to Time (sec):"), c) ;
        c.gridx = 1; c.gridy = 0;
        txt_gTime = new TextField(CStr2(myGlobalTime)) ;
        frame_jumpTime.add(txt_gTime, c) ;
        c.gridy = 1;
        Button btnOK = new Button("OK") ;
        frame_jumpTime.add(btnOK, c) ;
        btnOK.addActionListener(frame_jumplTime_exit_listener) ;
                
        frame_jumpTime.invalidate() ;
        //frame_jumplTime.repaint() ;
        frame_jumpTime.setVisible(true) ;
        frame_jumpTime.setResizable(false) ;
        frame_jumpTime.show() ;
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
        //frame_controller.validate() ;
        frame_controller.setVisible(true) ;
        //frame_controller.setResizable(false) ;
        //frame_controller.show() ;

        /* moved to intscDrawArea startup initialization
        load_LCD_Screens() ;    // read LCD screen database
        // load LCD formats
        load_LCD_Data_Format() ;
        
        try {
            // read from JAR file
            InputStream is = getClass().getResourceAsStream("Econolite5.png") ;
            myControllerImage = ImageIO.read(is) ;
        } catch (IOException e) {
            System.out.println("popSignalController: Failed to read controller image file!") ;
        }
        */
        controlPanel = new imageCanvas(myControllerImage) ; 
        controlPanel.setSize(controlPanel.getWidth(), controlPanel.getHeight()) ;
        
        MouseMotionAdapter myMouseMotionAdapter = new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (lcd_page_ID == 389) {
                    // starting page
                    LCD_update() ;
                    try {Thread.sleep(1000) ;}
                    catch (InterruptedException ie) {} ;                    
                }
            }
            public void mouseDragged(MouseEvent e) {
                
            }
        } ;
        
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
                    parseSignalTimingFromVirtualController() ;
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
                    //System.out.println("page_ID="+lcd_page_ID) ;
                    //LCD_repaint(_char) ;
                    if (lcd_page_ID==36) {
                        // cursor sel key prompt
                        //old_page_index = myLCD_PAGES[lcd_page_index].previousPage ;
                    //    System.out.println("old index="+old_page_index) ;
                    }
                }   // if valid click ?
                
                LCD_update() ;  // 2/9/2007
            }   // end of mouse clicked
            
            public void mouseEntered(MouseEvent e) {
                //LCD_repaint("a") ; 
                LCD_update() ;
            }
        } ;
        
        controlPanel.addMouseListener(myMouseAdapter) ;
        controlPanel.addMouseMotionListener(myMouseMotionAdapter) ;
        
        frame_controller.setLayout(new GridBagLayout()) ;
        // Create a constrains object, and specify default values
        GridBagConstraints c = new GridBagConstraints() ;
        c.fill = GridBagConstraints.BOTH ; // component grows in both directions
        c.weightx = 1.0 ; c.weighty = 1.0 ;
        c.gridx = 0 ; c.gridy = 0; c.gridwidth = 1 ; c.gridheight = 1 ;
        frame_controller.add(controlPanel, c) ;
        
        frame_controller.setVisible(true) ;
        frame_controller.setResizable(true) ;
        //frame_controller.setCenter() ;
        frame_controller.show() ;
        frame_controller.invalidate() ;
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
        int index5 = getLCDPageIndex(292) ; // page # F1-2-4 MAIN 0X124 #292, recall 
        // phase 1-8
        if (index1>=0 && index2>=0 && index3>=0 && index4>=0 && index5>=0) {
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
                // index 5 recall info
                // row 6 veh recall
                char char_data = ' ' ;
                char_data = myLCD_PAGES[index5].parseChar(5,  17+j*2) ;
                if (char_data == 'X') {
                    mySignalControl.myPhases[j].Veh_Recall = true ;
                } else {
                    mySignalControl.myPhases[j].Veh_Recall = false ;
                }
                // row 8, recall to max
                char_data = myLCD_PAGES[index5].parseChar(7,  17+j*2) ;
                if (char_data == 'X') {
                    mySignalControl.myPhases[j].Max_Recall = true ;
                } else {
                    mySignalControl.myPhases[j].Max_Recall = false ;
                }
                // debug
                //System.out.println("phase"+(j+1)+", veh recall="+mySignalControl.myPhases[j].Veh_Recall+", max recall="+mySignalControl.myPhases[j].Max_Recall) ;
                
            }   // for loop
            controller_configured = true ;
            statusBar_Message("Controller settings are loaded!") ; 
        } else {
            System.out.println("parseSignalTimingfromVirtualController(): Page 289/303/4624/4639/292 not found!") ;
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
    
    public void drawVehicles() {
        Graphics gr = this.getGraphics() ;
        Graphics2D g = (Graphics2D)gr ;
        plotVehicles(g) ;
    }
    
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

        ActionListener frame_msgbox_ok_listener = new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                
                frame_msgbox.dispose() ;
            }
        } ;

        frame_msgbox.setLayout(new BorderLayout(1,1)) ;
        TextArea myTitle = new TextArea(message, 3, 60) ;
        myTitle.setFont(new Font("SansSerif", Font.PLAIN , 12)) ;
        myTitle.setForeground(new Color(0,0,218)) ;
        frame_msgbox.setBackground(new Color(200, 200, 200)) ;
        frame_msgbox.add("Center",myTitle) ;
        
        Button btn_ok = new Button(" OK ") ;
        frame_msgbox.add("South",btn_ok) ;
        btn_ok.addActionListener(frame_msgbox_ok_listener) ;
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
        //sb.setStatusBarText(3, new Float(draw_scale).toString()) ;
        //repaint();
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
    public boolean isOverExtDetector1(Point _det, Point pos, int width, int length) {
        // check if pos locates inside detector square box
        boolean state = false ;
        if (pos.x>=_det.x && pos.x <= _det.x+width && 
            pos.y >= _det.y && pos.y <=_det.y+length) {
            state = true ;
        }
        return state ;
    }
    
    public boolean enteredIntsc(Point front) {
        if (front.x> intsc_left && front.x < intsc_right && 
            front.y > intsc_top && front.y < intsc_bottom) {
            return true ;
        } else {
            return false ;
        }
    }

    public void statusBar_Message(String str) {
        sb.setStatusBarText(3, str) ;
    }
    public void statusBar_Status(String str) {
        sb.setStatusBarText(0, str) ;
    }
    public void statusBar_Error(String str) {
        sb.setStatusBarText(1, str) ;
    }
    // save a copy of signal timing at current second step
    public void saveStepSignalTiming() {
        int r1 = mySignalControl.ring1ptr ;
        int r2 = mySignalControl.ring2ptr ;
        int ps1 = mySignalControl.actuatedTimer1.phase_stage ;
        int ps2 = mySignalControl.actuatedTimer2.phase_stage ;
        
        // timer 1
        boolean time_up1 = mySignalControl.actuatedTimer1.isTimeUp() ;
        
        boolean gap_out1 = mySignalControl.actuatedTimer1.greenTimer.isGapOut() ;
        boolean init_out1 = mySignalControl.actuatedTimer1.greenTimer.isInitOut() ;
        boolean max_out1 = mySignalControl.actuatedTimer1.greenTimer.isMaxOut() ;
        
        boolean yellow_time_up1 = mySignalControl.actuatedTimer1.yellowTimer.isTimeUp() ;
        boolean red_time_up1 = mySignalControl.actuatedTimer1.redTimer.isTimeUp() ;
        
        boolean ok_to_terminate1 = mySignalControl.actuatedTimer1.ok_to_terminate ;
        int timeup_flag1 = mySignalControl.actuatedTimer1.timeup_flag ;
        
        // timer 2
        boolean time_up2 = mySignalControl.actuatedTimer2.isTimeUp() ;
         
        boolean gap_out2 = mySignalControl.actuatedTimer2.greenTimer.isGapOut() ;
        boolean init_out2 = mySignalControl.actuatedTimer2.greenTimer.isInitOut() ;
        boolean max_out2 = mySignalControl.actuatedTimer2.greenTimer.isMaxOut() ;
        
        boolean yellow_time_up2 = mySignalControl.actuatedTimer2.yellowTimer.isTimeUp() ;
        boolean red_time_up2 = mySignalControl.actuatedTimer2.redTimer.isTimeUp() ;
        
        boolean ok_to_terminate2 = mySignalControl.actuatedTimer2.ok_to_terminate ;
        int timeup_flag2 = mySignalControl.actuatedTimer2.timeup_flag ;
       
        SavedSignalTime[savedSignalTime_ptr] = new stepSignalTimeState(r1, r2, ps1, ps2, myGlobalTime,
            time_up1, gap_out1, init_out1, max_out1, yellow_time_up1, red_time_up1, ok_to_terminate1,
            time_up2, gap_out2, init_out2, max_out2, yellow_time_up2, red_time_up2, ok_to_terminate2 ) ; 
         
        // timer 1
        float _init1 = mySignalControl.actuatedTimer1.greenTimer.get_initial() ;
        float _ext1 = mySignalControl.actuatedTimer1.greenTimer.get_extension() ;
        float _max1 = mySignalControl.actuatedTimer1.greenTimer.get_maximum() ;
        float _sINI1 = mySignalControl.actuatedTimer1.greenTimer.get_setting_INI() ;
        float _sEXT1 = mySignalControl.actuatedTimer1.greenTimer.get_setting_EXT() ;
        float _sMAX1 = mySignalControl.actuatedTimer1.greenTimer.get_setting_MAX() ;
        
        float yellow1 = mySignalControl.actuatedTimer1.yellowTimer.getCount() ;
        float red1 = mySignalControl.actuatedTimer1.redTimer.getCount() ;
        boolean ext_rst_flag1 = mySignalControl.actuatedTimer1.greenTimer.extension_reset_flag ;
        SavedSignalTime[savedSignalTime_ptr].saveTimer1(
            _init1, _ext1, _max1, 
            _sINI1, _sEXT1, _sMAX1,
            yellow1, red1, ext_rst_flag1, timeup_flag1) ;

        // timer 2
        float _init2 = mySignalControl.actuatedTimer2.greenTimer.get_initial() ;
        float _ext2 = mySignalControl.actuatedTimer2.greenTimer.get_extension() ;
        float _max2 = mySignalControl.actuatedTimer2.greenTimer.get_maximum() ;
        float _sINI2 = mySignalControl.actuatedTimer2.greenTimer.get_setting_INI() ;
        float _sEXT2 = mySignalControl.actuatedTimer2.greenTimer.get_setting_EXT() ;
        float _sMAX2 = mySignalControl.actuatedTimer2.greenTimer.get_setting_MAX() ;
        
        float yellow2 = mySignalControl.actuatedTimer2.yellowTimer.getCount() ;
        float red2 = mySignalControl.actuatedTimer2.redTimer.getCount() ;
        boolean ext_rst_flag2 = mySignalControl.actuatedTimer2.greenTimer.extension_reset_flag ;
        SavedSignalTime[savedSignalTime_ptr].saveTimer2(
            _init2, _ext2, _max2, 
            _sINI2, _sEXT2, _sMAX2,
            yellow2, red2, ext_rst_flag2, timeup_flag2) ;

        //System.out.println("saved ring1ptr="+SavedSignalTime[savedSignalTime_ptr].ring1ptr) ;
        //System.out.println("saved ring2ptr="+SavedSignalTime[savedSignalTime_ptr].ring2ptr) ;

        for (int i=0; i<mySignalControl.NUM_PHASES; i++){
            SavedSignalTime[savedSignalTime_ptr].phaseStatus[i] = mySignalControl.phaseStatus[i] ;
            SavedSignalTime[savedSignalTime_ptr].presenceRegisters[i] = mySignalControl.myPresenceRegisters[i] ; 
            SavedSignalTime[savedSignalTime_ptr].extensionRegisters[i] = mySignalControl.myExtRegisters[i] ; 
        }
        for (int i=0; i<4; i++) {
            LinkData myLink = new LinkData() ;
            switch (i) {
                case 0: // EB
                    myLink = myDB.EB_data ;
                    break ;
                case 1: // WB
                    myLink = myDB.WB_data ;
                    break ;
                case 2: // NB
                    myLink = myDB.NB_data ;
                    break ;
                case 3: // SB
                    myLink = myDB.SB_data ;
                    break ;
            }   // switch
            int N = myLink.getLaneSize()/2 ;
            for (int j=0; j<=N; j++) {
                SavedSignalTime[savedSignalTime_ptr].loopOccupied[i][j] = myLink.isLoopOccupied(j) ;
                SavedSignalTime[savedSignalTime_ptr].presenceLoopOccupied[i][j] = myLink.isPresenceLoopOccupied(j) ;
            }   // j
        }
        SavedSignalTime[savedSignalTime_ptr].statusBarMessage = sb.sigInfoStr ;
        SavedSignalTime[savedSignalTime_ptr].statusBarError = sb.errorStr ;
        
        savedSignalTime_steps++ ;
        savedSignalTime_ptr++ ;
        if (savedSignalTime_ptr>stepSignalTimeState_BUF_SIZE-1) {
            savedSignalTime_ptr = 0 ;
        }   // if
        if (savedSignalTime_steps>stepSignalTimeState_BUF_SIZE) {
            savedSignalTime_steps = stepSignalTimeState_BUF_SIZE ;
        }   // if
    }   // saveStepSignalTiming()
    
    public int lookupSavedSignalTiming(float _timestamp) {
        int index = -1 ;
        for (int i=0; i<stepSignalTimeState_BUF_SIZE; i++) {
            if (_timestamp==SavedSignalTime[i].global_time) {
                index = i  ;
                break ;
            }
        }   // for i
        return index ;
    }   // sub lookupSavedSignalTiming
    
    public void restoreLastSignalTiming() {
        if (savedSignalTime_steps>0) {
            savedSignalTime_steps-- ;
            savedSignalTime_ptr-- ;
            //System.out.println("savedSignalTime_steps=" + savedSignalTime_steps) ;
            //System.out.println("savedSignalTime_ptr=" + savedSignalTime_ptr) ;
            
            if (savedSignalTime_ptr<0) {    // round robbin buffer
                savedSignalTime_ptr = stepSignalTimeState_BUF_SIZE - 1 ;
            }
            jumpToSavedSignalTiming(savedSignalTime_ptr) ;
            repaint() ; // repaint screen
        } else {
            msgBox_title = "Simulation step back" ;
            msgBox_body = "Reaching the end of storage buffer." ;
            msgBox_flag = true ;

        }   //  if (savedSignalTime_steps>0)
        
    }  // restoreLastSignalTiming() 
     
    public void jumpToSavedSignalTiming(int _index) {
        myGlobalTime = SavedSignalTime[_index].global_time ;
        mySignalControl.ring1ptr = SavedSignalTime[_index].ring1ptr ;
        mySignalControl.ring2ptr = SavedSignalTime[_index].ring2ptr ;
        //System.out.println("restored ring1ptr="+mySignalControl.ring1ptr) ;
        //System.out.println("restored ring2ptr="+mySignalControl.ring2ptr) ;
        
        mySignalControl.actuatedTimer1.phase_stage = SavedSignalTime[_index].timer1_phaseStage ;
        mySignalControl.actuatedTimer2.phase_stage = SavedSignalTime[_index].timer2_phaseStage ;
        
        // timer 1
        mySignalControl.actuatedTimer1.setTimesup(SavedSignalTime[_index].time_up1) ;
        mySignalControl.actuatedTimer1.greenTimer.setGapOut(SavedSignalTime[_index].gap_out1) ;
        mySignalControl.actuatedTimer1.greenTimer.setInitOut(SavedSignalTime[_index].init_out1) ;
        mySignalControl.actuatedTimer1.greenTimer.setMaxOut(SavedSignalTime[_index].max_out1) ;
        mySignalControl.actuatedTimer1.yellowTimer.setTimesup(SavedSignalTime[_index].yellow_time_up1) ;
        mySignalControl.actuatedTimer1.redTimer.setTimesup(SavedSignalTime[_index].red_time_up1) ;
        mySignalControl.actuatedTimer1.ok_to_terminate = SavedSignalTime[_index].ok_to_terminate1 ;        
        
        // timer 2
        mySignalControl.actuatedTimer2.setTimesup(SavedSignalTime[_index].time_up2) ;
        mySignalControl.actuatedTimer2.greenTimer.setGapOut(SavedSignalTime[_index].gap_out2) ;
        mySignalControl.actuatedTimer2.greenTimer.setInitOut(SavedSignalTime[_index].init_out2) ;
        mySignalControl.actuatedTimer2.greenTimer.setMaxOut(SavedSignalTime[_index].max_out2) ;
        mySignalControl.actuatedTimer2.yellowTimer.setTimesup(SavedSignalTime[_index].yellow_time_up2) ;
        mySignalControl.actuatedTimer2.redTimer.setTimesup(SavedSignalTime[_index].red_time_up2) ;
        mySignalControl.actuatedTimer2.ok_to_terminate = SavedSignalTime[_index].ok_to_terminate2 ;
        
        // timer 1
        mySignalControl.actuatedTimer1.greenTimer.set_initial(SavedSignalTime[_index].timer1_initial) ;
        mySignalControl.actuatedTimer1.greenTimer.set_extension(SavedSignalTime[_index].timer1_extension) ;
        mySignalControl.actuatedTimer1.greenTimer.set_maximum(SavedSignalTime[_index].timer1_maximum) ;
        mySignalControl.actuatedTimer1.yellowTimer.setCount(SavedSignalTime[_index].timer1_yellow) ;
        mySignalControl.actuatedTimer1.redTimer.setCount(SavedSignalTime[_index].timer1_red) ;
        mySignalControl.actuatedTimer1.greenTimer.extension_reset_flag = SavedSignalTime[_index].timer1_extension_reset_flag ;
        mySignalControl.actuatedTimer1.timeup_flag = SavedSignalTime[_index].timeup_flag1 ;
        
        mySignalControl.actuatedTimer1.greenTimer.set_setting_INI(SavedSignalTime[_index].timer1_settings_INI) ;
        mySignalControl.actuatedTimer1.greenTimer.set_setting_EXT(SavedSignalTime[_index].timer1_settings_EXT) ;
        mySignalControl.actuatedTimer1.greenTimer.set_setting_MAX(SavedSignalTime[_index].timer1_settings_MAX) ;

        // timer 2
        mySignalControl.actuatedTimer2.greenTimer.set_initial(SavedSignalTime[_index].timer2_initial) ;
        mySignalControl.actuatedTimer2.greenTimer.set_extension(SavedSignalTime[_index].timer2_extension) ;
        mySignalControl.actuatedTimer2.greenTimer.set_maximum(SavedSignalTime[_index].timer2_maximum) ;
        mySignalControl.actuatedTimer2.yellowTimer.setCount(SavedSignalTime[_index].timer2_yellow) ;
        mySignalControl.actuatedTimer2.redTimer.setCount(SavedSignalTime[_index].timer2_red) ;
        mySignalControl.actuatedTimer2.greenTimer.extension_reset_flag = SavedSignalTime[_index].timer2_extension_reset_flag ;
        mySignalControl.actuatedTimer2.timeup_flag = SavedSignalTime[_index].timeup_flag2 ;
        
        mySignalControl.actuatedTimer2.greenTimer.set_setting_INI(SavedSignalTime[_index].timer2_settings_INI) ;
        mySignalControl.actuatedTimer2.greenTimer.set_setting_EXT(SavedSignalTime[_index].timer2_settings_EXT) ;
        mySignalControl.actuatedTimer2.greenTimer.set_setting_MAX(SavedSignalTime[_index].timer2_settings_MAX) ;

        // ===========================================
        for (int i=0; i<mySignalControl.NUM_PHASES; i++){
            mySignalControl.phaseStatus[i] = SavedSignalTime[_index].phaseStatus[i] ;
            mySignalControl.myPresenceRegisters[i] = SavedSignalTime[_index].presenceRegisters[i] ;
            mySignalControl.myExtRegisters[i] = SavedSignalTime[_index].extensionRegisters[i] ;
            //System.out.println("phase_status "+i+" = "+mySignalControl.phaseStatus[i]) ;
        }
        int N ;
        for (int i=0; i<4; i++) {
            LinkData myLink = new LinkData() ;
            switch (i) {
                case 0: // EB
                    N = myDB.EB_data.getLaneSize()/2 ;
                    for (int j=0; j<=N; j++) {
                        myDB.EB_data.setLoopOccupied(j, SavedSignalTime[_index].loopOccupied[i][j]) ;
                        myDB.EB_data.setPresenceLoopOccupied(j, SavedSignalTime[_index].presenceLoopOccupied[i][j]) ;
                    }   // j
                    break ;
                case 1: // WB
                    N = myDB.WB_data.getLaneSize()/2 ;
                    for (int j=0; j<=N; j++) {
                        myDB.WB_data.setLoopOccupied(j, SavedSignalTime[_index].loopOccupied[i][j]) ;
                        myDB.WB_data.setPresenceLoopOccupied(j, SavedSignalTime[_index].presenceLoopOccupied[i][j]) ;
                    }   // j
                    break ;
                case 2: // NB
                    N = myDB.NB_data.getLaneSize()/2 ;
                    for (int j=0; j<=N; j++) {
                        myDB.NB_data.setLoopOccupied(j, SavedSignalTime[_index].loopOccupied[i][j]) ;
                        myDB.NB_data.setPresenceLoopOccupied(j, SavedSignalTime[_index].presenceLoopOccupied[i][j]) ;
                    }   // j
                    break ;
                case 3: // SB
                    N = myDB.SB_data.getLaneSize()/2 ;
                    for (int j=0; j<=N; j++) {
                        myDB.SB_data.setLoopOccupied(j, SavedSignalTime[_index].loopOccupied[i][j]) ;
                        myDB.SB_data.setPresenceLoopOccupied(j, SavedSignalTime[_index].presenceLoopOccupied[i][j]) ;
                    }   // j
                    break ;
            }   // switch
        }
        statusBar_Error(SavedSignalTime[_index].statusBarError) ;  // clear ststaus bar error message, if any
        statusBar_Message(SavedSignalTime[_index].statusBarMessage) ;
        
    }
    
    // save LCD pages
    public void saveLCDPages() {
        int i = 0 ;
        int j = 0 ;
        int k = 0 ; 
        FileOutputStream fos = null;
        DataOutputStream w = null;

        try
        {
            FileDialog fd=new FileDialog(new Frame(),"Save Controller Data", FileDialog.SAVE);
            fd.setFile("*.cfg");
            fd.show();
            String fullpath=fd.getDirectory()+fd.getFile();
            fd.dispose();
//System.out.println("filepath="+fullpath);
            if(fullpath!=null) {
                if (fullpath.indexOf(".cfg")<0) {
                    fullpath += ".cfg" ;
                }
                fos = new FileOutputStream(fullpath);
                w = new DataOutputStream( new BufferedOutputStream(fos,512)); 
                // save page by page
                System.out.println("LCD PAGES=" + NUM_LCD_PAGES) ;
                
                for (i=0; i<NUM_LCD_PAGES; i++) {
                    // save PAGE_CHARS, 16x40
                    for (j=0; j<16; j++) {
                        for (k=0; k<40; k++) {
                            w.writeChar(myLCD_PAGES[i].PAGE_CHARS[j][k]) ;
                        }   // for k
                        w.flush();
                    }   // for j
                    
                    // save pageID, prev page, next page, left page, right page, dataIndex
                    w.writeInt(myLCD_PAGES[i].page_ID) ;
                    w.writeInt(myLCD_PAGES[i].previousPage) ;
                    w.writeInt(myLCD_PAGES[i].nextPage) ;
                    w.writeInt(myLCD_PAGES[i].leftPage) ;
                    w.writeInt(myLCD_PAGES[i].rightPage) ;
                    w.writeInt(myLCD_PAGES[i].dataEntryIndex) ;
                    w.flush();
                    
                }   // for i
            }   // if 
            w.close();
            fos.close();
        }
        catch (Exception e){
                //do nothing
            System.out.println("Save Controller Data:"+e.toString());
        } // try

    }   // saveLCDPages
    
    // openLCDPages
    public void openLCDPages() {
        FileInputStream fis=null;
        DataInputStream br=null;

        int i, j, k ;
        String dir = "", filename = "" ;

        try
        {
            FileDialog fd=new FileDialog(new Frame(),"Load Controller Data", FileDialog.LOAD);
            fd.setFile("*.cfg");
            fd.show();
            dir = fd.getDirectory() ;
            filename = fd.getFile() ;
            String fullpath = dir + filename ;
            fd.dispose();

            if(filename != null && dir != null) {
                //System.out.println("Open filename="+fullpath);

                fis = new FileInputStream(fullpath);
                br = new DataInputStream( new BufferedInputStream(fis,512)); 
                for (i=0; i<NUM_LCD_PAGES; i++) {
                    // save PAGE_CHARS, 16x40
                    for (j=0; j<16; j++) {
                        for (k=0; k<40; k++) {
                            myLCD_PAGES[i].PAGE_CHARS[j][k] = br.readChar() ;
                        }   // for k
                    }   // for j
                    
                    // save pageID, prev page, next page, left page, right page, dataIndex
                    myLCD_PAGES[i].page_ID = br.readInt() ;
                    myLCD_PAGES[i].previousPage = br.readInt() ;
                    myLCD_PAGES[i].nextPage = br.readInt() ;
                    myLCD_PAGES[i].leftPage = br.readInt() ;
                    myLCD_PAGES[i].rightPage = br.readInt() ;
                    myLCD_PAGES[i].dataEntryIndex = br.readInt() ;
                     
                }   // for i
                br.close();
                fis.close();

            }

        }
        catch (Exception e){
                //do nothing
            System.out.println("Open Controller Data:"+e.toString());
            System.out.println("File directory = "+dir+", filename = "+filename);
        } // try
    } // openLCDPages
               
    public void popSignalTimingData(){
       if (!controller_configured) {
            parseSignalTimingFromVirtualController() ;   // added 4/2/08
            controller_configured = true ;
       }
        
        frmSignalTimingTable = new JFrame("View Signal Timing Data") ;
        frmSignalTimingTable.setSize(350, 200);
        //Make sure we have nice window decorations.
  //      frmSignalTimingTable.setDefaultLookAndFeelDecorated(true);
        frmSignalTimingTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String timingPrintStr ;             // save & print timing data
             
        String[] headers = { "Interval", "Phas 1", "Phas 2", "Phas 3", "Phas 4", "Phas 5", "Phas 6", "Phas 7", "Phas 8" };
        String[] items = { "Initial", "Extension", "Max 1", "Yellow", "Red" };
        int[] fieldSize = {70,50,50,50,50,50,50,50,50} ;
        String[][] data = new String[5][headers.length];
        int i;
        for (i=0;i<items.length; i++) {
            data[i][0] = items[i] ;
        }
        for (i=0;i<headers.length-1; i++) {
            data[0][i+1] = CStr(mySignalControl.myPhases[i].getInitial()) ;     // init
            data[1][i+1] = CStr(mySignalControl.myPhases[i].getExtension()) ;       // extenstion
            data[2][i+1] = CStr(mySignalControl.myPhases[i].getMax1()) ;        // max 1
            data[3][i+1] = CStr(mySignalControl.myPhases[i].getYellow()) ;         // yellow
            data[4][i+1] = CStr(mySignalControl.myPhases[i].getRed()) ;         // red

        }   // for i
        stationTable = new JTable(data, headers) {
        // override isCellEditable method, 11/13/06
           public boolean isCellEditable(int row, int column) {
               if (column == -1) {
                   return true ;
               } else {
                   return false ;
               }    
           }    // isCellEditable method
        } ;
        stationTable.setPreferredScrollableViewportSize(new Dimension(600, 100));
        stationTable.setColumnSelectionAllowed(true) ;
        
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(stationTable);

        //Add the scroll pane to this panel.
        frmSignalTimingTable.add(scrollPane);
        //Get the column model.
        TableColumnModel colModel = stationTable.getColumnModel();
        //Get the column at index pColumn, and set its preferred width.
        colModel.getColumn(0).setPreferredWidth(75);   
        
        //Display the window.
        Dimension screen = getToolkit().getDefaultToolkit().getScreenSize();
        double top = 0.5*(screen.getWidth()-frmSignalTimingTable.getWidth());
        double left = 0.5*(screen.getHeight()-frmSignalTimingTable.getHeight());
        int x = new Double(top).intValue();
        int y = new Double(left).intValue();
        frmSignalTimingTable.setLocation(x, y);

        frmSignalTimingTable.pack();
        frmSignalTimingTable.setVisible(true);
        frmSignalTimingTable.show();
       
    }   // popSignalTimingData ;
    

}   // intscDrawArea class
