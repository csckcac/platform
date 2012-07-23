/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.mediator.tests.smooks;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.tests.smooks.util.RequestUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LargeCSVTransformSmooksTestCase extends ESBIntegrationTestCase {
    public static final String csvInput = "EXTRACT\n" +
                                    "DETAIL|128|2012-07-12|1|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|171|REG|Personal Car Mileage|2012-06-15|USD|1.0000|M|N|Example testing|||N|N||||||||||||||||||||||||||||||||||||||||||||||||0.0000|29.4200|29.4200|29.4200|29.4200|CASH|Cash|||||||||||||||||||||||||||||||||HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+29.4200|2043|53||||||||||||||||||172|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|29.4200|0.0000|29.4200|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|2|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|172|REG|Business Meal (attendees)|2012-06-19|USD|1.0000|M|N|New product discussion||Lure|N|N|2||1|||||||||||||||||||||||||||||||||||||||||US||||0.0000|150.0000|150.0000|150.0000|150.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70210|DR|+150.0000|2044|||||||||||||||||||173|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|150.0000|0.0000|150.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|3|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|173|REG|Seminar Fees|2012-06-20|USD|1.0000|M|N|Time management||Skillpath|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|200.0000|200.0000|200.0000|200.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70556|DR|+200.0000|2045|||||||||||||||||||174|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|200.0000|0.0000|200.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|4|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|175|CHD|Airfare|2012-06-25|USD|1.0000|M|N|Training|Alaska Airlines|Alaska Airlines|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|195.0000|195.0000|195.0000|195.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+195.0000|2046|||||||||||||||||||176|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|195.0000|0.0000|195.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|5|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|176|CHD|Airline Fees|2012-06-25|USD|1.0000|M|N|Training|Alaska Airlines|Alaska Airlines|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|25.0000|25.0000|25.0000|25.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+25.0000|2047|||||||||||||||||||177|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|25.0000|0.0000|25.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|6|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|178|CHD|Hotel Tax|2012-06-25|USD|1.0000|M|N|Training|Marriott Hotels|Marriott Hotels|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|15.0000|15.0000|15.0000|15.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+15.0000|2048|||||||||||||||||||179|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|15.0000|0.0000|15.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|7|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|179|CHD|Hotel|2012-06-25|USD|1.0000|M|N|Training|Marriott Hotels|Marriott Hotels|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|80.0000|80.0000|80.0000|80.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+80.0000|2049|||||||||||||||||||180|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|80.0000|0.0000|80.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|8|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|180|CHD|C-Dinner|2012-06-25|USD|1.0000|M|N|Training|Marriott Hotels|Marriott Hotels|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|25.0000|25.0000|25.0000|25.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70210|DR|+25.0000|2050|||||||||||||||||||181|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|25.0000|0.0000|25.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|9|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|182|CHD|Car Rental|2012-06-25|USD|1.0000|M|N|||35|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|35.0000|35.0000|35.0000|35.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+35.0000|2051|||||||||||||||||||183|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|35.0000|0.0000|35.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|10|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|183|CHD|Gasoline|2012-06-25|USD|1.0000|M|N|Training||35|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|65.0000|65.0000|65.0000|65.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70200|DR|+65.0000|2052|||||||||||||||||||184|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|65.0000|0.0000|65.0000|Testing123||||||\n" +
                                    "DETAIL|128|2012-07-12|11|8550|Smith|Patrick||US|30|99|500|G&A Function||||||E12F3C8A2D55412B9F9F|33|SEMTECH|USD|UNITED STATES|2012-07-09|2012-06-30|2012-07-11|testJW2079|Y|N|N|827.4200|827.4200|Non VAT Expense Policy||30|99|500|G&A Function|||G&A Function||||||||||||||US|8550||46|1410|BE1|184|REG|A-Breakfast|2012-06-25|USD|1.0000|M|N|Training||Starbucks|N|N||||||||||||||||||||||||||||||||||||||||||||US||||0.0000|8.0000|8.0000|8.0000|8.0000|CASH|Cash|||||||||||||||||||||||||||||||US|US-CA|HOME|||Company|Company/Employee Pseudo Payment Code|Employee|Company/Employee Pseudo Payment Code|70210|DR|+8.0000|2053|||||||||||||||||||185|100.0000||||||||||||||||||||||||||||||||||||||||||||||||||||||||0.0000|8.0000|0.0000|8.0000|Testing123||||||" ;
    private static final int REC_COUNT = 11;

    public void init() throws Exception {
        loadESBConfigurationFromClasspath("/mediators/smooks/smooks_csv_test_synapse.xml");
//        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 17:  Introduction to payload Mediator")
    public void transformUsingPayloadFactory() throws AxisFault {
        OMElement response;
        RequestUtil testCSVInputRequest = new RequestUtil();
        response = testCSVInputRequest.sendReceive(
                getMainSequenceURL(),
                "http://localhost:9000/services/SimpleStockQuoteService",
                csvInput);
        Iterator csvRecs = response.getChildrenWithLocalName("csv-record");
        int recordCount = 0 ;
        while (csvRecs.hasNext()){
            recordCount++;
        }
        //we are going to check whether number of record counts does match
        //issue is reported in https://issues.apache.org/jira/browse/SYNAPSE-809
        assertEquals(recordCount, REC_COUNT);

    }

    @Override
    protected void cleanup() {
        super.cleanup();
    }


}
