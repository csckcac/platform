package org.wso2.bps.samples.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public class SamplesInvoker {
    private static final Log log = LogFactory.getLog(SamplesInvoker.class);

    public static final String ACTION = "action";
    public static final String REQ_MSG = "requestMsg";
    public static final String SVC_NAME = "serviceName";

    private static OMElement requestPayload = null;
    private static String action = null;
    private static String serviceName = null;

    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0) {
            result = def;
        }
        return result;
    }

    public static void main(String args[]) {
        try {
            executeClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadServiceProperties(String propFilePath) {
        File f = new File(propFilePath);
        Properties properties = new Properties();
        FileInputStream fin = null;
        ByteArrayInputStream bin = null;
        try {
            fin = new FileInputStream(f);
            properties.load(fin);
            action = properties.getProperty(ACTION).trim();
            String xmlStr = properties.getProperty(REQ_MSG).trim();
            serviceName = properties.getProperty(SVC_NAME).trim();
            bin = new ByteArrayInputStream(xmlStr.getBytes());
            StAXOMBuilder builder = new StAXOMBuilder(bin);
            requestPayload = builder.getDocumentElement();
        } catch (IOException e) {
            System.out.println("Exception in reading properties file" + e.getMessage());
            System.exit(0);
        } catch (XMLStreamException xe) {
            System.out.println("Error in the xml request msg " + xe.getMessage());
            System.exit(0);
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
            }
            try {
                bin.close();
            } catch (IOException e) {
            }
        }

    }

    /**
     * Check whether a property file exist for the given sample name
     *
     * @param sampleName
     * @param sampleDir
     * @return
     */

    private static String getPropertyFile(String sampleName, String sampleDir) {
        // Check whether the specified sample exists in the repository/samples/bpel directory
        String samplePath = sampleDir + File.separator + "resources" + File.separator + "bpel" +
                            File.separator + sampleName + ".properties";
        File sample = new File(samplePath);
        if (sample.exists()) {
            return samplePath;
        }

        samplePath = sampleDir + File.separator + "resources" + File.separator + "humantask" +
                     File.separator + sampleName + ".properties";

        sample = new File(samplePath);
        if (sample.exists()) {
            return samplePath;
        }
        return null;
    }

    private static void printResult(OMElement element) throws Exception {
        System.out.println("Received response from the service");
        System.out.println(element.toStringWithConsume());
        System.exit(0);
    }

    public static void executeClient() throws Exception {

        String soapVer = getProperty("soapver", "soap11");
        String addUrl = getProperty("addurl", null);
        String trpUrl = getProperty("trpurl", null);
        String prxUrl = getProperty("prxurl", null);
        String repository = getProperty("repository", "client_repo");
        String sampleName = getProperty("sample", null);
        String sampleDir = getProperty("sampleDir", ".");


        String propertyFile = getPropertyFile(sampleName, sampleDir);
        if (propertyFile != null) {
            loadServiceProperties(propertyFile);
        } else {
            System.out.println("Matching properties file not found for the specified sample");
            System.exit(0);
        }

        ConfigurationContext configContext = null;

        ServiceClient serviceClient;

        if (repository != null && !"null".equals(repository)) {
            configContext =
                    ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(repository,
                             repository + File.separator + "conf" + File.separator + "axis2.xml");
            serviceClient = new ServiceClient(configContext, null);
        } else {
            serviceClient = new ServiceClient();
        }

        Options options = new Options();
        options.setAction(action);

        if (addUrl != null && !"null".equals(addUrl + serviceName)) {
            options.setTo(new EndpointReference(addUrl));
        }
        if (trpUrl != null && !"null".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl + serviceName);
        }
        if (prxUrl != null && !"null".equals(prxUrl)) {
            HttpTransportProperties.ProxyProperties proxyProperties =
                    new HttpTransportProperties.ProxyProperties();
            URL url = new URL(prxUrl);
            proxyProperties.setProxyName(url.getHost());
            proxyProperties.setProxyPort(url.getPort());
            proxyProperties.setUserName("");
            proxyProperties.setPassWord("");
            proxyProperties.setDomain("");
            options.setProperty(HTTPConstants.PROXY, proxyProperties);
        }

        if ("soap12".equals(soapVer)) {
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }
        serviceClient.setOptions(options);
        OMElement response = serviceClient.sendReceive(requestPayload);
        printResult(response);
        return;
    }
}
