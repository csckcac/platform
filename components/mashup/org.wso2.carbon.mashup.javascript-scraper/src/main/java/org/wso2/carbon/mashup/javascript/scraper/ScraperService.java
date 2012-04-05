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
package org.wso2.carbon.mashup.javascript.scraper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;


public class ScraperService {

    private static final Log log = LogFactory.getLog(ScraperService.class);

    /**
     * Reads the contents of a given URL and returns it
     *
     * @param url A String representing the URL to read
     * @return The contents found at the given URL
     */
    public OMElement getUrl(String url, String renderDHTML, String userAgent) throws AxisFault {
        String userAgentString =
                "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20060601 Firefox/2.0.0.6 (Ubuntu-edgy)";

        String cobraLibException = "Cannot find cobra library in the class path. Please make " +
                                   "sure its in the classpath or use basicHTML instead of " +
                                   "rendering DHTML";

        if (url != null) {

            if (userAgent != null) {
                userAgentString = userAgent;
            }
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            OMElement responseElement = omFactory.createOMElement("response", null);

            try {

                String pageContent = "";

                if ("true".equals(renderDHTML)) {
                    try {
                        Class userAgentInterfaceClass = Class.forName("org.lobobrowser.html.UserAgentContext");
                        Class userAgentContextClass = Class.forName("org.lobobrowser.html.test.SimpleUserAgentContext");
                        Method userAgentContextSetAgent = userAgentContextClass.getMethod("setUserAgent", String.class);

                        Object userAgentContext = userAgentContextClass.newInstance();                       
                        userAgentContextSetAgent.invoke(userAgentContext, userAgentString);

                        Class documentBuilderClass = Class.forName("org.lobobrowser.html.parser.DocumentBuilderImpl");
                        Constructor documentBuilderConstructor = documentBuilderClass.getConstructor(new Class[]{userAgentInterfaceClass});
                        Object documentBuilder = documentBuilderConstructor.newInstance(new Object[]{userAgentContext});

                        Class inputSourceClass = Class.forName("org.lobobrowser.html.parser.InputSourceImpl");

                        Constructor inputSourceConstructor = inputSourceClass.getConstructor(
                                new Class[]{String.class});
                        Method documentBuilderParse = documentBuilderClass.getMethod("parse", InputSource.class);

                        Object inputSource = inputSourceConstructor.newInstance(
                                new Object[]{url});
                        Document document = (Document) documentBuilderParse.invoke(documentBuilder, inputSource);

                        StringWriter sw = new StringWriter();

                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();
                        transformer.transform(new DOMSource(document), new StreamResult(sw));

                        pageContent = sw.toString();

                    } catch (ClassNotFoundException e) {
                        log.error(cobraLibException, e);
                        throw new AxisFault(cobraLibException, e);
                    } catch (InstantiationException e) {
                        log.error(cobraLibException, e);
                        throw new AxisFault(cobraLibException, e);
                    } catch (IllegalAccessException e) {
                        log.error(cobraLibException, e);
                        throw new AxisFault(cobraLibException, e);
                    } catch (NoSuchMethodException e) {
                        log.error(cobraLibException, e);
                        throw new AxisFault(cobraLibException, e);
                    } catch (InvocationTargetException e) {
                        log.error(cobraLibException, e);
                        throw new AxisFault(cobraLibException, e);
                    } catch (TransformerException e) {
                        String msg = "Error parsing content retrieved from the URL " + url;
                        log.error(msg, e);
                        throw new AxisFault(msg, e);
                    }

                } else {
                    //Reading the contents
                    URL urlToRead = new URL(url);
                    URLConnection connection = urlToRead.openConnection();
                    connection.addRequestProperty("User-Agent", userAgentString);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder stringBuilder = new StringBuilder(pageContent);
                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                    pageContent = stringBuilder.toString();
                    in.close();
                }
                //Adding page contents to the response after wrapping with CDATA
                responseElement
                        .addChild(omFactory.createOMText(pageContent, XMLStreamConstants.CDATA));

                return responseElement;

            } catch (IOException e) {
                String msg = "Error Occurred while retrieving content from the URL " + url;
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }

        return null;
    }
}