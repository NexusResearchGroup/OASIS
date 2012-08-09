/*
 * myTimer.java
 * Basic count up or down timer used for fixed green/yellow/red phases
 * Created on June 22, 2006, 9:59 AM
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

public class myTimer {
    private int flag = 0 ;     // 0-count down, >=1-count up
    private float countValue =0f ;  // Count up/down time
    private float currentCount=0f ; // current timer count
    private long tLast, tNow ;
    private Runnable runThread0 ;
    private Thread tTimer ;
    private boolean timerPaused = true ;
    private boolean timesup = false ;
    public boolean stepSim = false ;
    private boolean first_start = true ;
    public float elapsedTimeSec = 0f ;
    public boolean step_finished = true ;
    
    /** Creates a new instance of myTimer */
    public myTimer() {
        init() ;
    }
    public myTimer(int dir) {
        flag = dir ;
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
                            long elapsedTimeMillis = tNow-tLast;
                            // Get elapsed time in seconds
                            elapsedTimeSec = elapsedTimeMillis/1000f;      
                            if (stepSim) {
                                elapsedTimeSec = 1f ;   // 1 seconf step simulation
                            }
                            if (flag==0) {   // count down
                                currentCount -= elapsedTimeSec ;
                                //System.out.println("cur="+currentCount+",elapse="+elapsedTimeSec) ;

                                if (currentCount<=0f) {
                                    // time is up
                                    currentCount=0f ;
                                    timesup = true ;        // set time up flag
                                    timerPaused = true ;    // pause timer
                                }
                            } else {    // count up
                                currentCount += elapsedTimeSec ;
                                if (currentCount>=countValue) {
                                    // time is up
                                    timesup = true ;        // set time up flag
                                    timerPaused = true ;    // pause timer
                                }   // end if currentCount>=countValue?
                            }   // if flag==0
                        } else {
                            first_start = false ;
                        }
                        tLast = tNow ;
                        if (stepSim) {
                            timerPaused = true ;
                            step_finished = true ;
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
        tTimer = new Thread(runThread0, "Timer-X") ;
        tTimer.start() ;
    }    
    
    public void setTime(float _time) {
        countValue = _time ;
    }
    public float getTime() {
        return countValue ;
    }
    
    public void start() {
        first_start = true ;
        timesup = false ;
        if (flag==0) { // count down
            currentCount = countValue ;
        } else {    // count up
            currentCount = 0f ;
        }
        timerPaused = false ;
        
        tLast = System.currentTimeMillis();
    }
    public void resume() {
        timerPaused = false ;
        
        tLast = System.currentTimeMillis();
    }
    public void clear() {
        timesup = false ;
    }
    
    public void pause() {
        timerPaused = true ;
    }
    // reset timer
    public void reset() {
        timesup = false ;
        if (flag==0) { // count down
            currentCount = countValue ;
        } else {    // count up
            currentCount = 0f ;
        }        
    }
    public boolean isTimeUp() {
        return timesup ;
    }
    
    public float getSetCount() {
        return countValue ;
    }
    
    public float getCount() {
        return currentCount ;
    }
    public void setCount(float _value) {
        currentCount = _value ;
    }    
    public float getCountValue() {
        return countValue ;
    }
    public void setCountValue(float _value) {
        countValue = _value ;
    }
    public int getFlag() {
        return flag ;
    }
    public void setFlag(int _flag) {
        flag = _flag ;
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
    public boolean getFirstStart() {
        return first_start ;
    }
    public void setFirstStart(boolean status) {
        first_start = status ;
    }
 
    //public void destroy() {
    //    tTimer.destroy() ;
    //}
}
