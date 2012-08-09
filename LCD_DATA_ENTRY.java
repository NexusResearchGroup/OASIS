/*
 * LCD_DATA_ENTRY.java
 * hold the data field format info for keypad data entry
 *
 * Created on January 8, 2007, 12:03 PM
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

public class LCD_DATA_ENTRY {
    public char[][] PAGE_CHARS = new char[16][40] ; // 16 lines x 40 char LCD array
    public int page_ID = 0 ;
    public int lcd_page_index = -1 ;
    public int startRow = -1 ;
    public int rowLength = 0 ;
    public int row_index = -1 ; // cursor location y
    public int col_index = -1 ; // cursor location x
    public int data_mode = -1 ; // 1-numerical data(*), 2-Text(T), 3-Toggle(X), 4-^ special char
                                // 5-& 3-digit number, 6-% float ##.# number ex. "45.6"
    public int num_key_pressed = 0 ;
    
    /** Creates a new instance of LCD_DATA_ENTRY */
    public LCD_DATA_ENTRY() {
        for (int i=0; i<16; i++) {
            for (int j=0; j<40; j++) {
                PAGE_CHARS[i][j] = ' ' ;    // init to ' ' space
            }   // j
        }   // i
    }
    
    public void cursorInit() {
        // exe every 500 ms
        if (row_index<0 || col_index<0) {
            // move to beginneing or data entry field
            row_index = startRow ;
            col_index = getStartCol(0) ; 
        }   // if        
    }   // cursorInit subroutine
    
    public void moveCursorLeft() {
         
        if (row_index<0 || col_index<0) {
            // move to beginneing or data entry field
            row_index = startRow ;
            col_index = getStartCol(0) ; 
        } else {
            // find next left data field
            for (int j=col_index-1; j>=0; j--) {
                if (PAGE_CHARS[row_index][j] != ' ') {
                    col_index = j ;
                    update_data_mode(PAGE_CHARS[row_index][col_index]) ;
                    break ;
                }
            }   // for j
        }   // if 
        num_key_pressed = 0 ;
        //System.out.println("cur row, col="+row_index+","+col_index );
    }   // move cursor left
    
    public void moveCursorRight() {
         
        if (row_index<0 || col_index<0) {
            // move to beginneing or data entry field
            row_index = startRow ;
            col_index = getStartCol(0) ; 
        } else {
            // find next right data field
            for (int j=col_index+1; j<40; j++) {
                if (PAGE_CHARS[row_index][j] != ' ') {
                    col_index = j ;
                    update_data_mode(PAGE_CHARS[row_index][col_index]) ;
                    break ;
                }
            }   // for j
        }   // if 
        num_key_pressed = 0 ;
        //System.out.println("cur row, col="+row_index+","+col_index );
    }    // move cursor right
    
    public void moveCursorUp() {
         
        if (row_index<0 || col_index<0) {
            // move to beginneing or data entry field
            row_index = startRow ;
            col_index = getStartCol(0) ; 
        } else {
            // find next up data field
            for (int i=row_index-1; i>=0; i--) {
                if (isFormatExists(i)) {
                    row_index = i ;
                    findNextCol() ;
                    break ;
                }   // format exists
            }   // for i
        }   // if 
        num_key_pressed = 0 ;
    }    // moveCursorUp
    
    public void moveCursorDown() {
         
        if (row_index<0 || col_index<0) {
            // move to beginneing or data entry field
            row_index = startRow ;
            col_index = getStartCol(0) ; 
        } else {
            // find next up data field
            for (int i=row_index+1; i<16; i++) {
                if (isFormatExists(i)) {
                    row_index = i ;
                    findNextCol() ;
                    break ;
                }   // format exists
            }   // for i
        }   // if 
        num_key_pressed = 0 ;
    }    // moveCursorDown
    
    private boolean isFormatExists(int row) {
        boolean state = false ;
        for (int j=0; j<40; j++) {
            if (PAGE_CHARS[row][j] != ' ') {
                state = true ;
                break ;
            }   // if
        }   // for
        return state ;
    }   // isFormatExists sub 
    
    private int getStartCol(int col_index) {
        int idx = -1 ;
        for (int i=col_index; i<40; i++) {
            if (PAGE_CHARS[startRow][i] != ' ') {
                idx = i ;
                update_data_mode(PAGE_CHARS[startRow][i]) ;
                break ;
            }   // if  char not equal to blanl ' '
        }   // for
        System.out.println("start row, col="+row_index+","+idx );
        return idx ;
    }   // findStartCol
    
    private void update_data_mode(char my_char) {
        switch (my_char) {
            case '*':
                data_mode = 1 ; // 2-digit numerical data
                break ;
            case 'T':
                data_mode = 2 ; // TEXT
                break ;
            case 'X':
                data_mode = 3 ; // toggle
                break ;
            case '^':
                data_mode = 4 ; // special ^
                break ;
            case '&':
                data_mode = 5 ; // 3-digit number
                break ;
            case '%':
                data_mode = 6 ; // 3-digit float eg. 45.6
                break ;
            default:
                System.out.println("LCD_DATA_ENTRY Format error: "+my_char) ;
        }
        
    }   // update data mode
    
    private void findNextCol() {
        boolean isEmpty = true ;
        for (int j=col_index; j>=0; j--) {
            if (PAGE_CHARS[row_index][j] != ' ') {
                col_index = j ;
                update_data_mode(PAGE_CHARS[row_index][col_index]) ;
                isEmpty = false ;
                break ;
            }
        }   // for j
        if (isEmpty) {
            // try search in right direction
            for (int j=col_index+1; j<40; j++) {
                if (PAGE_CHARS[row_index][j] != ' ') {
                    col_index = j ;
                    update_data_mode(PAGE_CHARS[row_index][col_index]) ;
                    break ;
                }
            }   // for j
        }   // if empty?
        //System.out.println("cur row, col="+row_index+","+col_index );
    }   // findNextCol
}
