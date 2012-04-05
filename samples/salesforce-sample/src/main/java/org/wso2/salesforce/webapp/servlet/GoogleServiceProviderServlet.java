package org.wso2.salesforce.webapp.servlet;

import com.sforce.soap.partner.PartnerConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.salesforce.webapp.constants.SalesForceWebAppConstants;
import org.wso2.salesforce.webapp.gspread.GspreadManager;
import org.wso2.salesforce.webapp.salesforce.SalesForceLoginManager;
import org.wso2.salesforce.webapp.salesforce.SalesForceUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles the data export functionalities carried out with google spread sheets.
 */
public class GoogleServiceProviderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(GoogleServiceProviderServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        PartnerConnection conn = null;
        GspreadManager gSpreadSheetManager;
        String category = request.getParameter("category");
        String username = request.getParameter("g_username");
        String password = request.getParameter("g_password");
        String title = request.getParameter("title");

        String context = request.getContextPath();

        HttpSession session = request.getSession();
        SalesForceLoginManager manager = (SalesForceLoginManager) session.getAttribute("manager");
        if (manager == null || manager.getConnection() == null) {
            response.sendRedirect(response.encodeRedirectURL(context +
                    "/login.jsp?error=Session invalid!"));
        }

        try {
            if (manager != null) {
                conn = manager.getConnection();
            }
        } catch (Throwable e) {
            log.error("Unable to obtain the partner connection", e);
            response.sendRedirect(response.encodeRedirectURL(context +
                    "/salesforce/index.jsp?error=" + e.getMessage()));
            return;
        }

        try {
            gSpreadSheetManager = new GspreadManager();
            gSpreadSheetManager.initializeSpreadSheet(username, password, title, category);
            if (SalesForceWebAppConstants.CASES.equals(category)) {
                gSpreadSheetManager.updateCasesWorkSheet(SalesForceUtil.getCasesForAccount(conn));
            }
            if (SalesForceWebAppConstants.OPPORTUNITIES.equals(category)) {
                gSpreadSheetManager.updateOpportunitiesWorkSheet(
                        SalesForceUtil.getOpportunitiesForAccount(conn));
            }
            if (SalesForceWebAppConstants.CONTACTS.equals(category)) {
                gSpreadSheetManager.updateContactsWorkSheet(
                        SalesForceUtil.getContactsForAccount(conn));
            }
            if (SalesForceWebAppConstants.LEADS.equals(category)) {
                gSpreadSheetManager.updateLeadWorkSheet(SalesForceUtil.getLeadsForAccount(conn));
            }
            if (SalesForceWebAppConstants.ALL.equals(category)) {
                gSpreadSheetManager.updateLeadWorkSheet(SalesForceUtil.getLeadsForAccount(conn));
                gSpreadSheetManager.updateContactsWorkSheet(
                        SalesForceUtil.getContactsForAccount(conn));
                gSpreadSheetManager.updateOpportunitiesWorkSheet(
                        SalesForceUtil.getOpportunitiesForAccount(conn));
                gSpreadSheetManager.updateCasesWorkSheet(SalesForceUtil.getCasesForAccount(conn));
            }
            response.sendRedirect(response.encodeRedirectURL(context +
                    "/salesforce/index.jsp?category=" + category));
        } catch (Exception e) {
            log.error("Unable to export data", e);
            response.sendRedirect(response.encodeRedirectURL(context +
                    "/salesforce/google-login-error.jsp"));
        }
    }

}
