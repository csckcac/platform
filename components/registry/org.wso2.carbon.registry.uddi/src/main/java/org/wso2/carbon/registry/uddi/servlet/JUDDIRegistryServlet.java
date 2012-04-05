package org.wso2.carbon.registry.uddi.servlet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.Registry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


public class JUDDIRegistryServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(JUDDIRegistryServlet.class);

    /**
     * Create the shared instance of jUDDI's Registry class and call it's
     * "init()" method to initialize all core components.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Registry.start();
        } catch (ConfigurationException e) {
            log.error("jUDDI registry could not be started." + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        try {
            Registry.stop();
        } catch (ConfigurationException e) {
            log.error("jUDDI registry could not be stopped." + e.getMessage(), e);
        }
        super.destroy();
    }


}
