package org.wso2.carbon.gadget.ide.util;

import org.apache.axiom.om.*;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Utils {
    private static DataSourceInformationRepositoryService dataSourceService;
    private static RegistryService registryService;
    private static UserRegistry configReg;
    private static UserRegistry userRegistry;

    public static DataSourceInformationRepositoryService getCarbonDataSourceService() {
        return Utils.dataSourceService;
    }

    public static void setCarbonDataSourceService(DataSourceInformationRepositoryService service) {
        dataSourceService = service;
    }

    public static void setRegistryService(RegistryService service) {
        registryService = service;
        try {
            userRegistry = service.getConfigSystemRegistry();
        } catch (RegistryException e) {
            //ignore
        }
    }


    public static OMDocument ResultSet2DOM(ResultSet rs) throws SQLException {
        OMDocument myDocument = null;
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        myDocument = omFactory.createOMDocument();
        OMElement root = omFactory.createOMElement(QName.valueOf("result"));
        myDocument.addChild(root);

        ResultSetMetaData rsmd = rs.getMetaData();

        OMElement element, row;
        String value;

        while (rs.isLast() == false) {
            rs.next();
            row = omFactory.createOMElement(QName.valueOf("row"));
            root.addChild(row);
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                element = omFactory.createOMElement(new QName(rsmd.getColumnLabel(i)));

                value = rs.getString(i);
                if (value == null) {
                    OMAttribute omAttribute = omFactory.createOMAttribute(
                            "xsi:nil",
                            omFactory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi"),
                            "true"
                    );
                } else {
                    element.addChild(
                            omFactory.createOMText(rs.getString(i)));
                }
                row.addChild(element);
            }
        }
        return myDocument;
    }

    public static UserRegistry getUserRegistry() {
        return userRegistry;
    }
}
