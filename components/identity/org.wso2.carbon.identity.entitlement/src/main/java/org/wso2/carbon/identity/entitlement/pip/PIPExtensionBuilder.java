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
package org.wso2.carbon.identity.entitlement.pip;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinderModule;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

/**
 * Build PIP configuration from pip-config.xml. First this will try to find the configuration file
 * from [CARBON_HOME]\repository\conf - failing to do so will load the file from this bundle it
 * self.The default file ships with the bundle only includes
 * org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder as an AttributeDesignator.
 * <PIPConfig> 
 *  <AttributeDesignators> 
 *      <Designator class="" /> 
 *  </AttributeDesignators> 
 *  <Extensions>
 *      <Extension class="" /> 
 *  </Extensions>
 *  <ResourceFinders>
 *      <Finder class="" />
 *  </ResourceFinders>
 * </PIPConfig> 
 * Extensions will be fired for each and every
 * XACML request - which will give a handle to the incoming request. AttributeDesignators will be
 * fired by CarbonAttributeFinder whenever it finds an attribute supported by this module and
 * missing in the XACML request.
 */
public class PIPExtensionBuilder {    

    public static final String LOCAL_NAME_EXTENSIONS = "Extensions";
    public static final String LOCAL_NAME_EXTENSION = "Extension";
    public static final String LOCAL_NAME_EXTENSION_CLASS_ATTR = "class";

    public static final String LOCAL_NAME_ATTR_DESIGNATORS = "AttributeDesignators";
    public static final String LOCAL_NAME_ATTR_DESIGNATOR = "Designator";
    public static final String LOCAL_NAME_ATTR_CLASS_ATTR = "class";

    public static final String LOCAL_NAME_RESOURCE_FINDERS = "ResourceFinders";
    public static final String LOCAL_NAME_RESOURCE_FINDER = "Finder";
    public static final String LOCAL_NAME_RESOURCE_FINDER_CLASS = "class";

    public static final String LOCAL_NAME_META_DATA_FINDERS = "MetaDataFinders";
    public static final String LOCAL_NAME_META_DATA_FINDER = "Finder";
    public static final String LOCAL_NAME_META_DATA_FINDER_CLASS = "class";    

    public static final String LOCAL_NAME_PROPERTY = "Property";
    public static final String LOCAL_NAME_PROPERTY_NAME = "name";


    private static Log log = LogFactory.getLog(PIPExtensionBuilder.class);

    private static final String PIP_CONFIG = "pip-config.xml";

