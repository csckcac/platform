package org.wso2.salesforce.webapp.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.salesforce.webapp.salesforce.SalesForceLoginManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet implementation class MyServlet
 */

public class SalesForceLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private String defaultUsername;
    private String defaultPassword;
    public static final String DEFAULT_USERNAME = "defaultUsername";
    public static final String DEFAULT_PASSWORD = "defaultPassword";
    private static final Log log = LogFactory.getLog(SalesForceLoginServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.defaultUsername = getServletContext().getInitParameter(DEFAULT_USERNAME);
        this.defaultPassword = getServletContext().getInitParameter(DEFAULT_PASSWORD);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        HttpSession session;
        String context = request.getContextPath();

        String action = request.getParameter("action");
        if (action != null && action.equals("submit")) {
            String emailAddress = request.getParameter("emailAddress");
            String password = request.getParameter("password");
            String loginMethod = request.getParameter("loginMethod");

            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            if ("default".equals(loginMethod)) {
                emailAddress = this.defaultUsername;
                password = this.defaultPassword;
            }

            if (emailAddress == null || password == null) {
                log.error("Username/Password are not specified");
                response.sendRedirect(response.encodeRedirectURL(context +
                        "/login.jsp?error=Please enter a valid username and password!"));
            } else {
                SalesForceLoginManager manager = new SalesForceLoginManager();
                try {
                    manager.login(emailAddress, password);
                    session = request.getSession(true);
                    session.setAttribute("manager", manager);
                    response.sendRedirect("salesforce/index.jsp");
                } catch (Exception e) {
                    log.error("Unable to establish a partner connection to Salesforce", e);
                    response.sendRedirect(response.encodeRedirectURL(context +
                            "/salesforce/salesforce-login-error.jsp"));
                }
            }
        }
    }
}
