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
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rest.api.ui.client.template.APITemplateException;

import java.io.InputStream;
import java.util.*;

/**
 * A utility class for loading API templates from the file system. Loaded templates will
 * be cached in memory for further reuse. If a specified template cannot be located, this
 * implementation will return an empty string.
 */
public class TemplateLoader {

    private static final Log log = LogFactory.getLog(TemplateLoader.class);
    
    public static final String TEMPLATE_TYPE_API = "api";
    public static final String TEMPLATE_TYPE_RESOURCE = "resource";
    public static final String TEMPLATE_TYPE_HANDLERS = "handlers";
    public static final String TEMPLATE_TYPE_HANDLER = "handler";
    
    private static final String TEMPLATE_FILE_PREFIX = "/api_templates_";
    
    private static final TemplateLoader instance = new TemplateLoader();
    
    private Map<String,String> templates = new HashMap<String, String>();
    
    private TemplateLoader() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing API template loader");
        }
    }
    
    public static TemplateLoader getInstance() {
        return instance;
    }
    
    public String getTemplate(String type) throws APITemplateException {
        String template = templates.get(type);
        if (template == null) {
            synchronized (this) {
                template = templates.get(type);
                if (template == null) {
                    template = loadTemplate(type);
                    templates.put(type, template);
                }
            }
        }
        return template;
    }

    private String loadTemplate(String type) throws APITemplateException {
        String fileName = TEMPLATE_FILE_PREFIX + type + ".xml";
        InputStream in = getClass().getResourceAsStream(fileName);        
        if (in != null) {
            OMXMLParserWrapper builder = null;
            try {
                builder = OMXMLBuilderFactory.createOMBuilder(in);
                OMElement documentEl = builder.getDocumentElement();
                if (documentEl != null) {
                    removeIndentations(documentEl);
                    return documentEl.toString();
                } else {
                    throw new APITemplateException("Error while parsing the template for type: " + type);
                }
            } finally {
                if (builder != null) {
                    builder.close();
                }
            }
        } else {
            throw new APITemplateException("Unable to locate a template for type: " + type);
        }
    }

    private void removeIndentations(OMElement element) {
        List<OMText> removables = new ArrayList<OMText>();
        removeIndentations(element, removables);
        for (OMText node : removables) {
            node.detach();
        }
    }

    private void removeIndentations(OMElement element, List<OMText> removables) {
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            Object next = children.next();
            if (next instanceof OMText) {
                OMText text = (OMText) next;
                if (text.getText().trim().equals("")) {
                    removables.add(text);
                }
            } else if (next instanceof OMElement) {
                removeIndentations((OMElement) next, removables);
            }
        }
    }
}
