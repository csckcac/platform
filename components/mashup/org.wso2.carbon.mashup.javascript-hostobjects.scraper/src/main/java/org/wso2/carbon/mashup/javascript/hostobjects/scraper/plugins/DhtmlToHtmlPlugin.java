/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.scraper.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webharvest.definition.HttpDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.w3c.dom.Document;
import org.webharvest.runtime.web.HttpInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DhtmlToHtmlPlugin extends WebHarvestPlugin {

    private static final Log log = LogFactory.getLog(DhtmlToHtmlPlugin.class);

    private final String EXCEPTION = "Cannot find cobra library in the class path. Please make " +
                                     "sure its in the classpath or remove <dhtml-to-html/> from " +
                                     "the Scraper config";

    @Override
    public String getName() {
        return "dhtml-to-html";
    }

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context) {

        IElementDef[] defs = elementDef.getOperationDefs();
        ListVariable result = new ListVariable();

        String transformError = "Error Transforming document";

        if (defs.length > 0) {

            try {
                Class userAgentInterfaceClass = Class.forName("org.lobobrowser.html.UserAgentContext");
                Class userAgentContextClass = Class.forName("org.lobobrowser.html.test.SimpleUserAgentContext");
                Object userAgentContext = userAgentContextClass.newInstance();

                Class documentBuilderClass = Class.forName("org.lobobrowser.html.parser.DocumentBuilderImpl");
                Constructor documentBuilderConstructor = documentBuilderClass.getConstructor(new Class[]{userAgentInterfaceClass});
                Object documentBuilder = documentBuilderConstructor.newInstance(new Object[]{userAgentContext});

                Class inputSourceClass = Class.forName("org.lobobrowser.html.parser.InputSourceImpl");

                Constructor inputSourceConstructor = inputSourceClass.getConstructor(
                        new Class[]{InputStream.class, String.class, String.class});
                Method documentBuilderParse = documentBuilderClass.getMethod("parse", InputSource.class);

                for (int i = 0; i < defs.length; i++) {
                    HttpProcessor processor = (HttpProcessor) ProcessorResolver.createProcessor(
                            defs[i], scraper.getConfiguration(), scraper);
                    String documentURI = ((HttpDef) processor.getElementDef()).getUrl();
                    HttpInfo httpInfo = (HttpInfo) context.get("http");
                    Variable content = processor.run(scraper, context);

                    try {
                        // A document URI and a charset should be provided.
                        Object inputSource = inputSourceConstructor.newInstance(
                                new Object[]{new ByteArrayInputStream(content.toBinary()), documentURI, httpInfo.charset});
                        Document document = (Document) documentBuilderParse.invoke(documentBuilder, inputSource);
                        Source source = new DOMSource(document);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Result domResult = new StreamResult(out);
                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();
                        transformer.transform(source, domResult);
                        result.addVariable(new NodeVariable(out.toByteArray()));
                    } catch (TransformerException e) {
                        log.error(transformError, e);
                        throw new PluginException(e);
                    } catch (IllegalAccessException e) {
                        throw new PluginException(e);
                    } catch (InstantiationException e) {
                        throw new PluginException(e);
                    } catch (Exception e) {
                        if (e instanceof SAXException) {
                            String msg = "Error parsing content retrieved from the url" + documentURI;
                            log.error(msg, e);
                            throw new PluginException(msg, e);
                        } else if(e instanceof IOException) {
                            String msg = "Error retrieving content from the url" + documentURI;
                            log.error(msg, e);
                            throw new PluginException(msg, e);
                        } else if(e instanceof ClassNotFoundException) {
                            log.error(EXCEPTION, e);
                            throw new PluginException(EXCEPTION, e);
                        } else if(e instanceof InvocationTargetException) {
                            log.error(EXCEPTION, e);
                            throw new PluginException(EXCEPTION, e);
                        } else if(e instanceof IllegalAccessException) {
                            log.error(EXCEPTION, e);
                            throw new PluginException(EXCEPTION, e);
                        } else {
                            String msg = "Error occurred with the content of " + documentURI;
                            log.error(msg, e);
                            throw new PluginException(msg, e);
                        }
                    }
                }

            } catch (ClassNotFoundException e) {
                log.error(EXCEPTION, e);
                throw new PluginException(EXCEPTION, e);
            } catch (InstantiationException e) {
                log.error(EXCEPTION, e);
                throw new PluginException(EXCEPTION, e);
            } catch (IllegalAccessException e) {
                log.error(EXCEPTION, e);
                throw new PluginException(EXCEPTION, e);
            } catch (NoSuchMethodException e) {
                log.error(EXCEPTION, e);
                throw new PluginException(EXCEPTION, e);
            } catch (InvocationTargetException e) {
                log.error(EXCEPTION, e);
                throw new PluginException(EXCEPTION, e);
            }

        } else {
            result.addVariable(new NodeVariable(elementDef.getBodyText()));
        }
        return result;
    }

    public String[] getValidAttributes() {
        return new String[] {"scripting", "externalCSS"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("scripting".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        } else if ("externalCSS".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public Class[] getDependantProcessors() {
        return null;
    }
}
