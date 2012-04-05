/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CommonUtil {

    public static String[] getAllLifeCycleStates(Registry registry, String lifeCycleName) throws RegistryException {
        boolean isLiteral = true;
        List<String> stateList = new ArrayList<String>();

        String[] allAspects = registry.getAvailableAspects();

//        Check if the given LC name is there in the registry. If not will return null
        if (!Arrays.asList(allAspects).contains(lifeCycleName)) {
            String msg = "There are no lifecycles with the given name";
            throw new RegistryException(msg);
        }

//        Here we are getting the LC configuration from the default LC configuration path.
//        If this fails we use the registry.xml to see whether the LC name is there and will get the resource.
        Resource resource=null;

//        reading the registry.xml to see of the LC configuration is there
        if (!registry.resourceExists(RegistryConstants.CONFIG_REGISTRY_BASE_PATH+
                            RegistryConstants.LIFECYCLE_CONFIGURATION_PATH + lifeCycleName)) {

            /*
            * Getting the registry.xml from the client side is not possible.
            * Therefore if there are no resource in the default life cycle resource store path we consider this life cycle
            * as a static life cycle which has been configured using the registry.xml
            * Since we are unable to read the registry.xml from a client program we throw an exception here.
            * */

            String msg = "The given lifecycle configuration is an static configuration. Unable to read the registry.xml";
            throw new RegistryException(msg);
/*
            try {
                FileInputStream inputStream = new FileInputStream(getConfigFile());
                StAXOMBuilder builder = new StAXOMBuilder(inputStream);
                OMElement configElement = builder.getDocumentElement();

                Iterator aspectElement = configElement.getChildrenWithName(new QName("aspect"));
                while (aspectElement.hasNext()) {
                    OMElement next = (OMElement) aspectElement.next();
                    String name = next.getAttributeValue(new QName("name"));

                    if(name.equals(lifeCycleName)){
                        OMElement element = next.getFirstElement();
                        resource = registry.get(element.getText());
                        isLiteral = false;
                        break;
                    }
                }

            } catch (FileNotFoundException e) {
                throw new RegistryException("", e);
            } catch (XMLStreamException e) {
                throw new RegistryException("", e);
            }
*/
        }else{
            resource = registry.get(RegistryConstants.CONFIG_REGISTRY_BASE_PATH+
                            RegistryConstants.LIFECYCLE_CONFIGURATION_PATH + lifeCycleName);

        }

//        here we get the resource content and build a OMElement from it
        try {
            String xmlContent = new String((byte[])resource.getContent());
            OMElement configurationElement =  AXIOMUtil.stringToOM(xmlContent);

//            if the config type is literal we take the lifecycle element from it
            if(isLiteral){
                OMElement typeElement = configurationElement.getFirstElement();
                configurationElement = typeElement.getFirstElement();
            }

//            this is to see whether this is the new configuration or the old one
            Iterator statesElement = configurationElement.getChildrenWithName(new QName("scxml"));

//            if it is the new configuration we use the scxml parser to get all the elements
            if(statesElement.hasNext()){
                while (statesElement.hasNext()) {
                    OMElement scxmlElement = (OMElement) statesElement.next();
                    Iterator stateElements = scxmlElement.getChildrenWithName(new QName("state"));
                    while (stateElements.hasNext()) {
                        OMElement next = (OMElement) stateElements.next();
                        stateList.add(next.getAttributeValue(new QName("id")));
                    }
                }
            }
            else{
                Iterator states = configurationElement.getChildElements();
                while (states.hasNext()) {
                    OMElement next = (OMElement) states.next();
                    stateList.add(next.getAttributeValue(new QName("name")));
                }
            }


        } catch (XMLStreamException e) {
            throw new RegistryException("", e);
        }

        String[] retArray =  new String[stateList.size()];
        return stateList.toArray(retArray);

    }


//    The following method is commented out since this is an utility method and
//    we are not able to get the registry.xml from the client side.


/*
    private static File getConfigFile() throws org.wso2.carbon.registry.core.exceptions.RegistryException {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (!registryXML.exists()) {
                String msg = "Registry configuration file (registry.xml) file does " +
                        "not exist in the path " + configPath;
                throw new org.wso2.carbon.registry.core.exceptions.RegistryException(msg);
            }
            return registryXML;
        } else {
            String msg = "Cannot find registry.xml";
            throw new org.wso2.carbon.registry.core.exceptions.RegistryException(msg);
        }
    }
*/

}
