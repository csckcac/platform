/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.mapping.output.Output;
import org.wso2.carbon.cep.core.Query;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.config.output.OutputHelper;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;




import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;






/**
 * This class will help to build Query object from a given OMElement
 */
public class QueryHelper {
    private static final Log log = LogFactory.getLog(QueryHelper.class);


    public static Query fromOM(OMElement queryElement) {
        Query query = new Query();
        String name = queryElement.getAttribute(new QName(CEPConstants.CEP_CONF_ELE_NAME)).getAttributeValue();
        query.setName(name);
        OMElement expressionElement = queryElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_EXPRESSION));
        if (expressionElement != null) {
            query.setExpression(ExpressionHelper.fromOM(expressionElement));
        }

        OMElement outputOmElement = queryElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_OUTPUT));
        if (expressionElement != null && outputOmElement != null) {
            query.setOutput(OutputHelper.fromOM(outputOmElement));
        }
        return query;
    }

    public static void addQueriesToRegistry(List<Query> queryList,
                                            Registry registry,
                                            String parentCollectionPath) throws CEPConfigurationException {
        try {
            String queriesCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_QUERIES;
            registry.put(queriesCollectionPath, registry.newCollection());
            for (Query query : queryList) {
                String queryPath = queriesCollectionPath + "/" + query.getName();
                Collection queryCollection = registry.newCollection();
                queryCollection.addProperty(CEPConstants.CEP_REGISTRY_NAME, query.getName());
                queryCollection.addProperty(CEPConstants.CEP_REGISTRY_QUERY_INDEX,query.getQueryIndex()+"");
                registry.put(queryPath, queryCollection);
                Resource queryResource = registry.newResource();
                queryResource.setProperty(CEPConstants.CEP_REGISTRY_TYPE, query.getExpression().getType());
                queryResource.setProperty(CEPConstants.CEP_REGISTRY_LISTENER_NAME, query.getExpression().getListenerName());
                queryResource.setContent(query.getExpression().getText());
                registry.put(queryPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_EXPRESSION, queryResource);


                if (query.getOutput() != null) {
                    OutputHelper.addOutputToRegistry(registry, query.getOutput(), queryPath);
                }


            }
        } catch (RegistryException e) {
            String errorMessage = "Can not add query to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }

    public static void loadQueriesFromRegistry(Registry registry,
                                               Bucket bucket,
                                               String names) throws CEPConfigurationException {
        try {
            Query query = new Query();
            Expression expression = new Expression();
            Output output = null;
            Collection collection3 = (Collection) registry.get(names);
            query.setName(collection3.getProperty(CEPConstants.CEP_REGISTRY_NAME));
            query.setQueryIndex(Integer.parseInt(collection3.getProperty(CEPConstants.CEP_REGISTRY_QUERY_INDEX).trim()));
            for (String names2 : collection3.getChildren()) {
                if (registry.get(names2) instanceof Collection) {
                    output = OutputHelper.loadOutputsFromRegistry(registry, names2);
                } else {
                    Resource detailsResource = registry.get(names2);
                    String content = new String((byte[]) detailsResource.getContent());
                    expression.setType(detailsResource.getProperty(CEPConstants.CEP_REGISTRY_TYPE))  ;
                    expression.setListenerName(detailsResource.getProperty(CEPConstants.CEP_REGISTRY_LISTENER_NAME));  ;
                    expression.setText(content);
                }
            }
            query.setExpression(expression);
            query.setOutput(output);
            bucket.addQuery(query);
        } catch (RegistryException e) {
            String errorMessage = "Can not load queries from the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyQueriesInRegistry(Registry registry,
                                               Bucket bucket,
                                               String parentCollectionPath) throws CEPConfigurationException {
        try {
            String queriesCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_QUERIES;
            if (registry.resourceExists(queriesCollectionPath)) {
                registry.delete(queriesCollectionPath);
                registry.commitTransaction();
            }
            registry.put(queriesCollectionPath, registry.newCollection());
            for (Query query : bucket.getQueries()) {
                String queryPath = queriesCollectionPath + "/" + query.getName();
                Collection queryCollection = registry.newCollection();
                queryCollection.addProperty(CEPConstants.CEP_REGISTRY_NAME, query.getName());
                queryCollection.addProperty(CEPConstants.CEP_REGISTRY_QUERY_INDEX,query.getQueryIndex()+"");
                registry.put(queryPath, queryCollection);
                Resource queryResource = registry.newResource();
                queryResource.setProperty(CEPConstants.CEP_REGISTRY_TYPE, query.getExpression().getType());
                queryResource.setContent(query.getExpression().getText());
                registry.put(queryPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_EXPRESSION, queryResource);

                if (query.getOutput() != null && query.getOutput().getTopic().length() > 0) {
                    OutputHelper.modifyOutputsInRegistry(registry, query.getOutput(), queryPath);
                }

            }
        } catch (RegistryException e) {
            String errorMessage = "Can not load queries from the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }


    
    
	public static OMElement queryToOM(Query query) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement queryChild = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_QUERY,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		OMElement queryOutput = OutputHelper.outputToOM(query.getOutput());
		Expression queryExpression = query.getExpression();
		String queryName = query.getName();
		queryChild
				.addAttribute(CEPConstants.CEP_REGISTRY_NAME, queryName, null);
		OMElement omQueryExpression = ExpressionHelper
				.expressionToOM(queryExpression);
		queryChild.addChild(omQueryExpression);
		queryChild.addChild(queryOutput);
		return queryChild;
	}
    
    
}