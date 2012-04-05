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
package org.wso2.carbon.mashup.javascript.hostobjects.scraper;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.Variable;
import org.wso2.carbon.CarbonException;
import org.wso2.javascript.xmlimpl.XML;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamException;
import java.io.*;

public class ScraperHostObject extends ScriptableObject {

    private static Logger log = Logger.getLogger(ScraperHostObject.class);
    private OMElement config;
    private ScraperContext scraperContext;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {
        ScraperHostObject result = new ScraperHostObject();

        if (args.length == 1 && !(args[0] == Context.getUndefinedValue())) {
            OMElement configElement;
            if (args[0] instanceof XML) {
                XML xml = (XML) args[0];
                configElement = (OMElement) xml.getAxiomFromXML();
            } else if (args[0] instanceof File) {
                File configFile = (File) args[0];
                FileInputStream fis;
                try {
                    fis = new FileInputStream(configFile);
                } catch (FileNotFoundException e) {
                    throw new CarbonException(e);
                }
                StAXOMBuilder staxOMBuilder;
                try {
                    staxOMBuilder = new StAXOMBuilder(fis);
                } catch (XMLStreamException e) {
                    log.error("The configuration file should contain XML");
                    throw new CarbonException("The configuration file should contain XML");
                }
                configElement = staxOMBuilder.getDocumentElement();
            } else {
                log.error(
                        "Unsupported parameter type. The config should be XML or a File that has XML");
                throw new CarbonException(
                        "Unsupported parameter type. The config should be XML or a File that has XML");
            }
            result.setConfig(configElement);
            result.scrape();
            return result;
        }
        log.error("Invalid parameters. The configuration should be XML");
        throw new CarbonException("Invalid parameters. The configuration should be XML");
    }

    /**
     * This function does not get invoked explicitly. It is called by rhino internally.
     *
     * @return The name of this hostObject
     */
    public String getClassName() {
        return "Scraper";
    }

    private void scrape() throws CarbonException {
        //todo need to add proxy info
        InputStream in = new ByteArrayInputStream(config.toString().getBytes());
        InputSource inputSource = new InputSource(in);
        ScraperConfiguration scraperConfiguration = new ScraperConfiguration(inputSource);
        Scraper scraper = new Scraper(scraperConfiguration, "");
        // Execute the scraper config
        scraper.execute();
        scraperContext = scraper.getContext();
    }

    private void setConfig(OMElement config) {
        this.config = config;
    }

    /**
     * <p/>
     * <pre>
     * eg:
     *    var config =  <config>
     *                       <var-def name='response'>
     *                           <html-to-xml>
     *                               <http method='get' url='http://ww2.wso2.org/~builder/'/>
     *                           </html-to-xml>
     *                       </var-def>
     *                   </config>;
     *    var scraper = new Scraper(config);
     *    var response = scraper.response;
     * </pre>
     *
     * @param s          - The property requested for
     * @param scriptable - The Scriptable object that the property was requested from
     * @return String - The property if it exist
     */
    public Object get(String s, Scriptable scriptable) {
        String result = null;
        Variable var = scraperContext.getVar(s);
        if (var != null) {
            result = var.toString();
        }
        return result;
    }

    /**
     * This function does not get invoked explicitly. It is called by rhino internally when something like scraper.
     * response is attempted. Rhino uses this function to check whether the property is available.
     *
     * @param s          - The property requested for
     * @param scriptable - The Scriptable object that the property was requested from
     * @return boolean - Indicating whether the proty exist or not
     */
    public boolean has(String s, Scriptable scriptable) {
        Variable var = scraperContext.getVar(s);
        return var != null;
    }
}