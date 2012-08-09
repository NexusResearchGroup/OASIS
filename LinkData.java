/*
 * LinkData.java
 * Definition of a traffic link including loop detector in each lane
 * Assume maximum lane N, 0-left turn lane if exists
 *
 * Created on June 7, 2006, 10:22 AM
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
import java.awt.* ;

public class LinkData {
    public static final int MAX_LANE_SIZE = 4 ;   // max number of lanes in each approach, 
                                    // 0-left turn lane if exists
    private int numberOfLane = 0 ;
    private float speedLimit=0f ;   // speed limit MPH
    private float volume=0f ;       // traffic volume vph
    private boolean hasLeftTurnLane = false ;       // exclusive left turn lane
    public Point stopbar_UL, stopbar_LR ;           // stop bar upper left & lower left corners
    public int detector_dist = 175 ;                 // distance of extension loop detector away from stop bar
    public int LT_detector_dist = 130 ;                 // distance of extension loop detector away from stop bar
    public int presence_detector_dist = 45 ;         // distance of presence loop detector away from stop bar
    public int[] loopDetCount = new int[MAX_LANE_SIZE] ;        // detection count, 4 lanes , used for calculation 
                                                                // purpose to determine if the loop is occupied or not
    public int[] presenceLoopDetCount = new int[MAX_LANE_SIZE] ; // detection count, 4 lanes , used for calculation 
                                                                // purpose to determine if the loop is occupied or not
    private boolean[] loopOccupied = new boolean[MAX_LANE_SIZE] ;       // max 3 lanes + left turn lane each approach
    private boolean[] presenceLoopOccupied = new boolean[MAX_LANE_SIZE] ; // max 3 lanes + left turn lane each approach
    public Point[] detectorUL = new Point[MAX_LANE_SIZE]   ;            // detector upper left corner location
                                                                        // max 3 lanes + left turn lane each approach
    public Point[] presence_detectorUL = new Point[MAX_LANE_SIZE]   ;  // detector upper left corner location
                                                                        // max 3 lanes + left turn lane each approach
    private int[] detVehCount = new int[MAX_LANE_SIZE] ;            // loop detector vehicle counts
    private int[] presenceDetVehCount = new int[MAX_LANE_SIZE] ;            // presence loop detector vehicle counts
    public Point[] signalLit_UL = new Point[MAX_LANE_SIZE]   ;      // signal light
    
    /** Creates a new instance of LinkData */
    public LinkData() {
        // reset vehicle detector counter
        for (int i=0; i<MAX_LANE_SIZE; i++) {
            detVehCount[i] = 0 ;
        }
    }
    
    public LinkData(float _spd, float _vol, boolean state, int _laneNum) {
        speedLimit = _spd ;
        volume = _vol ;
        hasLeftTurnLane = state ;
        numberOfLane = _laneNum ;
        // reset vehicle detector counter
        for (int i=0; i<MAX_LANE_SIZE; i++) {
            detVehCount[i] = 0 ;
        }
    }    
    
    // public methods here
    public float getSpeedLimit()
    {
        return speedLimit;
    }
    public float getVolume()
    {
        return volume;
    }
    public boolean leftTurnLaneExists()
    {
        return hasLeftTurnLane;
    }
    public int getLaneSize()
    {
        return numberOfLane ;
    }
    public void setSpeedLimit(float _spd)
    {
        speedLimit = _spd ;
    }
    public void setVolume(float _vol)
    {
        volume = _vol ;
    }
    public void setLeftTurnLane(boolean state)
    {
        hasLeftTurnLane = state ;
    }
    public void setLaneSize(int _lane)
    {
        numberOfLane = _lane ;
    }
    
    // return # of vehicle detected at each lane
    public int getVehicleCount(int _laneID) {
        return detVehCount[_laneID] ;
    }
    
    public void setLoopOccupied(int _laneID, boolean state) {
        if ((loopOccupied[_laneID] != state) && state ) {
            // rising edge detectioin
            detVehCount[_laneID]++ ;
        }
        loopOccupied[_laneID] = state ;
    }
    public void setPresenceLoopOccupied(int _laneID, boolean state) {
        if ((presenceLoopOccupied[_laneID] != state) && state ) {
            // rising edge detectioin
            presenceDetVehCount[_laneID]++ ;
        }
        presenceLoopOccupied[_laneID] = state ;
    }    
    public boolean toggleLoopOccupied(int _laneID) {
        if (loopOccupied[_laneID] == true ) {
            loopOccupied[_laneID] = false ;
        } else {
            loopOccupied[_laneID] = true ;
        }
        return loopOccupied[_laneID] ;
    }
    public boolean togglePresenceLoopOccupied(int _laneID) {
        if (presenceLoopOccupied[_laneID] == true ) {
            presenceLoopOccupied[_laneID] = false ;
        } else {
            presenceLoopOccupied[_laneID] = true ;
        }
        return presenceLoopOccupied[_laneID] ;
    }    
    
    public boolean isLoopOccupied(int _laneID) {
        return loopOccupied[_laneID] ;
    }
    public void resetExtLoops() {
        
        for (int i=1; i<MAX_LANE_SIZE; i++) {
            loopOccupied[i] = false ;
        }
    }
    public void resetPresenceLoops() {
        for (int i=0; i<MAX_LANE_SIZE; i++) {
            presenceLoopOccupied[i] = false ;
        }
    }
    public void resetAllLoops() {
        for (int i=0; i<MAX_LANE_SIZE; i++) {
            presenceLoopOccupied[i] = false ;
            loopOccupied[i] = false ;
        }
    }
    public boolean isPresenceLoopOccupied(int _laneID) {
        return presenceLoopOccupied[_laneID] ;
    }
}
