/*
 * Created on 14.02.2007
 */
package eionet.meta.exports.codelist;

import eionet.meta.DDRuntimeException;
import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.exports.codelist.ExportElement.Element;
import eionet.meta.exports.codelist.ExportStatics.ObjectType;
import java.util.ArrayList;
import java.util.List;
import eionet.meta.dao.domain.DataElement.DataElementValueType;
import eionet.util.sql.ConnectionUtil;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jaanus
 */
public class Codelist {

    public static enum ExportType{
        UNKNOWN,
        CSV,
        XML;
    }
    
    private static final Logger LOGGER = Logger.getLogger(Codelist.class);
    
    private final ExportType exportType;
    
    @Autowired
    private final CodeValueHandlerProvider codeValueHandlerProvider;

    public Codelist(ExportType exportType, CodeValueHandlerProvider codeValueHandlerProvider) {
        this.exportType = exportType;
        this.codeValueHandlerProvider = codeValueHandlerProvider;
    }
    
    /**
     * 
     * @param objID
     * @param objType
     * @return
     * @throws Exception 
     */
    String write(String objID, String objType){
        
        List<DataElement> elements = fetchElement(objID, objType);

        return write(elements, objType);
    }
    
    /**
     * 
     * @param inputElements
     * @param objType
     * @return
     * @throws Exception 
     */
    String write(List<DataElement> inputElements, String objType){
        if (inputElements == null || inputElements.isEmpty()) {
            return "";
        }
        ExportElement export = prepareExportElement(inputElements, objType);
        
        switch ( exportType ){
            case CSV:{
                return export.toCSV() ;
            }
            case XML:{
                return export.toXML();
            }
            default:
                return "";
        }
    }
    
    /**
     * Prepare the intermediate object ExportElement based on the input in order to support 
     * XML export via Jackson XML annotations and CSV export.
     * 
     * @param inputElements
     * @param objType
     * @return 
     */
    private ExportElement prepareExportElement( List<DataElement> inputElements, String objType ){
        boolean isElement = objType.equalsIgnoreCase(ObjectType.ELM.name());
        
        boolean datasetAware = false;
        
        //Exportable Elements
        List<Element> elements = new ArrayList<Element>();
        
        for (DataElement inputElement : inputElements) {
            DataElement legacyElement = (DataElement) inputElement;
            
            //Upgrade element to most update implementation of DataElement
            eionet.meta.dao.domain.DataElement dataElement = this.upgradeElement(legacyElement);
            DataElementValueType type = DataElementValueType.parse( dataElement.getType() );
            
            //Prepare Export Element
            Element element = new Element();
            //IDENTIFIER
            String elementIdentifier = legacyElement.getIdentifier();
            if (elementIdentifier == null || elementIdentifier.trim().length() == 0) {
                throw new DDRuntimeException("Failed to get the element's identifier");
            }
            element.setIdentifier(elementIdentifier);
            //SET TABLE, DATASET
            // When not element or when not common element
            datasetAware = ( !isElement || ( isElement && !legacyElement.isCommon() ) );
            if ( datasetAware ){
                //TABLE IDENTIFIER
                String tableIdentifier = inputElement.getTblIdentifier();
                if (tableIdentifier == null || tableIdentifier.trim().length() == 0) {
                    LOGGER.info("Failed to get the table's identifier for element identified by: "+elementIdentifier);
                    //Go to next Element
                    continue;
                }
                element.setTableIdentifier(tableIdentifier);
                //DATASET IDENTIFIER
                String datasetIdentifier = inputElement.getDstIdentifier();
                if (datasetIdentifier == null || datasetIdentifier.trim().length() == 0) {
                    LOGGER.info("Failed to get the dataset's identifier for element identified by: "+elementIdentifier);
                    //Go to next Element
                    continue;
                }
                element.setDatasetIdentifier(datasetIdentifier);
            }
            //SET FIXED
            // When type is Fixed
            if (type.equals(DataElementValueType.FIXED)) {
                element.setFixed(true);
            }
            
            //Get Related List of Values (Fixed or Vocabulary Concepts)
            //and relationship information
            CodeValueHandler handler = codeValueHandlerProvider.get(type);
            handler.setDataElement(dataElement);
            
            element.setValues( handler.getCodeItemList() );
            element.setRelationshipNames( handler.getRelationshipNames() );
            
            elements.add(element);
        }
        
        ExportElement export = new ExportElement();
        export.setDatasetAware(datasetAware);
        export.setElements(elements);
        
        return export;
    }
    
    /**
     * Employs DDSearchEngine to connect with the Database and fetch a suitable Element according to the required objType
     * 
     * @param objID
     * @param objType
     * @return List<DataElement>
     */
    private List<DataElement> fetchElement( String objID, String objType ){
        List<DataElement> elements = new ArrayList<DataElement>();
        
        Connection dbConnection = null;
        try {
            dbConnection = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine( dbConnection );

            if (objType.equalsIgnoreCase(ObjectType.ELM.name())) {
                DataElement elm = searchEngine.getDataElement(objID);
                
                if (elm != null) {
                    elements.add(elm);
                }
            } else if (objType.equalsIgnoreCase(ObjectType.TBL.name())) {
                elements.addAll( searchEngine.getDataElements(null, null, null, null, objID) );
            } else if (objType.equalsIgnoreCase(ObjectType.DST.name())) {
                elements.addAll( searchEngine.getAllDatasetElements(objID) );
            } 
        } catch (SQLException sqle ){
            LOGGER.error("Failed to fetch Element identified by ID "+objID+" and type "+objType+" from DDSearchEngine", sqle);
        } finally{
            if ( dbConnection != null ){ try { dbConnection.close(); } catch( SQLException sqle ){ LOGGER.error("Failed to close DB Connection", sqle ); } }
        }
        return elements;
    }
    /**
     * Upgrade a legacy DataElement to its most up-to-date implementation
     * by copying attributes.
     * Here we start with what we currently need and do not provide an exhaustive copy.
     * 
     * @param legacyElement of type eionet.meta.DataElement
     * @return eionet.meta.dao.domain.DataElement
     */
    protected eionet.meta.dao.domain.DataElement upgradeElement( eionet.meta.DataElement legacyElement ){
        eionet.meta.dao.domain.DataElement element = new eionet.meta.dao.domain.DataElement();
        if ( legacyElement.getID() != null )
            element.setId( Integer.parseInt( legacyElement.getID() ) );
        if ( legacyElement.getVocabularyId() != null )
            element.setVocabularyId( Integer.parseInt( legacyElement.getVocabularyId() ) );
        element.setType( legacyElement.getType() );
        if ( legacyElement.getNamespace() != null && legacyElement.getNamespace().getID() != null ){
            element.setParentNamespace( Integer.parseInt( legacyElement.getNamespace().getID() ) );
        }
        return element;
    }
}
