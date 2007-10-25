package eionet.util;

import java.util.StringTokenizer;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * A Class class.
 * <P>
 * @author Enriko K�sper, Jaanus Heinlaid
 */
public class QueryString{
	
	/** */
	private String queryString=null;
	
	public QueryString(){
		
	}
	/**
	 * 
	 * @param queryString
	 */
	public QueryString(String queryString) {
		this.queryString = queryString;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getValue(){
		return queryString;
	}
	
	/**
	 * 
	 * @param param
	 * @param value
	 * @return
	 */
	public String changeParam(String param, String value){
		if (hasParam(param))
			change(param, value);
		else
			add(param, value);
		
		return queryString;
	}
	
	/**
	 * 
	 * @param param
	 * @param value
	 * @return
	 */
	public String addParam(String param, String value){
		return changeParam(param, value);
	}
	
	/**
	 * 
	 * @param param
	 * @return
	 */
	public String removeParam(String param){
		remove(param);
		return queryString;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public boolean equals(QueryString s){
		return equals(s.getValue());
	}
	
	/**
	 * 
	 * @param param
	 * @return
	 */
	private boolean hasParam(String param){
		
		if (queryString.indexOf(param + "=")>0)
			return true;
		return false;
	}
	
	/**
	 * 
	 * @param param
	 * @param value
	 */
	
	private void add(String param, String value){
		
		String s =queryString.indexOf("?")>0 ? "&" : "?";
		
		queryString += s + param + "=" + value;
	}
	
	/**
	 * 
	 * @param param
	 */
	private void remove(String param){
		
		int i=queryString.indexOf(param);
		if (i<1) return;
		
		int and=queryString.indexOf("&", i);
		
		if (and>0)
			queryString = queryString.substring(0,i-1) + queryString.substring(and);
		else
			queryString = queryString.substring(0,i-1);
	}
	
	/**
	 * 
	 * @param param
	 * @param value
	 */
	private void change(String param, String value){
		
		int i=queryString.indexOf(param);
		if (i<1) return;
		String begin=queryString.substring(0, i);
		String str=queryString.substring(i);
		int j = str.indexOf("&");
		String end = j>0 ? str.substring(j) : "";
		
		queryString=begin + param + "=" + value + end ;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public boolean equals(String s){
		
		if (queryString.equals(s)) return true;
		
		int sep = queryString.indexOf("?");
		int sep2 = s.indexOf("?");
		if (sep>0){
			if (sep!=sep2) return false;
			if (!queryString.substring(0,sep).equalsIgnoreCase(s.substring(0,sep))) return false;
			
			String query = queryString.substring(sep+1);
			StringTokenizer tokens = new StringTokenizer(query, "&");
			String query2 = s.substring(sep+1);
			StringTokenizer tokens2 = new StringTokenizer(query2, "&");
			
			
			if (tokens.countTokens()!=tokens2.countTokens()) return false;
			boolean ok = false;
			while (tokens.hasMoreTokens()) {
				String t=tokens.nextToken();
				while (tokens2.hasMoreTokens())
					if (t.equals(tokens2.nextToken())) ok = true;
				if (ok==false) return false;
				ok=false;
				tokens2 = new StringTokenizer(query2, "&");
			}
			
		}
		else{
			return queryString.equalsIgnoreCase(s);
		}
		return true;
	}
	
	@Test
	public void testHasParam(){
		
		QueryString qryStr = new QueryString("param1=value1&param2=");
		assertEquals(true, qryStr.hasParam("param2"));
	}
}

