/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.automation.selenium.page.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to hold all ui element mapping. Each ui element or locator used inside test classes will have a matching
 * constant
 */
public class UIElementMapper {
    public static final Properties prop = new Properties();
    private static final Log log = LogFactory.getLog(UIElementMapper.class);
    private static UIElementMapper instance;

    private UIElementMapper() {
    }

    public static synchronized UIElementMapper getInstance() throws IOException {
        if (instance == null) {
            instance = new UIElementMapper();
            setStream();
        }
        return instance;
    }

    public static Properties setStream() throws IOException {
        InputStream inputStream = new FileInputStream
                (getSystemResourceLocation() + File.separator + "mapper.properties");
        prop.load(inputStream);
        inputStream.close();
        return prop;
    }

    public String getElement(String key) {
        if (prop != null ){
            return prop.getProperty(key);
        }
        return null;
    }

    public static String getSystemResourceLocation() {
        String resourceLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            resourceLocation = System.getProperty("system.test.sample.location").replace("/", "\\");
        } else {
            resourceLocation = System.getProperty("system.test.sample.location").replace("/", "/");
        }
        return resourceLocation;
    }
}
