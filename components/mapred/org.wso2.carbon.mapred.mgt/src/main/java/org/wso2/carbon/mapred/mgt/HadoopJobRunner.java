package org.wso2.carbon.mapred.mgt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.regexp.RE;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.*;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Tag;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;

public class HadoopJobRunner extends AbstractAdmin {
	private Log log = LogFactory.getLog(HadoopJobRunner.class);
	public static final String HADOOP_CONFIG = System.getProperty(ServerConstants.CARBON_HOME)+File.separator+"repository"+File.separator+"conf"+File.separator+"advanced"+File.separator+"hadoop.properties";
	public static final String REG_JAR_PATH = "/job/jar/";
	public static final String DEFAULT_HADOOP_JAR_PATH = ".";
	public static int DEFAULT_READ_LENGTH = 1024;
	
	private String getCurrentTGTCache() throws AxisFault, RemoteException, Exception {
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		ConfigurationContext configCtx = msgCtx.getConfigurationContext();
		HttpServletRequest request = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        String cookie = request.getHeader(HTTPConstants.COOKIE_STRING);
		String serviceEPR = "https://localhost:9443/services/" + "Krb5Authenticator";
		Krb5AuthenticatorStub stub = new Krb5AuthenticatorStub(configCtx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        if (cookie != null) {
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        return stub.getTicketCache();
	}
	
	public void runJob(String jarName, String className, String args) /*throws Throwable*/ {
		String currentTGT = null;
		try {
			currentTGT = getCurrentTGTCache();
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e ) {
			e.printStackTrace();
		}
		HadoopJobRunnerThread hadoopJobThread = new HadoopJobRunnerThread(jarName, className, args, currentTGT);
		hadoopJobThread.start();
	}
	
	public void getJar(String jarPath) {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		Resource resource = null;
		try {
			resource = reg.get(REG_JAR_PATH+jarPath);
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			InputStream resIS = resource.getContentStream();
			//Write jarFile to default location
			String[] uriParts = jarPath.split("/");
			String jarFilePath = DEFAULT_HADOOP_JAR_PATH+"/"+uriParts[uriParts.length - 1];
			FileOutputStream fos = new FileOutputStream(jarFilePath);
			byte[] buffer = new byte[DEFAULT_READ_LENGTH];
			int readLen = 0;
			while ((readLen = resIS.read(buffer)) > -1) {
				fos.write(buffer, 0, readLen);
				buffer = new byte[DEFAULT_READ_LENGTH];
			}
			resIS.close();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public void putJar(String friendlyName, DataHandler dataHandler) {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		try {
			if (reg.resourceExists(REG_JAR_PATH+friendlyName)) {
				log.info("Deleting already exsiting "+REG_JAR_PATH+friendlyName);
				reg.delete(REG_JAR_PATH+friendlyName);
			}
			Resource resource = reg.newResource();
			resource.setContentStream(dataHandler.getInputStream());
			String out = reg.put(REG_JAR_PATH+friendlyName, resource);
			log.info(out);
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] getJarList() {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		String sql1 = "SELECT REG_PATH_ID,REG_NAME FROM REG_RESOURCE WHERE REG_NAME LIKE ?";
		String[] paths = null;
		try {
			Resource query = reg.newResource();
			query.setContent(sql1);
			query.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
	        query.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.RESOURCES_RESULT_TYPE);
	        reg.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", query);
	        Map parameters = new HashMap();
	        parameters.put("1", "%.jar");
	        Resource result = reg.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
	        paths = (String[])result.getContent();
	        for (int i=0; i<paths.length; i++) {
	        	String[] subStrs = paths[i].split("/");
	        	paths[i] = subStrs[subStrs.length - 1];
	        }
	        result.discard();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return paths;
	}
}
