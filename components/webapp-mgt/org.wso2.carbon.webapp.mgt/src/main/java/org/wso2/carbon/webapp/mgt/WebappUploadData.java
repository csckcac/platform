package org.wso2.carbon.webapp.mgt;

import javax.activation.DataHandler;

/**
 *  Represents a Webapp data set which is uploaded from the Management Console
 */
public class WebappUploadData {

    private String fileName;
    private DataHandler dataHandler;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }
}
