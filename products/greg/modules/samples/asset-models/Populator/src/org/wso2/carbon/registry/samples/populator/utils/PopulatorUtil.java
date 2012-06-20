package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.net.URL;

public class PopulatorUtil {

    public static String authenticate(ConfigurationContext ctx, String serverURL, String username, String password) throws AxisFault, AuthenticationException {
        String serviceEPR = serverURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, new URL(serviceEPR).getHost());
            if (result){
                return (String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING);
            }
            return null;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    public static ReportConfigurationBean getReportConfigurationBean(String model){
        ReportConfigurationBean bean = new ReportConfigurationBean();
        if("ApplicationModel".equals(model)){
            bean.setName("ApplicationReport");
            bean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml");
            bean.setType("pdf");
            bean.setReportClass("org.wso2.carbon.registry.samples.reporting.ApplicationReportGenerator");
        } else if("BusinessProcessModel".equals(model)){
            bean.setName("ProcessReport");
            bean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/process_template.jrxml");
            bean.setType("pdf");
            bean.setReportClass("org.wso2.carbon.registry.samples.reporting.ProcessReportGenerator");
        } else if("ProjectModel".equals(model)){
            bean.setName("ProjectReport");
            bean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/project_template.jrxml");
            bean.setType("pdf");
            bean.setReportClass("org.wso2.carbon.registry.samples.reporting.ProjectReportGenerator");
        } else if("TestPlanModel".equals(model)){
            bean.setName("TestSuiteReport");
            bean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/testsuite_template.jrxml");
            bean.setType("pdf");
            bean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestSuiteReportGenerator");
        } else if("PeopleModel".equals(model)){
            bean.setName("OrganizationReport");
            bean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/organization_template.jrxml");
            bean.setType("pdf");
            bean.setReportClass("org.wso2.carbon.registry.samples.reporting.OrganizationReportGenerator");
        }
        return bean;
    }

}
