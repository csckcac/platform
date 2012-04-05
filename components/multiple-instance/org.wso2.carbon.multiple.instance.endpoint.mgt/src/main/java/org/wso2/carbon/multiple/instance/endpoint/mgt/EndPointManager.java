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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.AnonymousListMediator;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.algorithms.RoundRobin;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.LogMediator;
import org.apache.synapse.mediators.builtin.SendMediator;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.mediators.builtin.DropMediator;
import org.apache.synapse.mediators.filters.InMediator;
import org.apache.synapse.mediators.filters.OutMediator;
import org.apache.synapse.mediators.filters.FilterMediator;
import org.apache.synapse.mediators.transform.HeaderMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.SynapseConstants;
import org.apache.axis2.clustering.Member;
import org.wso2.carbon.multiple.instance.endpoint.mgt.internal.EndPointManagerServiceComponent;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Pattern;
import java.net.ServerSocket;
import java.net.BindException;

public class EndPointManager extends Thread {
    private static Log log = LogFactory.getLog(EndPointManager.class);
    private static final String NIO_PORTS = "nioportlist.txt";
    private static final String NORMAL_PORTS = "normalportlist.txt";
    private static List<String> addedEndPointNameList = new ArrayList<String>();
    private static Map<String,Member> addedEndpointList = new HashMap<String,Member>();

    public void run() {
        updateEndpoints();
    }

