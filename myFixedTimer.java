/*
 * myFixedTimer.java
 * Fixed signal timing timer
 *
 * Created on February 27, 2007, 2:26 PM
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

public class myFixedTimer {
    public myTimer greenTimer = new myTimer() ;
    public myTimer yellowTimer = new myTimer() ;
    public myTimer redTimer = new myTimer() ;
    public int phase_stage = 0 ;   // 1 - green, 2 - yellow, 3 - red
    private Runnable runThread0 ;
    private Thread tTimer ;
    private boolean timerPaused = true ;
    private boolean timesup = false ;
    private boolean stepSim_flag = false ;       // sec by sec simulation
    
    /** Creates a new instance of myFixedTimer */
    public myFixedTimer() {
        init() ;
    }
    public boolean getStepSim() {
        return stepSim_flag ;
    }
    public void setStepSim(boolean _state) {
        stepSim_flag = _state ;
    }
    
    public myFixedTimer(float green, float yellow, float red) {
        greenTimer.setTime(green) ;
        yellowTimer.setTime(yellow) ;
        redTimer.setTime(red) ;
        init() ;
    }
    public void setDurations(float green, float yellow, float red) {
        greenTimer.setTime(green) ;
        yellowTimer.setTime(yellow) ;
        redTimer.setTime(red) ;
//        System.out.println("timer G Y R = "+greenTimer.getTime()+","+yellowTimer.getTime()+","+redTimer.getTime()) ; 
    }

    private void init() {
        runThread0 = new Runnable() {
            public void run() {
                while (true) {
                    if (!timerPaused){
                        // go thru green yellow & red sequence
                        if (phase_stage==0) {
                            phase_stage=1 ; // get green phase started
                            greenTimer.start() ;
                            //System.out.println("greenTimer starts...") ;
                        } else if (phase_stage==1 && greenTimer.isTimeUp()) {
                            phase_stage=2 ;
                            yellowTimer.start() ;
                            //System.out.println("yellowTimer starts...") ;
                        } else if (phase_stage==2 && yellowTimer.isTimeUp()) {
                            phase_stage=3 ;
                            redTimer.start() ;
                            //System.out.println("redTimer starts...") ;
                        } else if (phase_stage==3 && redTimer.isTimeUp()) {
                            timesup = true ;
                            phase_stage=-1 ;
                        }
                        try {Thread.sleep(100) ;}
                        catch (InterruptedException ie) {} ;
                    } else {    // motion paused
                        // Get current time
                        //tLast = System.currentTimeMillis();
                        //tTimer.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }
                }
             }   // void run
        } ; // runThread 0
        tTimer = new Thread(runThread0, "FixedTimer") ;
        tTimer.start() ;
    }    
    public void start() {
        greenTimer.stepSim = stepSim_flag ; 
        yellowTimer.stepSim = stepSim_flag ;
        redTimer.stepSim = stepSim_flag ;
        phase_stage = 0 ;
        timesup = false ;
        timerPaused = false ;
    }
    public void resume() {
        greenTimer.stepSim = stepSim_flag ; 
        yellowTimer.stepSim = stepSim_flag ;
        redTimer.stepSim = stepSim_flag ;
        timerPaused = false ;
        switch (phase_stage) {
            case 1: 
                greenTimer.resume() ;
                break ;
            case 2:
                yellowTimer.resume() ;
                break ;
            case 3:
                redTimer.resume() ;
                break ;
        }   // switch
    }   // resume
    
    public void pause() {
        timerPaused = true ;
        switch (phase_stage) {
            case 1: 
                greenTimer.pause() ;
                break ;
            case 2:
                yellowTimer.pause() ;
                break ;
            case 3:
                redTimer.pause() ;
                break ;
        }   // switch
    }
    // reset timer
    public void reset() {
        timesup = false ;
        phase_stage = 0 ;
        greenTimer.reset() ;
        yellowTimer.reset() ;
        redTimer.reset() ;
    }
    public boolean isGreenTimeUp() {
        return greenTimer.isTimeUp() ;
    }
    public boolean isYellowTimeUp() {
        return yellowTimer.isTimeUp() ;
    }
    public boolean isRedTimeUp() {
        return redTimer.isTimeUp() ;
    }
    public boolean isTimeUp() {
        return timesup ;
    }
    public void clear() {
        timesup = false ;
    }
    
    public boolean getTimerPaused() {
        return timerPaused ;
    }
    public void setTimerPaused(boolean status) {
        timerPaused = status ;
    }
    public boolean getTimesup() {
        return timesup ;
    }
    public void setTimesup(boolean status) {
        timesup = status ;
    }

}
