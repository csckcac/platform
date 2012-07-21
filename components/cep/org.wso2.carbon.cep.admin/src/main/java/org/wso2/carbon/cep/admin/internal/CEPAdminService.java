package org.wso2.carbon.cep.admin.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.admin.internal.config.XMLMappingHelper;
import org.wso2.carbon.cep.admin.internal.exception.CEPAdminException;
import org.wso2.carbon.cep.admin.internal.util.CEPAdminDSHolder;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.BucketBasicInfo;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.Query;
import org.wso2.carbon.cep.core.XpathDefinition;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.MapInputMapping;
import org.wso2.carbon.cep.core.mapping.output.Output;
import org.wso2.carbon.cep.core.mapping.output.mapping.ElementOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.TupleOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.XMLOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.MapOutputMapping;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.core.AbstractAdmin;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * this class is published as a web service. so that front end can invoke the
 * methods to invoke the cep engine.
 */
public class CEPAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(CEPAdminService.class);

    /**
     * This method will add bucketDTO to the CEP Engine
     *
     * @param bucketDTO - Admin module bucketDTO
     */
    public boolean addBucket(BucketDTO bucketDTO) throws CEPAdminException {

        List<Input> backendInputList = new ArrayList<Input>();
        Bucket backEndBucket = new Bucket();

        QueryDTO[] queryDTOs = bucketDTO.getQueries();
        InputDTO[] inputDTOs = bucketDTO.getInputs();
        if (inputDTOs != null) {
            for (InputDTO inputDTO : inputDTOs) {
                backendInputList.add(adaptInput(inputDTO));
            }
        }

        List<Query> backEndQueryList = new ArrayList<Query>();
        if (queryDTOs != null) {
            int queryIndex = 0;
            for (QueryDTO queryDTO : queryDTOs) {
                Query query = adaptQuery(queryDTO);
                query.setQueryIndex(queryIndex);
                backEndQueryList.add(query);
                queryIndex++;
            }
        }


        backEndBucket.setName(bucketDTO.getName());
        backEndBucket.setDescription(bucketDTO.getDescription());
        backEndBucket.setEngineProvider(bucketDTO.getEngineProvider());
        backEndBucket.setInputs(backendInputList);
        backEndBucket.setQueries(backEndQueryList);

        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            cepServiceInterface.addBucket(backEndBucket, getAxisConfig());
            return true;
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in Adding bucket to back end ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean editBucket(BucketDTO bucketDTO) throws CEPAdminException {

        try {
            List<Input> backendInputList = new ArrayList<Input>();
            Bucket backEndBucket = new Bucket();


            QueryDTO[] queryDTOs = bucketDTO.getQueries();
            InputDTO[] inputDTOs = bucketDTO.getInputs();
            if (inputDTOs != null) {
                for (InputDTO inputDTO : inputDTOs) {
                    backendInputList.add(adaptInput(inputDTO));
                }
            }

            List<Query> backEndQueryList = new ArrayList<Query>();
            if (queryDTOs != null) {
                int queryIndex = 0;
                for (QueryDTO queryDTO : queryDTOs) {
                    Query query = adaptQuery(queryDTO);
                    query.setQueryIndex(queryIndex);
                    backEndQueryList.add(query);
                    queryIndex++;
                }
            }


            if (!(inputDTOs == null && queryDTOs == null)) {
                backEndBucket.setName(bucketDTO.getName());
                backEndBucket.setDescription(bucketDTO.getDescription());
                backEndBucket.setEngineProvider(bucketDTO.getEngineProvider());
                backEndBucket.setInputs(backendInputList);
                backEndBucket.setQueries(backEndQueryList);

                CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();

                cepServiceInterface.editBucket(backEndBucket);
            }
            return true;
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in editing the bucket";
            if (e.getCause().getMessage().contains("Error during creating rule")) {
                errorMessage = "Error in processing the query ";
            }
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }

    }


    /**
     * This method will map CEP Admin module InputDTO Topic to CEP Core module InputDTO
     *
     * @param inputDTO - CEP Admin module inputs
     * @return InputTopic  - CEP Core module InputDTO
     * @throws org.wso2.carbon.cep.admin.internal.exception.CEPAdminException
     *
     */
    private Input adaptInput(InputDTO inputDTO) throws CEPAdminException {

        Input backendInput = new Input();

        backendInput.setTopic(inputDTO.getTopic());
        backendInput.setBrokerName(inputDTO.getBrokerName());

        if (inputDTO.getInputXMLMappingDTO() != null) {
            InputXMLMappingDTO inputXMLMappingDTO = inputDTO.getInputXMLMappingDTO();
            backendInput.setInputMapping(adaptInputMapping(inputXMLMappingDTO));
        } else if (inputDTO.getInputTupleMappingDTO() != null) {
            InputTupleMappingDTO inputTupleMappingDTO = inputDTO.getInputTupleMappingDTO();
            backendInput.setInputMapping(adaptInputMapping(inputTupleMappingDTO));
        }


        return backendInput;
    }


    /**
     * This method will adapt CEP Admin module InputXMLMappingDTO to CEP Core module InputXMLMappingDTO
     *
     * @param inputXMLMappingDTO - CEP Admin module inputXMLMappingDTO
     * @return Mapping - CEP Core module InputXMLMappingDTO
     * @throws org.wso2.carbon.cep.admin.internal.exception.CEPAdminException
     *
     */

    private InputMapping adaptInputMapping(InputXMLMappingDTO inputXMLMappingDTO)
            throws CEPAdminException {
        XMLInputMapping backendInputMapping = new XMLInputMapping();
        XMLPropertyDTO[] XMLPropertyDTOs = inputXMLMappingDTO.getProperties();
        List<XMLProperty> backendInputPropertyList = new ArrayList<XMLProperty>();

        if (XMLPropertyDTOs != null) {
            for (XMLPropertyDTO inputXMLPropertyDTO : XMLPropertyDTOs) {
                XMLProperty backendInputProperty = new XMLProperty();
                backendInputProperty.setName(inputXMLPropertyDTO.getName());
                backendInputProperty.setXpath(inputXMLPropertyDTO.getXpath());
                backendInputProperty.setType(inputXMLPropertyDTO.getType());
                backendInputProperty.setType(inputXMLPropertyDTO.getType());
                backendInputProperty.setInputProperty(true);
                backendInputPropertyList.add(backendInputProperty);
            }
        }

        XpathDefinitionDTO[] xpathDefinitionDTOs = inputXMLMappingDTO.getXpathDefinition();
        if (xpathDefinitionDTOs != null) {
            for (XpathDefinitionDTO xpathDefinitionDTO : xpathDefinitionDTOs) {
                XpathDefinition backEndXpathDefinition = new XpathDefinition();
                backEndXpathDefinition.setNamespace(xpathDefinitionDTO.getNamespace());
                backEndXpathDefinition.setPrefix(xpathDefinitionDTO.getPrefix());
                backendInputMapping.addXpathDefinition(backEndXpathDefinition);
            }
        }

        backendInputMapping.setStream(inputXMLMappingDTO.getStream());
        try {
            Class mappingClass;
            String mappingClassName = inputXMLMappingDTO.getMappingClass();
            if (mappingClassName == null || mappingClassName.equals("") || mappingClassName.toLowerCase().equals("map")) {
                mappingClass = Map.class;
            } else {
                mappingClass = Class.forName(inputXMLMappingDTO.getMappingClass());
            }
            backendInputMapping.setMappingClass(mappingClass);
        } catch (ClassNotFoundException e) {
            String errorMessage = "No class found matching " + inputXMLMappingDTO.getMappingClass();
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
        backendInputMapping.setProperties(backendInputPropertyList);


        return backendInputMapping;

    }

    /**
     * This method will adapt CEP Admin module InputTupleMappingDTO to CEP Core module InputTupleMappingDTO
     *
     * @param inputTupleMappingDTO - CEP Admin module inputTupleMappingDTO
     * @return Mapping - CEP Core module InputTupleMappingDTO
     * @throws org.wso2.carbon.cep.admin.internal.exception.CEPAdminException
     *
     */
    private InputMapping adaptInputMapping(InputTupleMappingDTO inputTupleMappingDTO)
            throws CEPAdminException {
        TupleInputMapping backendInputMapping = new TupleInputMapping();
        TuplePropertyDTO[] tuplePropertyDTOs = inputTupleMappingDTO.getProperties();
        List<TupleProperty> backendInputPropertyList = new ArrayList<TupleProperty>();

        if (tuplePropertyDTOs != null) {
            for (TuplePropertyDTO tuplePropertyDTO : tuplePropertyDTOs) {
                TupleProperty backendInputProperty = new TupleProperty();
                backendInputProperty.setName(tuplePropertyDTO.getName());
                backendInputProperty.setType(tuplePropertyDTO.getType());
                backendInputProperty.setDataType(tuplePropertyDTO.getDataType());
                backendInputProperty.setInputProperty(true);
                backendInputPropertyList.add(backendInputProperty);
            }
        }

        backendInputMapping.setStream(inputTupleMappingDTO.getStream());
        try {
            Class mappingClass;
            String mappingClassName = inputTupleMappingDTO.getMappingClass();
            if (mappingClassName == null || mappingClassName.equals("") || mappingClassName.toLowerCase().equals("tuple")) {
                mappingClass = Map.class;
            } else {
                mappingClass = Class.forName(inputTupleMappingDTO.getMappingClass());
            }
            backendInputMapping.setMappingClass(mappingClass);
        } catch (ClassNotFoundException e) {
            String errorMessage = "No class found matching " + inputTupleMappingDTO.getMappingClass();
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
        backendInputMapping.setProperties(backendInputPropertyList);

        return backendInputMapping;

    }

    /**
     * This method will map CEP Admin module QueryDTO to CEP Core module QueryDTO
     *
     * @param queryDTO - CEP Admin module queryDTO
     * @return Query - CEP Core module queryDTO
     */
    private Query adaptQuery(QueryDTO queryDTO) {

        Query backEndQuery = new Query();
        Expression backEndExpression = new Expression();

        backEndExpression.setType(queryDTO.getExpression().getType());
        backEndExpression.setText(queryDTO.getExpression().getText());

        backEndQuery.setName(queryDTO.getName());
        backEndQuery.setQueryIndex(queryDTO.getQueryIndex());
        backEndQuery.setExpression(backEndExpression);

        if (queryDTO.getOutput() != null) {
            Output backEndOutput = new Output();

            OutputDTO outputDTO = queryDTO.getOutput();
            backEndOutput.setTopic(outputDTO.getTopic());
            backEndOutput.setBrokerName(outputDTO.getBrokerName());

            OutputElementMappingDTO outputElementMappingDTO = outputDTO.getOutputElementMapping();

            if (outputElementMappingDTO != null) {

                ElementOutputMapping backEndElementOutputMapping = null;
                if (outputElementMappingDTO.getDocumentElement() != null && !outputElementMappingDTO.getDocumentElement().equals("")) {
                    backEndElementOutputMapping = new ElementOutputMapping();
                    backEndElementOutputMapping.setDocumentElement(outputElementMappingDTO.getDocumentElement());
                    backEndElementOutputMapping.setNamespace(outputElementMappingDTO.getNamespace());

                    XMLPropertyDTO[] XMLPropertyDTOs = outputElementMappingDTO.getProperties();
                    for (XMLPropertyDTO XMLPropertyDTO : XMLPropertyDTOs) {
                        XMLProperty backEndProperty = new XMLProperty();
                        backEndProperty.setName(XMLPropertyDTO.getName());
                        backEndProperty.setXmlFieldName(XMLPropertyDTO.getXmlFieldName());
                        backEndProperty.setXmlFieldType(XMLPropertyDTO.getXmlFieldType());
                        backEndElementOutputMapping.addProperty(backEndProperty);
                    }
                }
                backEndOutput.setOutputMapping(backEndElementOutputMapping);
            }

            OutputXMLMappingDTO outputXmlMappingDTO = outputDTO.getOutputXmlMapping();
            if (outputXmlMappingDTO != null) {

                XMLOutputMapping backEndXMLOutputMapping = null;
                if (outputXmlMappingDTO.getMappingXMLText() != null && !outputXmlMappingDTO.getMappingXMLText().equals("")) {
                    backEndXMLOutputMapping = new XMLOutputMapping();
                    backEndXMLOutputMapping.setMappingXMLText(outputXmlMappingDTO.getMappingXMLText());
                }
                backEndOutput.setOutputMapping(backEndXMLOutputMapping);
            }

            OutputTupleMappingDTO outputTupleMappingDTO = outputDTO.getOutputTupleMappingDTO();
            if (outputTupleMappingDTO != null) {
                TupleOutputMapping backEndTupleOutputMapping = null;
                if (outputTupleMappingDTO.getStreamId() != null) {
                    backEndTupleOutputMapping = new TupleOutputMapping();
                    backEndTupleOutputMapping.setStreamId(outputTupleMappingDTO.getStreamId());
                } else {
                    backEndTupleOutputMapping = new TupleOutputMapping();
                    backEndTupleOutputMapping.setStreamId(outputDTO.getTopic());
                }
                if (outputTupleMappingDTO.getMetaDataProperties() != null && outputTupleMappingDTO.getMetaDataProperties().length != 0) {
                    backEndTupleOutputMapping.setMetaDataProperties(Arrays.asList(outputTupleMappingDTO.getMetaDataProperties()));
                }
                if (outputTupleMappingDTO.getCorrelationDataProperties() != null && outputTupleMappingDTO.getCorrelationDataProperties().length != 0) {
                    backEndTupleOutputMapping.setCorrelationDataProperties(Arrays.asList(outputTupleMappingDTO.getCorrelationDataProperties()));
                }
                if (outputTupleMappingDTO.getPayloadDataProperties() != null && outputTupleMappingDTO.getPayloadDataProperties().length != 0) {
                    backEndTupleOutputMapping.setPayloadDataProperties(Arrays.asList(outputTupleMappingDTO.getPayloadDataProperties()));
                }
                backEndOutput.setOutputMapping(backEndTupleOutputMapping);
            }
            backEndQuery.setOutput(backEndOutput);
        }
        return backEndQuery;
    }

    /**
     * This method will map the front end xml InputXMLMappingDTO String to backe end XML InputXMLMappingDTO object
     *
     * @param outputXmlMappingDTO - Admin module XML InputXMLMappingDTO Object
     */
    private OutputXMLMappingDTO getAdaptedXMLMappingElement(
            OutputXMLMappingDTO outputXmlMappingDTO) {
        OMElement omElement = null;
        try {
            omElement = AXIOMUtil.stringToOM(outputXmlMappingDTO.getMappingXMLText());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        outputXmlMappingDTO = XMLMappingHelper.fromOM(omElement);
        return outputXmlMappingDTO;
    }

    /**
     * This method will return all the bucket names from backEnd
     */
    public BucketBasicInfoDTO[] getAllBucketNames(int startingIndex, int maxBucketCount)
            throws CEPConfigurationException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        BucketBasicInfoDTO[] bucketBasicInfoDTOArray = null;

        List<BucketBasicInfo> backEndBucketBasicInfoList =
                cepServiceInterface.getBucketList();
        int resultSetSize = maxBucketCount;
        if ((backEndBucketBasicInfoList.size() - startingIndex) < maxBucketCount) {
            resultSetSize = (backEndBucketBasicInfoList.size() - startingIndex);
        }
        bucketBasicInfoDTOArray = new BucketBasicInfoDTO[resultSetSize];

        int index = 0;
        int basicInfoIndex = 0;
        for (BucketBasicInfo basicInfo : backEndBucketBasicInfoList) {
            if (startingIndex == index || startingIndex < index) {
                BucketBasicInfoDTO bucketBasicInfoDTO = new BucketBasicInfoDTO();
                bucketBasicInfoDTO.setName(basicInfo.getName());
                bucketBasicInfoDTO.setDescription(basicInfo.getDescription());
                bucketBasicInfoDTOArray[basicInfoIndex] = bucketBasicInfoDTO;
                basicInfoIndex++;
                if (basicInfoIndex == maxBucketCount) {
                    break;
                }
            }
            index++;
        }

        return bucketBasicInfoDTOArray;
    }

    /**
     * this method will return the full bucket count
     */
    public int getAllBucketCount() throws CEPConfigurationException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        return cepServiceInterface.getBucketList().size();
    }

    /**
     * This method will return the bucket with the given name from backEnd
     *
     * @param bucketName - Name of the bucket
     */

    public BucketDTO getBucket(String bucketName) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        BucketDTO bucketDTO = new BucketDTO();
        Bucket backEndBucket = null;
        try {
            backEndBucket = cepServiceInterface.getBucket(bucketName);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in getting the bucketDTO ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
        bucketDTO.setName(backEndBucket.getName());
        bucketDTO.setDescription(backEndBucket.getDescription());
        bucketDTO.setEngineProvider(backEndBucket.getEngineProvider());
        bucketDTO.setInputs(adaptInput(backEndBucket.getInputs()));
        bucketDTO.setQueries(adaptQueries(backEndBucket.getQueries()));

        return bucketDTO;
    }

    /**
     * This method maps CEP Core module input topic list to CEP Admin module InputDTO array
     *
     * @param backEndInputList - CEP Core module input  list
     * @return CEP Admin module InputDTO  array
     */
    private InputDTO[] adaptInput(List<Input> backEndInputList) {
        InputDTO[] inputDTOs = new InputDTO[backEndInputList.size()];
        int i = 0;
        for (Input backEndInput : backEndInputList) {
            InputDTO inputDTO = new InputDTO();
            inputDTO.setTopic(backEndInput.getTopic());
            inputDTO.setBrokerName(backEndInput.getBrokerName());
            if (backEndInput.getInputMapping() instanceof XMLInputMapping) {
                inputDTO.setInputXMLMappingDTO(adaptMapping((XMLInputMapping) backEndInput.getInputMapping()));
            } else if (backEndInput.getInputMapping() instanceof MapInputMapping) {
                inputDTO.setInputMapMappingDTO(adaptMapping((MapInputMapping)backEndInput.getInputMapping()));
            } else {
                inputDTO.setInputTupleMappingDTO(adaptMapping((TupleInputMapping) backEndInput.getInputMapping()));
            }
            inputDTOs[i] = inputDTO;
            i++;
        }
        return inputDTOs;
    }

    /**
     * This method maps CEP Core module InputXMLMappingDTO to CEP Admin module  Mappings
     *
     * @param backEndXMLInputMapping - CEP Core module input Stream Mappings
     */
    private InputXMLMappingDTO adaptMapping(XMLInputMapping backEndXMLInputMapping) {
        InputXMLMappingDTO inputXMLMappingDTO = new InputXMLMappingDTO();
        inputXMLMappingDTO.setStream(backEndXMLInputMapping.getStream());
        inputXMLMappingDTO.setProperties(adaptProperties(backEndXMLInputMapping.getProperties()));
        inputXMLMappingDTO.setXpathDefinition(adaptXpathDefinitions(backEndXMLInputMapping.getXpathNamespacePrefixes()));
        return inputXMLMappingDTO;
    }

    /**
     * This method maps CEP Core module InputTupleMappingDTO to CEP Admin module  Mappings
     *
     * @param backEndTupleInputMapping - CEP Core module input Stream Mappings
     */
    private InputTupleMappingDTO adaptMapping(TupleInputMapping backEndTupleInputMapping) {
        InputTupleMappingDTO inputTupleMappingDTO = new InputTupleMappingDTO();
        inputTupleMappingDTO.setStream(backEndTupleInputMapping.getStream());
        inputTupleMappingDTO.setProperties(adaptProperties(backEndTupleInputMapping.getProperties()));
        return inputTupleMappingDTO;
    }

    private InputMapMappingDTO adaptMapping(MapInputMapping backEndMapInputMapping){
        InputMapMappingDTO inputMapMappingDTO = new InputMapMappingDTO();
        inputMapMappingDTO.setStream(backEndMapInputMapping.getStream());
        return inputMapMappingDTO;
    }

    /**
     * This method maps CEP core module InputDTO property list to CEP Admin module input property array
     *
     * @param backEndXMLPropertyList - CEP Core module input property list
     */
    private XMLPropertyDTO[] adaptProperties(List<XMLProperty> backEndXMLPropertyList) {
        XMLPropertyDTO[] inputXMLPropertyDTOs = new XMLPropertyDTO[backEndXMLPropertyList.size()];
        int i = 0;
        for (XMLProperty backEndInputProperty : backEndXMLPropertyList) {
            XMLPropertyDTO XMLPropertyDTO = new XMLPropertyDTO();
            XMLPropertyDTO.setName(backEndInputProperty.getName());
            XMLPropertyDTO.setXpath(backEndInputProperty.getXpath());
            XMLPropertyDTO.setType(backEndInputProperty.getType());
            XMLPropertyDTO.setInputProperty(backEndInputProperty.isInputProperty());
            inputXMLPropertyDTOs[i] = XMLPropertyDTO;
            i++;
        }
        return inputXMLPropertyDTOs;
    }


    /**
     * This method maps CEP core module InputDTO property list to CEP Admin module input property array
     *
     * @param backEndTuplePropertyList - CEP Core module input property list
     */
    private TuplePropertyDTO[] adaptProperties(List<TupleProperty> backEndTuplePropertyList) {
        TuplePropertyDTO[] inputTuplePropertyDTOs = new TuplePropertyDTO[backEndTuplePropertyList.size()];
        int i = 0;
        for (TupleProperty backEndInputProperty : backEndTuplePropertyList) {
            TuplePropertyDTO tuplePropertyDTO = new TuplePropertyDTO();
            tuplePropertyDTO.setName(backEndInputProperty.getName());
            tuplePropertyDTO.setDataType(backEndInputProperty.getDataType());
            tuplePropertyDTO.setType(backEndInputProperty.getType());
            tuplePropertyDTO.setInputProperty(backEndInputProperty.isInputProperty());
            inputTuplePropertyDTOs[i] = tuplePropertyDTO;
            i++;
        }
        return inputTuplePropertyDTOs;
    }

    /**
     * This method maps CEP Core module nameSpacePrefixes Map in to CEP Admin module NamespacePrefixDTO array
     *
     * @param backEndXpathDefinitions
     */
    private XpathDefinitionDTO[] adaptXpathDefinitions(
            List<XpathDefinition> backEndXpathDefinitions) {
        XpathDefinitionDTO[] xpathDefinitionDTOs = new XpathDefinitionDTO[backEndXpathDefinitions.size()];
        int i = 0;
        for (XpathDefinition xpathDefinition : backEndXpathDefinitions) {
            XpathDefinitionDTO xpathDefinitionDTO = new XpathDefinitionDTO();
            xpathDefinitionDTO.setPrefix(xpathDefinition.getPrefix());
            xpathDefinitionDTO.setNamespace(xpathDefinition.getNamespace());
            xpathDefinitionDTOs[i] = xpathDefinitionDTO;
            i++;
        }
        return xpathDefinitionDTOs;
    }

    /**
     * This method maps CEP Core module QueryDTO list in to CEP Admin module QueryDTO array
     *
     * @param backEndQueryList
     */
    private QueryDTO[] adaptQueries(List<Query> backEndQueryList) {
        QueryDTO[] queryDTOs = new QueryDTO[backEndQueryList.size()];
        int index = 0;
        for (Query backEndQuery : backEndQueryList) {
            QueryDTO queryDTO = new QueryDTO();
            queryDTO.setName(backEndQuery.getName());
            queryDTO.setQueryIndex(backEndQuery.getQueryIndex());
            if (backEndQuery.getOutput() != null) {
                queryDTO.setOutput(adaptOutput(backEndQuery.getOutput()));
            }
            queryDTO.setExpression(adaptExpression(backEndQuery.getExpression()));
            queryDTOs[index] = queryDTO;
            index++;
        }
        return queryDTOs;
    }

    /**
     * This method maps CEP Core module OutputDTO in to CEP Admin module OutputDTO Topic
     *
     * @param backEndOutput
     */
    private OutputDTO adaptOutput(Output backEndOutput) {

        OutputDTO outputDTO = new OutputDTO();
        outputDTO.setTopic(backEndOutput.getTopic());
        outputDTO.setBrokerName(backEndOutput.getBrokerName());
        if (backEndOutput.getOutputMapping() instanceof ElementOutputMapping) {
            outputDTO.setOutputElementMapping(adaptOutputElementMapping((ElementOutputMapping) backEndOutput.getOutputMapping()));
        } else if (backEndOutput.getOutputMapping() instanceof TupleOutputMapping) {
            outputDTO.setOutputTupleMappingDTO(adaptOutputTupleMapping((TupleOutputMapping) backEndOutput.getOutputMapping()));
        } else if (backEndOutput.getOutputMapping() instanceof MapOutputMapping) {
            outputDTO.setOutputMapMappingDTO(adaptOutputMapMapping((MapOutputMapping)backEndOutput.getOutputMapping()));
        } else {
            outputDTO.setOutputXmlMapping(adaptOutputXMLMapping((XMLOutputMapping) backEndOutput.getOutputMapping()));
        }
        return outputDTO;
    }

    /**
     * This method will map CEP Core module OutputElementMappingDTO  in to CEP Admin module ElementMappings
     *
     * @param backEndElementOutputMapping
     */
    private OutputElementMappingDTO adaptOutputElementMapping(
            ElementOutputMapping backEndElementOutputMapping) {
        OutputElementMappingDTO outputElementMappingDTO = null;
        if (backEndElementOutputMapping != null) {
            outputElementMappingDTO = new OutputElementMappingDTO();
            outputElementMappingDTO.setNamespace(backEndElementOutputMapping.getNamespace());
            outputElementMappingDTO.setDocumentElement(backEndElementOutputMapping.getDocumentElement());
            outputElementMappingDTO.setProperties(adaptOutputProperties(backEndElementOutputMapping.getProperties()));
        }
        return outputElementMappingDTO;
    }


    private OutputMapMappingDTO adaptOutputMapMapping(MapOutputMapping mapOutputMapping){
        OutputMapMappingDTO outputMapMappingDTO = new OutputMapMappingDTO();
        return outputMapMappingDTO;
    }

    /**
     * This method will map CEP Core module OutputTupleMappingDTO  in to CEP Admin module ElementMappings
     *
     * @param backEndTupleOutputMapping
     */
    private OutputTupleMappingDTO adaptOutputTupleMapping(
            TupleOutputMapping backEndTupleOutputMapping) {
        OutputTupleMappingDTO outputTupleMappingDTO = null;
        if (backEndTupleOutputMapping != null) {
            outputTupleMappingDTO = new OutputTupleMappingDTO();
            outputTupleMappingDTO.setStreamId(backEndTupleOutputMapping.getStreamId());
            if (backEndTupleOutputMapping.getMetaDataProperties() != null) {
                outputTupleMappingDTO.setMetaDataProperties(backEndTupleOutputMapping.getMetaDataProperties().toArray(new String[backEndTupleOutputMapping.getMetaDataProperties().size()]));
            } else {
                outputTupleMappingDTO.setMetaDataProperties(new String[0]);
            }
            if (backEndTupleOutputMapping.getCorrelationDataProperties() != null) {
                outputTupleMappingDTO.setCorrelationDataProperties(backEndTupleOutputMapping.getCorrelationDataProperties().toArray(new String[backEndTupleOutputMapping.getCorrelationDataProperties().size()]));
            } else {
                outputTupleMappingDTO.setCorrelationDataProperties(new String[0]);
            }
            if (backEndTupleOutputMapping.getPayloadDataProperties() != null) {
                outputTupleMappingDTO.setPayloadDataProperties(backEndTupleOutputMapping.getPayloadDataProperties().toArray(new String[backEndTupleOutputMapping.getPayloadDataProperties().size()]));
            } else {
                outputTupleMappingDTO.setPayloadDataProperties(new String[0]);
            }
        }
        return outputTupleMappingDTO;
    }

    /**
     * This method will map CEP Core module OutputXMLMappingDTO in to CEP Admin module OutputXMLMappingDTO
     *
     * @param backEndXmlOutputMapping
     */
    private OutputXMLMappingDTO adaptOutputXMLMapping(XMLOutputMapping backEndXmlOutputMapping) {
        OutputXMLMappingDTO outputXmlMappingDTO = null;
        if (backEndXmlOutputMapping != null && backEndXmlOutputMapping.getMappingXMLText().length() > 0) {
            outputXmlMappingDTO = new OutputXMLMappingDTO();
            String xmlMappingText = backEndXmlOutputMapping.getMappingXMLText();
            outputXmlMappingDTO.setMappingXMLText(xmlMappingText);
        }
        return outputXmlMappingDTO;
    }

    /**
     * This method maps CEP Core module OutputDTO Properties List in to CEP Admin module output properties Array
     *
     * @param backEndPropertyList
     */
    private XMLPropertyDTO[] adaptOutputProperties(List<XMLProperty> backEndPropertyList) {
        XMLPropertyDTO[] XMLPropertyDTOs = new XMLPropertyDTO[backEndPropertyList.size()];
        int i = 0;
        for (XMLProperty backEndProperty : backEndPropertyList) {
            XMLPropertyDTO XMLPropertyDTO = new XMLPropertyDTO();
            XMLPropertyDTO.setName(backEndProperty.getName());
            XMLPropertyDTO.setXmlFieldName(backEndProperty.getXmlFieldName());
            XMLPropertyDTO.setXmlFieldType(backEndProperty.getXmlFieldType());
            XMLPropertyDTO.setInputProperty(backEndProperty.isInputProperty());
            XMLPropertyDTOs[i] = XMLPropertyDTO;
            i++;
        }
        return XMLPropertyDTOs;
    }

    /**
     * This method maps CEP core module ExpressionDTO to CEP Admin module ExpressionDTO
     *
     * @param backendExpression
     */
    private ExpressionDTO adaptExpression(Expression backendExpression) {
        ExpressionDTO expressionDTO = new ExpressionDTO();
        expressionDTO.setText(backendExpression.getText());
        expressionDTO.setType(backendExpression.getType());
        return expressionDTO;
    }

    /**
     * This method will remove the bucket with the provided bucket name from the back end
     *
     * @param bucketName - name of the bucket to be removed
     * @return status - status of the operation
     */

    public boolean removeBucket(String bucketName) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeBucket(bucketName);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing the bucket from back end";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean removeAllBuckets() throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeAllBuckets();
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing all buckets from back end ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public String[] getEngineProviders() throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.getCEPEngineProviders();
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in getting engine providers from back end ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public String[] getBrokerNames() throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.getCEPBrokerNames();
        } catch (CEPConfigurationException e) {
            throw new CEPAdminException("Error in getting engine providers from back end : " + e);
        }
    }

    public boolean removeQuery(String bucketName, String queryName) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeQuery(bucketName, queryName);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing query " + queryName + " from bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean removeAllQueries(String bucketName) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeAllQueries(bucketName);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing all queries from bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean editQuery(String bucketName, QueryDTO queryDTO) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.editQuery(bucketName, adaptQuery(queryDTO));
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in editing the queryDTO :" + queryDTO.getName();
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public QueryDTO[] getAllQueries(String bucketName, int startingIndex, int maxQueryCount)
            throws CEPAdminException {
        try {
            CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
            QueryDTO[] queriesArray = null;
            List<Query> backEndQueryList =
                    cepServiceInterface.getBucket(bucketName).getQueries();
            QueryDTO[] operationsQueryDTOs = null;
            operationsQueryDTOs = adaptQueries(backEndQueryList);

            int resultSetSize = maxQueryCount;
            if ((backEndQueryList.size() - startingIndex) < maxQueryCount) {
                resultSetSize = (backEndQueryList.size() - startingIndex);
            }
            queriesArray = new QueryDTO[resultSetSize];

            int index = 0;
            int queryIndex = 0;
            for (QueryDTO queryDTO : operationsQueryDTOs) {
                if (startingIndex == index || startingIndex < index) {
                    queriesArray[queryIndex] = queryDTO;
                    queryIndex++;
                    if (queryIndex == maxQueryCount) {
                        break;
                    }
                }
                index++;
            }

            return queriesArray;
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in reading queries from back end ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public int getAllQueryCount(String bucketName) throws CEPAdminException {
        try {
            CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
            Bucket bucket = cepServiceInterface.getBucket(bucketName);
            return bucket.getQueries().size();
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in getting all query count for the bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public InputDTO[] getAllInputs(String bucketName, int startingIndex, int maxQueryCount)
            throws CEPAdminException {
        try {
            CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
            InputDTO[] inputsArray = null;
            List<Input> backEndInputList =
                    cepServiceInterface.getBucket(bucketName).getInputs();
            InputDTO[] operationsInputDTOs = null;
            operationsInputDTOs = adaptInput(backEndInputList);

            int resultSetSize = maxQueryCount;
            if ((backEndInputList.size() - startingIndex) < maxQueryCount) {
                resultSetSize = (backEndInputList.size() - startingIndex);
            }
            inputsArray = new InputDTO[resultSetSize];

            int index = 0;
            int inputIndex = 0;
            for (InputDTO inputDTO : operationsInputDTOs) {
                if (startingIndex == index || startingIndex < index) {
                    inputsArray[inputIndex] = inputDTO;
                    inputIndex++;
                    if (inputIndex == maxQueryCount) {
                        break;
                    }
                }
                index++;
            }

            return inputsArray;
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in reading inputs from back end ";
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public int getAllInputCount(String bucketName) throws CEPAdminException {
        try {
            CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
            Bucket bucket = cepServiceInterface.getBucket(bucketName);
            return bucket.getInputs().size();
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in getting all query count for the bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean removeInput(String bucketName, String inputTopic) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeInput(bucketName, inputTopic);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing input " + inputTopic + " from bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }

    public boolean removeAllInputs(String bucketName) throws CEPAdminException {
        CEPServiceInterface cepServiceInterface = CEPAdminDSHolder.getInstance().getCEPService();
        try {
            return cepServiceInterface.removeAllInputs(bucketName);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Error in removing all inputs from bucket :" + bucketName;
            log.error(errorMessage, e);
            throw new CEPAdminException(errorMessage, e);
        }
    }
}

