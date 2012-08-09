/*
 * mPointF.java
 * A customized floating point 2D point class.
 *
 * Created on March 17, 2006, 12:19 PM
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

public class mPointF {
    public float X ;
    public float Y ;
    /** Creates a new instance of mPoint */
    public mPointF() {
    }
    public mPointF(float _x, float _y) {
        X =_x;
        Y =_y;
    }
    public mPointF(int _x, int _y) {
        X =_x;
        Y =_y;
    }
    // public methods here
    public float getX() {
        return X ;
    }
    public float getY() {
        return Y ;
    }
    // compute unit vector
    public mPointF unitVector() {
        double len = Math.sqrt(X*X+Y*Y) ;
        return new mPointF(new Double(X/len).floatValue(), 
            new Double(Y/len).floatValue()) ;
    }
    //get vector length
    public float getLength() {
        double L1 = Math.sqrt(X*X+Y*Y) ;
        return new Double(L1).floatValue() ;
    }
    
    // vector dot product
    public float dot (mPointF _vec) {
        float L1 = getLength() ;
        float L2 = _vec.getLength() ;
        // return cosine value
        return (X*_vec.X+Y*_vec.Y)/L1/L2 ;
    }
    public float dotVal (mPointF _vec) {
        // return cosine value
        return (X*_vec.X+Y*_vec.Y) ;
    }
    
    public String toStr() {
        return "("+CStr(CInt(X*100f)/100f)+","+CStr(CInt(Y*100f)/100f)+")" ; 
    }
    
    private int CInt(float val){
        return new Float(val).intValue();
    }

    private String CStr(float val){
        return new Float(val).toString();
    }   
}
