package org.wso2.carbon.bam.clustermonitor.ui.data;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.FileInputStream;

public class ConfigReader {

    private static final Log log = LogFactory.getLog(ConfigReader.class);

    private static String clusterIndexName;

    private static String serviceIndexName;

    private static String operationIndexName;

    private static String proxyServiceIndexName;


    public static String getProxyServiceIndexName() {
        return proxyServiceIndexName;
    }

    public static void setProxyServiceIndexName(String proxyServiceIndexName) {
        ConfigReader.proxyServiceIndexName = proxyServiceIndexName;
    }

    public static String getClusterIndexName() {
        return clusterIndexName;
    }

    public static void setClusterIndexName(String clusterIndexName) {
        ConfigReader.clusterIndexName = clusterIndexName;
    }

    public static String getServiceIndexName() {
        return serviceIndexName;
    }

    public static void setServiceIndexName(String serviceIndexName) {
        ConfigReader.serviceIndexName = serviceIndexName;
    }

    public static String getOperationIndexName() {
        return operationIndexName;
    }

    public static void setOperationIndexName(String operationIndexName) {
        ConfigReader.operationIndexName = operationIndexName;
    }


    public static void getClusterMonitorConfig() {
        if (getClusterIndexName() == null) {
            try {
                String pathToConfig = CarbonUtils.getCarbonConfigDirPath() + "/" + "cluster-monitor.xml";
                OMElement clusterMonitorOMElement = new StAXOMBuilder(new FileInputStream(pathToConfig)).getDocumentElement();

                OMElement monitorOMElement = clusterMonitorOMElement.getFirstChildWithName(new QName("monitor"));

                OMElement clusterOMElement = monitorOMElement.getFirstChildWithName(new QName("cluster"));
                OMElement serviceOMElement = monitorOMElement.getFirstChildWithName(new QName("service"));
                OMElement operationOMElement = monitorOMElement.getFirstChildWithName(new QName("operation"));
                OMElement proxyOMElement = monitorOMElement.getFirstChildWithName(new QName("proxy"));

                OMElement clusterIndexNameOMElement = clusterOMElement.getFirstChildWithName(new QName("indexName"));
                OMElement serviceIndexNameOMElement = serviceOMElement.getFirstChildWithName(new QName("indexName"));
                OMElement operationIndexNameOMElement = operationOMElement.getFirstChildWithName(new QName("indexName"));
                                OMElement proxyIndexNameOMElement = proxyOMElement.getFirstChildWithName(new QName("indexName"));

                String clusterIndexName = clusterIndexNameOMElement.getText();
                String serviceIndexName = serviceIndexNameOMElement.getText();
                String operationIndexName = operationIndexNameOMElement.getText();
                String proxyIndexName = proxyIndexNameOMElement.getText();

                setClusterIndexName(clusterIndexName);
                setOperationIndexName(operationIndexName);
                setServiceIndexName(serviceIndexName);
                setProxyServiceIndexName(proxyIndexName);

            } catch (Exception e) {
                log.error("Error occurred while reading config file", e);
            }
        }
    }
}
