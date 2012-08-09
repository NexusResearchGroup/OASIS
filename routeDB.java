/*
 * routeDB.java
 * Vehicle trajectory database, used for vehicle trajectory following
 *
 * Created on June 14, 2006, 11:14 AM
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

public class routeDB {
    private int dataSize=0 ;      // number of route data points
    private Point[] pos;          // position DB
    private int[] link;           // each position associated link,  0-EB,   1-WB,   2-NB,   3-SB
                                  // link passed intersection (99) 104-EB, 105-WB, 106-NB, 107-SB
    private float heading=0 ;     // current heading 
    private int index=0;          // route data point index
    
    /** Creates a new instance of routeDB */
    public routeDB() {
    }
    public routeDB(int _dbSize) {
        dataSize = _dbSize ;
        // initialize DB
        pos = new Point[dataSize] ;
        link = new int[dataSize] ;
        for (int i=0; i<dataSize; i++){
            pos[i] = new Point(0, 0) ;
            link[i] = -1 ;
        }
        index=0 ;
    }
    public routeDB(int _dbSize, Point[] _pos) {
        dataSize = _dbSize ;
        link = new int[dataSize] ;
        // initialize DB
        pos = new Point[dataSize] ;
        
        for (int i=0; i<dataSize; i++){
            pos[i] = _pos[i] ;
            link[i] = -1 ;
        }
        index=0 ;
    }
    
    public boolean last() {
        if (index>=dataSize-1) {
            return true ;
        } else {
            return false ;
        }
    }
    
    // public methods
    public void setRouteDB(Point[] _pos) {
        for (int i=0; i<dataSize; i++){
            pos[i] = _pos[i] ;
            
        }
    }
    
    public void setRouteDBi(int idx, Point _pos, int _link_id) {
        pos[idx] = _pos ;
        link[idx] = _link_id ;
    }
    
    public int getDataSize() {
        return dataSize ;
    }
    public void setIndex(int idx) {
        index = idx ;
    }
    public int getIndex() {
        return index ;
    }
    public int getLinkID() {
        return link[index] ;
    }
    public float dist2NextPt() {
        if (index==dataSize-1) {
            return 0f ;
        } else {
            float dx = pos[index].x - pos[index+1].x ;
            float dy = pos[index].y - pos[index+1].y ;
            float dist = new Double(Math.sqrt(dx*dx+dy*dy)).floatValue() ;
            return dist ;
        }
    }
    public float getCurrentHeading() {
        if (index < dataSize-1) {
            heading = new Float(Math.atan2(pos[index].y-pos[index+1].y, pos[index+1].x-pos[index].x)).floatValue() ;
        }
        return heading ;
    }
    public float getHeading(int idx) {
        if (idx < dataSize-1) {
            heading = new Float(Math.atan2(pos[idx].y-pos[idx+1].y, pos[idx+1].x-pos[idx].x)).floatValue() ;
        } else {
            heading = new Float(Math.atan2(pos[dataSize-2].y-pos[dataSize-1].y, pos[dataSize-1].x-pos[dataSize-2].x)).floatValue() ;
        }
        return heading ;
    }    
    public Point getCurrentPosition() {
        return pos[index] ;
    }
    public Point getTargetPosition(float dist) {
        Point _target = new Point(0,0) ;
        if (index==dataSize-1) {
            _target = pos[index] ;
        } else {
            float dx = pos[index+1].x - pos[index].x ;
            float dy = pos[index+1].y - pos[index].y ;
            float link_dist = new Double(Math.sqrt(dx*dx+dy*dy)).floatValue() ;
            float sf = dist / link_dist ;
            _target.x = pos[index].x + new Float(dx*sf).intValue() ;
            _target.y = pos[index].y + new Float(dy*sf).intValue() ;
        }
        return _target ;
    }
    public Point getPosition(int idx) {
        return pos[idx] ;
    }
    public void nextIndex() {
        if (index<dataSize-1) {
            index++ ;
        } else {
            index = dataSize-1 ;
        }
    }   // nextIndex
    public void print() {
        for (int i=0; i<dataSize; i++) {
            System.out.println(pos[i].x+","+pos[i].y+","+getHeading(i)) ;
        }
    }
}
