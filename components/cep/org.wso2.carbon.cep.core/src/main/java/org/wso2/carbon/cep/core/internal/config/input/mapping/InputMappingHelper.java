package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.MapInputMapping;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * This class will help to build Mappin Object from a given OMElement
 */
public class InputMappingHelper {
    private static final Log log = LogFactory.getLog(InputMappingHelper.class);

    static Method getMethod(Class eventClass, String name)
            throws CEPConfigurationException {
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(eventClass).getPropertyDescriptors()) {
                if (name.equals(pd.getName())) {
                    return pd.getWriteMethod();
                }
            }
            throw new CEPConfigurationException("WriteMethod " + name + " not found in Event Class " + eventClass);
        } catch (IntrospectionException e) {
            throw new CEPConfigurationException("Cannot get the methods for Event Class " + eventClass, e);
        }
    }

    public static void addMappingToRegistry(Registry registry, Input input,
                                            String inputResourcePath)
            throws CEPConfigurationException {
        InputMapping inputMapping = input.getInputMapping();
        try {
            Collection mappingCollection = registry.newCollection();
            mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_STREAM, inputMapping.getStream());
            Class mappingClass = inputMapping.getMappingClass();
            if (mappingClass != null){
                 mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_EVENT_CLASS, mappingClass.getName());
            }

            String mappingPath = inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING + CEPConstants.CEP_REGISTRY_BS;


            if (inputMapping instanceof XMLInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_XML);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                XMLInputMappingHelper.addMappingToRegistry(registry, (XMLInputMapping) inputMapping, mappingPath);
            } else if (input.getInputMapping() instanceof TupleInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_TUPLE);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                TupleInputMappingHelper.addMappingToRegistry(registry, (TupleInputMapping) inputMapping, mappingPath);
            } else if (input.getInputMapping() instanceof MapInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_MAP);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                MapInputMappingHelper.addMappingToRegistry(registry, (MapInputMapping) inputMapping, mappingPath);
            } else {
                throw new CEPConfigurationException(inputMapping.getStream() + " has not valid input mapping");
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not add mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }


    }

    public static void modifyMappingsInRegistry(Registry registry, Input input,
                                                String inputResourcePath)
            throws CEPConfigurationException {

        InputMapping inputMapping = input.getInputMapping();
        try {
            Collection mappingCollection = registry.newCollection();
            mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_STREAM, inputMapping.getStream());
            Class mappingClass = inputMapping.getMappingClass();
            mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_EVENT_CLASS, mappingClass.getName());
            String mappingPath = inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING + CEPConstants.CEP_REGISTRY_BS;

            if (inputMapping instanceof XMLInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_XML);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                XMLInputMappingHelper.addMappingToRegistry(registry, (XMLInputMapping) inputMapping, mappingPath);
            } else if (input.getInputMapping() instanceof TupleInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_TUPLE);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                TupleInputMappingHelper.addMappingToRegistry(registry, (TupleInputMapping) inputMapping, mappingPath);
            } else if (input.getInputMapping() instanceof MapInputMapping) {
                mappingCollection.addProperty(CEPConstants.CEP_REGISTRY_MAPPING, CEPConstants.CEP_REGISTRY_MAPPING_MAP);
                registry.put(inputResourcePath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_MAPPING, mappingCollection);
                MapInputMappingHelper.addMappingToRegistry(registry, (MapInputMapping) inputMapping, mappingPath);
            } else {
                throw new CEPConfigurationException(inputMapping.getStream() + " has not valid input mapping");
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not modify mappings in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }


    public static InputMapping loadMappingsFromRegistry(Registry registry, String mappingPath)
            throws CEPConfigurationException {
        InputMapping inputMapping = null;
        try {
            Collection mappingCollection = (Collection) registry.get(mappingPath);

            String streamName = mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_STREAM);

            String eventClass = mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_EVENT_CLASS);
            Class mappingClass = null;
            try {
                if (eventClass != null) {
                    mappingClass = Class.forName(eventClass);
                }
            } catch (ClassNotFoundException e) {
                throw new CEPConfigurationException("No class found matching " + mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_EVENT_CLASS), e);
            }


            if (mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_MAPPING).equals(CEPConstants.CEP_REGISTRY_MAPPING_XML)) {
                inputMapping = new XMLInputMapping();
                inputMapping.setStream(streamName);
                inputMapping.setMappingClass(mappingClass);
                XMLInputMappingHelper.loadMappingsFromRegistry(registry, (XMLInputMapping) inputMapping, mappingCollection);
            } else if (mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_MAPPING).equals(CEPConstants.CEP_REGISTRY_MAPPING_TUPLE)) { //Tuple
                inputMapping = new TupleInputMapping();
                inputMapping.setStream(streamName);
                inputMapping.setMappingClass(mappingClass);
                TupleInputMappingHelper.loadMappingsFromRegistry(registry, (TupleInputMapping) inputMapping, mappingCollection);
            } else if (mappingCollection.getProperty(CEPConstants.CEP_REGISTRY_MAPPING).equals(CEPConstants.CEP_REGISTRY_MAPPING_MAP)){
                inputMapping = new MapInputMapping();
                inputMapping.setStream(streamName);
                inputMapping.setMappingClass(mappingClass);
                MapInputMappingHelper.loadMappingsFromRegistry(registry, (MapInputMapping) inputMapping, mappingCollection);
            }


        } catch (RegistryException e) {
            String errorMessage = "Can not load mappings from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return inputMapping;
    }


}
