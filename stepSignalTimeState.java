/*
 * stepSignalTimeState.java
 *
 * Created on March 29, 2007, 1:38 PM
 * used to store last second signal timing & phase info
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

public class stepSignalTimeState {
    private final int NUM_PHASES = SignalTiming.NUM_PHASES ;
    private final int MAX_LANE_SIZE = LinkData.MAX_LANE_SIZE ;   // max number of lanes in each approach, 
    
    public int ring1ptr = -1 ;
    public int ring2ptr = -1 ;
    public int timer1_phaseStage = -1 ;
    public int timer2_phaseStage = -1 ;
    public float global_time = 0f ;
    
    public float timer1_initial = 0f;
    public float timer1_extension = 0f;
    public float timer1_maximum = 0f;
    public float timer1_settings_INI = 0f;
    public float timer1_settings_EXT = 0f;
    public float timer1_settings_MAX = 0f;
    
    public float timer1_yellow = 0f ;
    public float timer1_red = 0f ;
    public boolean timer1_extension_reset_flag = false ;
    public int timeup_flag1 = 0 ;
    
    public boolean time_up1 = false ;
    public boolean gap_out1 = false ;
    public boolean init_out1 = false ;
    public boolean max_out1 = false ;
    public boolean yellow_time_up1 = false ;
    public boolean red_time_up1 = false ;
    public boolean ok_to_terminate1 = false ;

    public float timer2_initial = 0f;
    public float timer2_extension = 0f;
    public float timer2_maximum = 0f;
    public float timer2_settings_INI = 0f;
    public float timer2_settings_EXT = 0f;
    public float timer2_settings_MAX = 0f;
    
    public float timer2_yellow = 0f ;
    public float timer2_red = 0f ;
    public boolean timer2_extension_reset_flag = false ;
    public int timeup_flag2 = 0 ;
    
    
    public boolean time_up2 = false ;
    public boolean gap_out2 = false ;
    public boolean init_out2 = false ;
    public boolean max_out2 = false ;
    public boolean yellow_time_up2 = false ;
    public boolean red_time_up2 = false ;
    public boolean ok_to_terminate2 = false ;
    
    public boolean[] phaseStatus = new boolean[NUM_PHASES] ; 
    public boolean[] presenceRegisters = new boolean[NUM_PHASES] ; 
    public boolean[] extensionRegisters = new boolean[NUM_PHASES] ; 
    
    public boolean[][] loopOccupied = new boolean[4][MAX_LANE_SIZE] ;
    public boolean[][] presenceLoopOccupied = new boolean[4][MAX_LANE_SIZE] ;
    public String statusBarMessage = "" ;
    public String statusBarError = "" ;
    
    /** Creates a new instance of stepSignalTimeState */
    public stepSignalTimeState() {
    }
    
    public stepSignalTimeState(int r1ptr, int r2ptr, int t1_stage, int t2_stage, float _gTime,
        boolean _time_up1, boolean _gap_out1, boolean _init_out1, boolean _max_out1, boolean _yellow_time_up1, boolean _red_time_up1, boolean _ok_to_terminate1,
        boolean _time_up2, boolean _gap_out2, boolean _init_out2, boolean _max_out2, boolean _yellow_time_up2, boolean _red_time_up2, boolean _ok_to_terminate2) {
        ring1ptr = r1ptr ;
        ring2ptr = r2ptr ;
        timer1_phaseStage = t1_stage ; 
        timer2_phaseStage = t2_stage ;
        global_time = _gTime ;
        // timer 1
        time_up1 = _time_up1 ;
        gap_out1 = _gap_out1 ;
        init_out1 = _init_out1 ;
        max_out1 = _max_out1 ;
        yellow_time_up1 = _yellow_time_up1 ;
        red_time_up1 = _red_time_up1 ;
        ok_to_terminate1 = _ok_to_terminate1 ;
        
        // timer 2
        time_up2 = _time_up2 ;
        gap_out2 = _gap_out2 ;
        init_out2 = _init_out2 ;
        max_out2 = _max_out2 ;
        yellow_time_up2 = _yellow_time_up2 ;
        red_time_up2 = _red_time_up2 ;
        ok_to_terminate2 = _ok_to_terminate2 ;
        
        for (int i=0; i<4; i++) {
            for (int j=0; j<MAX_LANE_SIZE; j++) {
                loopOccupied[i][j] = false ;
                presenceLoopOccupied[i][j] = false ;
            }
        }
    }
    public void saveTimer1(float _init, float _ext, float _max, 
        float _setting_INI, float _setting_EXT, float _setting_MAX,
        float _yel, float _red, boolean _ext_rst_flag, int _timeup_flag) {
        timer1_initial = _init ;
        timer1_extension = _ext ;
        timer1_maximum = _max ;
        
        timer1_settings_INI = _setting_INI ;
        timer1_settings_EXT = _setting_EXT ;
        timer1_settings_MAX = _setting_MAX ;
        
        timer1_yellow = _yel ;
        timer1_red = _red ;
        timer1_extension_reset_flag = _ext_rst_flag ;
        timeup_flag1 = _timeup_flag ;
    }
    public void saveTimer2(float _init, float _ext, float _max, 
        float _setting_INI, float _setting_EXT, float _setting_MAX,
        float _yel, float _red, boolean _ext_rst_flag, int _timeup_flag) {
        timer2_initial = _init ;
        timer2_extension = _ext ;
        timer2_maximum = _max ;
        
        timer2_settings_INI = _setting_INI ;
        timer2_settings_EXT = _setting_EXT ;
        timer2_settings_MAX = _setting_MAX ;

        timer2_yellow = _yel ;
        timer2_red = _red ;
        timer2_extension_reset_flag = _ext_rst_flag ;
        timeup_flag2 = _timeup_flag ;
    }
}
