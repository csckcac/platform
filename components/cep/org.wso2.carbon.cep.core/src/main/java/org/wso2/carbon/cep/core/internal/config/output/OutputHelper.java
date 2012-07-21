package org.wso2.carbon.cep.core.internal.config.output;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.output.Output;
import org.wso2.carbon.cep.core.mapping.output.mapping.ElementOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.MapOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.TupleOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.XMLOutputMapping;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;

/**
 * This class will help to build Output Object from a given OMELement
 */
public class OutputHelper {
    private static final Log log = LogFactory.getLog(OutputHelper.class);

    public static Output fromOM(OMElement outputElement) {
        Output output = new Output();

        String topic = outputElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_TOPIC));
        output.setTopic(topic);

        String brokerProxy = outputElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_BROKER_NAME));
        output.setBrokerName(brokerProxy);

        OMElement xmlMappingElement = outputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                    CEPConstants.CEP_CONF_ELE_XML_MAPPING));
        if (xmlMappingElement != null) {
            output.setOutputMapping(XMLOutputMappingHelper.fromOM(xmlMappingElement));
        }
        if (output.getOutputMapping() == null) {
            OMElement tupleMappingElement = outputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                          CEPConstants.CEP_CONF_ELE_TUPLE_MAPPING));
            if (tupleMappingElement != null) {
                TupleOutputMapping tupleOutputMapping = TupleOutputMappingHelper.fromOM(tupleMappingElement);
                tupleOutputMapping.setStreamId(topic);
                output.setOutputMapping(tupleOutputMapping);
            }
        }
        if (output.getOutputMapping() == null) {
            OMElement elementMappingElement = outputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                            CEPConstants.CEP_CONF_ELE_EMAPPING));
            if (elementMappingElement != null) {
                output.setOutputMapping(ElementOutputMappingHelper.fromOM(elementMappingElement));
            }
        }

        if (output.getOutputMapping() == null) {
            OMElement mapMappingElement = outputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                            CEPConstants.CEP_CONF_ELE_MAP_MAPPING));
            if (mapMappingElement != null) {
                output.setOutputMapping(MapOutputMappingHelper.fromOM(mapMappingElement));
            }
        }


        return output;
    }

    public static void addOutputToRegistry(Registry registry, Output output, String queryPath)
            throws CEPConfigurationException {
        try {
            Collection outputCollection = registry.newCollection();
            outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TOPIC, output.getTopic());
            outputCollection.addProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME, output.getBrokerName());

            String registryOutputPath = queryPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_OUTPUT;
            
            if (output.getOutputMapping() instanceof ElementOutputMapping) {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_ELEMENT_MAPPING);
                registry.put(registryOutputPath, outputCollection);
                ElementOutputMappingHelper.addElementMappingToRegistry(registry, (ElementOutputMapping) output.getOutputMapping(), queryPath);

            } else if (output.getOutputMapping() instanceof TupleOutputMapping) {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_TUPLE_MAPPING);
                registry.put(registryOutputPath, outputCollection);
                TupleOutputMappingHelper.addTupleMappingToRegistry(registry, (TupleOutputMapping) output.getOutputMapping(), queryPath);
            } else if (output.getOutputMapping() instanceof MapOutputMapping){
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_MAPPING_MAP);
                registry.put(registryOutputPath, outputCollection);
                MapOutputMappingHelper.addMapMappingToRegistry(registry, (MapOutputMapping) output.getOutputMapping(), queryPath);
            } else {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_XML_MAPPING);
                registry.put(registryOutputPath, outputCollection);
                XMLOutputMappingHelper.addXMLMappingToRegistry(registry, queryPath, (XMLOutputMapping) output.getOutputMapping());
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not add output to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyOutputsInRegistry(Registry registry, Output output, String queryPath)
            throws CEPConfigurationException {

        try {
            Collection outputCollection = registry.newCollection();
            outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TOPIC, output.getTopic());
            outputCollection.addProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME, output.getBrokerName());


            String registryOutputPath = queryPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_OUTPUT;
            if (output.getOutputMapping() instanceof ElementOutputMapping) {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_ELEMENT_MAPPING);
                registry.put(registryOutputPath, outputCollection);

                ElementOutputMappingHelper.modifyElementMappingInRegistry(registry, (ElementOutputMapping) output.getOutputMapping(), queryPath);
            } else if (output.getOutputMapping() instanceof TupleOutputMapping) {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_TUPLE_MAPPING);
                registry.put(registryOutputPath, outputCollection);

                TupleOutputMappingHelper.modifyTupleMappingInRegistry(registry, (TupleOutputMapping) output.getOutputMapping(), queryPath);
            } else if (output.getOutputMapping() instanceof MapOutputMapping) {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_MAPPING_MAP);
                registry.put(registryOutputPath, outputCollection);

                MapOutputMappingHelper.modifyMapMappingInRegistry(registry, (MapOutputMapping) output.getOutputMapping(), queryPath);
            } else {
                outputCollection.addProperty(CEPConstants.CEP_REGISTRY_TYPE, CEPConstants.CEP_REGISTRY_XML_MAPPING);
                registry.put(registryOutputPath, outputCollection);

                XMLOutputMappingHelper.modifyXMLMappingInRegistry(registry, queryPath, (XMLOutputMapping) output.getOutputMapping());
            }

        } catch (RegistryException e) {
            String errorMessage = "Can not modify output in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static Output loadOutputsFromRegistry(Registry registry,
                                                 String outputCollectionPath)
            throws CEPConfigurationException {
        Output output = new Output();
        try {
            Collection outputCollection = (Collection) registry.get(outputCollectionPath);
            output.setTopic(outputCollection.getProperty(CEPConstants.CEP_REGISTRY_TOPIC));
            output.setBrokerName(outputCollection.getProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME));
            for (String outputS : outputCollection.getChildren()) {
                if (registry.get(outputS) instanceof Resource) {
                    String mapping = outputS.substring(outputS.lastIndexOf(CEPConstants.CEP_REGISTRY_BS));
                    if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING)
                            .equals(mapping)) {
                        TupleOutputMapping tupleOutputMapping = TupleOutputMappingHelper.loadTupleMappingFromRegistry(registry, outputS);
                        output.setOutputMapping(tupleOutputMapping);
                    } else if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_ELEMENT_MAPPING)
                            .equals(mapping)) {
                        ElementOutputMapping elementOutputMapping = ElementOutputMappingHelper.loadElementMappingFromRegistry(registry, outputS);
                        output.setOutputMapping(elementOutputMapping);

                    } else if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAP_MAPPING).equals(mapping)) {
                        MapOutputMapping mapOutputMapping = MapOutputMappingHelper.loadMapMappingFromRegistry(registry, outputS);
                        output.setOutputMapping(mapOutputMapping);

                    } else {
                        XMLOutputMapping xmlOutputMapping = XMLOutputMappingHelper.loadXMLMappingFromRegistry(registry, outputS);
                        output.setOutputMapping(xmlOutputMapping);
                    }
                }
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not load output from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

        return output;
    }

    

	public static OMElement outputToOM(Output output) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement queryOutput = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_OUTPUT,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String queryOutputName = output.getTopic();
		String queryOutputBrokerName = output.getBrokerName();
		queryOutput.addAttribute(CEPConstants.CEP_REGISTRY_TOPIC,
				queryOutputName, null);
		queryOutput.addAttribute(CEPConstants.CEP_CONF_ELE_BROKER_NAME,
				queryOutputBrokerName, null);
		if (output.getOutputMapping() instanceof XMLOutputMapping) {
			OMElement queryXMLOutputMapping = XMLOutputMappingHelper
					.xmlOutputMappingToOM((XMLOutputMapping) output
							.getOutputMapping());
			queryOutput.addChild(queryXMLOutputMapping);
		} else if (output.getOutputMapping() instanceof ElementOutputMapping) {
			OMElement queryElementOutputMapping = ElementOutputMappingHelper
					.elementOutputMappingToOM((ElementOutputMapping) output
							.getOutputMapping());
			queryOutput.addChild(queryElementOutputMapping);
		}

		return queryOutput;
	}

	

}
