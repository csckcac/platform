/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.platform.test.core.utils.dashboardutils.DashboardVariables;
import org.wso2.platform.test.core.utils.dbutils.MySqlDatabaseManager;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.xml.sax.SAXException;
import sun.misc.BASE64Encoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Class will read testNG result xml and send result data to the mySQL database
 */
public class TestResultDeployer {

    private static final Log log = LogFactory.getLog(MySqlDatabaseManager.class);
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static BASE64Encoder enc = new BASE64Encoder();

    public void writeResult(String testResultFilePath) {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        DashboardVariables dashboardVariables = environmentBuilder.getFrameworkSettings().getDashboardVariables();
        String databaseName = dashboardVariables.getDbName();

        String lastBuildNumber = null;
        String lastSuiteID = null;
        String lastTestClassID = null;
        String lastTestCaseID = null;

        try {
            MySqlDatabaseManager mySqlDatabaseManager = new MySqlDatabaseManager(dashboardVariables.getJdbcUrl(),
                                                                                 dashboardVariables.getDbUserName(),
                                                                                 dashboardVariables.getDbPassword());


            File file = new File(testResultFilePath);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);

            //getting suite list
            NodeList suiteNodes = doc.getElementsByTagName("suite");
            for (int i = 0; i < suiteNodes.getLength(); i++) {
                Element suiteElement = (Element) suiteNodes.item(i);


                ResultSet resultSet = mySqlDatabaseManager.executeQuery
                        ("SELECT WA_BUILD_NUMBER FROM " + databaseName + ".WA_BUILD_HISTORY ORDER BY " +
                         "WA_BUILD_NUMBER DESC LIMIT 1 ");
                while (resultSet.next()) {
                    lastBuildNumber = resultSet.getString("WA_BUILD_NUMBER");
                }

                mySqlDatabaseManager.execute("INSERT INTO " + databaseName + ".WA_TEST_SUITE_DETAIL VALUES"
                                             + "(" + lastBuildNumber + ",NULL,'" + suiteElement.
                        getAttribute("name") + "'," + suiteElement.getAttribute("duration-ms")
                                             + ",'" + suiteElement.getAttribute("started-at")
                                             + "','" + suiteElement.getAttribute("finished-at") + "')");

                NodeList testClassNode = suiteElement.getElementsByTagName("test");
                for (int j = 0; j < testClassNode.getLength(); j++) {
                    Element testClassElement = (Element) testClassNode.item(j);


                    ResultSet testResultSet = mySqlDatabaseManager.executeQuery
                            ("SELECT WA_BUILD_NUMBER FROM " + databaseName + ".WA_BUILD_HISTORY ORDER BY " +
                             "WA_BUILD_NUMBER DESC LIMIT 1 ");
                    while (testResultSet.next()) {
                        lastBuildNumber = testResultSet.getString("WA_BUILD_NUMBER");
                    }

                    testResultSet = mySqlDatabaseManager.executeQuery
                            ("SELECT WA_TEST_SUITE_ID FROM " + databaseName + ".WA_TEST_SUITE_DETAIL ORDER BY " +
                             "WA_TEST_SUITE_ID DESC LIMIT 1 ");
                    while (testResultSet.next()) {
                        lastSuiteID = testResultSet.getString("WA_TEST_SUITE_ID");
                    }

                    mySqlDatabaseManager.execute("INSERT INTO " + databaseName + ".WA_TEST_CLASS_STAT VALUES"
                                                 + "(" + lastBuildNumber + "," + lastSuiteID + ",NULL,'"
                                                 + testClassElement.getAttribute("name") + "',"
                                                 + testClassElement.getAttribute("duration-ms")
                                                 + ",'" + testClassElement.getAttribute("started-at")
                                                 + "','" + testClassElement.getAttribute("finished-at")
                                                 + "')");

                    NodeList testMethodNode = testClassElement.getElementsByTagName("test-method");
                    for (int testMethodCount = 0; testMethodCount < testMethodNode.getLength();
                         testMethodCount++) {
                        Element testMethodElement = (Element) testMethodNode.item(testMethodCount);


                        ResultSet testMethodResultSet = mySqlDatabaseManager.executeQuery
                                ("SELECT WA_TEST_CLASS_ID FROM " + databaseName + ".WA_TEST_CLASS_STAT ORDER BY " +
                                 "WA_TEST_CLASS_ID DESC LIMIT 1 ");
                        while (testMethodResultSet.next()) {
                            lastTestClassID = testMethodResultSet.getString("WA_TEST_CLASS_ID");
                        }
                        testMethodResultSet = mySqlDatabaseManager.executeQuery
                                ("SELECT WA_TEST_SUITE_ID FROM " + databaseName + ".WA_TEST_SUITE_DETAIL ORDER BY " +
                                 "WA_TEST_SUITE_ID DESC LIMIT 1 ");
                        while (testMethodResultSet.next()) {
                            lastSuiteID = testMethodResultSet.getString("WA_TEST_SUITE_ID");
                        }
                        testMethodResultSet = mySqlDatabaseManager.executeQuery
                                ("SELECT WA_BUILD_NUMBER FROM " + databaseName + ".WA_BUILD_HISTORY ORDER BY " +
                                 "WA_BUILD_NUMBER DESC LIMIT 1 ");
                        while (testMethodResultSet.next()) {
                            lastBuildNumber = testMethodResultSet.getString("WA_BUILD_NUMBER");
                        }

                        mySqlDatabaseManager.execute("INSERT INTO " + databaseName + ".WA_TESTCASE_STAT VALUES"
                                                     + "(" + lastBuildNumber + "," + lastSuiteID + ","
                                                     + lastTestClassID
                                                     + ",NULL,'" + testMethodElement.getAttribute("status")
                                                     + "','" + testMethodElement.getAttribute("signature") + "','"
                                                     + testMethodElement.getAttribute("name") + "',"
                                                     + testMethodElement.getAttribute("duration-ms")
                                                     + ",'" + testMethodElement.getAttribute("started-at")
                                                     + "','" + testMethodElement.getAttribute("finished-at")
                                                     + "','" + testMethodElement.getAttribute("is-config")
                                                     + "')");


                        NodeList exceptionNode = testMethodElement.getElementsByTagName("exception");
                        if (exceptionNode.getLength() > 0) {
                            for (int exceptionCount = 0; exceptionCount < exceptionNode.getLength();
                                 exceptionCount++) {
                                Element exceptionElement = (Element) exceptionNode.item(exceptionCount);

                                String exceptionClass;
                                String exceptionMessage = null;
                                String stackRace = null;

                                exceptionClass = exceptionElement.getAttribute("class");


                                NodeList exceptionMessageNode = exceptionElement.getElementsByTagName("message");
                                if (exceptionMessageNode.getLength() > 0) {
                                    for (int exceptionMsgCount = 0; exceptionMsgCount <
                                                                    exceptionMessageNode.getLength();
                                         exceptionMsgCount++) {
                                        Element exceptionMsgElement = (Element) exceptionMessageNode.item
                                                (exceptionCount);
                                        exceptionMessage = exceptionMsgElement.getFirstChild().
                                                getNextSibling().toString();
                                        exceptionMessage = exceptionMessage.replaceAll("#cdata-section:", "");
                                    }
                                } else {
                                    exceptionMessage = exceptionClass;
                                }

                                NodeList stackRaceNode = exceptionElement.getElementsByTagName
                                        ("full-stacktrace");
                                for (int stackRaceCount = 0; stackRaceCount < stackRaceNode.
                                        getLength(); stackRaceCount++) {
                                    Element exceptionMsgElement = (Element) stackRaceNode.
                                            item(stackRaceCount);
                                    stackRace = exceptionMsgElement.getFirstChild().
                                            getNextSibling().toString();
                                    stackRace = stackRace.replaceAll("#cdata-section:", "");
                                }

                                ResultSet exceptionResultSet = mySqlDatabaseManager.executeQuery
                                        ("SELECT WA_TESTCASE_ID FROM " + databaseName + ".WA_TESTCASE_STAT " +
                                         "ORDER BY WA_TESTCASE_ID DESC LIMIT 1 ");
                                while (exceptionResultSet.next()) {
                                    lastTestCaseID = exceptionResultSet.getString("WA_TESTCASE_ID");
                                }
                                exceptionResultSet = mySqlDatabaseManager.executeQuery
                                        ("SELECT WA_BUILD_NUMBER FROM " + databaseName + ".WA_BUILD_HISTORY " +
                                         "ORDER BY WA_BUILD_NUMBER DESC LIMIT 1 ");
                                while (exceptionResultSet.next()) {
                                    lastBuildNumber = exceptionResultSet.getString("WA_BUILD_NUMBER");
                                }

                                mySqlDatabaseManager.execute("INSERT INTO " + databaseName + ".WA_ERROR_DETAIL " +
                                                             "VALUES"
                                                             + "(" + lastBuildNumber + ","
                                                             + lastTestCaseID
                                                             + ",NULL,'"
                                                             + exceptionClass + "','"
                                                             + enc.encode(exceptionMessage.getBytes
                                        (DEFAULT_ENCODING)) + "','"
                                                             + enc.encode(stackRace.getBytes
                                        (DEFAULT_ENCODING)) + "')");
                            }
                        }


                    }
                }

            }
        } catch (SQLException e) {
            log.error(e);
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (ParserConfigurationException e) {
            log.error(e);
        } catch (SAXException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }

    }
/*
    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }*/

}


/*
public static void main(String[] args) throws Exception {
    File file = new File("/home/chamara/wso2/project/automation/graphite/platform-integration/system-test-framework/reports/ESBTestSuite/testng-results.xml");
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(file);

    NodeList suiteNodes = doc.getElementsByTagName("suite");
    for (int i = 0; i < suiteNodes.getLength(); i++) {
        Element element = (Element) suiteNodes.item(i);
        NodeList testClassNode = element.getElementsByTagName("test");
        for (int j = 0; j < testClassNode.getLength(); j++) {
            Element line = (Element) testClassNode.item(0);
        }
//            System.out.println(line.getFirstChild().getNextSibling().toString());
//            System.out.println("Title: " + getCharacterDataFromElement(line));
    }

}*/
