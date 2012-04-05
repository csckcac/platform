/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.announcement.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class AnnouncementsConfiguration {
    private static Log log = LogFactory.getLog(AnnouncementsConfiguration.class);
    private Map<String, String> announcementConfigMap = new HashMap<String, String>();
    private static AnnouncementsConfiguration announcementsConfig = null;

    // making it singelton
    private AnnouncementsConfiguration() {
        String announcementsFileName = CarbonUtils.getCarbonConfigDirPath() + "/announcement.xml";
        File announcementFile = new File(announcementsFileName);
        if (!announcementFile.exists()) {
            log.error("Announcements file is not present at: " + announcementsFileName +".");
            return;
        }
        try {
            loadConfigurations(announcementFile);
        } catch (Exception e) {
            String msg = "Error in loading configuration for announcements: " +
                    announcementsFileName + ".";
            log.error(msg, e);
            return;
        }
    }

    public static AnnouncementsConfiguration getAnnouncementsConfiguration() {
        if (announcementsConfig == null) {
            announcementsConfig = new AnnouncementsConfiguration();
        }
        return announcementsConfig;
    }


    public void loadConfigurations(File file) throws Exception {
        //create the parser
        FileInputStream ip = null;
        try {
            ip = new FileInputStream(file);
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(ip);

            //create the builder
            StAXOMBuilder builder = new StAXOMBuilder(parser);

            //get the root element
            OMElement documentElement =  builder.getDocumentElement();

            // load the configurations
            loadConfig(documentElement, documentElement.getLocalName() + ".");
        } finally {
            if(ip != null) {
                ip.close();
            }
        }
    }

    private void loadConfig(OMElement root, String prefix) {
        Iterator childIterator = root.getChildElements();

        while (childIterator.hasNext()) {
            Object childObj = childIterator.next();
            if (!(childObj instanceof OMElement)) {
                // we are eliminating the texts right here, inbetween
                continue;
            }
            OMElement childElement = (OMElement)childObj;
            if (elementHasText(childElement)) {
                String key = prefix + childElement.getLocalName();
                String value = childElement.getText();
                announcementConfigMap.put(key, value);
            } else {
                String key = prefix + childElement.getLocalName() + ".";
                loadConfig(childElement, key);
            }
        }
    }
    
    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    public String getValue(String key) {
        return announcementConfigMap.get(key);
    }
}

