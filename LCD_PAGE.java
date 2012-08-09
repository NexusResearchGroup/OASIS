/*
 * LCD_PAGE.java
 *
 * Created on December 18, 2006, 3:06 PM
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

public class LCD_PAGE {
    public char[][] PAGE_CHARS = new char[16][40] ; // 16 lines x 40 char LCD array
    public int page_ID = 0 ;
    public int previousPage = -1 ;
    public int nextPage = -1 ;
    public int leftPage = -1 ;
    public int rightPage = -1 ;
    // header string format: #id, previousPage, nextPage, leftPage, rightPage in LCD_SCRNS.txt
    public int dataEntryIndex = -1 ; // -1 no data entry, >0 has data entry
    //public int dataLen = 0 ;
    //public int[] dataField = null ;
    
    /** Creates a new instance of LCD_PAGE */
    public LCD_PAGE() {
        for (int i=0; i<16; i++) {
            for (int j=0; j<40; j++) {
                PAGE_CHARS[i][j] = ' ' ;    // init to ' ' space
            }   // j
        }   // i
    }   // class 
    
    public String parseData(int startRow, int startCol, int fieldLength) {
        String str = "" ;
        if (startRow<16) {
            for (int i=0; i<fieldLength; i++) {
                if (startCol+i<40) {
                    str += PAGE_CHARS[startRow][startCol+i] ;
                } else {
                    break ;
                }
            }   // for
        }   // if
        //System.out.println("dataField="+str) ;
        return str ;
    }   // parseData
    public char parseChar(int startRow, int startCol) {
        char _data = ' ' ;
        if (startRow<16) {
            if (startCol<40) {
                _data = PAGE_CHARS[startRow][startCol] ;
            }
        }   // if
        //System.out.println("dataField="+_data) ;
        return _data ;
    }   // parseChar
    
}   // LCD_PAGE class
