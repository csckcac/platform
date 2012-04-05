package org.wso2.carbon.lb.endpoint.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.LBConfigParser;
import org.wso2.carbon.lb.endpoint.util.TenantDomainRangeContext;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceClusterDomainConfigParser {

    private static Log log = LogFactory.getLog(ServiceClusterDomainConfigParser.class);

    private static class SynchronizingClass {
    }

    private static final SynchronizingClass loadlock = new SynchronizingClass();
    private static final String CONFIG_FILENAME = "loadbalancer.conf";
    private static final String CONFIG_NS = "http://wso2.com/carbon/cloud/mgt/services";
    private static final String CLOUD_SERVICE_ELEMENT_NAME = "cloudService";

    /*   public static Map<String, TenantDomainRangeContext> loadCloudServicesConfiguration() throws Exception {
        Map<String, TenantDomainRangeContext> hostTenantDomainRangeContextMap = new HashMap<String, TenantDomainRangeContext>();
        synchronized (loadlock) {

            try {
                String configFileName = CarbonUtils.getCarbonConfigDirPath() + "/" + CONFIG_FILENAME;
                OMElement configElement = buildOMElement(new FileInputStream(configFileName));
                Iterator configChildIt = configElement.getChildrenWithName(new QName("services"));
                while (configChildIt.hasNext()) {
                    Object configChildObj = configChildIt.next();
                    if (!(configChildObj instanceof OMElement)) {
                        continue;
                    }
                    OMElement configChildEle = (OMElement) configChildObj;
                    OMElement configServiceChildEle = (OMElement) configElement.getChildrenWithName(new QName("services")).next();
                    for (Iterator iterator = configServiceChildEle.getChildrenWithName(new QName("service")); iterator.hasNext(); ) {
                        OMElement serviceElement = (OMElement) iterator.next();
                        System.out.println(serviceElement.toString());
                        String hostName = ((OMElement) (((OMElement) serviceElement.getChildrenWithName(new QName("hosts")).next()).getChildrenWithName(new QName("host")).next())).getText();
                        OMElement domaonsElement = (OMElement) serviceElement.getChildrenWithName(new QName("domains")).next();
                        TenantDomainRangeContext domainRangeContext = new TenantDomainRangeContext();
                        if (domaonsElement != null) {
                            for (Iterator iterator1 = domaonsElement.getChildrenWithName(new QName("domain")); iterator1.hasNext(); ) {
                                OMElement domainElement = (OMElement) iterator1.next();
                                String serviceName = ((OMElement) domainElement.getChildrenWithName(new QName("name")).next()).getText();
                                String tenantRangeString = ((OMElement) domainElement.getChildrenWithName(new QName("tenantRange")).next()).getText();
                                domainRangeContext.addTenantDomain(serviceName, tenantRangeString);

                            }

                        }
                        hostTenantDomainRangeContextMap.put(hostName, domainRangeContext);
                    }

                }
                return hostTenantDomainRangeContextMap;
            } catch (Exception e) {
                String msg = "Error in building the cloud service configuration.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
    }*/

    public static Map<String, TenantDomainRangeContext> loadCloudServicesConfiguration() throws Exception {
        Map<String, TenantDomainRangeContext> hostTenantDomainRangeContextMap = new HashMap<String, TenantDomainRangeContext>();
        synchronized (loadlock) {

            try {
                String configFileName = CarbonUtils.getCarbonConfigDirPath() + "/" + CONFIG_FILENAME;

                LBConfigParser lbConfigParser = new LBConfigParser();

                // Element which needs to drag from config file is "services"
                String elementName = "services";

                // Drag given tag(services tag) from given file
                String servicesContentSubString = lbConfigParser.dragConfigTagFromFile(configFileName, elementName);

                for (Map.Entry<String, String> mapEntry : lbConfigParser.separateServices(servicesContentSubString).entrySet()) {

                    TenantDomainRangeContext domainRangeContext = new TenantDomainRangeContext();
                    log.info(mapEntry.getKey() + ": ");
                    Object value = mapEntry.getValue();

                    String elementValue = lbConfigParser.getConfigElementFromString((String) value, "domains");

                    //if elementValue is null then don't proceed bellow step
                    if (elementValue != null) {
                        HashMap<String, String> topLevelElementMap = lbConfigParser.getAllTopLevelConfigElements(elementValue);

                        for (Map.Entry<String, String> entry : topLevelElementMap.entrySet()) {
                            String serviceName = entry.getKey();
                            String tenantRangeString = lbConfigParser.getConfigPropertyFromString(entry.getValue(), "tenant_range").get(0);
                            //log.info(entryKey + ": ");
                            //log.info(lbConfigParser.getConfigPropertyFromString(entryValue, "tenant_range").get(0));
                            domainRangeContext.addTenantDomain(serviceName, tenantRangeString);
                        }

                    }

                    ArrayList<String> propertyValueList = lbConfigParser.getConfigPropertyFromString((String) value, "hosts");

                    for (String hostName : propertyValueList) {
                        hostTenantDomainRangeContextMap.put(hostName, domainRangeContext);

                    }
                }


                return hostTenantDomainRangeContextMap;
            } catch (Exception e) {
                String msg = "Error in building the cloud service configuration.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
    }


    /**
     * builds the OMElement from the given inputStream
     *
     * @param inputStream, given input - inputStream
     * @return OMElement
     * @throws Exception, if building OMElement from the inputStream failed.
     */
    public static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        // create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        // get the root element (in this case the envelope)
        return builder.getDocumentElement();
    }

}