/*
 * Vehicle2D.java
 * 2D vehicle model.
 * Vehicle will run based on specified speed and trajectory.
 *
 * Created on June 15, 2006, 1:36 PM
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
import java.util.Random ;

public class Vehicle2D {
    
    private int route_id ;          // 0 - left turn, 1 - right turn, 2 & after - straight thru
    private int dir_id ;            // direction id, 0-EB, 1-WB, 2-NB, 3-SB
    public int width = 12 ;         // 6 ft
    public int length = 30 ;        // 15 ft
    private float heading = 0f ;                    // vehicle heading in radians, 0-East
    private Point pos = new Point(0, 0) ;           // vehicle location
    //private Point obstacle = new Point(-1, -1) ;
    private Point[] boundary = new Point[4] ;       // vehicle boundaries 4 points
    private int[] xpoints = new int[4];             // transformed vehicle corners
    private int[] ypoints = new int[4];
    private Color myColor = Color.blue ;             // default veh color
    private routeDB myRoute ;                       // assigned route
    private float speed = 0f ;                      // MPH, 1 MPH ~ 3 pixel/sec
    public float speed_control = 1f ;               // speed control factor 0-1
    //private Runnable runThread0 = null ;          // vehicle motion thread
    //private Thread tMove ;
    //private long tLast, tNow ;                    // last system clock time
    public long tEnterNetwork ;                     // time vehicel enetered network
    private float accuDist = 0f ;                   // acumulated distance vehicle will tarvel in next time period
    private float accuDistf = 0f ;                  // forecasted accu dist
    private int prev_route_index = 0 ;
//    public float nearest_veh_dist = 9999f ;     
    //private boolean vehPaused = true ;            // vehicle stopped initially
//    public boolean finished = false ;             // vehicle finished flag
//    Random random = new Random(1) ;                 // a random class
    private Point nextPos = new Point(0, 0) ;       // next vehicle location
    public boolean veh_stop_flag = false ;
    
    /** Creates a new instance of Vehicle2D */
    public Vehicle2D() {
    }

    public Vehicle2D(int _dir, int _route, routeDB _newRoute, float _spd) {
        dir_id = _dir ;
        route_id = _route ;
        speed = _spd ;
        myRoute = _newRoute ;
        myRoute.setIndex(0) ;
        pos = myRoute.getPosition(0) ;      // init position
        heading = myRoute.getHeading(0) ;   // init heading
        for (int i=0; i<4; i++) {
            boundary[i] = new Point(0,0) ;
        }
    }
    
    // public methods here    
    public void restorePrevRouteIndex() {
        myRoute.setIndex(prev_route_index) ;
        nextPos.x = pos.x ;
        nextPos.y = pos.y ;
    }
    
    public void forecastNextPosition(float _dt) {
        float distf, link_distf ;
        if (!veh_stop_flag) {
            if (route_id==0) {  // left turn lane
                distf = _dt * speed * speed_control ; //* 2.9f ;  // pix/sec=pixel/ft * ft/sec
            } else {
                distf = _dt * speed * speed_control / 2.9f ;  // pix/sec=pixel/ft * ft/sec
            }
            prev_route_index = myRoute.getIndex() ; // save a copy
            link_distf = myRoute.dist2NextPt() ;
            accuDistf = accuDist ;
            accuDistf += distf ;
            //System.out.println("accu_dist="+accuDist+", link_dist="+link_dist) ;
            while (accuDistf>=link_distf) {
                accuDistf -= link_distf ;
                myRoute.nextIndex() ;
                link_distf = myRoute.dist2NextPt() ;
                
                if (myRoute.getIndex()==myRoute.getDataSize()-1) {
                    // end of animation
                    //vehPaused = true ;
                    //finished = true ;
                    break ;
                    //tMove.destroy() ;
                }   // end if
                
            }   // end while
            nextPos = myRoute.getTargetPosition(accuDistf) ;
        }   // if stop_flag
        // return forecasted position at next sim time step
        //return nextPos ;
    }
    // get next forecasted vehicle position
    public Point getNextForecastedPos() {
        return nextPos ;
    }
    public String getNextForecastedPos2Str() {
        String str = "("+nextPos.x + ", " + nextPos.y + ")" ;
        return str ;
    }
    // update vehicle position
    public void updatePos() {
        pos.x = nextPos.x ;
        pos.y = nextPos.y ;
        accuDist = accuDistf ;
        prev_route_index = myRoute.getIndex() ; // save a copy
        updateBoundary() ;
    }
    public void vehPause() {
        veh_stop_flag = true ;
    }    
    public void vehResume() {
        veh_stop_flag = false ;
        speed_control = 1f ;
    }  
    // vehicle color
    //public void setColor(Color _c) {
    //    myColor = _c ;
    //}
    // generate random vehicle color
    public void setColor(int r, int g, int b) {
        if (colorNearBy(r, 123, 5) && colorNearBy(g, 123, 5) && colorNearBy(b, 123, 5)) {
            myColor = Color.black ;
        } else {
            myColor = new Color(r, g, b) ;
        }
    }
    public Color getVehColor() {
        return myColor ;
    }
    
    private boolean colorNearBy(int v1, int v2, int dv) {
        if (Math.abs(v1-v2) <= dv) {
            return true ;
        } else {
            return false ;
        }
    }
     
    // calculate vehicle boundary with corresponding heading
    private void updateBoundary() {
        //pos = _newPos ;
        heading = myRoute.getCurrentHeading() ;
        //System.out.println("heading="+heading);
        Point vertex = new Point(0,0) ;
        vertex.x = length/2 ;
        vertex.y = width/2 ;
        boundary[0] = transform(vertex) ;
        
        vertex.x = -length/2 ;
        vertex.y = width/2 ;
        boundary[1] = transform(vertex) ;
        
        vertex.x = -length/2 ;
        vertex.y = -width/2 ;
        boundary[2] = transform(vertex) ;
        
        vertex.x = length/2 ;
        vertex.y = -width/2 ;
        boundary[3] = transform(vertex) ;
        for (int i=0; i<4; i++) {
            xpoints[i] = boundary[i].x ;
            ypoints[i] = boundary[i].y ;
        }
    }
    // 2D vector transform of current vehicle boundary point
    private Point transform(Point pt1) {
        Point pt2 = new Point(0,0) ;
        pt2.x = pos.x + new Double(pt1.x*Math.cos(heading)+pt1.y*Math.sin(heading)).intValue() ;
        pt2.y = pos.y + new Double(-pt1.x*Math.sin(heading)+pt1.y*Math.cos(heading)).intValue() ;
        return pt2 ;
    }
    
    public int[] getXpoints() {
        return xpoints ;
    }
    public int[] getYpoints() {
        return ypoints ;
    }
    public String getPosition2Str() {
        String str = "("+pos.x + ", " + pos.y + ")" ;
        return str ;
    }
    public Point getPosition() {
        return pos ;
    }
    public int getRouteID() {
        return route_id ;
    }
    
    public int getRouteIndex() {
        return myRoute.getIndex() ;
    }
    public int getVehAtLink() {
        return myRoute.getLinkID() ;
    }
    
    public void slowdown() {
        speed_control = .3f ;
    }
    public void slowdown(float sf) {
        speed_control = sf ;
    }
 
    public void stop() {
        speed_control = 0f ;
        veh_stop_flag = true ;
    }
    public boolean isVehStopped() {
        return veh_stop_flag ;
    }
    public Point getFrontBumperCenter() {
        int fbcx = (boundary[0].x + boundary[3].x)/2 ;
        int fbcy = (boundary[0].y + boundary[3].y)/2 ;
        return new Point(fbcx, fbcy) ;
    }
    public String getFrontBumperCenter2Str() {
        int fbcx = (boundary[0].x + boundary[3].x)/2 ;
        int fbcy = (boundary[0].y + boundary[3].y)/2 ;
        return "("+fbcx+", "+fbcy+")" ;
    }
    public Point getRearBumperCenter() {
        int rbcx = (boundary[1].x + boundary[2].x)/2 ;
        int rbcy = (boundary[1].y + boundary[2].y)/2 ;
        return new Point(rbcx, rbcy) ;
    }
    public routeDB getRoute() {
        return myRoute ;
    }
    public float getHeading() {
        return heading ;
    }
    public mPointF headingVector() {
        // vehicle heading vector
        mPointF hVector = new mPointF(xpoints[0]-xpoints[1], ypoints[0]-ypoints[1]) ;   
        return hVector.unitVector() ;
    }
    // check if clicked point inside vehicle boundary
    public boolean isClicked(int x, int y) {
        boolean in_flag = false ;
        int i ;
        int j=0 ;
        for (i=1; i<4; i++) {
            j=i-1 ;
            if ((((ypoints[i] <= y) && (y < ypoints[j])) || ((ypoints[j] <= y) && (y < ypoints[i]))) 
            && (x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i]) + xpoints[i])) {
                in_flag = !in_flag ;
            }
        }   // end for
        return in_flag ;
        //System.out.println("IN_FLAG="+in_flag+VIDStr(100)) ;
        /*
        int dx = x - pos.x ;
        int dy = y - pos.y ;
        double dist = Math.sqrt(dx*dx+dy*dy) ;
        if (dist<=length) {
            return true ;
        } else {
            return false ; 
        }
         */
    }
    public float getSpeed() {
        return speed * speed_control * 2.933f ;
    }
    public float getSpeed_MPH() {
        return speed  ;
    }
    public float getAccuDist() {
        return accuDist ;
    }
    public String VIDStr(int veh_index) {
        return "("+dir_id+", "+veh_index+", "+route_id+")" ;
    }
}
