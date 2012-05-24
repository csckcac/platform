package org.wso2.carbon.hosting.mgt.utils;

import javax.activation.DataHandler;

/**
 *  Represents a file data set which is uploaded from the Management Console
 */
public class FileUploadData {

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
