/*
 * SignalPhase.java
 *
 * Created on June 21, 2006, 10:04 AM
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

public class SignalPhase {
    private float Walk = 0f ;       // default walk time
    private float Ped_Clear = 0f ; // pedestrian clear time
    private float Initial = 5f ;   // initial green time
    private float MaxInit = 30f ;   // Max imum initial green time
    private float Extension = 5f ;  // green extension
    private float Max1 = 35f ;      // Max green time 1
    private float Max2 = 40f;       // Max green time 2
    private float Yellow = 3f ;     // default amber/yellow time
    private float Red = 1f ;        // default red time
    private float minGap = 0f ;     // minimum gap
    
    private boolean Permit;
    public boolean Veh_Recall ;    // if vehicle recall selected
    public boolean Max_Recall ;    // if recall to max
    private boolean Ped_Recall ;
    private boolean Lag_Phase ;
    
    private boolean activated = false ; // phase enable flag
    public Point sigbar_UL = new Point(-1,-1) ;
    public Point sigbar_LR = new Point(-1,-1);           // signal bar upper left & lower left corners
    
    /** Creates a new instance of SignalPhase */
    public SignalPhase() {
    }
    public SignalPhase( float _walk,
                        float _ped_claer,
                        float _initial,
                        float _extension,
                        float _max1,
                        float _max2,
                        float _yellow,
                        float _red) {
        Walk = _walk ;
        Ped_Clear = _ped_claer ;
        Initial =  _initial ;
        Extension = _extension ;
        Max1 = _max1 ;
        Max2 = _max2 ;
        Yellow = _yellow ;
        Red = _red ;
    }
    
    // public methods here
    public float getWalk() {
        return Walk ;
    }
    public float getPedClear() {
        return Ped_Clear ;
    }
    public float getInitial() {
        return Initial ;
    }
    public float getMaxInit() {
        return MaxInit ;
    }
    public float getExtension() {
        return Extension ;
    }
    public float getYellow() {
        return Yellow ;
    }
    public float getRed() {
        return Red ;
    }
    public float getMax1() {
        return Max1 ;
    }
    public float getMax2() {
        return Max2 ;
    }
    public float getMinGap() {
        return minGap ;
    }
    
    // =====================
    public void setWalk(float _val){
        Walk = _val ;
    }
    public void setPedClear(float _val){
        Ped_Clear = _val ;
    }    
    public void setInitial(float _val){
        Initial = _val ;
    }   
    public void setMaxInit(float _val){
        MaxInit = _val ;
    }   
    public void setExtension(float _val){
        Extension = _val ;
    }   
    public void setMax1(float _val){
        Max1 = _val ;
    }   
    public void setMax2(float _val){
        Max2 = _val ;
    }   
    public void setYellow(float _val){
        Yellow = _val ;
    }   
    public void setRed(float _val){
        Red = _val ;
    }   
    public void setMinGap(float _val) {
        minGap = _val ;
    }
    
    // ====================
    public void isEnable(boolean state) {
        activated = state ;
    }
    public boolean isEnable() {
        return activated ;
    }
}
