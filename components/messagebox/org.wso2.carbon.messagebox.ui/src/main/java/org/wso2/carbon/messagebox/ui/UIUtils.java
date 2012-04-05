package org.wso2.carbon.messagebox.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.messagebox.stub.MessageQueueStub;
import org.wso2.carbon.messagebox.stub.QueueServiceStub;
import org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub;
import org.wso2.carbon.qpid.stub.service.QpidAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.ListUsersUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.ui.UserAdminClient;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

public class UIUtils {

    public static QueueServiceStub getQueueServiceClient(ServletConfig config, HttpSession session,
                                                         HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "QueueService";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        QueueServiceStub stub = new QueueServiceStub(configContext,backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        return stub;
    }

    public static MessageQueueStub getMessageServiceClient(ServletConfig config,
                                                           HttpSession session,
                                                           HttpServletRequest request,
                                                           String queueName)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "MessageQueue/" + queueName;
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        MessageQueueStub stub = new MessageQueueStub(configContext ,backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        return stub;
    }

    public static UserAdminStub getUserAdminServiceClient(ServletConfig config,
                                                          HttpSession session,
                                                          HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "UserAdmin/";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminStub stub = new UserAdminStub(configContext,backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        return stub;
    }

    public static MessageBoxAdminServiceStub getMessageBoxAdminServiceStub(ServletConfig config,
                                                                           HttpSession session,
                                                                           HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "MessageBoxAdminService";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        MessageBoxAdminServiceStub stub = new MessageBoxAdminServiceStub(configContext,backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }

        return stub;
    }

    public static QpidAdminServiceStub getQpidAdminServiceStub(ServletConfig config,
                                                               HttpSession session,
                                                               HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "QpidAdminService";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        QpidAdminServiceStub stub = new QpidAdminServiceStub(configContext, backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }

        return stub;
    }

    public static String getBackendServerUrl(ServletConfig config, HttpSession session,
                                             HttpServletRequest request) {
        return CarbonUIUtil.getServerURL(config.getServletContext(), session);
    }

    public static UserAdminClient getUserAdminClient(ServletConfig config, HttpSession session,
                                                     HttpServletRequest request) throws Exception {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        if (!backendServerURL.endsWith("/")) {
            backendServerURL = backendServerURL + "/";
        }
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new UserAdminClient(cookie, backendServerURL, null);
    }

    public static String getValue(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if ("null".equals(value)) {
            return "";
        }
        return value != null ? value : "";
    }

    public static String getDialog(String message) {
        return "CARBON.showErrorDialog('" + message + "');";
    }

    public static String getHtmlString(String message) {
        return message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

    }

    public static String[] searchUsers(String prefix, UserAdminStub userAdminStub) {
        try {
            return userAdminStub.listUsers(prefix);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ListUsersUserAdminExceptionException e) {
            e.printStackTrace();
        }
        return new String[0];
    }
}
