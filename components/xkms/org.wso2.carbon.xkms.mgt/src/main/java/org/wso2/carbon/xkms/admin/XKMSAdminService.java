/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.xkms.admin;

import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.xkms.admin.types.XKMSConfigData;

public class XKMSAdminService {

    public static final java.lang.String SERVER_AUTHENTICATION_CODE = "org.wso2.xkms2.service.crypto.authen.code";
    public static final java.lang.String KEY_STORE_LOCATION = "org.wso2.xkms2.service.crypto.keystore.location";
    public static final java.lang.String KEY_STORE_PASSWORD = "org.wso2.xkms2.service.crypto.keystore.password";
    public static final java.lang.String SERVER_CERT_ALIACE = "org.wso2.xkms2.service.crypto.server.cert.aliase";
    public static final java.lang.String SERVER_KEY_PASSWORD = "org.wso2.xkms2.service.crypto.server.key.password";
    public static final java.lang.String ISSUER_CERT_ALIACE = "org.wso2.xkms2.service.crypto.issuer.cert.aliase";
    public static final java.lang.String ISSUER_KEY_PASSWORD = "org.wso2.xkms2.service.crypto.issuer.key.password";
    public static final java.lang.String DEFAULT_EXPIRY_INTERVAL = "org.wso2.xkms2.service.crypto.default.expriy.interval";
    public static final java.lang.String DEFAULT_PRIVATE_KEY_PASSWORD = "org.wso2.xkms2.service.crypto.default.private.key.password";
    public static final java.lang.String ENABLE_PERSISTENCE = "org.wso2.xkms2.service.crypto.persistence.enabled";

    private AxisConfiguration axisConfig;
    private ServicePersistenceManager persistenceManager;

    public static final String XKMS_SERVICE_NAME = "XKMS";

    public XKMSAdminService() throws Exception {
        axisConfig = MessageContext.getCurrentMessageContext().getConfigurationContext()
                .getAxisConfiguration();
        persistenceManager = PersistenceFactory.getInstance(axisConfig).getServicePM();
    }

    public boolean setXKMSConfig(XKMSConfigData configData) throws Exception {

        AxisService xkmsService = getXKMSService();
        setParameters(xkmsService, configData);

        return true;
    }

    public XKMSConfigData getXKMSConfig() throws Exception {

        AxisService xkmsService = getXKMSService();
        XKMSConfigData xkmsConfigData = new XKMSConfigData();
        populateXKMSConfig(xkmsService, xkmsConfigData);

        return xkmsConfigData;
    }

    private void setParameters(AxisService service, XKMSConfigData configData) throws Exception {

        Parameter param;

        if (configData.getAuthenCode() != null) {
            param = new Parameter(SERVER_AUTHENTICATION_CODE, configData.getAuthenCode());
            updateParameter(service, param);
        }
        if (configData.getKeystoreLocation() != null) {
            param = new Parameter(KEY_STORE_LOCATION, configData.getKeystoreLocation());
            updateParameter(service, param);
        }
        if (configData.getKeystorePassword() != null) {
            param = new Parameter(KEY_STORE_PASSWORD, configData.getKeystorePassword());
            updateParameter(service, param);
        }
        if (configData.getServerCertAlias() != null) {
            param = new Parameter(SERVER_CERT_ALIACE, configData.getServerCertAlias());
            updateParameter(service, param);
        }
        if (configData.getServerKeyPassword() != null) {
            param = new Parameter(SERVER_KEY_PASSWORD, configData.getServerKeyPassword());
            updateParameter(service, param);
        }
        if (configData.getIssuerCertAlias() != null) {
            param = new Parameter(ISSUER_CERT_ALIACE, configData.getIssuerCertAlias());
            updateParameter(service, param);
        }
        if (configData.getIssuerKeyPassword() != null) {
            param = new Parameter(ISSUER_KEY_PASSWORD, configData.getIssuerKeyPassword());
            updateParameter(service, param);
        }
        if (configData.getDefaultPrivateKeyPassword() != null) {
            param = new Parameter(DEFAULT_PRIVATE_KEY_PASSWORD, configData.getDefaultPrivateKeyPassword());
            updateParameter(service, param);
        }

        param = new Parameter(DEFAULT_EXPIRY_INTERVAL, String.valueOf(configData.getDefaultExpriyInterval()));
        updateParameter(service, param);

        param = new Parameter(ENABLE_PERSISTENCE, Boolean.toString(configData.isPersistenceEnabled()));
        updateParameter(service, param);
    }

    private void populateXKMSConfig(AxisService service, XKMSConfigData configData) {

        String paramValue;

        if (service.getParameter(SERVER_AUTHENTICATION_CODE) != null) {
            paramValue = (String) service.getParameterValue(SERVER_AUTHENTICATION_CODE);
            configData.setAuthenCode(paramValue);
        }
        if (service.getParameter(KEY_STORE_LOCATION) != null) {
            paramValue = (String) service.getParameterValue(KEY_STORE_LOCATION);
            configData.setKeystoreLocation(paramValue);
        }
        if (service.getParameter(KEY_STORE_PASSWORD) != null) {
            paramValue = (String) service.getParameterValue(KEY_STORE_PASSWORD);
            configData.setKeystorePassword(paramValue);
        }
        if (service.getParameter(SERVER_CERT_ALIACE) != null) {
            paramValue = (String) service.getParameterValue(SERVER_CERT_ALIACE);
            configData.setServerCertAlias(paramValue);
        }
        if (service.getParameter(SERVER_KEY_PASSWORD) != null) {
            paramValue = (String) service.getParameterValue(SERVER_KEY_PASSWORD);
            configData.setServerKeyPassword(paramValue);
        }
        if (service.getParameter(ISSUER_CERT_ALIACE) != null) {
            paramValue = (String) service.getParameterValue(ISSUER_CERT_ALIACE);
            configData.setIssuerCertAlias(paramValue);
        }
        if (service.getParameter(ISSUER_KEY_PASSWORD) != null) {
            paramValue = (String) service.getParameterValue(ISSUER_KEY_PASSWORD);
            configData.setIssuerKeyPassword(paramValue);
        }
        if (service.getParameter(DEFAULT_PRIVATE_KEY_PASSWORD) != null) {
            paramValue = (String) service.getParameterValue(DEFAULT_PRIVATE_KEY_PASSWORD);
            configData.setDefaultPrivateKeyPassword(paramValue);
        }
        if (service.getParameter(DEFAULT_EXPIRY_INTERVAL) != null) {
            paramValue = (String) service.getParameterValue(DEFAULT_EXPIRY_INTERVAL);
            configData.setDefaultExpriyInterval(Integer.parseInt(paramValue));
        }
        if (service.getParameter(ENABLE_PERSISTENCE) != null) {
            paramValue = (String) service.getParameterValue(ENABLE_PERSISTENCE);
            configData.setPersistenceEnabled(Boolean.parseBoolean(paramValue));
        }

    }

    private AxisService getXKMSService() throws Exception {

        AxisService xkmsService = axisConfig.getService(XKMS_SERVICE_NAME);

        if (xkmsService == null) {
            throw new Exception("XKMS service not found ...");
        }

        return xkmsService;
    }

    private void updateParameter(AxisService service, Parameter param) throws Exception {
        service.addParameter(param);
        if (param.getParameterElement() == null) {
            String paramString = "<parameter name=\"" + param.getName() + "\">" + param.getValue() + "</parameter>";
            param.setParameterElement(AXIOMUtil.stringToOM(paramString));
        }
        persistenceManager.updateServiceParameter(service, param);
    }

}
