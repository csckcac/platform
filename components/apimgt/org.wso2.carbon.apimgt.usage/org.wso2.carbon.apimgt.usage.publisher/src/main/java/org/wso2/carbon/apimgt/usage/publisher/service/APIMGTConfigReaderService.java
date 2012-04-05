package org.wso2.carbon.apimgt.usage.publisher.service;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.apimgt.usage.publisher.exception.APIMGTDataPublisherException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class APIMGTConfigReaderService{

    String bamServerThriftPort;
    String bamServerURL;
    String bamServerUser;
    String bamServerPassword;
    String bamAgentTrustStore;
    String bamAgentTrustStorePassword;

    public APIMGTConfigReaderService() {
        String config = null;
        try {
            config = FileUtil.readFileToString(APIMgtUsagePublisherConstants.CONFIG_PATH);
            OMElement omElement = AXIOMUtil.stringToOM(config);

            OMElement apiMGTDataAgentConfig = omElement.getFirstChildWithName(new QName("apiMGTDataAgentConfig"));

            bamServerThriftPort = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamServerThriftPort")).getText();
            bamServerURL = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamServerURL")).getText();
            bamServerUser = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamServerUser")).getText();
            bamServerPassword = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamServerPassword")).getText();
            bamAgentTrustStore = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamAgentTrustStore")).getText();
            bamAgentTrustStore = CarbonUtils.getCarbonHome() + File.separator + bamAgentTrustStore;
            bamAgentTrustStorePassword = apiMGTDataAgentConfig.getFirstChildWithName(
                    new QName("bamAgentTrustStorePassword")).getText();


        } catch (IOException e) {
            String msg = "Failed to read amConfig.xml configuration file from path" +
                         APIMgtUsagePublisherConstants.CONFIG_PATH;
            throw new APIMGTDataPublisherException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error occured while reading amConfig.xml configuration file";
            throw new APIMGTDataPublisherException(msg, e);
        }
    }

    public String getBamServerThriftPort() {
        return bamServerThriftPort;
    }

    public String getBamAgentTrustStore() {
        return bamAgentTrustStore;
    }

    public String getBamServerPassword() {
        return bamServerPassword;
    }

    public String getBamServerUser() {
        return bamServerUser;
    }

    public String getBamServerURL() {
        return bamServerURL;
    }
    public String getBamAgentTrustStorePassword() {
        return bamAgentTrustStorePassword;
    }
}
