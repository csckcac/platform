/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.fusion.backend;

import junit.framework.TestCase;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntimeFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.listener.CEPEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestFusionBackEndRuntimeFactory extends TestCase {

    public void testFusionBackEndRuntimeFacotry() throws CEPConfigurationException {

        CEPBackEndRuntimeFactory factory = new FusionBackEndRuntimeFactory();
        CEPBackEndRuntime cepBackEndRuntime = factory.createCEPBackEndRuntime(null, null, null,0);

        Expression expression = new Expression();
        expression.setText("package org.wso2.carbon.cep.fusion;\n" +
                "\n" +
                "import java.util.Map;\n" +
                "import java.util.HashMap;\n" +
                "\n" +
                "global org.wso2.carbon.cep.fusion.listener.FusionEventListener fusionListener;\n" +
                "\n" +
                "declare HashMap\n" +
                "    @role( event )\n" +
                "end\n" +
                "\n" +
                "rule InvokeIBM\n" +
                "\n" +
                "when\n" +
                "    $stockQuote : HashMap($symbol : this[\"symbol\"], $stockPrice : this[\"price\"], this[\"picked\"] != \"true\") over window:time(2m) from entry-point \"allStockQuotes\";\n" +
                "    $average : Double() from accumulate(HashMap(this[\"symbol\"] == $symbol,$price : this[\"price\"]) over window:time(2m) from entry-point \"allStockQuotes\" , average( $price));\n" +
                "    eval((Double)$stockPrice > $average * 1.01);\n" +
                "then\n" +
                "    $stockQuote.put(\"picked\",\"true\");\n" +
                "    update($stockQuote);\n" +
                "    Map $fastMovingStock = new HashMap();\n" +
                "    $fastMovingStock.put(\"price\",$stockPrice);\n" +
                "    $fastMovingStock.put(\"symbol\",$symbol);\n" +
                "    $fastMovingStock.put(\"average\",$average);\n" +
                "    fusionListener.onEvent($fastMovingStock);\n" +
                "end");
        expression.setType(CEPConstants.CEP_CONF_EXPRESSION_INLINE);
        try {

            CEPEventListener cepEventListener = new CEPEventListener(null, 0, CarbonConstants.REGISTRY_SYSTEM_USERNAME){
                public void onComplexEvent(List events) {
                    for (Object event : events){
                        Map eventMap = (Map) event;
                        System.out.println(" Stock price " + eventMap.get("price")
                                + " symbol " + eventMap.get("symbol")
                                + " average " + eventMap.get("average"));
                    }
                }
            };

            cepBackEndRuntime.addQuery("testQuery", expression, cepEventListener);
            XMLInputMapping mapping = new XMLInputMapping();
            mapping.setStream("allStockQuotes");

            Map symbolMap = null;

            symbolMap = new HashMap();
            symbolMap.put("symbol","IBM");
            symbolMap.put("price", 143.80);

            cepBackEndRuntime.insertEvent(symbolMap, mapping);

            symbolMap = new HashMap();
            symbolMap.put("symbol","IBM");
            symbolMap.put("price", 160.80);

            cepBackEndRuntime.insertEvent(symbolMap, mapping);

         /*   symbolMap = new HashMap();
            symbolMap.put("symbol","SUN");
            symbolMap.put("price", 30.00);

            cepBackEndRuntime.insertEvent(symbolMap, mapping);

            symbolMap = new HashMap();
            symbolMap.put("symbol","IBM");
            symbolMap.put("price", 30.00);

            cepBackEndRuntime.insertEvent(symbolMap, mapping);

            symbolMap = new HashMap();
            symbolMap.put("symbol","SUN");
            symbolMap.put("price", 40.00);

            cepBackEndRuntime.insertEvent(symbolMap, mapping);*/


        } catch (CEPConfigurationException e) {
            e.printStackTrace();
        } catch (CEPEventProcessingException e) {
            e.printStackTrace();
        }

    }

    public void testFusionBackEndRuntimeFacotry1() throws CEPConfigurationException {

        CEPBackEndRuntimeFactory factory = new FusionBackEndRuntimeFactory();
        CEPBackEndRuntime cepBackEndRuntime = factory.createCEPBackEndRuntime(null, null, null,0);

        Expression expression = new Expression();


        expression.setText("package org.wso2.carbon.cep.demo.bam;\n" +
                "                            import java.util.Map;\n" +
                "                            global org.wso2.carbon.cep.fusion.listener.FusionEventListener fusionListener;\n" +
                "                            declare Map\n" +
                "                                @role( event )\n" +
                "                            end\n" +
                "                            rule Statistics\n" +
                "                            when\n" +
                "                                 serviceStatisticsData : Map(this[\"requestCount\"] > 5) over window:time( 2m ) from entry-point ServiceStatisticsDataEvent;\n" +
                "                            then    " +
                "                                 fusionListener.onEvent(serviceStatisticsData);\n" +
                "                            end");

        expression.setType(CEPConstants.CEP_CONF_EXPRESSION_INLINE);
        try {

            CEPEventListener cepEventListener = new CEPEventListener(null,0,  CarbonConstants.REGISTRY_SYSTEM_USERNAME){
                public void onComplexEvent(List events) {
                    for (Object event : events){
                        Map eventMap = (Map) event;
                        System.out.println(" Bam data " + eventMap.get("requestCount")
                                + " symbol " + eventMap.get("responseCount")
                                + " average " + eventMap.get("serviceName"));
                    }
                }
            };

            cepBackEndRuntime.addQuery("testQuery", expression, cepEventListener);
            XMLInputMapping mapping = new XMLInputMapping();
            mapping.setStream("ServiceStatisticsDataEvent");

            Map serviceStatisticsData  = new HashMap();
            serviceStatisticsData.put("requestCount", new Integer(20));
            serviceStatisticsData.put("responseCount", new Integer(25));
            serviceStatisticsData.put("serviceName", "TestService");
            serviceStatisticsData.put("faultCount", new Integer(5));

            cepBackEndRuntime.insertEvent(serviceStatisticsData, mapping);



        } catch (CEPConfigurationException e) {
            e.printStackTrace();
        } catch (CEPEventProcessingException e) {
            e.printStackTrace();
        }

    }

}