    public void updateEndpoints() {
        try {
            while (true) {
                /* We run this continuesly to sync the changes happen with Instances */
                setStartedEndpoints(PortSelector.nioEndpointList);
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error("Error Updating Endpoints in to SynapseConfiguration", e);
        }
    }

    /**
     * This method get called time to time and set the endpoints in to synapse configuration
     *
     * @param portList
     * 
     * @throws Exception
     */
    private void setStartedEndpoints(List<String> portList) throws Exception {
        SynapseConfigurationService synCfgService = EndPointManagerServiceComponent.getScService();
        String hostName = NetworkUtils.getLocalHostname();
        if (synCfgService != null) {
            SynapseConfiguration synCfg = synCfgService.getSynapseConfiguration();
            /* check whether a new instance has started or or not */
            Iterator it = portList.iterator();
            List<Member> childEndpoints = new ArrayList<Member>();
            while (it.hasNext()) {
                String nextLine = (String) it.next();
                /* endpoint is added only if the endpoint is not in the list*/
                if (!addedEndPointNameList.contains(nextLine)) {
                    String[] portData = nextLine.split(" ");
                    /* before adding any entry in to loadbalancer list we are checking whether the port is up and running
                        This is useful when one of the instance has been shutdown due to some reason so this check avoid
                        adding that endpoint to the load balancing list
                     */
                    try {
                        ServerSocket testSocket = new ServerSocket(Integer.parseInt(portData[0]));
                        testSocket.close();
                    } catch (BindException e) {
                        /* set the port number as the EndPoint Name */
                        /* Split the line read from the file because it contain the port number
                          with transport type
                        */
                        StringBuffer url = new StringBuffer();
                        url.append(portData[1]);
                        url.append("://"+ hostName +":");
                        url.append(portData[0] + "/");
                        Member member = null;
                        /* here we put the Member in to a Map with the Instance ID as the Key */
                        if((member=addedEndpointList.get(portData[2]))==null){
                            member = new Member(hostName,-1);
                            addedEndpointList.put(portData[2],member);
                        }
                        if ("http".equals(portData[1])) {
                            member.setHttpPort(Integer.parseInt(portData[0]));

                        } else if("https".equals(portData[1])){
                            member.setHttpsPort(Integer.parseInt(portData[0]));
                        }
                        if(!"set".equals(System.getProperty("lb.disable"))){
                            log.info("Following Endpoint added to Load Balancing configuration: " + url.toString());
                        }
                        /* add the string we are reading from the file to addedEndPointList so that the endpoint will not
                          duplicate
                        */
                        addedEndPointNameList.add(nextLine);
                        childEndpoints.add(member);
                    }
                }
            }

            /*if we haven't added any endpoint yet we have to remove the main sequence before
                    new main sequence and we are not removing this always, only at the first time
            */

            if (childEndpoints.size() != 0) {
                synCfg.removeSequence("main");
                SequenceMediator mainSeq = new SequenceMediator();
                mainSeq.setName("main");
                InMediator in = new InMediator();

                OutMediator out = new OutMediator();
                out.addChild(new SendMediator());
                mainSeq.addChild(in);
                mainSeq.addChild(out);

                SendMediator send = new SendMediator();
                LoadbalanceEndpoint lb = new LoadbalanceEndpoint();

                FilterMediator filterMediator = new FilterMediator();
                in.addChild(filterMediator);
                SynapseXPath xpath = new SynapseXPath("get-property('To')");
                filterMediator.setSource(xpath);
                filterMediator.setRegex(Pattern.compile("/carbon"));

                PropertyMediator httpSCProperty = new PropertyMediator();
                httpSCProperty.setName("HTTP_SC");
                httpSCProperty.setScope("axis2");
                httpSCProperty.setValue("302");

                PropertyMediator locationHeader = new PropertyMediator();
                locationHeader.setName("Location");
                locationHeader.setScope("transport");
                locationHeader.setValue(getMasterHttpsEndpoint(PortSelector.normalEndpointList));

                PropertyMediator responseProperty = new PropertyMediator();
                responseProperty.setName(SynapseConstants.RESPONSE);
                responseProperty.setValue("true");

                HeaderMediator headerMediator = new HeaderMediator();
                headerMediator.setQName(new QName("To"));
                headerMediator.setAction(1);

                SendMediator sendMediator = new SendMediator();
                DropMediator dropMediator = new DropMediator();

                filterMediator.addChild(locationHeader);
                filterMediator.addChild(httpSCProperty);
                filterMediator.addChild(responseProperty);
                filterMediator.addChild(headerMediator);
                filterMediator.addChild(sendMediator);
                filterMediator.addChild(dropMediator);

                AnonymousListMediator elseMediator = new AnonymousListMediator();
                filterMediator.setElseMediator(elseMediator);
                elseMediator.addChild(new LogMediator());
                elseMediator.addChild(send);
                /* if there are ncew endpoints we are removing the current sequence */
                RoundRobin algorithm = new RoundRobin(childEndpoints);
                lb.setAlgorithm(algorithm);
                /* adding all the endpoints started since the current main sequence is removing */
                if(!"set".equals(System.getProperty("lb.disable"))){
                    lb.setMembers(new ArrayList<Member>(addedEndpointList.values()));
                }
                send.setEndpoint(lb);
                //in.addChild(send);
                synCfg.addSequence(mainSeq.getName(), mainSeq);
                if(!"set".equals(System.getProperty("lb.disable"))){
                    log.info("Adding new Endpoint from the newly Started" +
                            " Instance to Load Balance");
                }
                mainSeq.init(EndPointManagerServiceComponent.getSynapseEnvService().getSynapseEnvironment());
            }
        }
    }

    /**
     * Find the master node HTTPS port
     *
     * @param portList
     *
     * @return HTTPS port of master-node
     *
     * @throws Exception
     */
    private String getMasterHttpsEndpoint(List<String> portList)throws Exception{
        String hostName = NetworkUtils.getLocalHostname();
        Iterator it = portList.iterator();
        String[] split = null;
        while(it.hasNext()){
            String nextLine = (String)it.next();
            if(nextLine.split(" ")[2].contains("-master")){
                if("https".equals(nextLine.split(" ")[1])){
                    /* by this logic we get the latest master node from the file */
                    split = nextLine.split(" ");
                }
            }
        }
        String masterEndpoint = split[1] + "://"+ hostName +":" + split[0] + "/carbon";
        log.info("Adding master Endpoint for the Redirect: " + masterEndpoint);
        return masterEndpoint;
    }
}

