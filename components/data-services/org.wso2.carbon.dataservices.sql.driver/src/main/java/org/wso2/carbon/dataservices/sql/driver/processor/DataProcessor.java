package org.wso2.carbon.dataservices.sql.driver.processor;

import org.wso2.carbon.dataservices.sql.driver.TResultSet;

import java.sql.SQLException;

public interface DataProcessor {

    public abstract TResultSet process() throws SQLException;
    
}
