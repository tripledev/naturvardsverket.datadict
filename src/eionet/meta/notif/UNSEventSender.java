/*
 * Created on 26.04.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.notif;

import eionet.meta.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletException;
import org.apache.xmlrpc.XmlRpcClient;
import eionet.util.*;

/**
 * @author jaanus
 */
public class UNSEventSender {
	
	/** */
	public static final String PROP_UNS_EVENTS_NAMESPACE = "uns.events-namespace";
	public static final String PROP_UNS_USER_PREDICATE = "uns.user.predicate";
	public static final String PROP_UNS_DEFINITION_URL_PREDICATE =
		"uns.definition-url.predicate";
	
	/**
	 * 
	 *
	 */
	public UNSEventSender(){
	}

	/**
	 * 
	 * @param elm
	 * @param eventType
	 * @param user
	 */
	public static void definitionChanged(DataElement elm, String eventType, String user){
		
		if (elm==null || eventType==null)
			return;
		
		String elmIdfier = elm.getIdentifier();
		String elmURL = elm.getReferenceURL();
		if (elmIdfier==null && elmURL==null)
			return;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (elmIdfier!=null){
			objects = new Vector();
			objects.add(elmIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_COMMONELEM_PREDICATE), objects);
		}

		if (elmURL!=null){
			objects = new Vector();
			objects.add(elmURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}

		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}

		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);

		sendEvent(predicateObjects, user);
	}

	/**
	 * 
	 * @param tbl
	 * @param eventType
	 * @param user
	 */
	public static void definitionChanged(DsTable tbl, String eventType, String user){
		
		if (tbl==null || eventType==null)
			return;
		
		String tblIdfier = tbl.getIdentifier();
		String tblURL = tbl.getReferenceURL();
		if (tblIdfier==null && tblURL==null)
			return;
		
		// the identifier sent to UNS must be like dataset_identifer/table_identifer
		String dstIdfier = tbl.getDstIdentifier();
		if (dstIdfier!=null)
			tblIdfier = dstIdfier + "/" + tblIdfier;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (tblIdfier!=null){
			objects = new Vector();
			objects.add(tblIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_TABLE_PREDICATE), objects);
		}

		if (tblURL!=null){
			objects = new Vector();
			objects.add(tblURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}

		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}

		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);

		sendEvent(predicateObjects, user);
	}

	/**
	 * 
	 * @param dst
	 * @param eventType
	 * @param user
	 */
	public static void definitionChanged(Dataset dst, String eventType, String user){
		
		if (dst==null || eventType==null)
			return;
		
		String dstIdfier = dst.getIdentifier();
		String dstURL = dst.getReferenceURL();
		if (dstIdfier==null && dstURL==null)
			return;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (dstIdfier!=null){
			objects = new Vector();
			objects.add(dstIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_DATASET_PREDICATE), objects);
		}

		if (dstURL!=null){
			objects = new Vector();
			objects.add(dstURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}
		
		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}
		
		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);
		
		sendEvent(predicateObjects, user);
	}

	/*
	 * 
	 */
	public static void definitionChanged(String defType, String defIdfier){
		
		if (defType==null || defIdfier==null)
			return;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = new Vector();
		objects.add(defIdfier);
		predicateObjects.put(defType, objects);
		
		sendEvent(predicateObjects);
	}

	/*
	 * 
	 */
	public static void sendEvent(Hashtable predicateObjects){
		sendEvent(predicateObjects, null);
	}

	/*
	 * 
	 */
	public static void sendEvent(Hashtable predicateObjects, String eventIDTrailer){
		
		try{
			if (predicateObjects==null || predicateObjects.size()==0)
				return;
			
			Vector rdfTriples = new Vector();
			RDFTriple rdfTriple = new RDFTriple();
			String eventID = String.valueOf(System.currentTimeMillis());
			if (eventIDTrailer!=null)
				eventID = eventID + eventIDTrailer;
			
			String digest = Util.digestHexDec(eventID, "MD5");
			if (digest!=null && digest.length()>0)
				eventID = digest;
				
			eventID = Props.getProperty(PROP_UNS_EVENTS_NAMESPACE) + eventID;
			
			rdfTriple.setSubject(eventID);
			rdfTriple.setPredicate(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));
			rdfTriple.setObject("Data Dictionary event");
			rdfTriples.add(rdfTriple.toVector());
//	
//			rdfTriple = new RDFTriple();
//			rdfTriple.setSubject(eventID);
//			rdfTriple.setPredicate(Props.getProperty(PropsIF.PREDICATE_RDF_LABEL));
//			rdfTriple.setObject("Definition changed");
//			rdfTriples.add(rdfTriple.toVector());
//			
//			rdfTriple = new RDFTriple();
//			rdfTriple.setSubject(eventID);
//			rdfTriple.setPredicate(Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE));
//			rdfTriple.setObject("Definition changed");
//			rdfTriples.add(rdfTriple.toVector());
			
			Enumeration predicates = predicateObjects.keys();
			while (predicates.hasMoreElements()){
				String predicate = (String)predicates.nextElement();
				Vector objects = (Vector)predicateObjects.get(predicate);
				for (int i=0; objects!=null && i<objects.size(); i++){
					String object = (String)objects.get(i);
					
					rdfTriple = new RDFTriple();
					rdfTriple.setSubject(eventID);
					rdfTriple.setPredicate(predicate);
					rdfTriple.setObject(object);
					rdfTriples.add(rdfTriple.toVector());
				}
			}
			
			// DEBUG
			//logTriples(rdfTriples);
			
			makeCall(rdfTriples);
		}
		catch (Exception e){
			e.printStackTrace(System.out);			
		}
	}

	/*
	 * 
	 */
	public static void makeCall(Object rdfTriples) throws Exception{
				
        String serverURL = Props.getProperty(Subscribe.PROP_UNS_XMLRPC_SERVER_URL);
        String channelName = Props.getProperty(Subscribe.PROP_UNS_CHANNEL_NAME);
        
        XmlRpcClient server = new XmlRpcClient(serverURL);
        server.setBasicAuthentication(Props.getProperty(Subscribe.PROP_UNS_USERNAME),
				Props.getProperty(Subscribe.PROP_UNS_PASSWORD));
        
        Vector params = new Vector();
        params.add(channelName);
        params.add(rdfTriples);

        String result = (String) server.execute("sendNotification", params);
        if (result.length()>0)
        	System.out.println("sendNotification result: " + result);
    }
	
	/*
	 * 
	 */
	private static void logTriples(Vector triples){
		
		for (int i=0; triples!=null && i<triples.size(); i++){
			Vector triple = (Vector)triples.get(i);
			System.out.println("========================= triple " + i);
			System.out.println(triple);
		}
	}
	
	/*
	 * 
	 */
	public static void main(String[] args){
		
		try{
//			DataElement elm = new DataElement();
//			elm.setIdentifier("Percentile10");
//			elm.setNamespace(new Namespace("11", null, null, null, null));
//			
//			definitionChanged(elm);
			
//			DsTable tbl = new DsTable(null, null, null);
//			tbl.setIdentifier("Summer_ozone/Monthly-1h-max");
//			tbl.setParentNs("12");
//			
//			definitionChanged(tbl);
			
			Dataset dst = new Dataset(null, null, null);
			dst.setIdentifier("CLC2000");
			definitionChanged(dst, Subscribe.DATASET_CHANGED_EVENT, "heinlja");
			
			//definitionChanged("http://dd.eionet.eu.int/schema.rdf#commonelem", "Percentile10");
		}
		catch (Exception e){
			e.printStackTrace(System.out);
		}
	}
}