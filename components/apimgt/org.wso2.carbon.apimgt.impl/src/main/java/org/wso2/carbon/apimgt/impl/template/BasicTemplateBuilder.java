/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.template;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Constructs API and resource configurations for the ESB/Synapse using a set of
 * cached templates.
 */
public class BasicTemplateBuilder implements APITemplateBuilder {

    private static final Log log = LogFactory.getLog(BasicTemplateBuilder.class);

    private TemplateLoader templateLoader = TemplateLoader.getInstance();

    private Map<String,String> apiMappings;
    private List<Map<String,String>> resourceMappings;
    private List<Map<String,String>> handlerMappings;
    
    private boolean blockedAPI = false;

    public BasicTemplateBuilder(Map<String,String> apiMappings,
                                List<Map<String,String>> resourceMappings,
                                List<Map<String,String>> handlerMappings) {
        this.apiMappings = apiMappings;
        this.resourceMappings = resourceMappings;
        this.handlerMappings = handlerMappings;
    }
    
    public BasicTemplateBuilder(Map<String,String> apiMappings) {
        this.apiMappings = apiMappings;
        this.blockedAPI = true;
    }

    public String getConfigStringForTemplate() throws APITemplateException {
        String configAPI = constructAPIConfig();
        OMElement configAPIOM = createOMElementFrom(configAPI);

        List<String> configResources = constructResourceConfig();
        for (String configResource : configResources) {
            OMElement configResourceOM = createOMElementFrom(configResource);
            if (configResourceOM != null) {
                configAPIOM.addChild(configResourceOM);
            }
        }

        List<String> handlerConfigs = constructHandlerConfig();
        if (handlerConfigs.size() > 0) {
            OMElement handlersConfigOM = createOMElementFrom(
                    templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_HANDLERS));
            for (String handlerConfig : handlerConfigs) {
                OMElement configSingleHandlerOM = createOMElementFrom(handlerConfig);
                if (configSingleHandlerOM != null) {
                    handlersConfigOM.addChild(configSingleHandlerOM);
                }
            }
            configAPIOM.addChild(handlersConfigOM);
        }

        return configAPIOM.toString();
    }

    public OMElement getConfigXMLForTemplate() throws APITemplateException {
        return createOMElementFrom(getConfigStringForTemplate());
    }

    private String constructAPIConfig() throws APITemplateException {
        if (apiMappings.containsKey(KEY_FOR_API_NAME) &&
                apiMappings.containsKey(KEY_FOR_API_CONTEXT) &&
                apiMappings.containsKey(KEY_FOR_API_VERSION)) {

            String apiTemplate;
            if (blockedAPI) {
                apiTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_BLOCKED_API);
            } else {
                apiTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_API);
            }
            String apiConfig = apiTemplate.toString().
                    replaceAll("\\[1\\]", apiMappings.get(KEY_FOR_API_NAME)).
                    replaceAll("\\[2\\]", apiMappings.get(KEY_FOR_API_CONTEXT)).
                    replaceAll("\\[3\\]", apiMappings.get(KEY_FOR_API_VERSION));
            return apiConfig;
        }
        handleException("Required API mapping not provided");
        return null;
    }

    private List<String> constructResourceConfig() throws APITemplateException {
        List<String> resourceListString = new ArrayList<String>();
        if (resourceMappings == null) {
            return resourceListString;
        }
        Iterator<Map<String,String>> resourceMaps = resourceMappings.iterator();

        int i = 0;
        while (resourceMaps.hasNext()) {
            Map<String,String> singleResMap = resourceMaps.next();
            if (singleResMap != null && singleResMap.get(KEY_FOR_RESOURCE_METHODS) != null &&
                    singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE) != null &&
                    singleResMap.get(KEY_FOR_RESOURCE_URI) != null &&
                    singleResMap.get(KEY_FOR_RESOURCE_SANDBOX_URI) != null) {
                String resourceTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_COMPLEX_RESOURCE);
                String endpoint = StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(
                        singleResMap.get(KEY_FOR_RESOURCE_URI)));
                String testEndpoint = StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(
                        singleResMap.get(KEY_FOR_RESOURCE_SANDBOX_URI)));
                String replacedStr = resourceTemplate.
                        replaceAll("\\[1\\]", singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE)).
                        replaceAll("\\[2\\]", singleResMap.get(KEY_FOR_RESOURCE_METHODS)).
                        replaceAll("\\[3\\]", endpoint).
                        replaceAll("\\[4\\]", apiMappings.get(KEY_FOR_API_NAME)).
                        replaceAll("\\[5\\]", String.valueOf(i)).
                        replaceAll("\\[6\\]", testEndpoint);
                resourceListString.add(replacedStr);
            } else if (singleResMap != null && singleResMap.get(KEY_FOR_RESOURCE_METHODS) != null &&
                    singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE) != null &&
                    singleResMap.get(KEY_FOR_RESOURCE_URI) != null) {
                String resourceTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_RESOURCE);
                String endpoint = StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(
                        singleResMap.get(KEY_FOR_RESOURCE_URI)));
                String replacedStr = resourceTemplate.
                        replaceAll("\\[1\\]", singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE)).
                        replaceAll("\\[2\\]", singleResMap.get(KEY_FOR_RESOURCE_METHODS)).
                        replaceAll("\\[3\\]", endpoint).
                        replaceAll("\\[4\\]", apiMappings.get(KEY_FOR_API_NAME)).
                        replaceAll("\\[5\\]", String.valueOf(i));
                resourceListString.add(replacedStr);
            } else {
                handleException("Required resource mapping not provided");
            }
            i++;
        }
        return resourceListString;
    }

    private List<String> constructHandlerConfig() throws APITemplateException {
        List<String> handlerListStr = new ArrayList<String>();
        if (handlerMappings == null) {
            return handlerListStr;
        }
        Iterator<Map<String,String>> handlerMaps = handlerMappings.iterator();

        String complexHandlerTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_COMPLEX_HANDLER);
        String simpleHandlerTemplate = templateLoader.getTemplate(TemplateLoader.TEMPLATE_TYPE_SIMPLE_HANDLER);

        while (handlerMaps.hasNext()) {
            Map<String,String> singleHandler = handlerMaps.next();
            if (singleHandler != null && singleHandler.containsKey(KEY_FOR_HANDLER) &&
                    singleHandler.containsKey(KEY_FOR_HANDLER_POLICY_KEY)) {
                String replacedStr = complexHandlerTemplate.
                        replaceAll("\\[1\\]", singleHandler.get(KEY_FOR_HANDLER)).
                        replaceAll("\\[2\\]", singleHandler.get(KEY_FOR_HANDLER_POLICY_KEY));
                handlerListStr.add(replacedStr);
            } else if (singleHandler != null && singleHandler.containsKey(KEY_FOR_HANDLER)) {
                String replacedStr = simpleHandlerTemplate.
                        replaceAll("\\[1\\]", singleHandler.get(KEY_FOR_HANDLER));
                handlerListStr.add(replacedStr);
            } else {
                handleException("Required handler mapping not provided");
            }
        }
        return handlerListStr;
    }

    public static OMElement createOMElementFrom(String omString) throws APITemplateException {
        try {
            return AXIOMUtil.stringToOM(omString);
        } catch (XMLStreamException e) {
            String msg = "Error converting string to OMElement - String: " + omString;
            log.error(msg, e);
            throw new APITemplateException(msg, e);
        }
    }

    private void handleException(String msg) throws APITemplateException {
        log.error(msg);
        throw new APITemplateException(msg);
    }
}
