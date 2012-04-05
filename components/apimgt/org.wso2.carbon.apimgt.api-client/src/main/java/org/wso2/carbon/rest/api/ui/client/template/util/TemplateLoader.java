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
package org.wso2.carbon.rest.api.ui.client.template.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.InputStream;

public class TemplateLoader {
    static Log log = org.apache.commons.logging.LogFactory.getLog(TemplateLoader.class);

    public static String API_RESOURCE_TEMPLATE;
    public static String API_HANDLERS_TEMPLATE;
    public static String API_HANDLER_TEMPLATE;
    public static String API_API_TEMPLATE;

    static {
        try {
            API_API_TEMPLATE = TemplateLoader.getTemplate(TemplateLoader.class.getResourceAsStream("/api_templates_api_with_versioning.xml"));
            API_HANDLER_TEMPLATE = TemplateLoader.getTemplate(TemplateLoader.class.getResourceAsStream("/api_templates_handler.xml"));
            API_HANDLERS_TEMPLATE = TemplateLoader.getTemplate(TemplateLoader.class.getResourceAsStream("/api_templates_handlers.xml"));
            API_RESOURCE_TEMPLATE = TemplateLoader.getTemplate(TemplateLoader.class.getResourceAsStream("/api_templates_resource_new.xml"));
//            API_RESOURCE_TEMPLATE = TemplateLoader.getTemplate(TemplateLoader.class.getResourceAsStream("/api_templates_resource.xml"));

        } catch (IOException e) {
            log.error("Error while reading from resource file file \n" + e.getMessage());
        }

    }

    public static String getTemplate(InputStream in) throws IOException {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);
        OMElement documentEl = builder.getDocumentElement();
        if (documentEl != null) {
            return documentEl.toString();
        }
        builder.close();
        return "";
    }

    public static void main(String[] args) {
        System.out.println(API_API_TEMPLATE);
        System.out.println(API_RESOURCE_TEMPLATE);
        System.out.println(API_HANDLERS_TEMPLATE);
        System.out.println(API_HANDLER_TEMPLATE);
    }
}
