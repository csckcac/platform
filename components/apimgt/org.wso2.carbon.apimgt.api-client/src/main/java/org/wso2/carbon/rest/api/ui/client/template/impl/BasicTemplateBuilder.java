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
package org.wso2.carbon.rest.api.ui.client.template.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.rest.api.ui.client.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.ui.client.template.util.TemplateLoader;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BasicTemplateBuilder implements APITemplateBuilder{
    /*private static String resourceTemplate = "<resource xmlns=\"http://ws.apache.org/ns/synapse\" uri-template=\"[1]\" methods=\"[2]\" >\n" +
                                             "\t\t    <inSequence>\n" +
                                             "\t\t    \t<send>\n" +
                                             "\t\t\t\t<endpoint name=\"Delecious\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                                             "\t\t\t\t\t<address uri=\"[3]\"  />\n" +
                                             "\t\t\t\t</endpoint>\n" +
                                             "\t\t\t</send>\t\n" +
                                             "\t\t    </inSequence>\n" +
                                             "\t\t    <outSequence>\n" +
                                             "\t\t    \t<send />\n" +
                                             "\t\t    </outSequence>\n" +
                                             "\t\t</resource>\t";

    private static String apiTemplate = "<api xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"[1]\" context=\"[2]\">\t"
                                        +
                                        "</api>";

    private static String handlersTemplate = "<handlers xmlns=\"http://ws.apache.org/ns/synapse\"> </handlers>";
    private static String handlerTemplate = "<handler xmlns=\"http://ws.apache.org/ns/synapse\" class=\"[1]\" >" +
                                                "<property name=\"id\" value=\"A\"/>" +
                                                "<property name=\"policyKey\" value=\"[2]\"/>" +
                                            "</handler>";*/

    private static String resourceTemplate = TemplateLoader.API_RESOURCE_TEMPLATE;

    private static String apiTemplate = TemplateLoader.API_API_TEMPLATE;

    private static String handlersTemplate = TemplateLoader.API_HANDLERS_TEMPLATE;

    private static String handlerTemplate = TemplateLoader.API_HANDLER_TEMPLATE;

    private Map apiMappings;
    private List<Map> resourceMappings;
    private List<Map> handlerMappings;

    public BasicTemplateBuilder(Map apiMappings, List<Map> resourceMappings, List<Map> handlerMappings) throws AxisFault {
        this.apiMappings = apiMappings;
        this.resourceMappings = resourceMappings;
        this.handlerMappings = handlerMappings;
    }


    public OMElement getConfigXMLForTemplate() {
        return createOMElementFrom(getConfigStringForTemplate());
    }

    public String getAPIName() {
        return (String) apiMappings.get(KEY_FOR_API_NAME) + ":v" + apiMappings.get(KEY_FOR_API_VERSION);
    }

    public String getAPIContext() {
        return (String) apiMappings.get(KEY_FOR_API_CONTEXT);
    }

    public String getConfigStringForTemplate() {
        String configAPI = constructAPIConfig();
        OMElement configAPIOM = createOMElementFrom(configAPI);
        assert configAPIOM != null;

        List<String> configResources = constructResourceConfig();
        for (String configResource : configResources) {
            OMElement configResourceOM = createOMElementFrom(configResource);
            if (configResourceOM != null) {
                configAPIOM.addChild(configResourceOM);
            }
        }
        List<String> handlerConfigs = constructHandlerConfig();

        OMElement hadlersConfigOM = createOMElementFrom(handlersTemplate);
        for (String handlerConfig : handlerConfigs) {
            OMElement configSingleHandlerOM = createOMElementFrom(handlerConfig);
            if (configSingleHandlerOM != null) {
                hadlersConfigOM.addChild(configSingleHandlerOM);
            }
        }

        configAPIOM.addChild(hadlersConfigOM);
        return configAPIOM.toString();
    }

    private String constructAPIConfig() {
        StringBuffer apiTempl = new StringBuffer(apiTemplate);
        if (apiMappings.get(KEY_FOR_API_NAME) != null && apiMappings.get(KEY_FOR_API_CONTEXT) != null) {
            String apiConf = apiTempl.toString().replaceAll("\\[1\\]", (String) apiMappings.get(KEY_FOR_API_NAME)).
                    replaceAll("\\[2\\]", (String) apiMappings.get(KEY_FOR_API_CONTEXT)).replaceAll
                    ("\\[3\\]", (String) apiMappings.get(KEY_FOR_API_VERSION));
            return apiConf;
        }
        return null;
    }

    private List<String> constructHandlerConfig() {
        Iterator<Map> handlerMaps = handlerMappings.iterator();
        List<String> handlerListStr = new ArrayList<String>();

        while (handlerMaps.hasNext()) {
            Map singleHandler = handlerMaps.next();
            StringBuffer handlerTempl = new StringBuffer(handlerTemplate);
            if (singleHandler != null && singleHandler.get(KEY_FOR_HANDLER) != null) {
                String replacedStr = handlerTempl.toString().replaceAll("\\[1\\]", (String) singleHandler.get(KEY_FOR_HANDLER)).
                        replaceAll("\\[2\\]", (String) singleHandler.get(KEY_FOR_HANDLER_POLICY_KEY));
                handlerListStr.add(replacedStr);
            }
        }
        return handlerListStr;
    }

    private List<String> constructResourceConfig() {
        Iterator<Map> resourceMaps = resourceMappings.iterator();
        List<String> resListStr = new ArrayList<String>();

        while (resourceMaps.hasNext()) {
            Map singleResMap = resourceMaps.next();

            StringBuffer resTempl = new StringBuffer(resourceTemplate);

            if (singleResMap != null && singleResMap.get(KEY_FOR_RESOURCE_METHODS) != null &&
                singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE) != null &&
                singleResMap.get(KEY_FOR_RESOURCE_URI) != null) {
                String replacedStr = resTempl.toString().replaceAll("\\[1\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE)).
                        replaceAll("\\[2\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_METHODS)).
                        replaceAll("\\[3\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_URI));
                resListStr.add(replacedStr);
            }
        }
        return resListStr;
    }

    public static OMElement createOMElementFrom(String omString) {
        try {
            return AXIOMUtil.stringToOM(omString);
        } catch (XMLStreamException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(resourceTemplate);
        System.out.println(apiTemplate);
        System.out.println(handlersTemplate);
        System.out.println(handlerTemplate);
    }
}
