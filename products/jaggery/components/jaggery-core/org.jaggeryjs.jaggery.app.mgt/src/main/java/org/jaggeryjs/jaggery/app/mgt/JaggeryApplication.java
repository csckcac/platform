/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.jaggery.app.mgt;

import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.webapp.mgt.WebApplication;

import java.io.File;
import java.util.List;

/**
 * Represents a Tomcat Web Application
 */
@SuppressWarnings("unused")
public class JaggeryApplication extends WebApplication {
    private static final Log log = LogFactory.getLog(JaggeryApplication.class);

    private long configDirLastModifiedTime;
    private List<ServletParameter> servletParameters;
    private List<ServletMappingParameter> servletMappingParameters;

    public JaggeryApplication(Context context, File webappFile) {
        super(context, webappFile);
        this.context = context;
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
        if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
        	this.configDirLastModifiedTime = JaggeryDeploymentUtil.getConfig(webappFile).lastModified();
        }
    }

    public JaggeryApplication(File webappFile) {
        super(webappFile);
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
        if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
        	this.configDirLastModifiedTime = JaggeryDeploymentUtil.getConfig(webappFile).lastModified();
        }
    }

    public long getConfigDirLastModifiedTime() {
        return configDirLastModifiedTime;
    }

    public void setConfigDirLastModifiedTime(long configDirLastModifiedTime) {
        this.configDirLastModifiedTime = configDirLastModifiedTime;
    }

    public List<ServletParameter> getServletParameters() {
        return servletParameters;
    }

    public void setServletParameters(List<ServletParameter> servletParameters) {
        this.servletParameters = servletParameters;
    }

    public List<ServletMappingParameter> getServletMappingParameters() {
        return servletMappingParameters;
    }

    public void setServletMappingParameters(
            List<ServletMappingParameter> servletMappingParameters) {
        this.servletMappingParameters = servletMappingParameters;
    }

    public void delete() throws CarbonException {
        undeploy();
        if (getWebappFile().isDirectory()) {
            if (!FileManipulator.deleteDir(getWebappFile())) {
                throw new CarbonException("Webapp file " + getWebappFile() + " deletion failed");
            }
        } else if (!getWebappFile().delete()) {
            throw new CarbonException("Webapp file " + getWebappFile() + " deletion failed");
        }
    }

    public void undeploy() throws CarbonException {
        lazyUnload();
        File webappDir = new File(getAppBase(), getContext().getBaseName());
        if (webappDir.exists() && !FileManipulator.deleteDir(webappDir)) {
            throw new CarbonException("exploded Webapp directory " + webappDir + " deletion failed");
        }
    }

}