    private BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Builds the PIP configuration from pip-config.xml and loads that to PIPConfigHolder.
     * 
     * @param holder
     * @throws Exception
     */
    public void buildPIPConfig(PIPConfigHolder holder)  throws Exception {
        OMElement element = null;
        OMElement extensionsRoot = null;
        Iterator extIterator = null;
        OMElement designatorsRoot = null;
        Iterator designatorsIterator = null;
        OMElement resourceFinderRoot = null;
        Iterator resourceFinderIterator= null;
        OMElement metaDataFinderRoot = null;
        Iterator metaDataFinderIterator = null;

        try {
            element = getRootElement();
        } catch (IOException e) {
            String message = "Error while reading PIP configuration";
            log.error(message);
            throw new Exception(message, e);
        } catch (XMLStreamException e) {
            String message = "Error while reading PIP configuration";
            log.error(message);                  
            throw new Exception(message, e);
        }

        extensionsRoot = element.getFirstChildWithName(new QName(LOCAL_NAME_EXTENSIONS));

        if (extensionsRoot != null) {
            extIterator = extensionsRoot.getChildrenWithLocalName(LOCAL_NAME_EXTENSION);
            while (extIterator.hasNext()) {
                OMElement extension = null;
                String clazzName = null;
                extension = (OMElement) extIterator.next();
                clazzName = extension.getAttribute(new QName(LOCAL_NAME_EXTENSION_CLASS_ATTR))
                        .getAttributeValue();
                Class clazz = null;
                try {
                    clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                    holder.getExtensions().add((PIPExtension) clazz.newInstance());
                } catch (ClassNotFoundException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                } catch (InstantiationException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                } catch (IllegalAccessException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                }
            }
        }

        designatorsRoot = element.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_DESIGNATORS));

        if (designatorsRoot != null) {
            designatorsIterator = designatorsRoot
                    .getChildrenWithLocalName(LOCAL_NAME_ATTR_DESIGNATOR);
            while (designatorsIterator.hasNext()) {
                OMElement designator = null;
                String clazzName = null;
                PIPAttributeFinder attributeFinder = null;
                designator = (OMElement) designatorsIterator.next();
                clazzName = designator.getAttribute(new QName(LOCAL_NAME_ATTR_CLASS_ATTR))
                        .getAttributeValue();

                Properties properties = new Properties();
                Iterator designatorProperties = designator.getChildrenWithLocalName(LOCAL_NAME_PROPERTY);
                while(designatorProperties.hasNext()){
                    OMElement designatorProperty = (OMElement) designatorProperties.next();
                    String propertyName = designatorProperty.
                            getAttributeValue(new QName(LOCAL_NAME_PROPERTY_NAME));
                    String propertyValue = designatorProperty.getText();
                    if(propertyName != null && propertyValue != null){
                        properties.put(propertyName, propertyValue);
                    }
                }

                try{
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                    attributeFinder = (PIPAttributeFinder) clazz.newInstance();
                    attributeFinder.init(properties);
                } catch (ClassNotFoundException e) {
                    log.error("Error while initializing attribute designator : " + clazzName);
                } catch (InstantiationException e) {
                    log.error("Error while initializing attribute designator : " + clazzName);
                } catch (IllegalAccessException e) {
                    log.error("Error while initializing attribute designator : " + clazzName);
                } catch (Exception e) {
                    log.error("Error while initializing attribute designator : " + clazzName);
                }

                if(attributeFinder != null){
                    holder.getDesignators().add(attributeFinder);
                }
            }
        }

        resourceFinderRoot = element.getFirstChildWithName(new QName(LOCAL_NAME_RESOURCE_FINDERS));

        if (resourceFinderRoot != null) {
            resourceFinderIterator = resourceFinderRoot
                    .getChildrenWithLocalName(LOCAL_NAME_RESOURCE_FINDER);
            while (resourceFinderIterator.hasNext()) {
                OMElement finder = null;
                String clazzName = null;
                PIPResourceFinder resourceFinder = null;
                finder = (OMElement) resourceFinderIterator.next();
                clazzName = finder.getAttribute(new QName(LOCAL_NAME_RESOURCE_FINDER_CLASS))
                        .getAttributeValue();

                Properties properties = new Properties();
                Iterator finderProperties = finder.getChildrenWithLocalName(LOCAL_NAME_PROPERTY);
                while(finderProperties.hasNext()){
                    OMElement finderProperty = (OMElement) finderProperties.next();
                    String propertyName = finderProperty.
                            getAttributeValue(new QName(LOCAL_NAME_PROPERTY_NAME));
                    String propertyValue = finderProperty.getText();
                    if(propertyName != null && propertyValue != null){
                        properties.put(propertyName, propertyValue);
                    }
                }

                try{
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                    resourceFinder = (PIPResourceFinder) clazz.newInstance();
                    resourceFinder.init(properties);
                } catch (ClassNotFoundException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (InstantiationException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (IllegalAccessException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (Exception e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                }

                if(resourceFinder != null){
                    holder.getResourceFinders().add(resourceFinder);
                }
            }
        }

        metaDataFinderRoot = element.getFirstChildWithName(new QName(LOCAL_NAME_META_DATA_FINDERS));

        if (metaDataFinderRoot != null) {
            metaDataFinderIterator = metaDataFinderRoot
                    .getChildrenWithLocalName(LOCAL_NAME_META_DATA_FINDER);
            while (metaDataFinderIterator.hasNext()) {
                OMElement finder = null;
                String clazzName = null;
                PolicyMetaDataFinderModule metaDataFinderModule = null;
                finder = (OMElement) metaDataFinderIterator.next();
                clazzName = finder.getAttribute(new QName(LOCAL_NAME_META_DATA_FINDER_CLASS))
                        .getAttributeValue();

                Properties properties = new Properties();
                Iterator finderProperties = finder.getChildrenWithLocalName(LOCAL_NAME_PROPERTY);
                while(finderProperties.hasNext()){
                    OMElement finderProperty = (OMElement) finderProperties.next();
                    String propertyName = finderProperty.
                            getAttributeValue(new QName(LOCAL_NAME_PROPERTY_NAME));
                    String propertyValue = finderProperty.getText();
                    if(propertyName != null && propertyValue != null){
                        properties.put(propertyName, propertyValue);
                    }
                }

                try{
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                    metaDataFinderModule = (PolicyMetaDataFinderModule) clazz.newInstance();
                    metaDataFinderModule.init(properties);
                } catch (ClassNotFoundException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (InstantiationException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (IllegalAccessException e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                } catch (Exception e) {
                    log.error("Error while initializing resource designator : " + clazzName);
                }

                if(metaDataFinderModule != null){
                    holder.getPolicyMetaDataFinderModules().add(metaDataFinderModule);
                }
            }
        }
    }

    /**
     * 
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    private OMElement getRootElement() throws XMLStreamException, IOException {
        StAXOMBuilder builder = null;
        InputStream inStream = null;
        String warningMessage = "";
        try {

            File pipConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(), PIP_CONFIG);

            if (pipConfigXml.exists()) {
                inStream = new FileInputStream(pipConfigXml);
            } else {
                URL url;
                if (bundleContext != null) {
                    if ((url = bundleContext.getBundle().getResource(PIP_CONFIG)) != null) {
                        inStream = url.openStream();
                    } else {
                        warningMessage = "Bundle context could not find resource "
                                + PIP_CONFIG
                                + " or user does not have sufficient permission to access the resource.";
                    }

                } else {

                    if ((url = this.getClass().getClassLoader().getResource(PIP_CONFIG)) != null) {
                        inStream = url.openStream();
                    } else {
                        warningMessage = "PIP Config Builder could not find resource "
                                + PIP_CONFIG
                                + " or user does not have sufficient permission to access the resource.";
                    }
                }
            }

            if (inStream == null) {
                String message = "PIP configuration not found. Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            return builder.getDocumentElement();
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }

    }
}