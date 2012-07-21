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

package org.wso2.carbon.identity.entitlement.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinderModule;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

/**
 * Build Entitlement configuration from entitlement-config.xml. First this will try to find the configuration file
 * from [CARBON_HOME]\repository\conf - failing to do so will load the file from this bundle it
 * self.The default file ships with the bundle only includes
 * org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder as an AttributeDesignator and
 * default caching configurations
 * <EntitlementConfig>
 *  <CachingConfig>
 *  </CachingConfig>
 *  <PIPConfig>
 *      <AttributeDesignators>
 *          <Designator class="" />
 *      </AttributeDesignators>
 *      <Extensions>
 *          <Extension class="" />
 *      </Extensions>
 *      <ResourceFinders>
 *          <Finder class="" />
 *      </ResourceFinders>
 *  </PIPConfig>
 *  <PAPConfig>
 *      <MetaDataFinders>
 *          <Finder class="" />
 *      </MetaDataFinders>
 *  </PAPConfig>
 * </EntitlementConfig>
 */
public class EntitlementExtensionBuilder {

    public static final String LOCAL_NAME_PIP_CONFIG = "PIPConfig";
    public static final String LOCAL_NAME_CACHING_CONFIG = "CachingConfig";
    public static final String LOCAL_NAME_PAP_CONFIG = "PAPConfig";

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

    public static final String LOCAL_NAME_ATTRIBUTE_CACHING = "AttributeCaching";
    public static final String LOCAL_NAME_RESOURCE_CACHING = "ResourceCaching";
    public static final String LOCAL_NAME_DECISION_CACHING = "DecisionCaching";
    public static final String LOCAL_NAME_ON_DEMAND_LOADING= "OnDemandPolicyLoading";
    public static final String LOCAL_NAME_MAX_POLICIES = "MaxInMemoryPolicies";
    public static final String LOCAL_NAME_CACHING_INTERVAL = "CachingInterval";
    public static final String LOCAL_NAME_ENABLE = "Enable";

    private static final String PIP_CONFIG = "entitlement-config.xml";

    private static Log log = LogFactory.getLog(EntitlementExtensionBuilder.class);

    private BundleContext bundleContext;

