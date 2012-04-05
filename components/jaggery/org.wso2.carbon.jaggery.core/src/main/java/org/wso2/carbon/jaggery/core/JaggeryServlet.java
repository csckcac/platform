package org.wso2.carbon.jaggery.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jaggery.core.manager.WebAppManager;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class JaggeryServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(JaggeryServlet.class);

    private WebAppManager manager = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            //init scope with global scripts
            String jaggeryDir = System.getProperty("carbon.home");
            if (jaggeryDir == null) {
                jaggeryDir = System.getProperty("catalina.home");
            }
            if(jaggeryDir != null) {
                jaggeryDir += File.separator + "jaggery";
            }
            manager = new WebAppManager(jaggeryDir);
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }
}
