/*
 * SHARED.java
 * Shard data variable class.
 *
 * Created on May 31, 2006, 9:16 AM
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
/** Revision Log:
 *  Version 0.1: March 19, 2007, release actuation by mouse version for CE3201
 *      Show >| and |< button un-pressed after clicked, 3/30/07
 *      Add full step feature up to 64 steps(sec), 3/30/07
 *      Inlcude presence detector for green extension, 3/30/07
 *  Version 0.2: April 01, 2007
 *      - comment out presence detector 8/20/07
 *      - change timediaply to integers with no decimal points, 8/20/07
 *      - link extenstion detectors in the same approach, 8/20/07
 *      - add save settings & timing features
 *      - Modify signal timing & sychronization, 4/2/08
 *  Version 0.3: April 03, 2008
 */

import java.awt.*;

public class SHARED {
    public static final String VERSION = "Ver.0.3, April 2008 (c)" ;   // Version string
    public static final String MANUAL_PATH = "http://street.umn.edu/OASIS/asc_manual.pdf" ;  // user's manual file path
    public static final String CONTENTS_PATH = "http://street.umn.edu/OASIS/javahelp/OASISWebMain.html" ;  // help set file path
    public int LANE_WIDTH = 24 ;                            // lane width 12 feet, 1ft=2 pixel
    public int SIGNAL_HEAD_SIZE = 12 ;                       // signal head size
    public int LOOP_DET_WIDTH = 14 ;                        // loop detector width, 6ft x 6ft, or 12x12 pixel
    public int LOOP_DET_LENGTH = 24 ;                       // loop detector length
    public int PRESENCE_LOOP_DET_LENGTH = 20 ;              // presence loop detector length
    public int LT_LOOP_DET_LENGTH = 105 ;                   // exclusice left turn loop detector length
    public Color myPenColor=Color.blue ;                    // this is a color the user selects
    public LinkData NB_data = new LinkData() ;              // link geometry data
    public LinkData SB_data = new LinkData() ;
    public LinkData EB_data = new LinkData() ;
    public LinkData WB_data = new LinkData() ;
    public SignalTiming mySignalControl = new SignalTiming() ; // 8 phase signal control
    public boolean mainStreetNS = false ;                    // main street flag
                
    /** Creates a new instance of SHARED */
    public SHARED() {
    }

}
