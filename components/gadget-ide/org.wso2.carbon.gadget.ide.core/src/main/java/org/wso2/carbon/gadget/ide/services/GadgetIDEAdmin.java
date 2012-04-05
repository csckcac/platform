package org.wso2.carbon.gadget.ide.services;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.commons.datasource.factory.DataSourceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.gadget.ide.sigdom.CachedDomProvider;
import org.wso2.carbon.gadget.ide.sigdom.DomProvider;
import org.wso2.carbon.gadget.ide.sigdom.HTTPDomProvider;
import org.wso2.carbon.gadget.ide.util.Utils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.wsdl2form.Util;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GadgetIDEAdmin {
    private static XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final String ENDPOINT_STRING_EXP = "/services/service";
    private static final XPathExpression ENDPOINT_XPATH_EXP;
    private static final String ENDPOINT_ATTRIBUTE = "endpoint";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String OPERATIONS_STRING_EXP = "/operations/operation";
    private static final String TEMP_GENERATED_PATH = "/generated";
    private static final String TEMP_XML_GENERATED_PATH = TEMP_GENERATED_PATH + "/gadget.xml";
    private static final String TEMP_JS_GENERATED_PATH = TEMP_GENERATED_PATH + "/js/";
    private static final String DASHBOARD_PATH = "/repository/dashboards/gadgets/";
    private static final String REGISTRY_RESOURCE_PREFIX = "/registry/resource/_system/config";

    private DomProvider provider;

    public GadgetIDEAdmin(DomProvider provider) {
        this.provider = provider;
    }

    public GadgetIDEAdmin() {
        this(new CachedDomProvider(new HTTPDomProvider(),20));
    }

    static {
        XPathExpression xpath;
        try {
            xpath = XPATH.compile(ENDPOINT_STRING_EXP);
        } catch (XPathExpressionException e) {
            //ignore
            xpath = null;
        }
        ENDPOINT_XPATH_EXP = xpath;
    }


    private String getUserRegistryPath(UserRegistry registry) {
        return "/users/" + registry.getUserName() + "/gadgetide";
    }

    private String getTempSettingsPath(UserRegistry registry) {
        return getUserRegistryPath(registry) + "/temp.xml";
    }

    private DOMSource getSigDom(String uri) {
        return provider.getSigDom(uri);
    }

    public List<String> getEndpoints(String uri) throws Exception {
        NodeList nodes = (NodeList) ENDPOINT_XPATH_EXP.evaluate(getSigDom(uri).getNode(), XPathConstants.NODESET);
        ArrayList<String> output = new ArrayList<String>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            output.add(item.getAttributes().getNamedItem(ENDPOINT_ATTRIBUTE).getTextContent());
        }
        return output;
    }

    public List<String> getOperations(String uri, String endpoint) throws Exception {
        String expression = ENDPOINT_STRING_EXP + "[@" + ENDPOINT_ATTRIBUTE + "='" + endpoint + "']" + OPERATIONS_STRING_EXP;
        NodeList nodes = (NodeList) XPATH.evaluate(expression, getSigDom(uri).getNode(), XPathConstants.NODESET);
        ArrayList<String> output = new ArrayList<String>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            output.add(item.getAttributes().getNamedItem(NAME_ATTRIBUTE).getTextContent());
        }
        return output;
    }

    public String getStub(String uri) throws Exception {
        ByteArrayOutputStream jsStubOutputStream = new ByteArrayOutputStream();
        Result jsStubResult = new StreamResult(jsStubOutputStream);
        Util.generateStub(getSigDom(uri), jsStubResult, null);
        return jsStubOutputStream.toString();
    }

    public String getGeneratedFile(String fileName) throws Exception {
        UserRegistry registry = Utils.getUserRegistry();
        String userRegistryPath = getUserRegistryPath(registry);
        Resource generatedFile = registry.get(userRegistryPath + fileName);
        return new String((byte[]) generatedFile.getContent());
    }

    public List<String> listGeneratedFiles() throws Exception {
        List<String> list = new ArrayList<String>();
        UserRegistry registry = Utils.getUserRegistry();
        list.add(TEMP_XML_GENERATED_PATH);
        try {
            String userRegistryPath = getUserRegistryPath(registry);
            Collection jsFiles = (Collection) registry.get(userRegistryPath + TEMP_JS_GENERATED_PATH);
            for (String filename : jsFiles.getChildren()) {
                list.add(filename.substring(userRegistryPath.length()));
            }
        } catch (ResourceNotFoundException e) {
            //ignore : it is possible to have no JS files
        }
        return list;
    }

    public String deploy(String gadgetXmlName) throws Exception {
        UserRegistry registry = Utils.getUserRegistry();
        String userRegistryPath = getUserRegistryPath(registry);


        //moving gadget.xml
        if (!gadgetXmlName.toLowerCase().endsWith(".xml")) {
            gadgetXmlName = gadgetXmlName + ".xml";
        }
        String newXmlPath = DASHBOARD_PATH + gadgetXmlName;
        registry.copy(
                userRegistryPath + TEMP_XML_GENERATED_PATH,
                newXmlPath
        );

        //moving js files
        try {
            Collection jsFiles = (Collection) registry.get(userRegistryPath + TEMP_JS_GENERATED_PATH);
            for (String filePath : jsFiles.getChildren()) {
                String filename = filePath.split(TEMP_JS_GENERATED_PATH)[1];
                registry.copy(filePath, DASHBOARD_PATH + "js/" + filename);
            }
        } catch (ResourceNotFoundException e) {
            //ignore : it is possible to have no JS files
        }

        //adding static files
        InputStream staticJsInput = GadgetIDEAdmin.class.getResourceAsStream("js/gadgetide-client.js");
        Resource staticJsResource = registry.newResource();
        staticJsResource.setContentStream(staticJsInput);
        registry.put(DASHBOARD_PATH + "js/gadgetide-client.js", staticJsResource);

        InputStream staticCssInput = GadgetIDEAdmin.class.getResourceAsStream("css/gadgetide-client.css");
        Resource staticCssResource = registry.newResource();
        staticCssResource.setContentStream(staticCssInput);
        registry.put(DASHBOARD_PATH + "css/gadgetide-client.css", staticCssResource);

        return REGISTRY_RESOURCE_PREFIX + newXmlPath;
    }

    public boolean generateCode() throws Exception {

        UserRegistry registry = Utils.getUserRegistry();
        Resource settingsResource = registry.get(getTempSettingsPath(registry));
        InputStream xmlInput = settingsResource.getContentStream();
        InputStream xsltInput = GadgetIDEAdmin.class.getResourceAsStream("xslt/gadget-create.xsl");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document ret = builder.parse(xmlInput);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Util.transform(new DOMSource(ret), new StreamSource(xsltInput), new StreamResult(output), null);


        Resource gadgetXMLResource = registry.newResource();
        gadgetXMLResource.setMediaType("application/xml");
        gadgetXMLResource.setContent(output.toByteArray());

        registry.delete(getUserRegistryPath(registry) + TEMP_GENERATED_PATH);

        registry.put(getUserRegistryPath(registry) + TEMP_XML_GENERATED_PATH, gadgetXMLResource);

        NodeList nodes = (NodeList) XPATH.evaluate("//units/unit/state/config[wsdlUrl]", ret, XPathConstants.NODESET);
        generateStubsForDataSources(nodes, registry);

        return true;
    }

    private void generateStubsForDataSources(NodeList nodes, UserRegistry registry) throws Exception {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Node idNode = getChildByName(node, "operation");
            String id = idNode.getTextContent();

            Node uriNode = getChildByName(node, "wsdlUrl");
            Resource jsStubResource = registry.newResource();
            jsStubResource.setMediaType("text/javascript");
            jsStubResource.setContent(getStub(uriNode.getTextContent()));

            registry.put(getUserRegistryPath(registry) + TEMP_JS_GENERATED_PATH + id + ".js", jsStubResource);
        }
    }

    private static Node getChildByName(Node root, String name) {
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node child = childNodes.item(j);
            if (child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public boolean saveTempSettings(String settings) throws Exception {
        UserRegistry registry = Utils.getUserRegistry();
        Resource resource = registry.newResource();
        resource.setContent(settings);
        resource.setMediaType("application/xml");
        registry.put(getTempSettingsPath(registry), resource);
        return true;
    }

    public String getOperationSig(String uri, String endpoint, String operation) throws Exception {
        String expression = ENDPOINT_STRING_EXP + "[@" + ENDPOINT_ATTRIBUTE + "='" + endpoint + "']" + OPERATIONS_STRING_EXP + "[@name='" + operation + "']";
        Node node = (Node) XPATH.evaluate(expression, getSigDom(uri).getNode(), XPathConstants.NODE);
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    public List<String> getDataSourceNames() throws Exception {
        List<String> dataSourceNames = new ArrayList<String>();
        DataSourceInformationRepository dataSourceRepository = Utils.getCarbonDataSourceService().
                getDataSourceInformationRepository();
        Iterator<DataSourceInformation> allDataSourceInformation = dataSourceRepository.getAllDataSourceInformation();
        while (allDataSourceInformation.hasNext()) {
            DataSourceInformation dataSourceInformation = allDataSourceInformation.next();
            dataSourceNames.add(dataSourceInformation.getAlias());
        }
        return dataSourceNames;
    }

    public OMElement executeSQL(String dataSourceName, String sql, String[] parameters) throws Exception {
        DataSourceInformationRepository dataSourceRepository = Utils.getCarbonDataSourceService().
                getDataSourceInformationRepository();
        DataSourceInformation information = dataSourceRepository.getDataSourceInformation(dataSourceName);
        DataSource dataSource = DataSourceFactory.createDataSource(information);
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        //TODO: use a prepared statement instead
        StringBuilder builder = new StringBuilder();
        String[] sqlParts = sql.split("\\?");
        for (int i = 0; i < sqlParts.length; i++) {
            String sqlPart = sqlParts[i];
            builder.append(sqlPart);
            if (i < parameters.length) {
                builder.append(parameters[i]);
            }
        }

        ResultSet resultSet = statement.executeQuery(builder.toString());
        return Utils.ResultSet2DOM(resultSet).getOMDocumentElement();
    }

    public List<String> getTableNames(String dataSourceName) throws Exception {
        List<String> tableNames = new ArrayList<String>();
        DataSourceInformationRepository dataSourceRepository = Utils.getCarbonDataSourceService().
                getDataSourceInformationRepository();
        DataSourceInformation information = dataSourceRepository.getDataSourceInformation(dataSourceName);
        DataSource dataSource = DataSourceFactory.createDataSource(information);
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet tablesResultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
        while (tablesResultSet.next()) {
            tableNames.add(tablesResultSet.getString("TABLE_NAME"));
        }
        tablesResultSet.close();

        return tableNames;
    }

}