package org.wso2.carbon.appfactory.config.reader.xpath.ext;

import net.sf.saxon.expr.XPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;

/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

public class ConfigReaderXpathExt {
    private static final Log log = LogFactory.getLog(ConfigReaderXpathExt.class);

    private static AppFactoryConfiguration appFactoryConfig;

    static {
        try {
            appFactoryConfig = AppFactoryUtil.loadAppFactoryConfiguration();
        } catch (AppFactoryException e) {
            log.error("Error loading appfactory configuration");
        }
    }

    public static String getAdminUserName(XPathContext context) {
        return appFactoryConfig.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }


}
