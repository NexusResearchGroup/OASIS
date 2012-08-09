/*
 * VehPointer.java
 * reference to vehicle position & index
 *
 * Created on June 20, 2006, 11:43 AM
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
public class VehPointer {
    public int approach ;
    public int veh_index ;
    //public int px ;
    //public int py ;
    /** Creates a new instance of VehPointer */
    public VehPointer() {
    }
    public VehPointer(int i, int j) {   //, Point _pos) {
        approach = i ;
        veh_index = j ;
        //px = _pos.x ;
        //py = _pos.y ;
    }
}
