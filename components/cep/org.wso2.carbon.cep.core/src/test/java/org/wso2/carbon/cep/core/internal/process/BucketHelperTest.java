package org.wso2.carbon.cep.core.internal.process;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.Query;
import org.wso2.carbon.cep.core.XpathDefinition;
import org.wso2.carbon.cep.core.internal.config.BucketHelper;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.output.Output;
import org.wso2.carbon.cep.core.mapping.output.mapping.ElementOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.XMLOutputMapping;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;

public class BucketHelperTest extends TestCase {

	private Bucket bucket;
	private Input input;
	private Output outputOne;
	private Output outputTwo;
	private XMLProperty inputXMLProperty;
	private XMLProperty outputEleXMLProperty;
	private Expression expressionOne;
	private Expression expressiontwo;
	private XMLOutputMapping xmlOutPutMapping;
	private ElementOutputMapping elementOutputMapping;
	private XpathDefinition xpathDefinition;
	private XMLInputMapping xmlInputMapping;
	private Query queryOne;
	private Query queryTwo;
	private List<Input> inputList;
	private List<XMLProperty> inputPropertyList;
	private List<XpathDefinition> inputXpathDefinitionList;
	private List<XMLProperty> outputListXMLProperty;
	private List<Query> queryList;
	private OMElement bucketOM;

	@Override
	protected void setUp() throws Exception {
		bucket = new Bucket();
		input = new Input();
		queryOne = new Query();
		queryTwo = new Query();
		outputOne = new Output();
		outputTwo = new Output();
		inputXMLProperty = new XMLProperty();
		outputEleXMLProperty = new XMLProperty();
		expressionOne = new Expression();
		expressiontwo = new Expression();
		xmlOutPutMapping = new XMLOutputMapping();
		elementOutputMapping = new ElementOutputMapping();
		xpathDefinition = new XpathDefinition();
		xmlInputMapping = new XMLInputMapping();
		bucket.setName("TestBucket");
		bucket.setDescription("This is tesing Bucket");
		bucket.setEngineProvider("DroolsFusionEngineProvider");
		bucket.setOverWriteRegistry(true);
		inputList = new ArrayList<Input>();
		inputPropertyList = new ArrayList<XMLProperty>();
		inputXpathDefinitionList = new ArrayList<XpathDefinition>();
		queryList = new ArrayList<Query>();
		outputListXMLProperty = new ArrayList<XMLProperty>();
		input.setTopic("TestingInput");
		input.setBrokerName("wsEventBroker");
		xmlInputMapping.setStream("TestingStream");
		inputXMLProperty.setName("inputproperty");
		inputXMLProperty.setType("java.lang.Integer");
		inputXMLProperty.setXpath("xpath");
		inputPropertyList.add(inputXMLProperty);
		xmlInputMapping.setProperties(inputPropertyList);
		xpathDefinition.setPrefix("prefix");
		xpathDefinition.setNamespace("nameSpace");
		inputXpathDefinitionList.add(xpathDefinition);
		xmlInputMapping.setXpathDefinitionList(inputXpathDefinitionList);
		input.setInputMapping(xmlInputMapping);
		bucket.setInputs(inputList);
		queryOne.setName("query");
		outputOne.setTopic("output");
		outputOne.setBrokerName("wsEventBroker");
		outputTwo.setTopic("outputTwo");
		outputTwo.setBrokerName("wsEventBroker");
		xmlOutPutMapping.setMappingXMLText("<cep>Testing</cep>");
		outputOne.setOutputMapping(xmlOutPutMapping);
		elementOutputMapping.setDocumentElement("document");
		elementOutputMapping.setNamespace("anyNameSpace");
		outputEleXMLProperty.setName("elementMapping");
		outputEleXMLProperty.setXmlFieldName("xmlField");
		outputEleXMLProperty.setXmlFieldType("attribute");
		outputListXMLProperty.add(outputEleXMLProperty);
		elementOutputMapping.setProperties(outputListXMLProperty);
		outputTwo.setOutputMapping(elementOutputMapping);
		queryTwo.setName("queryTwo");
		expressionOne.setType("inlined");
		expressionOne.setText("Testing");
		expressiontwo.setType("key");
		expressiontwo.setText("TestingTwo");
		queryTwo.setExpression(expressionOne);
		queryTwo.setOutput(outputTwo);
		queryOne.setExpression(expressiontwo);
		queryList.add(queryOne);
		queryList.add(queryTwo);
		bucket.setQueries(queryList);
		super.setUp();

	}

	public void testOMBucket() throws Exception {
		bucketOM = BucketHelper.bucketToOM(bucket);
		assertTrue(bucketOM != null);
		bucket = BucketHelper.fromOM(bucketOM);
		assertTrue(bucket != null);
		assertTrue(bucket.getName().equals("TestBucket"));
		assertTrue(bucket.getDescription().equals("This is tesing Bucket"));
		assertTrue(bucket.getEngineProvider().equals(
				"DroolsFusionEngineProvider"));
		inputList = bucket.getInputs();
		queryList = bucket.getQueries();
		for (Input input : inputList) {
			assertTrue(input.getTopic().equals("TestingInput"));
			assertTrue(input.getBrokerName().equals("wsEventBroker"));
			xmlInputMapping = (XMLInputMapping) input.getInputMapping();
			assertTrue(xmlInputMapping.getStream().equals("TestingStream"));
			inputPropertyList = xmlInputMapping.getProperties();
			inputXpathDefinitionList = xmlInputMapping
					.getXpathNamespacePrefixes();
			for (XMLProperty property : inputPropertyList) {
				assertTrue(property.getName().equals("inputproperty"));
				assertTrue(property.getType().equals("java.lang.Integer"));
				assertTrue(property.getXpath().equals("xpath"));
			}
			for (XpathDefinition definition : inputXpathDefinitionList) {
				assertTrue(definition.getPrefix().equals("prefix"));
				assertTrue(definition.getNamespace().equals("nameSpace"));
			}
		}
		for (Query query : queryList) {
			if (query.getName().equals("query")) {
				outputOne = query.getOutput();
				expressionOne = query.getExpression();
				assertTrue(outputOne.getBrokerName().equals("wsEventBroker"));
				assertTrue(outputOne.getTopic().equals("output"));
				assertTrue(expressionOne.getType().equals("key"));
				assertTrue(expressionOne.getText().equals("TestingTwo"));
				xmlOutPutMapping = (XMLOutputMapping) outputOne
						.getOutputMapping();
				assertTrue(xmlOutPutMapping.getMappingXMLText().equals(
						"<cep>Testing</cep>"));
			} else if (query.getName().equals("queryTwo")) {
				outputTwo = query.getOutput();
				expressiontwo = query.getExpression();
				assertTrue(expressiontwo.getType().equals("inlined"));
				assertTrue(outputTwo.getTopic().equals("outputTwo"));
				assertTrue(outputTwo.getBrokerName().equals("wsEventBroker"));
				elementOutputMapping = (ElementOutputMapping) outputTwo
						.getOutputMapping();
				assertTrue(elementOutputMapping.getDocumentElement().equals(
						"document"));
				assertTrue(elementOutputMapping.getNamespace().equals(
						"anyNameSpace"));
				outputListXMLProperty = elementOutputMapping.getProperties();
				for (XMLProperty property : outputListXMLProperty) {
					assertTrue(property.getName().equals("elementMapping"));
					assertTrue(property.getXmlFieldName().equals("xmlField"));
					assertTrue(property.getXmlFieldType().equals("attribute"));
				}
			}

		}
	}
}
