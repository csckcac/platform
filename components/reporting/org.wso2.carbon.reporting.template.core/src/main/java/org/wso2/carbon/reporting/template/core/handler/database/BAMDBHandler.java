/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.reporting.template.core.handler.database;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.BAMDBClient;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.chart.DataDTO;
import org.wso2.carbon.reporting.template.core.util.chart.SeriesDTO;
import org.wso2.carbon.reporting.template.core.util.table.ColumnDTO;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;


public class BAMDBHandler {
    private static String TIMESTAMP = "timeStamp";

    private static Log log = LogFactory.getLog(BAMDBHandler.class);

    /**
     * @param xmlResult the result of the BAM DB
     * @return Map[] - each map will consists of column_name(key), value_of_column(value) for each row
     */
    public Map[] getDBData(OMElement xmlResult) throws ReportingException {
        String xmlString = xmlResult.toString().replaceAll("&lt;", "<");
        xmlString = xmlString.replaceAll("&gt;", ">");
        OMElement rows = getOMElement(xmlString);
        int rowCont = getRowCount(rows);
        Map[] rowMap = new Map[rowCont];

        initMap(rowMap);
        Iterator rowIterator = rows.getChildren();
        int rowIndex = 0;
        while (rowIterator.hasNext()) {
            OMElement row = (OMElement) rowIterator.next();
            insertHashMapRow(rowMap[rowIndex], row);
            rowIndex++;
        }
        return rowMap;

    }

