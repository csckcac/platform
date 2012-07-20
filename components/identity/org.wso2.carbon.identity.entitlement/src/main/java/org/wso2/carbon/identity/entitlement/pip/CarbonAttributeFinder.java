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

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.wso2.balana.attr.AttributeDesignator;
import org.wso2.balana.ctx.Status;
import net.sf.jsr107cache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ParsingException;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.finder.AttributeFinderModule;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * CarbonAttributeFinder registers with sun-xacml engine as an AttributeFinderModule and delegate
 * functionality to the attribute handlers registered with it self.
 * 
 * Whenever the XACML engine finds a missing attribute in the XACML request - it will call the
 * findAttribute() method of this class.
 * 
 */
public class CarbonAttributeFinder extends AttributeFinderModule {

	private Map<String, List<PIPAttributeFinder>> attrFinders = new HashMap<String, List<PIPAttributeFinder>>();
	private static Log log = LogFactory.getLog(CarbonAttributeFinder.class);
	private Cache attributeFinderCache = null;
	private boolean isAttributeCachingEnabled = false;
	private int tenantId;

	public CarbonAttributeFinder(int tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Registers PIP attribute handlers with the PDP against their supported attributes. This PIP
	 * attribute handlers are picked from pip-config.xml file - which should be inside
	 * [CARBON_HOME]\repository\conf.
	 */
	public void init() {
		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getCachingProperties();  
		if ("true".equals(properties.getProperty(EntitlementConstants.ATTRIBUTE_CACHING))) {
			attributeFinderCache = EntitlementUtil
					.getCommonCache(EntitlementConstants.PIP_ATTRIBUTE_CACHE);
			isAttributeCachingEnabled = true;
		}

        if(designators != null && !designators.isEmpty()){
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (Iterator iterator = pipAttributeFinders.iterator(); iterator.hasNext();) {
                PIPAttributeFinder pipAttributeFinder = (PIPAttributeFinder) iterator.next();
                Set<String> attrs = pipAttributeFinder.getSupportedAttributes();
                if (attrs != null) {
                    for (Iterator attrsIter = attrs.iterator(); attrsIter.hasNext();) {
                        String attr = (String) attrsIter.next();
                        if (attrFinders.containsKey(attr)) {
                            List<PIPAttributeFinder> finders = attrFinders.get(attr);
                            if (!finders.contains(pipAttributeFinder)) {
                                finders.add(pipAttributeFinder);
                                if (log.isDebugEnabled()) {
                                    log.debug(String
                                            .format("PIP attribute handler %1$s registered for the supported attribute %2$s",
                                                    pipAttributeFinder.getClass(), attr));
                                }
                            }
                        } else {
                            List<PIPAttributeFinder> finders = new ArrayList<PIPAttributeFinder>();
                            finders.add(pipAttributeFinder);
                            attrFinders.put(attr, finders);
                            if (log.isDebugEnabled()) {
                                log.debug(String
                                        .format("PIP attribute handler %1$s registered for the supported attribute %2$s",
                                                pipAttributeFinder.getClass(), attr));
                            }
                        }
                    }
                }
            }
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.balana.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI,
	 * java.net.URI, java.net.URI, org.wso2.balana.EvaluationCtx, int)
	 */
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
			URI category, EvaluationCtx context) {

		List<AttributeValue> attrBag = new ArrayList<AttributeValue>();
		// Get the list of attribute finders who are registered with this particular attribute.
		List<PIPAttributeFinder> finders = attrFinders.get(attributeId.toString());


		if (finders == null || finders.size() == 0) {
			try {
				refreshAttributeFindersForNewAttributeId();
			} catch (Exception e) {
				log.warn("Error while refreshing attribute finders");
			}
			finders = attrFinders.get(attributeId.toString());
			if (finders == null || finders.size() == 0) {
				log.info("No attribute designators defined for the attribute "
						+ attributeId.toString());
				ArrayList<String> code = new ArrayList<String>();
				code.add(Status.STATUS_MISSING_ATTRIBUTE);
				Status status = new Status(code,
						"No attribute designators defined for the attribute "
								+ attributeId.toString());
				return new EvaluationResult(status);
			}
		}

		try {

			for (Iterator iterator = finders.iterator(); iterator.hasNext();) {
				PIPAttributeFinder pipAttributeFinder = (PIPAttributeFinder) iterator.next();
				if (log.isDebugEnabled()) {
					log.debug(String.format(
							"Finding attributes with the PIP attribute handler %1$s",
							pipAttributeFinder.getClass()));
				}

				Set<String> attrs = null;
				IdentityCacheKey cacheKey = null;

				if (isAttributeCachingEnabled && !pipAttributeFinder.overrideDefaultCache()) {

                    String key = attributeType.toString() + attributeId.toString() + category.toString() +
                                 domToString(context.getRequestRoot());

                    if(issuer != null){
                        key += issuer;
                    }

					if (key != null) {
						cacheKey = new IdentityCacheKey(tenantId, key);
						IdentityCacheEntry entry = (IdentityCacheEntry) attributeFinderCache
								.get(cacheKey);
						if (entry != null) {
                            String[] values= entry.getCacheEntryArray();
                            attrs = new HashSet<String>(Arrays.asList(values));
							if (log.isDebugEnabled()) {
								log.debug("Carbon Attribute Cache Hit");
							}
						}
					}
				}

				if (attrs == null) {
					if (log.isDebugEnabled()) {
						log.debug("Carbon Attribute Cache Miss");
					}
					attrs = pipAttributeFinder.getAttributeValues(attributeType, attributeId, category,
                                                                    issuer, context);
					if (isAttributeCachingEnabled && cacheKey != null
							&& !pipAttributeFinder.overrideDefaultCache()) {
						IdentityCacheEntry cacheEntry = new IdentityCacheEntry(attrs.toArray(new String[attrs.size()]));
						attributeFinderCache.put(cacheKey, cacheEntry);
					}
				}

				if (attrs != null) {
					for (Iterator iterAttr = attrs.iterator(); iterAttr.hasNext();) {
						final String attr = (String) iterAttr.next();
						AttributeValue attribute = EntitlementUtil.
                                getAttributeValue(attr, attributeType.toString());
						attrBag.add(attribute);
					}
				}
			}
		} catch (ParsingException e) {
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
			ArrayList<String> code = new ArrayList<String>();
			code.add(Status.STATUS_MISSING_ATTRIBUTE);
			Status status = new Status(code,
					"Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
			return new EvaluationResult(status);
		} catch (ParseException e) {
                        e.printStackTrace();
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
			ArrayList<String> code = new ArrayList<String>();
			code.add(Status.STATUS_MISSING_ATTRIBUTE);
			Status status = new Status(code,
					"Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
			return new EvaluationResult(status);
		} catch (URISyntaxException e) {
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
			ArrayList<String> code = new ArrayList<String>();
			code.add(Status.STATUS_MISSING_ATTRIBUTE);
			Status status = new Status(code,
					"Error while parsing attribute values from EvaluationCtx :" + e.getMessage());
			return new EvaluationResult(status);
		} catch (Exception e) {
			log.error("Error while retrieving attribute values from PIP  attribute finder : " + e);
			ArrayList<String> code = new ArrayList<String>();
			code.add(Status.STATUS_MISSING_ATTRIBUTE);
			Status status = new Status(code, "Error while retrieving attribute values from PIP"
					+ " attribute finder : " + e.getMessage());
			return new EvaluationResult(status);
		}
		return new EvaluationResult(new BagAttribute(attributeType, attrBag));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.balana.finder.AttributeFinderModule#isDesignatorSupported()
	 */
	public boolean isDesignatorSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.balana.finder.AttributeFinderModule#getSupportedDesignatorTypes()
	 */
	public Set getSupportedDesignatorTypes() {
		HashSet<Integer> set = new HashSet<Integer>();
		set.add(Integer.valueOf(AttributeDesignator.ENVIRONMENT_TARGET));
		set.add(Integer.valueOf(AttributeDesignator.SUBJECT_TARGET));
		set.add(Integer.valueOf(AttributeDesignator.ACTION_TARGET));
		set.add(Integer.valueOf(AttributeDesignator.RESOURCE_TARGET));
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.balana.finder.AttributeFinderModule#getSupportedIds()
	 */
	public Set getSupportedIds() {
		return null;
	}

	/**
	 * Registers PIP attribute handlers are initialized when the server is start-up. This method can
	 * be used to refresh all attribute finders internally. refreshSupportedAttribute() method must be
     * implemented within the PIP attribute finder to perform this operation. Also this uses to find newly
     * defined attributes, attribute caches are would not be cleared.
	 * 
	 * @throws Exception throws then initialization of attribute finders are failed
	 */
	private void refreshAttributeFindersForNewAttributeId() throws Exception {
		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null  && !designators.isEmpty()){
            Set<Map.Entry<PIPAttributeFinder, Properties>> attributeFinders = designators.entrySet();
            for (Map.Entry<PIPAttributeFinder, Properties> attributeFinder : attributeFinders) {
                attributeFinder.getKey().init(attributeFinder.getValue());
            }
            init();
        }
	}
    
	/**
	 * Disables attribute Cache
	 */
	public void disableAttributeCache() {
		attributeFinderCache = null;
	}

	/**
	 * Enables attribute cache
	 */
	public void enableAttributeCache() {
		attributeFinderCache = EntitlementUtil
				.getCommonCache(EntitlementConstants.PIP_ATTRIBUTE_CACHE);
	}

	/**
	 * Clears attribute cache
	 */
	public void clearAttributeCache() {
		if (attributeFinderCache != null) {
			attributeFinderCache.clear();
			if (log.isDebugEnabled()) {
				log.debug("Attribute value cache is cleared for tenant " + tenantId);
			}
		}
	}

    /**
     * Converts DOM object to String. This is a helper method for creating cache key
     * @param node  Node value
     * @return String Object
     * @throws TransformerException Exception throws if fails
     */
    private String domToString(Node node) throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node),
              new StreamResult(buffer));
        return buffer.toString();
    }
}
