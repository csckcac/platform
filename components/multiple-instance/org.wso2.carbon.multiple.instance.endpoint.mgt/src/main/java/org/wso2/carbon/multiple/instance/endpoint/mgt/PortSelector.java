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
package org.wso2.carbon.multiple.instance.endpoint.mgt;

import org.wso2.carbon.utils.CarbonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* This class select the ports for the instances start by LocalLoadAnalyzerTask */
public class PortSelector {
    private static final String INSTANCE_LOG_FILE = "instance.log";
    private static final String PORTS_GIVEN = "ports.given";
    private static final String[] transports = {"http","https"};
    private static final String[] transportTypes = {"nioports","mgtports"};
    public static List<String> normalEndpointList = new ArrayList<String>();
    public static List<String> nioEndpointList = new ArrayList<String>();
    private static int start = 49152;
    private static int end =  65353;
    private static int range = (65535 - 49152);
    private static final Log log = LogFactory.getLog(PortSelector.class);

    /**
     * Initialize the static class before using, this set the port range by parsing ports-config.xml
     *
     */
    public static void init (){
        setPortRange();
    }
    /**
     * Read the port from ports-config.xml
     *
     * @param transport transport (http or https)
     *
     * @param transportType transport type  nio or mgt-console transport
     *
     * @return the port number for the given transport by reading ports-config.xml
     */
    public static int getPortFromConfig(String transport,String transportType,String instanceLog) {
        try {
            String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + "ports-config.xml";
            File file = new File(path);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            /* this system property will be set properly in autoscale-mediator when starting instances */
            NodeList instancePortConfig = doc.getElementsByTagName(instanceLog);
            Element inner = (Element) instancePortConfig.item(0);
            NodeList innerList = inner.getElementsByTagName(transportType);
            Element finalElement = (Element) innerList.item(0);
            NodeList port = finalElement.getElementsByTagName(transport);
            // after selecting a port we add it in to the file to pick them to configure the load balancer
            return Integer.parseInt(port.item(0).getTextContent());
        } catch (Exception e) {
            log.error("Error Parsing ports-config.xml",e);
            return 0;
        }
    }

    /**
     *
     * Set the start and end ports by parsing ports-config.xml
     *
     */
    public static void setPortRange(){
        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + "ports-config.xml";
        File file = new File(path);
        try{
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList rangeNode = doc.getElementsByTagName("range");
            Element rangeElement = (Element)rangeNode.item(0);
            NodeList startNode = rangeElement.getElementsByTagName("start");
            NodeList endNode = rangeElement.getElementsByTagName("end");
            start = Integer.parseInt(startNode.item(0).getTextContent());
            end = Integer.parseInt(endNode.item(0).getTextContent());
            range = end - start;
        }catch (Exception e){
            log.warn("Error reading ports range from ports-config.xml, Default port range will be: " +
                    start + " - " + end);
        }
    }
    /**
     * Generate the port depending on the given port range
     *
     * @return dynamically generated port number
     */
    public static int getPortNumber() {
        Random candidateInt = new Random();//
        int candidatePort = (candidateInt.nextInt(start) + range);
        if ((candidatePort < start) || (candidatePort > end)) {
            do {
                candidatePort = (candidateInt.nextInt(start) + range);
            }
            while ((candidatePort < start) || (candidatePort > end) || (isPortSelected(candidatePort)));
            return candidatePort;
        } else {
            return candidatePort;
        }
    }

    public static String genaratePort(String instanceId,String instanceLog){
        int port = 0;
        StringBuffer portSystemProperties = new StringBuffer();
        for(String transport:transports){
            for(String transportType:transportTypes){
                if ("set".equals(System.getProperty(PORTS_GIVEN))) {
                    port = getPortFromConfig(transport,transportType,instanceLog);
                }else{
                    port = getPortNumber();
                }
                if("nioports".equals(transportType)){
                    portSystemProperties.append(createSystemPropertyParam("nio" + transport +
                            "Port",Integer.toString(port)));
                    nioEndpointList.add(Integer.toString(port) + " " + transport + " " + instanceId);
                }else{
                    portSystemProperties.append(createSystemPropertyParam(transport +
                            "Port",Integer.toString(port)));
                    normalEndpointList.add(Integer.toString(port) + " " + transport + " " + instanceId);
                }
            }
        }
        return portSystemProperties.toString();
    }
    public static boolean isPortSelected(int port){
        String[] normalList = normalEndpointList.toArray(new String[normalEndpointList.size()]);
        String[] nioList = nioEndpointList.toArray(new String[nioEndpointList.size()]);
        for(String endpoint:normalList){
            if(Integer.toString(port).equals(endpoint.split(" ")[0])){
                return true;
            }
        }
        for(String endpoint:nioList){
            if(Integer.toString(port).equals(endpoint.split(" ")[0])){
                return true;
            }
        }
        return false;
    }
    private static String createSystemPropertyParam(String name, String value) {
        return " -D" + name + "=" + value + " ";
    }

}
