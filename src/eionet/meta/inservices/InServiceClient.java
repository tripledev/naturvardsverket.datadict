package eionet.meta.inservices;

import javax.servlet.http.*;
import com.tee.uit.client.*;
import eionet.util.*;
import java.util.*;

public abstract class InServiceClient implements InServiceClientIF{
	
	protected String serviceName = null;
	protected String serviceUrl  = null;
	protected String serviceUsr  = null;
	protected String servicePsw  = null;
	protected ServiceClientIF client = null;
	
	protected void load() throws Exception {
		
		if (Util.voidStr(serviceName) || Util.voidStr(serviceUrl))
			throw new Exception("serviceName or serviceUrl is missing!");
		
		client = ServiceClients.getServiceClient(serviceName, serviceUrl);
		if (!Util.voidStr(serviceUsr) && !Util.voidStr(serviceUsr))
			client.setCredentials(serviceUsr,servicePsw);
	}
	
	protected void getProps(String clientName){
		
		String prefix = Props.INSERV_PREFIX + clientName;
		serviceName = Props.getProperty(prefix + Props.INSERV_NAME);
		serviceUrl  = Props.getProperty(prefix + Props.INSERV_URL);
		serviceUsr  = Props.getProperty(prefix + Props.INSERV_USR);
		servicePsw  = Props.getProperty(prefix + Props.INSERV_PSW);
	}
	
	protected Object execute(String method, Vector params) throws Exception{
		
		if (client == null) load();
		return client.getValue(method, params);
	}
	
	public abstract void execute(HttpServletRequest req) throws Exception;
}
