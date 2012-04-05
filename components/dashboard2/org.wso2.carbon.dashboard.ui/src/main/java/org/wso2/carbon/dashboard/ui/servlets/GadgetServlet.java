package org.wso2.carbon.dashboard.ui.servlets;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import org.apache.poi.util.ArrayUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.dashboard.ui.DashboardServiceClient;
import org.wso2.carbon.dashboard.ui.GSUIConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

/**
 * Provides an API for gadget controller
 */
public class GadgetServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(GadgetServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String operation = req.getParameter("op");
        log.info(operation);

        if (GSUIConstants.ADD_GADGET.equals(operation)) {

        } else if (GSUIConstants.GET_GADGET.equals(operation)) {

        } else if (GSUIConstants.GET_GADGETS_FOR_TAB.equals(operation)) {
            String result = getGadgetsFortab(req);

            PrintWriter out = resp.getWriter();

            out.write(result);

        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    private String getGadget(String id) {
        return null;
    }

    private String addGadget() {
        return null;

    }

    private String getGadgetsFortab(HttpServletRequest req) throws AxisFault {
        String tabId = req.getParameter("tabId");
        DashboardServiceClient client = getServiceClient(req);
        String[] urlList = client.getGadgetUrlsToLayout("admin", tabId, null, req.getRequestURI());
        return ArrayUtils.toString(urlList);
    }

    private DashboardServiceClient getServiceClient(HttpServletRequest req) throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(req.getSession().getServletContext(), req.getSession());
        String context = req.getContextPath();

        ConfigurationContext configContext =
                (ConfigurationContext) req.getSession().getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) req.getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        DashboardServiceClient dashboardServiceClient = new DashboardServiceClient(cookie,
                backendServerURL,
                configContext,
                req.getLocale());

        return dashboardServiceClient;
    }
}
