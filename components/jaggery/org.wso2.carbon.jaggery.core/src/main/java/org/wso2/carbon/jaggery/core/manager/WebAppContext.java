package org.wso2.carbon.jaggery.core.manager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebAppContext extends JaggeryContext {

    private ServletContext servletConext = null;
    private HttpServletRequest servletRequest = null;
    private HttpServletResponse servletResponse = null;

    public ServletContext getServletConext() {
        return servletConext;
    }

    public void setServletConext(ServletContext servletConext) {
        this.servletConext = servletConext;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }
}
