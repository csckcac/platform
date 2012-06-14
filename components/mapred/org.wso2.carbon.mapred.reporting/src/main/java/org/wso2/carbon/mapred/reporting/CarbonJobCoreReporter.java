package org.wso2.carbon.mapred.reporting;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobCoreReporter;
import org.json.JSONException;
import org.json.JSONObject;

import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub;

public class CarbonJobCoreReporter extends JobCoreReporter {

	private Log log = LogFactory.getLog(CarbonJobCoreReporter.class);
	private ConfigurationContext confCtx;
	private HadoopJobRunnerStub jobRunnerStub;
	private AuthenticationAdminStub authAdminStub;
	private boolean isAuthenticated = false;
	private String cookie = null;
	@Override
	public void init(Configuration conf) {
		// Get authentication parameters
		String path = conf.get("hadoop.security.truststore", "wso2carbon.jks");
		String username = conf.get("hadoop.security.admin.username", "admin");
		String password = conf.get("hadoop.security.admin.password", "admin");
		String serviceUrl = conf.get(
				"hadoop.mapred.reporter.service.url",
				"https://127.0.0.1:9443/services/");
		System.setProperty("javax.net.ssl.trustStore", path);
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
		try {
			authAdminStub = new AuthenticationAdminStub(confCtx, serviceUrl+"AuthenticationAdmin");
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		}
        authAdminStub._getServiceClient().getOptions().setManageSession(true);
        try {
        	URI serviceUrlObj = new URI(serviceUrl);
        	String serviceHostName = serviceUrlObj.getHost();
			isAuthenticated = authAdminStub.login(username, password, serviceHostName);
			cookie = (String)authAdminStub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
			log.info("Logging in as admin");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		} catch (LoginAuthenticationExceptionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		}
		try {
			//confCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
			jobRunnerStub = new HadoopJobRunnerStub(confCtx, serviceUrl+"HadoopJobRunner");
			ServiceClient client = jobRunnerStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            jobRunnerStub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
			jobRunnerStub.attachFinalReport(JSONEncode());
			authAdminStub.logout();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e.getMessage());
		} catch (RemoteException e) {
			log.warn(e.getMessage());
		}
		catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	private String JSONEncode() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("JobID", getJobId());
			jsonObj.put("JobName", getJobName());
			jsonObj.put("JobUser", getJobUser());
			jsonObj.put("MapProgress", getMapProgress());
			jsonObj.put("ReduceProgress", getMapProgress());
			jsonObj.put("JobStatus", getStatus());
			jsonObj.put("StartTime", getStartTime());
			jsonObj.put("ScheduleInfo", getSchedInfo());
			jsonObj.put("FailureInfo", getFailureInfo());
		} catch (JSONException e) {
			log.warn(e.getMessage());
		}
		return jsonObj.toString();
	}
	
}
