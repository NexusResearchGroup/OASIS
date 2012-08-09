/*
 * myTimerAct.java
 * actuated timing clock for green phase ONLY
 * Created on February 22, 2007, 4:44 PM
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

public class myTimerAct {
//    private float currentCount=0f ; // current timer count
    private long tLast, tNow ;
    private Runnable runThread0 ;
    private Thread tTimer ;
    private boolean timerPaused = true ;
    private boolean max_out = false ;
    private boolean gap_out = false ;
    private boolean init_out = false ;
    //private boolean extention_ready = false ;

    private float grn_initial = 0 ;      // INIT counter
    private float grn_extension = 0 ;    // EXT counter
    private float grn_maximum = 0 ;      // MAX counter
    
    private float last_initial = 0 ;        // last intial when using step sim
    private float last_extension = 0 ;      // last intial when using step sim
    private float last_maximum = 0 ;        // last intial when using step sim
    
    private float Setting_EXT = 0 ;      // EXT setting
    private float Setting_INI = 0 ;      // INIT setting
    private float Setting_MAX = 0 ;      // MAX setting
    
    private boolean EXT_start = false ;
    private boolean hold_recall_flag = false ; // hold max & extension counter on recall phase
    public boolean stepSim = false ;        // sec by sec simulation, if true
    private boolean first_start = true ;
    public float elapsedTimeSec = 0f ;
    // 4/1/08 added
    public boolean extension_reset_flag = false ;   // extension clock was reset at previous step, if true
    public boolean step_finished = true ;
    
    /** Creates a new instance of myTimer */
    public myTimerAct() {
        init() ;
    }
    private void init() {
        runThread0 = new Runnable() {
            public void run() {
                while (true) {
                    if (!timerPaused){
                        step_finished = false ;
                        // Get elapsed time in milliseconds
                        long tNow = System.currentTimeMillis() ;
                        if (!first_start) {
                            if (grn_maximum<=0f) {
                                // MAX out
                                grn_maximum = 0f ;
                                max_out = true ;        // set max_out flag
                                timerPaused = true ;    // pause timer
                            }   // if grn_maximum<=0f
                            if (grn_extension<=0f) {
                                // GAP out
                                grn_extension=0f ;
                                gap_out = true ;        // set gap_out flag
                                //timerPaused = true ;    // pause timer
                            }   // if grn_extension<=0f
                            
                            if (!timerPaused){
                                // =============================================
                                long elapsedTimeMillis = tNow-tLast;
                                // Get elapsed time in seconds
                                elapsedTimeSec = elapsedTimeMillis/1000f;   
                                if (stepSim) {
                                    last_initial = grn_initial ;
                                    last_extension = grn_extension ;
                                    last_maximum = grn_maximum ;
                                    elapsedTimeSec = 1.0f ;   // 1 seconf step simulation
                                }   // end if stepSim

                                grn_initial -= elapsedTimeSec ;
                                if (hold_recall_flag) {
                                    grn_extension = Setting_EXT ;
                                    grn_maximum = Setting_MAX ;
                                    extension_reset_flag = false ;
                                } else {
                                    grn_maximum -= elapsedTimeSec ;
                                    //grn_extension -= elapsedTimeSec ;
                                    // 4/1/08 added
                                    if (extension_reset_flag) {
                                        // extension clock was reset at previous step
                                        // hold MAX extension for one step
                                        extension_reset_flag = false ;
                                    } else {
                                        grn_extension -= elapsedTimeSec ;
                                    }
                                }   // end if
                                if (grn_initial<0f) {
                                    grn_initial = 0f ;
                                    init_out = true ; 
                                } // end if

                                if (grn_maximum<=0f) {
                                    // max out
                                    grn_maximum = 0f ;
                                }   // if grn_maximum<=0f
                                if (grn_extension<=0f) {
                                    // gap out
                                    grn_extension=0f ;
                                }   // if grn_extension<=0f
                            }   // if not paused
                            // ===============================
                        } else {
                            first_start = false ;
                        }   // end if first_stop
                        
                        tLast = tNow ;
                        if (stepSim) {
                            timerPaused = true ;    // stop the loop after one 1-sec step
                            step_finished = true ;
                        }
                        //System.out.println("actuatedGreen INIT="+grn_initial+", EXT="+grn_extension) ;
                        try {Thread.sleep(100) ;}
                        catch (InterruptedException ie) {} ;

                    } else {    // motion paused
                        // Get current time
                        //tLast = System.currentTimeMillis();
                        //tTimer.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }   // end if timer paused
                }   // while loop
             }   // void run
        } ; // runThread 0
        tTimer = new Thread(runThread0, "Timer") ;
        tTimer.start() ;
        grn_initial = 0f ;      // INIT
        grn_extension = 0f ;    // EXT
        grn_maximum = 0f ;      // MAX
    }    
    
    public void start() {
        hold_recall_flag = false ;
        EXT_start = false ;
        Setting_EXT = grn_extension ;
        Setting_INI = grn_initial ;
        Setting_MAX = grn_maximum ;
        tLast = System.currentTimeMillis();
        max_out = false ;
        gap_out = false ;
        init_out = false ;
        timerPaused = false ;
        first_start = true ;
        
     }
    public void resume() {
        timerPaused = false ;
        
        tLast = System.currentTimeMillis();
    }
    public void clear() {
        max_out = false ;
        gap_out = false ;
        init_out = false ;
    }
    public void pause() {
        timerPaused = true ;
    }
    // reset timer
    public boolean isMaxOut() {
        if (stepSim) {
            do {    // wait till step finished
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!step_finished) ;
        }
        return max_out ;
    }
    public boolean isGapOut() {
        if (stepSim) {
            do {    // wait till step finished
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!step_finished) ;
        }
        return gap_out ;
    }
    public boolean isInitOut() {
        if (stepSim) {
            do {    // wait till step finished
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!step_finished) ;
        }
        return init_out ;
    }
        
    // actuated timers
    public void set_extension(float _ext) {
        gap_out = false ;
        grn_extension = _ext ;
        //System.out.println("actuatedGreen  EXT="+grn_extension) ;
    }
    public void set_initial(float _initial) {
        grn_initial = _initial ;
    }
    public void set_maximum(float _maximum) {
        grn_maximum = _maximum ;
    }
    // get EXT, INIT, MAX count
    public float get_extension() {
        return grn_extension ;
    }
    public float get_initial() {
        return grn_initial ;
    }
    public float get_maximum() {
        return grn_maximum ;
    }
    public float getCount() {
        float count ;
        if (grn_extension<grn_maximum) {
            count = grn_extension ;
        } else {
            count = grn_maximum ;
        }
        return count ;
    }
    public void hold_recall(boolean state) {
        hold_recall_flag = state ;
    }
    public void stepBack() {
        grn_initial = last_initial ;
        grn_extension = last_extension ;
        grn_maximum = last_maximum ;
    }
    public void resetTimer() {
        grn_initial = Setting_INI ;
        grn_extension = Setting_EXT ;
        grn_maximum = Setting_MAX ;
        //System.out.println("RESET timers INIT="+grn_initial+", EXT="+grn_extension+", MAX="+grn_maximum) ;
    }   // reset Timers
    
    // public methods
    public boolean getTimerPaused() {
        return timerPaused ;
    }
    public void setTimerPaused(boolean status) {
        timerPaused = status ;
    }
    public boolean getMaxOut() {
        return max_out ;
    }
    public void setMaxOut(boolean status) {
        max_out = status ;
    }
    public boolean getGapOut() {
        return gap_out ;
    }
    public void setGapOut(boolean status) {
        gap_out = status ;
    }
    public boolean getInitOut() {
        return init_out ;
    }
    public void setInitOut(boolean status) {
        init_out = status ;
    }
    public float get_lastextension() {
        return last_extension ;
    }
    public float get_lastinitial() {
        return last_initial ;
    }
    public float get_lastmaximum() {
        return last_maximum ;
    }
    public void set_lastextension(float _ext) {
        last_extension = _ext ;
    }
    public void set_lastinitial(float _initial) {
        last_initial = _initial ;
    }
    public void set_lastmaximum(float _maximum) {
        last_maximum = _maximum ;
    }
    public float get_setting_EXT() {
        return Setting_EXT ;
    }
    public float get_setting_INI() {
        return Setting_INI ;
    }
    public float get_setting_MAX() {
        return Setting_MAX ;
    }
    public void set_setting_EXT(float _ext) {
        Setting_EXT = _ext ;
    }
    public void set_setting_INI(float _initial) {
        Setting_INI = _initial ;
    }
    public void set_setting_MAX(float _maximum) {
        Setting_MAX = _maximum ;
    }
    public boolean get_EXT_start() {
        return EXT_start ;
    }
    public void set_EXT_start(boolean status) {
        EXT_start = status ;
    }
    public boolean get_hold_recall_flag() {
        return hold_recall_flag ;
    }
    public void set_hold_recall_flag(boolean status) {
        hold_recall_flag = status ;
    }
    public boolean get_first_start() {
        return first_start ;
    }
    public void set_first_start(boolean status) {
        first_start = status ;
    }  
    
    
}