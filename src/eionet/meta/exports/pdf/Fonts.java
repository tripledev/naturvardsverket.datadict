
package eionet.meta.exports.pdf;

import java.util.Hashtable;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.awt.Color;

public class Fonts {
    
    public static final String DOC_HEADER  = "doc-header";
    public static final String DOC_FOOTER  = "doc-footer";
    
    public static final String DOC_HEADER_BLACK  = "doc-header";
    
    public static final String TBL_HEADER  = "tbl-header";
    public static final String TBL_CAPTION = "tbl-caption";
    
    public static final String ATTR_TITLE  = "attr-title";
    public static final String CELL_VALUE  = "cell-value";
    
    public static final String WARNING     = "warning";
    
    public static final String HEADING_0   = "heading-0";
    public static final String HEADING_1   = "heading-1";
    public static final String HEADING_2   = "heading-2";
    public static final String HEADING_3   = "heading-3";
    
    public static final String HEADING_1_ITALIC   = "heading-1-italic";
    public static final String HEADING_2_ITALIC   = "heading-2-italic";
    public static final String HEADING_3_ITALIC   = "heading-3-italic";
    
    public static final String ANCHOR = "anchor";
    
    private static Hashtable fonts = null;
    
    private static void init(){
        
        fonts = new Hashtable();
        
        // set doc header/footer font
        Font font = FontFactory.getFont(FontFactory.COURIER, 10);
        font.setColor(new Color(0,100,200));
        fonts.put(DOC_HEADER, font);
        fonts.put(DOC_FOOTER, font);
        
        // set doc header black font
        font = FontFactory.getFont(FontFactory.COURIER, 10);
        font.setColor(new Color(0,0,0));
        fonts.put(DOC_HEADER_BLACK, font);
        
        // set table header font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        font.setColor(new Color(255,255,255));
        fonts.put(TBL_HEADER, font);
        
        // set table caption font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        fonts.put(TBL_CAPTION, font);
        
        // set attribute title font
        fonts.put(ATTR_TITLE, font);
        
        // set simple value cell font
        font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        fonts.put(CELL_VALUE, font);
        
        // set warning font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        font.setColor(new Color(255,0,0));
        fonts.put(WARNING, font);
        
        // set Heading 0 font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        font.setStyle("underline");
        fonts.put(HEADING_0, font);
        
        // set Heading 1 font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        fonts.put(HEADING_1, font);
        
        // set Heading 1 Italic font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 20);
        fonts.put(HEADING_1_ITALIC, font);
        
        // set Heading 2 font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        fonts.put(HEADING_2, font);
        
        // set Heading 2 Italic font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 16);
        fonts.put(HEADING_2_ITALIC, font);
        
        // set Heading 3 font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        fonts.put(HEADING_3, font);
        
        // set Heading 3 Italic font
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 14);
        fonts.put(HEADING_3_ITALIC, font);
        
        // set Anchor font
        font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        font.setColor(new Color(0,100,200));
        font.setStyle("underline");
        fonts.put(ANCHOR, font);
    }
    
    public static Font get(String style){
        if (fonts == null)
            init();
        return (Font)fonts.get(style);
    }
}