    private OMElement getOMElement(String xmlResult) throws ReportingException {
        InputStream is = new ByteArrayInputStream(xmlResult.getBytes());
        StAXOMBuilder stAXOMBuilder;
        XMLInputFactory xmlInputFactory;
        XMLStreamReader xmlStreamReader = null;
        xmlInputFactory = XMLInputFactory.newInstance();
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
            stAXOMBuilder = new StAXOMBuilder(xmlStreamReader);
            OMElement returnElement = stAXOMBuilder.getDocumentElement();
            Iterator iterator = returnElement.getChildElements();
            OMElement rows = null;
            while (iterator.hasNext()) {
                rows = (OMElement) iterator.next();
                break;
            }
            rows.build();
            return rows;
        } catch (XMLStreamException e) {
            throw new ReportingException("failed to read the cassendra result xml", e);
        }
    }

    private void initMap(Map[] rowMap) {
        for (int i = 0; i < rowMap.length; i++) {
            rowMap[i] = new HashMap<String, String>();
        }
    }

    private int getRowCount(OMElement rows) {
        Iterator rowIter = rows.getChildrenWithName(new QName("row"));
        int rowCount = 0;
        while (rowIter.hasNext()) {
            rowCount++;
            rowIter.next();
        }
        return rowCount;
    }


    private void insertHashMapRow(Map row, OMElement rowElement) {
        Iterator iterator = rowElement.getChildren();
        while (iterator.hasNext()) {
            OMElement column = (OMElement) iterator.next();
            String key = column.getQName().getLocalPart();
            String value = column.getText();
            row.put(key, value);
        }
    }


    public Map[] createMapDataSource(TableReportDTO tableReport) throws ReportingException {
        ColumnDTO[] columns = tableReport.getColumns();

        HashMap<String, Map[]> columnFamilies = getColumnFamilyData(columns);
        Map[] reportRows = createReportRows(columns, columnFamilies);
        return reportRows;
    }


    private HashMap<String, Map[]> getColumnFamilyData(ColumnDTO[] columns) throws ReportingException {
        HashMap<String, Map[]> columnFamilies = new HashMap<String, Map[]>();

        for (int i = 0; i < columns.length; i++) {
            ColumnDTO aColumn = columns[i];
            if (!columnFamilies.containsKey(aColumn.getColumnFamilyName())) {
                BAMDBClient BAMDBClient = ClientFactory.getBAMDBClient();
                OMElement xmlResult = BAMDBClient.queryColumnFamily(aColumn.getColumnFamilyName(), null, null);
                Map[] rows = getDBData(xmlResult);
                columnFamilies.put(aColumn.getColumnFamilyName(), rows);
            }
        }
        return columnFamilies;
    }


    private Map[] createReportRows(ColumnDTO[] columns, HashMap<String, Map[]> columnFamilies) {
        ArrayList<Map> reportRows = new ArrayList<Map>();
        ColumnDTO primaryColumn = getPrimaryColumn(columns);

        Map[] primaryTable = columnFamilies.get(primaryColumn.getColumnFamilyName());
        int primaryColId = getColumnId(columns, primaryColumn);

        for (int i = 0; i < primaryTable.length; i++) {
            if (primaryTable[i].containsKey(primaryColumn.getColumnName())) {
                HashMap row = new HashMap();
                row.put(String.valueOf(primaryColId + 1), primaryTable[i].get(primaryColumn.getColumnName()));

                for (int j = 0; j < columns.length; j++) {
                    if (j != primaryColId) {
                        //Assuming that the all data is in one table..
                        String colValue = primaryTable[i].get(columns[j].getColumnName()).toString();
                        row.put(String.valueOf(j + 1), colValue);
                    }
                }
                reportRows.add(row);
            }
        }
        return convertToMap(reportRows);

    }


    private int getColumnId(ColumnDTO[] columns, ColumnDTO aColumn) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(aColumn)) {
                return i;
            }
        }
        return -1;
    }


    private Map[] convertToMap(ArrayList<Map> reportRows) {
        Map[] maps = new Map[reportRows.size()];
        for (int i = 0; i < reportRows.size(); i++) {
            maps[i] = reportRows.get(i);
        }
        return maps;
    }


    private ColumnDTO getPrimaryColumn(ColumnDTO[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].isPrimaryColumn())
                return columns[i];
        }
        return columns[0];
    }


    public Map[] createMapDataSource(ChartReportDTO chartReportReport) throws ReportingException {
        SeriesDTO[] series = chartReportReport.getCategorySeries();

        HashMap<String, Map[]> columnFamilies = getColumnFamilyData(series);
        if (chartReportReport.getReportType().contains("xy")) {
            return createXYReportRows(series, columnFamilies);
        } else {
            return createReportRows(series, columnFamilies);
        }
    }


    private HashMap<String, Map[]> getColumnFamilyData(SeriesDTO[] series) throws ReportingException {
        HashMap<String, Map[]> columnFamilies = new HashMap<String, Map[]>();

        for (int i = 0; i < series.length; i++) {
            SeriesDTO aSeries = series[i];
            DataDTO xData = aSeries.getXdata();
            if (!columnFamilies.containsKey(xData.getDsTableName())) {
                BAMDBClient BAMDBClient = ClientFactory.getBAMDBClient();
                OMElement xmlResult = BAMDBClient.queryColumnFamily(xData.getDsTableName(), null, null);
                Map[] rows = getDBData(xmlResult);
                columnFamilies.put(xData.getDsTableName(), rows);
            }
            DataDTO yData = aSeries.getYdata();
            if (!columnFamilies.containsKey(yData.getDsTableName())) {
                BAMDBClient BAMDBClient = ClientFactory.getBAMDBClient();
                OMElement xmlResult = BAMDBClient.queryColumnFamily(yData.getDsTableName(), null, null);
                Map[] rows = getDBData(xmlResult);
                columnFamilies.put(yData.getDsTableName(), rows);
            }
        }
        return columnFamilies;
    }

    private Map[] createXYReportRows(SeriesDTO[] series, HashMap<String, Map[]> columnFamilies) throws ReportingException {
        ArrayList<Map> reportRows = new ArrayList<Map>();

        int rowId = 0;
        boolean loop = isDataExists(columnFamilies, rowId);

        while (loop) {
            HashMap row = new HashMap();

            for (SeriesDTO aSeries : series) {
                DataDTO xData = aSeries.getXdata();
                if (row.get(xData.getFieldId()) == null) {
                    String value = getValue(columnFamilies, xData.getDsTableName(), rowId, xData.getDsColumnName());

                    if (value != null) {
                        Number xNumber = isNumber(value);
                        if (xNumber != null) {
                            row.put(xData.getFieldId(), xNumber);
                        } else {
                            log.error("Unsupported value for X axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                            throw new ReportingException("Unsupported value for X axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                        }

                        DataDTO yData = aSeries.getYdata();
                        String yValue = getValue(columnFamilies, yData.getDsTableName(), rowId, yData.getDsColumnName());
                        Number yNumber = isNumber(yValue);
                        if (yNumber != null) {
                            row.put(yData.getFieldId(), yNumber);
                        } else {
                            log.error("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                            throw new ReportingException("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                        }
                    } else {
                        row.put(xData.getFieldId(), "No Data");
                        row.put(aSeries.getYdata().getFieldId(), 0);
                    }
                } else {
                    DataDTO yData = aSeries.getYdata();
                    String yValue = getValue(columnFamilies, yData.getDsTableName(), rowId, yData.getDsColumnName());
                    Number yNumber = isNumber(yValue);
                    if (yNumber != null) {
                        row.put(yData.getFieldId(), yNumber);
                    } else {
                        log.error("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                        throw new ReportingException("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                    }
                }
            }
            reportRows.add(row);
            rowId++;
            loop = isDataExists(columnFamilies, rowId);
        }

        Map[] mapReportRows = convertToMap(reportRows);
        return mapReportRows;
    }

    private Map[] createReportRows(SeriesDTO[] series, HashMap<String, Map[]> columnFamilies) throws ReportingException {
        ArrayList<Map> reportRows = new ArrayList<Map>();

        int rowId = 0;
        boolean loop = isDataExists(columnFamilies, rowId);

        while (loop) {
            HashMap row = new HashMap();

            for (SeriesDTO aSeries : series) {
                DataDTO xData = aSeries.getXdata();
                if (row.get(xData.getFieldId()) == null) {
                    String value = getValue(columnFamilies, xData.getDsTableName(), rowId, xData.getDsColumnName());

                    if (value != null) {
                        row.put(xData.getFieldId(), value);

                        DataDTO yData = aSeries.getYdata();
                        String yValue = getValue(columnFamilies, yData.getDsTableName(), rowId, yData.getDsColumnName());
                        Number yNumber = isNumber(yValue);
                        if (yNumber != null) {
                            row.put(yData.getFieldId(), yNumber);
                        } else {
                            throw new ReportingException("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                        }
                    } else {
                        row.put(xData.getFieldId(), "No Data");
                        row.put(aSeries.getYdata().getFieldId(), 0);
                    }
                } else {
                    DataDTO yData = aSeries.getYdata();
                    String yValue = getValue(columnFamilies, yData.getDsTableName(), rowId, yData.getDsColumnName());
                    Number yNumber = isNumber(yValue);
                    if (yNumber != null) {
                        row.put(yData.getFieldId(), yNumber);
                    } else {
                        throw new ReportingException("Unsupported value for Y axis value.." + aSeries.getName() + "\n Only Numbers can be used to fill the report");
                    }
                }
            }
            reportRows.add(row);
            rowId++;
            loop = isDataExists(columnFamilies, rowId);
        }

        Map[] mapReportRows = convertToMap(reportRows);
        return mapReportRows;
    }

    private String getValue(HashMap<String, Map[]> columnFamilies, String columnFamily, int rowId, String columnName) {
        Map[] columnFamilyData = columnFamilies.get(columnFamily);
        if (columnFamilyData.length > rowId) {
            Object value = columnFamilyData[rowId].get(columnName);
            if (value != null) {
                return value.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Number isNumber(String strNumber) {
        try {
            Integer intValue = Integer.parseInt(strNumber);
            return intValue;
        } catch (NumberFormatException ex) {
            try {
                Double doubleValue = Double.parseDouble(strNumber);
                return doubleValue;
            } catch (NumberFormatException dEx) {
                return null;
            }
        }
    }

    private boolean isDataExists(HashMap<String, Map[]> columnFamilies, int rowId) {
        Collection<Map[]> allRows = columnFamilies.values();
        Iterator<Map[]> iterator = allRows.iterator();
        while (iterator.hasNext()) {
            Map[] rows = iterator.next();
            if (rows.length > rowId) {
                return true;
            }
        }
        return false;
    }

}
