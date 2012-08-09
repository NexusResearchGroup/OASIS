/*
 * SignalTiming.java
 * actuated signal control timing class
 * Created on June 21, 2006, 10:25 AM
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

public class SignalTiming {
    statusbar sb;                           // status bar
    public int control_type = 2 ;           // 0-fixed time, 1-actuated by vehicle, 2-actuated by mouse
    public static final int NUM_PHASES = 8 ;       // max number of signal phases
    
    public SignalPhase[] myPhases = new SignalPhase[NUM_PHASES] ;       // default 8/12-phase controller
    public boolean[] myExtRegisters = new boolean[NUM_PHASES] ;         // conflicting calls
    public boolean[] myPresenceRegisters = new boolean[NUM_PHASES] ;    // conflicting calls
    // governing phase & clock status
    public boolean[] phaseStatus = new boolean[NUM_PHASES] ;        // current phase status, grn/yel:true, red:false
    public boolean[] protected_left = new boolean[NUM_PHASES/2] ;   // protected left turn phases
    public Color[] phaseLitColor = new Color[NUM_PHASES] ;          // phase lit colors

    //public myTimer initialTimer = new myTimer() ;        // count down timer
    //public myTimer extensionTimer = new myTimer() ;      // count down timer
    //public myTimer maxTimer = new myTimer() ;            // count down timer
    
  
    private Runnable runThread0 = null ;
    private Thread tSignalControl ;                     // signal control thread
    private boolean signalPaused = true ;               // flag to pasuse signal timing control
    private long tLast ;
    
    //private int[] ring1Seq = {1,2,3,4} ;        // phasing sequence
    //private int[] ring2Seq = {5,6,7,8} ;
    private int[] ring1Seq = new int[NUM_PHASES] ;      // ring 1 sequence
    private int[] ring2Seq = new int[NUM_PHASES] ;      // ring 2 sequence
    public boolean[] CG = new boolean[NUM_PHASES] ;    // barrier CG in LCD page F1-1-1
    
    // no exclusive LT lane
    //private int[] ring1Seq = {2,2,4,4} ;        // phasing sequence
   // private int[] ring2Seq = {6,6,8,8} ;
    
    // fixed time
    public int ring1ptr = -1 ;
    public int ring2ptr = -1 ;
    public myFixedTimer fixedTimer1 = new myFixedTimer() ;            // count down timer
    public myFixedTimer fixedTimer2 = new myFixedTimer() ;            // count down timer
    public myActuatedTimer actuatedTimer1 = new myActuatedTimer() ;        // count down timer
    public myActuatedTimer actuatedTimer2 = new myActuatedTimer() ;        // count down timer
    public boolean step_finished = true ;
    
    /* Creates a new instance of Signal Timing */
    public SignalTiming() {
        for (int i=0; i<NUM_PHASES; i++) {
            myPhases[i] = new SignalPhase() ;
            myExtRegisters[i] = false ;
            myPresenceRegisters[i] = false ;
            phaseStatus[i] = false ;    // all read initially
            CG[i] = false ;
            phaseLitColor[i] = Color.red ;
        }
        // enable phase 2 & 6 as default
        //phaseStatus[1] = false ;
        //phaseStatus[5] = false ;
        
         runThread0 = new Runnable() {
            public void run() {
                while (true) {
                    if (!signalPaused){
                        
                        // Get elapsed time in milliseconds
                        //System.out.println("tLast="+tLast) ;
                   //     long tNow = System.currentTimeMillis() ;
                        //System.out.println("tNow="+tNow) ;
                   //     long elapsedTimeMillis = tNow-tLast;
                        // Get elapsed time in seconds
                   //     float elapsedTimeSec = elapsedTimeMillis/1000F;      
                        //System.out.println("timeElapsed="+elapsedTimeSec) ;
                        
                        // signal control here ... 
                        // get detector data & determine governing clock & phases status
                        if (control_type==0) {
                            // fixed time
                            //System.out.println("timer1 count="+fixedTimer1.greenTimer.getCount()) ;

                            if (fixedTimer1.isTimeUp()) {
                                fixedTimer1.clear() ;
                                phaseStatus[ring1Seq[ring1ptr]-1] = false ;
                                int last_phase = ring1Seq[ring1ptr] ;
                                //ring1ptr++ ;
                                //if (ring1ptr>=4) { ring1ptr=0 ; }
                                // find next valid phase ;
                                do {
                                    ring1ptr++ ;
                                    if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                                } while ( ring1Seq[ring1ptr]<=0 ) ;
                                if (ring1Seq[ring1ptr]==last_phase) {
                                    // same phase, skip
                                    //ring1ptr++ ;
                                    //if (ring1ptr>=4) { ring1ptr=0 ; }
                                    // find next valid phase ;
                                    do { 
                                        ring1ptr++ ;
                                        if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                                    } while ( ring1Seq[ring1ptr]<=0 ) ;
                                }
                                fixedTimer1.setDurations(myPhases[ring1Seq[ring1ptr]-1].getInitial(),
                                    myPhases[ring1Seq[ring1ptr]-1].getYellow(), 
                                    myPhases[ring1Seq[ring1ptr]-1].getRed() ) ;
                                fixedTimer1.start() ;
                                phaseStatus[ring1Seq[ring1ptr]-1] = true ;
                                //System.out.println("ring1:"+ring1Seq[ring1ptr]) ;
                            //} else {
                            //    System.out.println("t1:"+fixedTimer1.greenTimer.getCount()) ;
                            }
                            if (fixedTimer2.isTimeUp()) {
                                fixedTimer2.clear() ;
                                phaseStatus[ring2Seq[ring2ptr]-1] = false ;
                                int last_phase = ring2Seq[ring2ptr] ;
                                //ring2ptr++ ;
                                //if (ring2ptr>=4) { ring2ptr=0 ; }
                                // find next valid phase ;
                                do {
                                    ring2ptr++ ;
                                    if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                                } while ( ring2Seq[ring2ptr]<=0 ) ;
                                if (ring2Seq[ring2ptr]==last_phase) {
                                    // same phase, skip
                                    //ring2ptr++ ;
                                    //if (ring2ptr>=4) { ring2ptr=0 ; }
                                    // find next valid phase ;
                                    do {
                                        ring2ptr++ ;
                                        if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                                    } while ( ring2Seq[ring2ptr]<=0 ) ;
                                }
                                fixedTimer2.setDurations(myPhases[ring2Seq[ring2ptr]-1].getInitial(), 
                                    myPhases[ring2Seq[ring2ptr]-1].getYellow(),
                                    myPhases[ring2Seq[ring2ptr]-1].getRed() ) ;
                                fixedTimer2.start() ;
                                phaseStatus[ring2Seq[ring2ptr]-1] = true ;
                                //System.out.println("ring2:"+ring2Seq[ring2ptr]) ;
                            }
                            // added 4/2/08
                            if (fixedTimer1.greenTimer.stepSim || fixedTimer2.greenTimer.stepSim) {
                                pauseFixedTime() ;
                            }
                        } else if (control_type==1 || control_type==2) {
                        // =========================================================
                        // =========================================================
                        // ACTUATED signal control
                        // will skip phase(s) if no actuation on none-recall phase
                            
                            if (actuatedTimer1.greenTimer.stepSim || actuatedTimer2.greenTimer.stepSim) {
                                step_finished = false ;
                            }
                            // actuated control
                            // =====================================
                            //=====================================================================
                            
                            if (actuatedTimer1.isGreenTimeUp()) {
                                //System.out.println("Ring 1 Green UP = true") ;
                                /*
                                 if (actuatedTimer1.phase_stage==2) {   // yellow
                                    System.out.println("yellow 1") ;
                                    if (actuatedTimer1.timeup_flag==1) {
                                        continueActuatedTime1(true) ; 
                                        /*do {
                                            try {Thread.sleep(100) ;}
                                            catch (InterruptedException ie) {} ;
                                        } while (!actuatedTimer1.step_finished) ;
                                        *
                                        actuatedTimer1.timeup_flag=2 ;  // yellow time up
                                    }  
                                }   // if yellow up                                    
                                else if (actuatedTimer1.phase_stage==3) {    // in red
                                    System.out.println("red 1") ;
                                    if (actuatedTimer1.timeup_flag==2) {
                                        continueActuatedTime1(true) ; 
                                        /*do {
                                            try {Thread.sleep(100) ;}
                                            catch (InterruptedException ie) {} ;
                                        } while (!actuatedTimer1.step_finished) ;
                                        *
                                        actuatedTimer1.timeup_flag=0 ;  // reset
                                    }
                                }   // if red up     
                                */
                                //System.out.println("R1 barrier="+CG[ring1ptr]) ;
                                 if (!CG[ring1ptr]) {   // not barrier
                                     actuatedTimer1.ok_to_terminate = true ;
                                     actuatedTimer1.greenTimer.resetTimer() ; 
                                     // display gap/max out
                                     if (actuatedTimer1.greenTimer.isGapOut()) {
                                        statusBar_Message("Ph "+ring1Seq[ring1ptr]+" GAP OUT ") ;
                                     } else if (actuatedTimer1.greenTimer.isMaxOut()) {
                                        statusBar_Message("Ph "+ring1Seq[ring1ptr]+" MAX OUT ") ;
                                     } else {
                                        statusBar_Message(" ") ;
                                     }
                                     //if (actuatedTimer1.timeup_flag==0) {
                                     //   continueActuatedTime1(true) ;
                                     //   actuatedTimer1.timeup_flag=1 ;  //  green timeup
                                     //} 
                                 } else {   // reaching barrier
                                 //    System.out.println("Ring 2 Green UP="+actuatedTimer2.isGreenTimeUp()) ;
                                     if (actuatedTimer2.isGreenTimeUp()) {
                                        actuatedTimer1.ok_to_terminate = true ;
                                        actuatedTimer2.ok_to_terminate = true ;
                                        actuatedTimer1.greenTimer.resetTimer() ; 
                                        actuatedTimer2.greenTimer.resetTimer() ; 
                                        // display gap/max out
                                        String str="";
                                        if (actuatedTimer1.greenTimer.isGapOut()) {
                                            str+="Ph "+ring1Seq[ring1ptr]+" GAP OUT," ;
                                        } else if (actuatedTimer1.greenTimer.isMaxOut()) {
                                            str+="Ph "+ring1Seq[ring1ptr]+" MAX OUT," ;
                                        }                                        // display gap/max out
                                        if (actuatedTimer2.greenTimer.isGapOut()) {
                                            str+="Ph "+ring2Seq[ring2ptr]+" GAP OUT " ;
                                        } else if (actuatedTimer1.greenTimer.isMaxOut()) {
                                            str+="Ph "+ring2Seq[ring2ptr]+" MAX OUT " ;
                                        }
                                        statusBar_Message(str);
                                        //if (actuatedTimer1.timeup_flag==0 && actuatedTimer2.timeup_flag==0) {
                                        //    continueActuatedTime(true) ;
                                        //    actuatedTimer1.timeup_flag=1 ;  // green time up
                                        //    actuatedTimer2.timeup_flag=1 ;
                                        //}
                                        
                                     } else {
                                        actuatedTimer1.ok_to_terminate = false ;
                                        statusBar_Message(" ");
                                     }
                                 } 
                                 
                            }   // timer1 green time up
                            
                            // timer 2 -----------------------------------------
                            
                            if (actuatedTimer2.isGreenTimeUp()) {
                                //System.out.println("Ring 2 Green UP = true") ;
                                /*
                                if (actuatedTimer2.phase_stage==2) {   // yellow
                                    System.out.println("yellow 2 ") ;
                                    if (actuatedTimer2.timeup_flag==1) {
                                        continueActuatedTime2(true) ; 
                                        /*do {
                                            try {Thread.sleep(100) ;}
                                            catch (InterruptedException ie) {} ;
                                        } while (!actuatedTimer2.step_finished) ;
                                        *
                                        actuatedTimer2.timeup_flag=2 ;  // yellow time up
                                    }

                                }   // if yellow up    
                                else if (actuatedTimer2.phase_stage==3) {
                                    System.out.println("red 2 ") ;
                                    if (actuatedTimer2.timeup_flag==2) {
                                        continueActuatedTime2(true) ;   // red time up once
                                        /*do {
                                            try {Thread.sleep(100) ;}
                                            catch (InterruptedException ie) {} ;
                                        } while (!actuatedTimer2.step_finished) ;
                                        *
                                        actuatedTimer2.timeup_flag=0 ;  // reset
                                    }
                                }   // if red up                                  
                                */ 
                                 if (!CG[ring2ptr]) {   // not barrier
                                     actuatedTimer2.ok_to_terminate = true ;
                                     actuatedTimer2.greenTimer.resetTimer() ;
                                    // display gap/max out
                                     if (actuatedTimer2.greenTimer.isGapOut()) {
                                        statusBar_Message("Ph "+ring2Seq[ring2ptr]+" GAP OUT ") ;
                                     } else if (actuatedTimer2.greenTimer.isMaxOut()) {
                                        statusBar_Message("Ph "+ring2Seq[ring2ptr]+" MAX OUT ") ;
                                     } else {
                                        statusBar_Message(" ") ;
                                     }
                                    //if (actuatedTimer1.timeup_flag==0) {
                                    //    continueActuatedTime2(true) ;
                                    //    actuatedTimer1.timeup_flag=1 ;  // green time up
                                    //}
                                 } else {   // reaching barrier
                                 //System.out.println("Ring 1 Green UP="+actuatedTimer1.isGreenTimeUp()) ;
                                     if (actuatedTimer1.isGreenTimeUp()) {
                                        actuatedTimer1.ok_to_terminate = true ;
                                        actuatedTimer2.ok_to_terminate = true ;
                                        actuatedTimer1.greenTimer.resetTimer() ; 
                                        actuatedTimer2.greenTimer.resetTimer() ;
                                        // display gap/max out
                                        String str="";
                                        if (actuatedTimer1.greenTimer.isGapOut()) {
                                            str+="Ph "+ring1Seq[ring1ptr]+" GAP OUT," ;
                                        } else if (actuatedTimer1.greenTimer.isMaxOut()) {
                                            str+="Ph "+ring1Seq[ring1ptr]+" MAX OUT," ;
                                        }                                        // display gap/max out
                                        if (actuatedTimer2.greenTimer.isGapOut()) {
                                            str+="Ph "+ring2Seq[ring2ptr]+" GAP OUT" ;
                                        } else if (actuatedTimer1.greenTimer.isMaxOut()) {
                                            str+="Ph "+ring2Seq[ring2ptr]+" MAX OUT" ;
                                        }
                                        statusBar_Message(str);
                                        //if (actuatedTimer1.timeup_flag==0 && actuatedTimer2.timeup_flag==0) {
                                        //    continueActuatedTime(true) ;
                                        //    actuatedTimer1.timeup_flag=1 ;  // green time up
                                        //    actuatedTimer2.timeup_flag=1 ;
                                        //}
                                     } else {
                                        actuatedTimer2.ok_to_terminate = false ;  
                                        statusBar_Message(" ");
                                     }
                                 }  // check barrier
                                
                            }   // timer2 green time up
                            
                            
                            // Ring 1 Timer =========================================
                            if (actuatedTimer1.isTimeUp()) {
                                // timer1 gap out or max out 
                                // check if reaching a barrier
                                if (!CG[ring1ptr]) {
                                    // not reaching a barrier
                                    // ring 1 change to next phase
                                    phaseStatus[ring1Seq[ring1ptr]-1] = false ;
                                    int last_phase = ring1Seq[ring1ptr] ;
                                    // find next valid phase ;
                                    do {
                                        ring1ptr++ ;
                                        if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                                    } while ( ring1Seq[ring1ptr]<=0 ) ;
                                    if (ring1Seq[ring1ptr]==last_phase) {
                                        // same phase, skip
                                        // find next valid phase ;
                                        do {
                                            ring1ptr++ ;
                                            if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                                        } while ( ring1Seq[ring1ptr]<=0 ) ;
                                    }
                                    startActuatedControl1() ;
                                    /*
                                    actuatedTimer1.setDurations(myPhases[ring1Seq[ring1ptr]-1].getInitial(),
                                        myPhases[ring1Seq[ring1ptr]-1].getExtension(),
                                        myPhases[ring1Seq[ring1ptr]-1].getMax1(),
                                        myPhases[ring1Seq[ring1ptr]-1].getYellow(),
                                        myPhases[ring1Seq[ring1ptr]-1].getRed() ) ;
                                    actuatedTimer1.start() ;
                                    phaseStatus[ring1Seq[ring1ptr]-1] = true ;
                                    */
                                } else {
                                    // ring 1 reaches barrier, check the other ring status
                                     if (actuatedTimer2.isTimeUp()) {
                                        // move both ring1 & ring2 to the next phase
                                        R12_crossBarrier() ;
                                        startActuatedControl() ;
                                     }  // ring2 status
                                }   // barrier?
                            //    System.out.println("R1 time up") ;
                            } else {
                                // not gap out & NOT max out
                                if (myExtRegisters[ring1Seq[ring1ptr]-1]) {
                                    // vehicle detected
                                    actuatedTimer1.greenTimer.set_extension(myPhases[ring1Seq[ring1ptr]-1].getExtension()) ;
                                }   // vehicle detected
                                if (myPhases[ring1Seq[ring1ptr]-1].Veh_Recall) {
                                //    System.out.println("R1 recall") ;
                                    // recall phase, check conflicting presence detectors
                                    if (!checkOtherApproaches(ring1Seq[ring1ptr], 1)) {
                                        // no veh presence at all other approaches, 
                                        // MAX does not count down
                                    //    System.out.println("R1 recall = true") ;
                                        actuatedTimer1.greenTimer.hold_recall(true) ;
                                    } else {
                                        actuatedTimer1.greenTimer.hold_recall(false) ;
                                    //    System.out.println("R1 recall = false") ;
                                    }
                                }   // if Veh_recall
                            }   // end if timer 1 gap/max out?
                            
                            // Ring 2 timer =========================================
                            if (actuatedTimer2.isTimeUp()) {
                                // timer2 gap out or max out 
                                // check if reaching a barrier
                                if (!CG[ring2ptr]) {
                                    // not reaching a barrier
                                    // ring 2 change to next phase
                                    phaseStatus[ring2Seq[ring2ptr]-1] = false ;
                                    int last_phase = ring2Seq[ring2ptr] ;
                                    // find next valid phase ;
                                    do {
                                        ring2ptr++ ;
                                        if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                                    } while ( ring2Seq[ring2ptr]<=0 ) ;
                                    if (ring2Seq[ring2ptr]==last_phase) {
                                        // same phase, skip
                                        // find next valid phase ;
                                        do {
                                            ring2ptr++ ;
                                            if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                                        } while ( ring2Seq[ring2ptr]<=0 ) ;
                                    }
                                    startActuatedControl2() ;
                                    /*
                                    actuatedTimer2.setDurations(myPhases[ring2Seq[ring2ptr]-1].getInitial(),
                                        myPhases[ring2Seq[ring2ptr]-1].getExtension(),
                                        myPhases[ring2Seq[ring2ptr]-1].getMax1(),
                                        myPhases[ring2Seq[ring2ptr]-1].getYellow(),
                                        myPhases[ring2Seq[ring2ptr]-1].getRed() ) ;
                                    actuatedTimer2.start() ;
                                    phaseStatus[ring2Seq[ring2ptr]-1] = true ;
                                    */
                                } else {
                                    // ring 2 reaches barrier, check the other ring status
                                     if (actuatedTimer1.isTimeUp()) {
                                        // move both ring1 & ring2 to the next phase
                                        R12_crossBarrier() ;
                                        startActuatedControl() ;
                                     }  // ring2 status
                                }   // barrier?
                            //    System.out.println("R2 time up") ;
                            } else {
                                // not gap out & not max out
                                if (myExtRegisters[ring2Seq[ring2ptr]-1]) {
                                    // vehicle detected
                                    actuatedTimer2.greenTimer.set_extension(myPhases[ring2Seq[ring2ptr]-1].getExtension()) ;
                                }   // vehicle detected  
                                if (myPhases[ring2Seq[ring2ptr]-1].Veh_Recall) {
                                    // recall phase, check conflicting presence detectors
                                    if (!checkOtherApproaches(ring2Seq[ring2ptr], 2)) {
                                        // no veh presence at all other approaches, 
                                        // MAX does not count down
                                        actuatedTimer2.greenTimer.hold_recall(true) ; 
                                    } else {
                                        actuatedTimer2.greenTimer.hold_recall(false) ;
                                    } 
                                }   // if Veh_recall
                            }   // end if timer 2 gap/max out?    
                            
                            //=====================================================================

                            // ========================== 
                            // added 4/2/08
                            if (actuatedTimer1.greenTimer.stepSim || actuatedTimer2.greenTimer.stepSim) {
                                //try {Thread.sleep(100) ;}
                                //catch (InterruptedException ie) {} ;
                                do {
                                    try {Thread.sleep(100) ;}
                                    catch (InterruptedException ie) {} ;
                                } while (!actuatedTimer1.step_finished || !actuatedTimer2.step_finished) ;
                                //pauseActuatedTime() ;
                                signalPaused = true ;
                                step_finished = true ;
                            } 

                            //======================================
                        }   // end if control_type #################
                        
                 //       tLast = tNow ;
                        //try {Thread.sleep(100) ;}
                        //catch (InterruptedException ie) {} ;
                         
                    } else {    // signal timing control paused
                        tSignalControl.yield();
                        try {Thread.sleep(200) ;}
                        catch (InterruptedException ie) {} ;
                        // Get current time
                        tLast = System.currentTimeMillis();
                    }
                }
             }   // void run
        } ; // runThread 0
        tSignalControl = new Thread(runThread0, "SignalControl") ;
        tSignalControl.start() ;
    }

    // return TRUE if there is vehicle present at conflicting approaches
    private boolean checkOtherApproaches(int phase, int ring_index) {
        boolean status = false ;
        // identify barrier group index
        int Num_barrier = 0 ;
        int[] Group_barrier = new int[NUM_PHASES] ;
        int Num_allow = 0 ;
        int[] allow_phases = new int[NUM_PHASES] ;
        for (int i=0; i<NUM_PHASES; i++) {
            if (CG[i]) {
                Group_barrier[Num_barrier] = i ;
                Num_barrier++ ;
            }   // if
        }   // for
        int start_ptr = 0 ;
        int found_jindex = -1 ;
        int found_kindex = -1 ;
        for (int j=0; j<Num_barrier; j++) {
            if (j==0) {
                start_ptr = 0 ;
            } else {
                start_ptr = Group_barrier[j-1]+1 ;
            }   // if j
            for (int k=start_ptr; k<=Group_barrier[j]; k++) {
                int cur_phase = 0 ;
                switch (ring_index) {
                    case 1:     // ring 1
                        cur_phase = ring1Seq[k] ;
                        break ;
                    case 2:     // ring 2
                        cur_phase = ring2Seq[k] ;
                        break ;
                }
                if (phase == cur_phase) {
                    found_jindex = j ;
                    found_kindex =start_ptr ;
                    //break ;
                    j = Num_barrier ;
                    k = Group_barrier[j]+1 ;
                }   // if phase
            }   // for k
        }   // for j
        
        for (int j=0; j<Num_barrier; j++) {
            if (j==found_jindex) {
                if (ring_index==1) {
                    // allow ring 2
                    for (int k=found_kindex; k<=Group_barrier[j]; k++) {
                        allow_phases[Num_allow] = ring2Seq[k] ;
                        Num_allow++ ;
                    }   // for k
                } else if (ring_index==2) {
                    // allow ring 1
                    for (int k=found_kindex; k<=Group_barrier[j]; k++) {
                        allow_phases[Num_allow] = ring1Seq[k] ;
                        Num_allow++ ;
                    }   // for k
                }   // if ring index
            }   // end if
        }   // for j
        // debug
        /* for (int i=0; i<Num_allow; i++) {
            if (allow_phases[i]>0) {
                System.out.print(","+allow_phases[i]) ;
            }
        } */
        //System.out.println(" allowed --- recall phase="+phase) ;
        for (int i=0; i<NUM_PHASES; i++) {
            boolean phase_allow = false ;
            for (int j=0; j<Num_allow; j++) {
                if (allow_phases[j]==i+1) {
                    phase_allow = true ;
                    break ;
                }
            }   // j
            if (!phase_allow && myPresenceRegisters[i]) {
                // conflicting phase
                status = true ;
                //System.out.println("phase "+i+" presence ON") ;
            }
        }   // for i
        return status ;
    }
    // move ring 1 & 2 phases across the barrier
    private void R12_crossBarrier() {
        // ring 1
        phaseStatus[ring1Seq[ring1ptr]-1] = false ;
        int last_phase = ring1Seq[ring1ptr] ;
        // find next valid phase ;
        do {
            ring1ptr++ ;
            if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
        } while ( ring1Seq[ring1ptr]<=0 ) ; // skip not assigned phases
        if (ring1Seq[ring1ptr]==last_phase) {
            // same as last phase, skip
            // find next valid phase ;
            do {
                ring1ptr++ ;
                if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
            } while ( ring1Seq[ring1ptr]<=0 ) ;
        }
        // ring 2
        phaseStatus[ring2Seq[ring2ptr]-1] = false ;
        last_phase = ring2Seq[ring2ptr] ;
        // find next valid phase ;
        do {
            ring2ptr++ ;
            if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
        } while ( ring2Seq[ring2ptr]<=0 ) ;
        if (ring2Seq[ring2ptr]==last_phase) {
            // same phase, skip
            // find next valid phase ;
            do {
                ring2ptr++ ;
                if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
            } while ( ring2Seq[ring2ptr]<=0 ) ;
        }
        
    }   // ring 1 & 2 across barrier
     
    // public methods here
    public void initPhase(int _phaseID, SignalPhase _phase){
        myPhases[_phaseID] = _phase ;
    }
    
    private void startFixedTime() {
        ring1Seq[0] = 1 ;        // phasing sequence ring 1
        ring1Seq[1] = 2 ;
        ring1Seq[2] = 3 ;
        ring1Seq[3] = 4 ;
        
        ring2Seq[0] = 5 ;        // phasing sequence ring 2
        ring2Seq[1] = 6 ;
        ring2Seq[2] = 7 ;
        ring2Seq[3] = 8 ;
       
        //control_type = 0 ;
        //signalPaused = false ;
        fixedTimer1.setDurations(myPhases[ring1Seq[ring1ptr]-1].getInitial(), 
            myPhases[ring1Seq[ring1ptr]-1].getYellow(),
            myPhases[ring1Seq[ring1ptr]-1].getRed() ) ;
        //System.out.println("timer1="+fixedTimer1.getSetCount()) ;
        fixedTimer1.start() ;
        phaseStatus[ring1Seq[ring1ptr]-1] = true ;
        fixedTimer2.setDurations(myPhases[ring2Seq[ring2ptr]-1].getInitial(),
            myPhases[ring2Seq[ring2ptr]-1].getYellow(),
            myPhases[ring2Seq[ring2ptr]-1].getRed() ) ;
        //System.out.println("timer2="+fixedTimer2.getSetCount()) ;
        fixedTimer2.start() ;
        phaseStatus[ring2Seq[ring2ptr]-1] = true ;
    }
    
    private void startActuatedControl() {
        int skip_phase1 = 0, skip_phase2=0 ;
        do {
            do {
                skip_phase1 = checkR1PhaseSkip() ;
            } while (skip_phase1>0) ;
            do {
                skip_phase2 = checkR2PhaseSkip() ;
            } while (skip_phase2>0) ;
            if (skip_phase1==-1 && skip_phase2==-1 && ring1ptr==ring2ptr) {
                // reaching barrier
                R12_crossBarrier() ;
                skip_phase1 = 1 ;
                skip_phase2 = 1 ;
            }
        } while (skip_phase1 != 0 && skip_phase2 != 0) ;
        
        startActuatedControl1() ;
        startActuatedControl2() ;
    }
    private void startActuatedControl1() {

        //control_type = 1 or 2 ;
        //for (int i=0; i<4; i++) {
        //System.out.println("ring1Seq["+i+"]="+ring1Seq[i]) ;
        //}
        actuatedTimer1.setDurations(myPhases[ring1Seq[ring1ptr]-1].getInitial(),
            myPhases[ring1Seq[ring1ptr]-1].getExtension(),
            myPhases[ring1Seq[ring1ptr]-1].getMax1(),
            myPhases[ring1Seq[ring1ptr]-1].getYellow(),
            myPhases[ring1Seq[ring1ptr]-1].getRed() ) ;
        
        
        actuatedTimer1.start() ;
        phaseStatus[ring1Seq[ring1ptr]-1] = true ;        
    }
    
    private void startActuatedControl2() {
        //control_type = 1 or 2 ;
        //for (int i=0; i<4; i++) {
        //System.out.println("ring2Seq["+i+"]="+ring2Seq[i]) ;
        //}
        actuatedTimer2.setDurations(myPhases[ring2Seq[ring2ptr]-1].getInitial(),
            myPhases[ring2Seq[ring2ptr]-1].getExtension(),
            myPhases[ring2Seq[ring2ptr]-1].getMax1(),
            myPhases[ring2Seq[ring2ptr]-1].getYellow(),
            myPhases[ring2Seq[ring2ptr]-1].getRed() ) ;
        
        
        actuatedTimer2.start() ;
        phaseStatus[ring2Seq[ring2ptr]-1] = true ;
        
    }

    // pause fixed timing
    public void pauseFixedTime() {
        signalPaused = true ;
        fixedTimer1.pause() ;
        fixedTimer2.pause() ;
    }
    // pause actuated timing
    public void pauseActuatedTime() {
        signalPaused = true ;
        actuatedTimer1.pause() ;
        actuatedTimer2.pause() ;
    }
    
    // actuated timing control
    public void doActuatedTime(boolean step_flag) {
        actuatedTimer1.setStepSim(step_flag)  ; // 3/12/07
        actuatedTimer2.setStepSim(step_flag)  ; // 3/12/07
        signalPaused = false ;
        if (ring1ptr<0 | ring2ptr<0) {
            ring1ptr=0 ;
            ring2ptr=0 ; 
            startActuatedControl() ;
        }
        tLast = System.currentTimeMillis() ;
        actuatedTimer1.start() ;
        actuatedTimer2.start() ;
        
    }
    public void continueActuatedTime(boolean step_flag) {
        actuatedTimer1.setStepSim(step_flag)  ; // 3/12/07
        actuatedTimer2.setStepSim(step_flag)  ; // 3/12/07
        signalPaused = false ;
        tLast = System.currentTimeMillis() ;
        actuatedTimer1.resume() ;
        actuatedTimer2.resume() ;
        
    }
    public void continueActuatedTime1(boolean step_flag) {
        actuatedTimer1.setStepSim(step_flag)  ; // 3/12/07
        
        tLast = System.currentTimeMillis() ;
        actuatedTimer1.resume() ;
        
    }
    public void continueActuatedTime2(boolean step_flag) {
        actuatedTimer2.setStepSim(step_flag)  ; // 3/12/07
        
        tLast = System.currentTimeMillis() ;
        actuatedTimer2.resume() ;
        
    }
    // fixed time signal control
    public void doFixedTime(boolean step_flag) {
        fixedTimer1.setStepSim(step_flag)  ; // 3/12/07
        fixedTimer2.setStepSim(step_flag)  ; // 3/12/07
        signalPaused = false ;
        if (ring1ptr<0 | ring2ptr<0) {
            ring1ptr=0 ;
            ring2ptr=0 ;
            startFixedTime() ;
        }
        tLast = System.currentTimeMillis() ;
        //fixedTimer1.start() ;
        //fixedTimer2.start() ; 
    }    
    public void continueFixedTime(boolean step_flag) {
        fixedTimer1.setStepSim(step_flag)  ; // 3/12/07
        fixedTimer2.setStepSim(step_flag)  ; // 3/12/07
        signalPaused = false ;
        tLast = System.currentTimeMillis() ;
        fixedTimer1.resume() ;
        fixedTimer2.resume() ;
    } 
    public void setRing1Seq(int p1, int p2, int p3, int p4) {
        ring1Seq[0] = p1 ;
        ring1Seq[1] = p2 ;
        ring1Seq[2] = p3 ;
        ring1Seq[3] = p4 ;
    }
    public void setRing1Seq_i(int index, int ph) {
        ring1Seq[index] = ph ;
    }  
    /*
    public void setRing1Seq_iThru(int index, int val) {
        ring1Seq[index] = val;
    }  
    public void setRing1Seq_iLeft(int index, int val) {
        ring1Seq[index] = val ;
    }  
    */
    public void setRing2Seq(int p1, int p2, int p3, int p4) {
        ring2Seq[0] = p1 ;
        ring2Seq[1] = p2 ;
        ring2Seq[2] = p3 ;
        ring2Seq[3] = p4 ;
    }
    public void setRing2Seq_i(int index, int ph) {
        ring2Seq[index] = ph ;
    }    
    /*
    public void setRing2Seq_iThru(int index, int val) {
        ring2Seq[index] = val ;
    }  
    public void setRing2Seq_iLeft(int index, int val) {
        ring2Seq[index] = val ;
    }  
    */
    public String toStrSeq1() {
        String str = "" ;
        for (int i=0; i<NUM_PHASES; i++) {
            str += ring1Seq[i]+"," ;
        }
        return str ;
    }
    public String toStrSeq2() {
        String str = "" ;
        for (int i=0; i<NUM_PHASES; i++) {
            str += ring2Seq[i]+"," ;
        }
        return str ;
    } 
    public String toStrCG() {
        String str = "" ;
        for (int i=0; i<NUM_PHASES; i++) {
            if (CG[i]) {
                str += new Integer(i+1).toString()+"," ;
            }
        }
        return str ;
    }
    public boolean inRing1(int id) {
        boolean found = false ;
        for (int i=0; i<NUM_PHASES; i++) {
            if (ring1Seq[i] == id) {
                found = true ;
                break ;
            }
        }
        return found ; 
    }
    public boolean inRing2(int id) {
        boolean found = false ;
        for (int i=0; i<NUM_PHASES; i++) {
            if (ring2Seq[i] == id) {
                found = true ;
                break ;
            }
        }
        return found ;
    }
    public int getR1PhaseID() {
        return ring1Seq[ring1ptr]-1 ;
    }
    public int getR2PhaseID() {
        return ring2Seq[ring2ptr]-1 ;
    }
    public int checkR1PhaseSkip() {
        int skip_phase = 0 ;    // 0-no skip, 1-skip phases, -1 barrier
        // check presence detector for phase skip
        // ring 1
        if (!myPresenceRegisters[ring1Seq[ring1ptr]-1] &&
            !myPhases[ring1Seq[ring1ptr]-1].Veh_Recall) {
            // no vehicle presence & not recall phase
            if (!CG[ring1ptr]) {
                // not reaching barrier
                // find next valid phase 
                do {
                    ring1ptr++ ;
                    if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                } while ( ring1Seq[ring1ptr]<=0 ) ;   
                skip_phase = 1 ;
            } else {
                //reaching barrier
                if (!myPresenceRegisters[ring2Seq[ring2ptr]-1] &&
                    !myPhases[ring2Seq[ring2ptr]-1].Veh_Recall  ) {
                    //do {
                    //    ring1ptr++ ;
                    //    if (ring1ptr>=NUM_PHASES) { ring1ptr=0 ; }
                    //} while ( ring1Seq[ring1ptr]<=0 ) ;   
                    skip_phase = -1 ;
                }   // if ring 2 detectors
            }   // end if barrier
        }   // end if
        //System.out.println("Ring 1 phase="+ring1Seq[ring1ptr]) ;
        return skip_phase ;
    }   // checkPhaseSkip
    
    public int checkR2PhaseSkip() {
        int skip_phase = 0 ;    // 0-no skip, 1-skip phases, -1 barrier
        // check presence detector for phase skip
        // ring 2
        if (!myPresenceRegisters[ring2Seq[ring2ptr]-1] &&
            !myPhases[ring2Seq[ring2ptr]-1].Veh_Recall) {
            // no vehicle presence & not recall phase
            if (!CG[ring2ptr]) {
                // not reaching barrier
                // find next valid phase 
                do {
                    ring2ptr++ ;
                    if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                } while ( ring2Seq[ring2ptr]<=0 ) ;   
                skip_phase = 1 ;
            } else {
                //reaching barrier
                if (!myPresenceRegisters[ring1Seq[ring1ptr]-1] &&
                    !myPhases[ring1Seq[ring1ptr]-1].Veh_Recall ) {
                    //do {
                    //    ring2ptr++ ;
                    //    if (ring2ptr>=NUM_PHASES) { ring2ptr=0 ; }
                    //} while ( ring2Seq[ring2ptr]<=0 ) ;   
                    skip_phase = -1 ;
                }   // if ring 1 detectors
            }   // end if barrier
        }   // end if
        //System.out.println("Ring 2 phase="+ring2Seq[ring2ptr]) ;
        return skip_phase ;
    }   // checkPhaseSkip
    
    public void resetRingPtr() {
        ring1ptr = -1 ;
        ring2ptr = -1 ;
    }
    
    public void clearPresenceRegistersOnGreen() {
        for (int i=0; i<NUM_PHASES; i++) {
            if (phaseLitColor[i]==Color.green) {   // green
                myPresenceRegisters[i] = false ;
            }
        }   // for
        
    }
    
    public void resetEXTRegisters() {
        // reset detector status
        for (int i=0; i<NUM_PHASES; i++) {
            myExtRegisters[i] = false ;
        }   // for
    }   // resetDetectors
    
    public void resetAllRegisters() {
        // reset detector status
        for (int i=0; i<NUM_PHASES; i++) {
            myExtRegisters[i] = false ;
            myPresenceRegisters[i] = false ;
            phaseStatus[i] = false ;
        }   // for
    }   // resetDetectors
    
    public int stepBack() {
        int state = -1 ;
        if (control_type==2) { 
            if (!actuatedTimer1.isGreenTimeUp() && !actuatedTimer2.isGreenTimeUp()) {
                actuatedTimer1.greenTimer.stepBack() ;
                actuatedTimer2.greenTimer.stepBack() ;
                state = 1 ;
            }
        }
        return state ;
    }
    
    public String getRegisteredPhases() {
        String str ="" ;
        for (int i=0; i<NUM_PHASES; i++) {
            //if ((phaseLitColor[i]==Color.red || phaseLitColor[i]==Color.yellow)
            if ((phaseLitColor[i] != Color.green)
                && myPresenceRegisters[i] ) {
                // not green or inactive
                str += ", "+(i+1) ;
            }   // if 
            //System.out.println("i="+i+", presence="+myPresenceRegisters[i]) ;
            
        }   // for
        if (str.length()>0) {
            return " Conflicting Calls"+str ;
        } else {
            return str ;
        }
    }
    public void statusBar_Message(String str) {
        sb.setStatusBarText(4, str) ;   //displai error status field
    }
    
    // added 8/10/07 
    public void resetRing1Extension() {
        actuatedTimer1.greenTimer.set_extension(myPhases[ring1Seq[ring1ptr]-1].getExtension()) ;
        //if (actuatedTimer1.greenTimer.get_extension()<myPhases[ring1Seq[ring1ptr]-1].getExtension()) {
            actuatedTimer1.greenTimer.extension_reset_flag = true ; // 4/1/08 added
        //}
    }
    public void resetRing2Extension() {
        actuatedTimer2.greenTimer.set_extension(myPhases[ring2Seq[ring2ptr]-1].getExtension()) ;
        //if (actuatedTimer2.greenTimer.get_extension()<myPhases[ring2Seq[ring2ptr]-1].getExtension()) {
            actuatedTimer2.greenTimer.extension_reset_flag = true ; // 4/1/08 added
        //}
    }
    
}   // end of class SignalTiming
