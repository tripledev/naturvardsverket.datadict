
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.meta.savers.Parameters;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import com.lowagie.text.*;

public class ElmPdfAll {
    
    private DDSearchEngine searchEngine = null;
    //private Section parentSection = null;
    //private Section section = null;
    
    //private Vector docElements = new Vector();
    
	private String vsPath = null;
	
	private Parameters params = null;
	
	private TblPdfAll owner = null;
	
	// methods
	///////////	
    
    public ElmPdfAll(DDSearchEngine searchEngine, TblPdfAll owner)
        throws Exception {
            
        //if (parentSection==null) throw new Exception("parentSection cannot be null!");
        if (searchEngine==null)
            throw new Exception("searchEngine cannot be null!");
            
        this.searchEngine = searchEngine;
        //this.parentSection = parentSection;
        this.owner = owner;
    }
    
	public void write(String elemID) throws Exception {
		write(elemID, null);
	}
    
    protected void write(String elemID, String tblID) throws Exception {
        
        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID, tblID, false);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E"));
        
        write(elem);
    }
    
    /**
    * Write a factsheet for a data element given by object.
    */
    private void write(DataElement elem) throws Exception {
        
        if (elem==null)
            throw new Exception("Element object was null!");
        
		String nr = "";
		Sectioning sect = null;
		if (owner != null)
			sect = owner.getSectioning();
		if (sect != null)
			nr = sect.level(elem.getShortName() + " column", 3);
		nr = nr==null ? "" : nr + " ";
				
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(nr +
        			elem.getShortName(), Fonts.get(Fonts.HEADING_3_ITALIC)));
        prg.add(new Chunk(" column", Fonts.get(Fonts.HEADING_3)));
        
        addElement(prg);
        
        // see if this guideline is part of a table, get the
        // latter's information.
        String tableID = elem.getTableID();
        if (Util.voidStr(tableID)){
                
            String msg =
            "\nWarning! This guideline does not fully reflect the " +
            "table and dataset where this data element belongs to!\n\n";
            
            addElement(new Phrase(msg, Fonts.get(Fonts.WARNING)));
        }
        
        // write simple attributes
        addElement(new Paragraph("\n"));
        Hashtable hash = null;
        Vector attrs = elem.getAttributes();
        
        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", elem.getShortName());
        attrs.add(0, hash);

		// version
		String ver = elem.getVersion();
		if (!Util.voidStr(ver)){
			hash = new Hashtable();
			hash.put("name", "Version");
			hash.put("value", ver);
			attrs.add(0, hash);
		}

		// reg stat
		String stat = elem.getStatus();
		if (!Util.voidStr(stat)){
			hash = new Hashtable();
			hash.put("name", "Registration status");
			hash.put("value", stat);
			attrs.add(0, hash);
		}
        
        addElement(PdfUtil.simpleAttributesTable(attrs));
        addElement(new Phrase("\n"));
        
		// write foreign key reltaions if any exist
		String dstID = params==null ? null : params.getParameter("dstID");
		Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
		if (fks!=null && fks.size()>0){
			addElement(PdfUtil.foreignKeys(fks));
			addElement(new Phrase("\n"));
		}
			 
        // write complex attributes, one table for each
        Vector v = elem.getComplexAttributes();
        if (v!=null && v.size()>0){
            
            DElemAttribute attr = null;
            for (int i=0; i<v.size(); i++){
                attr = (DElemAttribute)v.get(i);
                attr.setFields(searchEngine.getAttrFields(attr.getID()));
            }
            
            for (int i=0; i<v.size(); i++){
                
                addElement(PdfUtil.complexAttributeTable((DElemAttribute)v.get(i)));
                addElement(new Phrase("\n"));
            }
        }
        
        // write allowable values (for a factsheet levelling not needed I guess)
        v = searchEngine.getFixedValues(elem.getID(), "elem");
        if (v!=null && v.size()>0){
            addElement(new Phrase("! This data element may only have the " +
                                "following fixed values:\n", Fonts.get(Fonts.HEADING_0)));
            addElement(PdfUtil.fixedValuesTable(v, false));
        }

		// write image attributes
		Vector images = PdfUtil.imgAttributes(attrs, vsPath);
		if (images!=null){
			addElement(
				new Paragraph("Illustrations:", Fonts.get(Fonts.HEADING_0)));
			for (int i=0; i<images.size(); i++)
				addElement((Element)images.get(i));
		}
    }
    
	private void addElement(Element elm){
    	
		if (owner!=null)
			owner.addElement(elm);
        
		//if (elm != null) section.add(elm);        
		//return docElements.size();
	}

	public void setVsPath(String vsPath){
		this.vsPath = vsPath;
	}

	public void setParameters(Parameters params){
		this.params = params;
	}
	
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            //DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\elm_test_guideline.pdf";
            
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            Section chapter = (Section)new Chapter("test", 1);
            ElmPdfGuideline guideline = new ElmPdfGuideline(searchEngine, null);//chapter);
            guideline.write("4518");
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}