/*
 * myActuatedTimer.java
 * Actuated signal timing control
 * Created on February 27, 2007, 3:25 PM
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

public class myActuatedTimer {
    public myTimerAct greenTimer = new myTimerAct() ;   // variable green time timer
    public myTimer yellowTimer = new myTimer() ;    // fixed time timer
    public myTimer redTimer = new myTimer() ;       // fixed time timer
    public int phase_stage = -1 ;   // 1 - green, 2 - yellow, 3 - red
    private Runnable runThread0 ;
    private Thread tTimer ;         // timere thread
    private boolean timerPaused = true ;
    private boolean timesup = false ;
    public boolean ok_to_terminate = false ;    // used to check for barrier before terminate green
    private boolean stepSim_flag = false ;       // sec by sec simulation
    public boolean step_finished = true ;
    
    public int timeup_flag = 0 ;    // 1 - green time up, 2 - yellow time up, 0 - red time up
    
    /** Creates a new instance of myActuatedTimer */
    public myActuatedTimer() {
        init() ;
    }
    public boolean getStepSim() {
        return stepSim_flag ;
    }
    public void setStepSim(boolean _state) {
        stepSim_flag = _state ;
    }
    public myActuatedTimer(float init, float ext, float max, float yellow, float red) {
        greenTimer.set_initial(init) ;
        greenTimer.set_extension(ext) ;
        greenTimer.set_maximum(max) ;
        yellowTimer.setTime(yellow) ;
        redTimer.setTime(red) ;
        init() ;
    }
    public void setDurations(float init, float ext, float max, float yellow, float red) {
        greenTimer.set_initial(init) ;
        greenTimer.set_extension(ext) ;
        greenTimer.set_maximum(max) ;
        yellowTimer.setTime(yellow) ;
        redTimer.setTime(red) ;
    }
  
    private void init() {
        runThread0 = new Runnable() {
            public void run() {
                while (true) {
                    //if (!timerPaused){
                        //if (greenTimer.stepSim) {
                        //    step_finished = false ;
                        //}
                        // go thru green yellow & red sequence
                        if (phase_stage==0) {
                            phase_stage=1 ; // get green phase started
                            greenTimer.start() ;
                            /*
                            if (greenTimer.stepSim) {
                                // wait till step finished if in step mode
                                do {
                                    try {Thread.sleep(100) ;}
                                    catch (InterruptedException ie) {} ;
                                } while (!greenTimer.step_finished) ;
                            }
                             */
                        } else if (phase_stage==1 && (greenTimer.isGapOut() || greenTimer.isMaxOut()) ) {
                            if (ok_to_terminate) {
                                phase_stage=2 ;
                                yellowTimer.start() ; 
                                /*
                                if (yellowTimer.stepSim) {
                                    // wait till step finished if in step mode
                                    do {
                                        try {Thread.sleep(100) ;}
                                        catch (InterruptedException ie) {} ;
                                    } while (!yellowTimer.step_finished) ;
                                }
                                */
                            }
                        } else if (phase_stage==2 && yellowTimer.isTimeUp()) {
                            phase_stage=3 ;
                            redTimer.start() ;
                            /*
                            if (redTimer.stepSim) {
                                // wait till step finished if in step mode
                                do {
                                    try {Thread.sleep(100) ;}
                                    catch (InterruptedException ie) {} ;
                                } while (!redTimer.step_finished) ;
                            }    
                             */                        
                        } else if (phase_stage==3 && redTimer.isTimeUp()) {
                            timesup = true ;
                            phase_stage = -1 ;
                        } 
                        
                        // added 4/2/08
                        /*
                        if (phase_stage==1 && greenTimer.stepSim) {
                            // green & step by step (sec by sec)
                            timerPaused = true ;
                        } else if (phase_stage==2 && yellowTimer.stepSim) {
                            // green & step by step (sec by sec)
                            timerPaused = true ;
                            //System.out.println("yellow timer 1st start="+yellowTimer.getFirstStart()) ;
                        } else if (phase_stage==3 && redTimer.stepSim) {
                            // green & step by step (sec by sec)
                            timerPaused = true ;
                            //System.out.println("red timer 1st start="+yellowTimer.getFirstStart()) ;
                        } else {
                            timerPaused = true ;
                        } */
                        //if (greenTimer.stepSim) {
                        //    step_finished = true ;
                        //    timerPaused = true ;
                        //}
                        
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    /*} else {    // motion paused
                        // Get current time
                        //tLast = System.currentTimeMillis();
                        //tTimer.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                    }
                     */
                }
             }   // void run
        } ; // runThread 0 
        tTimer = new Thread(runThread0, "ActuatedTimer") ;
        tTimer.start() ;
    }    
    public void start() {
        greenTimer.stepSim = stepSim_flag ; 
        yellowTimer.stepSim = stepSim_flag ;
        redTimer.stepSim = stepSim_flag ;
        phase_stage = 0 ;
        timesup = false ;
        timerPaused = false ;
        ok_to_terminate = false ;
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
    /*
    public void reset() {
        timesup = false ;
        phase_stage = 0 ;
        greenTimer.reset() ;
        yellowTimer.reset() ;
        redTimer.reset() ;
    }
    */
    public boolean isGreenTimeUp() {
        //System.out.println("grn gap out="+greenTimer.isGapOut()) ;
        //System.out.println("grn init out="+greenTimer.isInitOut()) ;
        //System.out.println("grn max out="+greenTimer.isMaxOut()) ;
        if (greenTimer.stepSim) {
            do {
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!greenTimer.step_finished) ;
        }
        return ((greenTimer.isGapOut() && greenTimer.isInitOut()) || greenTimer.isMaxOut()) ;
    }
    public boolean isYellowTimeUp() {
        if (yellowTimer.stepSim) {
            do {
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!yellowTimer.step_finished) ;
        }
        return yellowTimer.isTimeUp() ;
    }
    public boolean isRedTimeUp() {
        if (redTimer.stepSim) {
            do {
                try {Thread.sleep(50) ;}
                catch (InterruptedException ie) {} ;
            } while (!redTimer.step_finished) ;
        }
        return redTimer.isTimeUp() ;
    }
    
    public boolean isTimeUp() {
        return timesup ;
    }

    public void clear() {
        timesup = false ;
    }   // clear
    
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