    private OMElement rootElement;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Builds the PIP configuration from entitlement-config.xml and loads that to EntitlementConfigHolder.
     *
     * @param holder EntitlementConfigHolder
     * @throws Exception throws, if fails
     */
    public void buildPIPConfig(EntitlementConfigHolder holder) throws Exception {

        OMElement pipConfigElement= null;
        OMElement extensionsRoot = null;
        Iterator extIterator = null;
        OMElement designatorsRoot = null;
        Iterator designatorsIterator = null;
        OMElement resourceFinderRoot = null;
        Iterator resourceFinderIterator= null;

        pipConfigElement = getPIPConfigElement();
        if(pipConfigElement == null){
            return;
        }
        
        extensionsRoot = pipConfigElement.getFirstChildWithName(new QName(LOCAL_NAME_EXTENSIONS));

        if (extensionsRoot != null) {
            extIterator = extensionsRoot.getChildrenWithLocalName(LOCAL_NAME_EXTENSION);
            while (extIterator.hasNext()) {
                OMElement extension = null;
                String clazzName = null;
                PIPExtension pipExtension = null;
                extension = (OMElement) extIterator.next();
                clazzName = extension.getAttribute(new QName(LOCAL_NAME_EXTENSION_CLASS_ATTR))
                        .getAttributeValue();

                Properties properties = new Properties();
                Iterator designatorProperties = extension.getChildrenWithLocalName(LOCAL_NAME_PROPERTY);
                while(designatorProperties.hasNext()){
                    OMElement designatorProperty = (OMElement) designatorProperties.next();
                    String propertyName = designatorProperty.
                            getAttributeValue(new QName(LOCAL_NAME_PROPERTY_NAME));
                    String propertyValue = designatorProperty.getText();
                    if(propertyName != null && propertyValue != null){
                        properties.put(propertyName, propertyValue);
                    }
                }
                try {
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                    pipExtension = (PIPExtension) clazz.newInstance();
                    pipExtension.init(properties);
                } catch (ClassNotFoundException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                } catch (InstantiationException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                } catch (IllegalAccessException e) {
                    log.error("Error while initializing pip extension : " + clazzName);
                }

                if(pipExtension != null){
                    holder.addExtensions(pipExtension, properties);
                }
            }
        }

        designatorsRoot = pipConfigElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTR_DESIGNATORS));

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
                    holder.addDesignators(attributeFinder, properties);
                }
            }
        }

        resourceFinderRoot = pipConfigElement.getFirstChildWithName(new QName(LOCAL_NAME_RESOURCE_FINDERS));

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
                    holder.addResourceFinders(resourceFinder, properties);
                }
            }
        }

    }


    /**
     * Builds the PAP configuration from entitlement-config.xml and loads that to EntitlementConfigHolder.
     * @param holder EntitlementConfigHolder
     * @throws Exception throws, if fails
     */
    public void buildPAPConfig(EntitlementConfigHolder holder) throws Exception {

        OMElement metaDataFinderRoot = null;
        Iterator metaDataFinderIterator = null;
        
        OMElement papElement = getPAPConfigElement();
        if(papElement == null){
            return;
        }

        metaDataFinderRoot = papElement.getFirstChildWithName(new QName(LOCAL_NAME_META_DATA_FINDERS));

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
                    holder.addPolicyMetaDataFinderModules(metaDataFinderModule, properties);
                }
            }
        }
    }

    /**
     * Builds the caching configuration from entitlement-config.xml and loads that to EntitlementConfigHolder.
     * @param holder EntitlementConfigHolder
     * @throws Exception throws, if fails
     */
    public void buildCachingConfig(EntitlementConfigHolder holder) throws Exception {
        
        Properties properties = new Properties();
        OMElement cachingElement = getCachingConfigElement();
        if(cachingElement == null){
            return;
        }

        OMElement attributeCaching = cachingElement.getFirstChildWithName(new QName(LOCAL_NAME_ATTRIBUTE_CACHING));
        if(attributeCaching != null){
            OMElement enable = attributeCaching.getFirstChildWithName(new QName(LOCAL_NAME_ENABLE));
            if(enable !=  null && enable.getText() != null){
                properties.setProperty(LOCAL_NAME_ATTRIBUTE_CACHING + "." + LOCAL_NAME_ENABLE,
                                       enable.getText().trim());
            }
        }


        OMElement resourceCaching = cachingElement.getFirstChildWithName(new QName(LOCAL_NAME_RESOURCE_CACHING));
        if(resourceCaching != null){
            OMElement enable = resourceCaching.getFirstChildWithName(new QName(LOCAL_NAME_ENABLE));
            if(enable !=  null && enable.getText() != null){
                properties.setProperty(LOCAL_NAME_RESOURCE_CACHING + "." + LOCAL_NAME_ENABLE,
                                       enable.getText().trim());
            }
        }

        OMElement decisionCaching = cachingElement.getFirstChildWithName(new QName(LOCAL_NAME_DECISION_CACHING));
        if(decisionCaching != null){
            OMElement enable = decisionCaching.getFirstChildWithName(new QName(LOCAL_NAME_ENABLE));
            if(enable !=  null && enable.getText() != null){
                properties.setProperty(LOCAL_NAME_DECISION_CACHING + "." + LOCAL_NAME_ENABLE,
                                       enable.getText().trim());
            }
            OMElement cachingInterval = decisionCaching.getFirstChildWithName(new QName(LOCAL_NAME_CACHING_INTERVAL));
            if(cachingInterval != null && cachingInterval.getText() != null){
                properties.setProperty(LOCAL_NAME_DECISION_CACHING + "." + LOCAL_NAME_CACHING_INTERVAL,
                                       cachingInterval.getText().trim());                
            }
        }

        OMElement onDemandLoading = cachingElement.getFirstChildWithName(new QName(LOCAL_NAME_ON_DEMAND_LOADING));
        if(onDemandLoading != null){
            OMElement enable = onDemandLoading.getFirstChildWithName(new QName(LOCAL_NAME_ENABLE));
            if(enable !=  null && enable.getText() != null){
                properties.setProperty(LOCAL_NAME_ON_DEMAND_LOADING + "." + LOCAL_NAME_ENABLE,
                                       enable.getText().trim());
            }
            OMElement maxPolicies = onDemandLoading.getFirstChildWithName(new QName(LOCAL_NAME_MAX_POLICIES));
            if(maxPolicies != null && maxPolicies.getText() != null){
                properties.setProperty(LOCAL_NAME_ON_DEMAND_LOADING + "." + LOCAL_NAME_MAX_POLICIES,
                                       maxPolicies.getText().trim());
            }
        }

        holder.setCachingProperties(properties);
    }

    /**
     * Gets PIP config element of the entitlement configurations
     * @return PIP config Element as OMElement
     * @throws Exception throws, if fails
     */
    private OMElement getPIPConfigElement() throws Exception {

        if(rootElement == null){
            rootElement = getRootElement();
        }
        if(rootElement != null){
            return rootElement.getFirstChildWithName(new QName(LOCAL_NAME_PIP_CONFIG));
        }
        return null;
    }

    /**
     * Gets PAP config element of the entitlement configurations
     * @return PAP config Element as OMElement
     * @throws Exception throws, if fails
     */
    private OMElement getPAPConfigElement() throws Exception {

        if(rootElement == null){
            rootElement = getRootElement();            
        }
        if(rootElement != null){
            return rootElement.getFirstChildWithName(new QName(LOCAL_NAME_PAP_CONFIG));
        }
        return null;
    }

    /**
     * Gets caching config element of the entitlement configurations
     * @return caching config Element as OMElement
     * @throws Exception throws, if fails
     */
    private OMElement getCachingConfigElement() throws Exception {

        if(rootElement == null){
            rootElement = getRootElement();
        }
        if(rootElement != null){
            return rootElement.getFirstChildWithName(new QName(LOCAL_NAME_CACHING_CONFIG));
        }
        return null;
    }

    /**
     * Builds the policy schema map. There are three schema s
     *
     * @param configHolder  holder EntitlementConfigHolder
     * @throws SAXException if fails
     */
    public void buildPolicySchema(EntitlementConfigHolder configHolder) throws SAXException {

        String[] schemaNSs = new String[]{EntitlementConstants.XACML_1_POLICY_XMLNS,
            EntitlementConstants.XACML_2_POLICY_XMLNS, EntitlementConstants.XACML_3_POLICY_XMLNS};

        for(String schemaNS : schemaNSs){

            String schemaFile;

            if(EntitlementConstants.XACML_1_POLICY_XMLNS.equals(schemaNS)){
                schemaFile = EntitlementConstants.XACML_1_POLICY_SCHEMA_FILE;
            } else if(EntitlementConstants.XACML_2_POLICY_XMLNS.equals(schemaNS)){
                schemaFile = EntitlementConstants.XACML_2_POLICY_SCHEMA_FILE;
            } else {
                schemaFile = EntitlementConstants.XACML_3_POLICY_SCHEMA_FILE;
            }

            InputStream schemaFileStream = EntitlementExtensionBuilder.class.
                                                getResourceAsStream("/"+ schemaFile);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(schemaFileStream));
            configHolder.getPolicySchemaMap().put(schemaNS, schema);
        }
    }


    /**
     * Gets root element of the entitlement configurations
     * @return root Element as OMElement
     * @throws Exception throws, if fails
     */
    private OMElement getRootElement() throws Exception {
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
                String message = "Entitlement configuration not found. Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            String message = "Error while reading Entitlement configuration";
            log.error(message);
            throw new Exception(message, e);
        } catch (XMLStreamException e) {
            String message = "Error while reading Entitlement configuration";
            log.error(message);
            throw new Exception(message, e);
        } catch (IOException e) {
            String message = "Error while reading Entitlement configuration";
            log.error(message);
            throw new Exception(message, e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Error occurs while closing in-stream for Entitlement Config");
                }
            }
        }

    }

}
