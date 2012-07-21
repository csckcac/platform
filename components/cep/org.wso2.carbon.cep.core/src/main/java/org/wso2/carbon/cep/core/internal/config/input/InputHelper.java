package org.wso2.carbon.cep.core.internal.config.input;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.config.input.mapping.InputMappingHelper;
import org.wso2.carbon.cep.core.internal.config.input.mapping.MapInputMappingHelper;
import org.wso2.carbon.cep.core.internal.config.input.mapping.TupleInputMappingHelper;
import org.wso2.carbon.cep.core.internal.config.input.mapping.XMLInputMappingHelper;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * This class helps to build an Input Object from a given OMElement
 */
public class InputHelper {
    private static final Log log = LogFactory.getLog(InputHelper.class);

    public static Input fromOM(OMElement inputElement)
            throws CEPConfigurationException {

        Input input = new Input();
        String topic = inputElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_TOPIC));
        input.setTopic(topic);

        String brokerProxy =
                inputElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_BROKER_NAME));
        input.setBrokerName(brokerProxy);


        // if there are more mappings we can check for them and process accordingly.
        OMElement xmlMappingElement =
                inputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                             CEPConstants.CEP_CONF_ELE_XML_MAPPING));

        if (xmlMappingElement != null) {
            input.setInputMapping(XMLInputMappingHelper.fromOM(xmlMappingElement));
            input.getInputMapping().setStream( xmlMappingElement.getAttributeValue(new QName(CEPConstants.CEP_REGISTRY_STREAM)));
        } else {
            OMElement tupleMappingElement =
                    inputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                 CEPConstants.CEP_CONF_ELE_TUPLE_MAPPING));

            if (tupleMappingElement != null) {
                input.setInputMapping(TupleInputMappingHelper.fromOM(tupleMappingElement));
                input.getInputMapping().setStream( tupleMappingElement.getAttributeValue(new QName(CEPConstants.CEP_REGISTRY_STREAM)));

            } else {

                OMElement mapMappingElement =
                        inputElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                     CEPConstants.CEP_CONF_ELE_MAP_MAPPING));
                if (mapMappingElement != null){
                    input.setInputMapping(MapInputMappingHelper.fromOM(mapMappingElement));
                    input.getInputMapping().setStream(mapMappingElement.getAttributeValue(new QName(CEPConstants.CEP_REGISTRY_STREAM)));
                }
            }
        }
        return input;
    }

    public static void addInputsToRegistry(List<Input> inputList,
                                           Registry registry,
                                           String parentCollectionPath)
            throws CEPConfigurationException {
        try {
            String inputsCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_INPUTS;
            registry.put(inputsCollectionPath, registry.newCollection());
            for (Input input : inputList) {
                String uniqueTopic = input.getTopic().replaceAll("/", "_") + "_" + input.getBrokerName();
                String inputResourcePath = inputsCollectionPath + "/" + uniqueTopic;

                Collection inputCollection = registry.newCollection();
                inputCollection.addProperty(CEPConstants.CEP_CONF_ELE_TOPIC, input.getTopic());
                inputCollection.addProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME, input.getBrokerName());
                registry.put(inputResourcePath, inputCollection);
                InputMappingHelper.addMappingToRegistry(registry, input, inputResourcePath);

            }
        } catch (RegistryException e) {
            String errorMessage = "Can not add inputs to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void loadInputsFromRegistry(Registry registry,
                                              Bucket bucket,
                                              String names) throws CEPConfigurationException {
        try {
            Input input = new Input();
            Collection inputsCollection = (Collection) registry.get(names);
            input.setTopic(inputsCollection.getProperty(CEPConstants.CEP_CONF_ELE_TOPIC));
            input.setBrokerName(inputsCollection.getProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME));
            for (String mappingName : inputsCollection.getChildren()) {
                if (registry.get(mappingName) instanceof Collection) {
                    input.setInputMapping(InputMappingHelper.loadMappingsFromRegistry(registry, mappingName));
                }
            }
            bucket.addInput(input);
        } catch (RegistryException e) {
            String errorMessage = "Can not load inputs from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyInputsInRegistry(Registry registry,
                                              Bucket bucket,
                                              String parentCollectionPath)
            throws CEPConfigurationException {
        try {
            String inputsCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_INPUTS;
            if (registry.resourceExists(inputsCollectionPath)) {
                registry.delete(inputsCollectionPath);
                registry.commitTransaction();
            }
            registry.put(inputsCollectionPath, registry.newCollection());
            for (Input input : bucket.getInputs()) {
                String uniqueTopic = input.getTopic().replaceAll("/", "_") + "_" + input.getBrokerName();
                String inputResourcePath = inputsCollectionPath + "/" + uniqueTopic;

                Collection inputCollection = registry.newCollection();
                inputCollection.addProperty(CEPConstants.CEP_CONF_ELE_TOPIC, input.getTopic());
                inputCollection.addProperty(CEPConstants.CEP_CONF_ELE_BROKER_NAME, input.getBrokerName());
                registry.put(inputResourcePath, inputCollection);
                InputMappingHelper.modifyMappingsInRegistry(registry, input, inputResourcePath);

            }
        } catch (RegistryException e) {
            String errorMessage = "Error in modifying inputs in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }
    
	public static OMElement inputToOM(Input input) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement inputChild = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_INPUT,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		if (input.getInputMapping() instanceof XMLInputMapping) {
			XMLInputMapping xmlInputMap = (XMLInputMapping) input
					.getInputMapping();
			OMElement xmlInputMapping = XMLInputMappingHelper
					.xmlInputMappingToOM(xmlInputMap);
			String inputName = input.getTopic();
			String inputBrokerName = input.getBrokerName();
			inputChild.addChild(xmlInputMapping);
			inputChild.addAttribute(CEPConstants.CEP_CONF_ELE_TOPIC, inputName,
					null);
			inputChild.addAttribute(CEPConstants.CEP_CONF_ELE_BROKER_NAME,
					inputBrokerName, null);
		}
		return inputChild;
	}

}
