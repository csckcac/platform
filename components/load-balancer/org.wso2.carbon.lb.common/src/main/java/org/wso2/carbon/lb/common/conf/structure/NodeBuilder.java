/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.lb.common.conf.structure;

/**
 * This responsible for build up a Node object from a given content.
 * Every closing brace should be in a new line.
 */
public class NodeBuilder {

    /**
     * 
     * @param aNode
     *            Node object whose name set.
     * @param content
     *            should be something similar to following.
     * 
     *            securityGroups stratos-appserver-lb;
     *            instanceType m1.large;
     *            instances 1;
     *            availabilityZone us-east-1c;
     *            payload /mnt/payload.zip;
     * 
     *            Note that the above content is extracted from following element.
     * 
     *            loadbalancer {
     *            securityGroups stratos-appserver-lb;
     *            instanceType m1.large;
     *            instances 1;
     *            availabilityZone us-east-1c;
     *            payload /mnt/payload.zip;
     *            }
     * @return fully constructed Node
     */
    public static Node buildNode(Node aNode, String content) {

        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // another node is detected and it is not a variable starting from $
            if (line.contains("{") && !line.contains("${")) {
                Node childNode = new Node();
                childNode.setName(line.substring(0, line.indexOf("{")).trim());

                StringBuilder sb = new StringBuilder();

                int matchingBraceTracker = 1;

                while (!line.contains("}") || matchingBraceTracker != 0) {
                    i++;
                    if (i == lines.length) {
                        break;
                    }
                    line = lines[i];
                    if (line.contains("{")) {
                        matchingBraceTracker++;
                    }
                    if (line.contains("}")) {
                        matchingBraceTracker--;
                    }
                    sb.append(line + "\n");
                }

                //System.err.println(sb.toString());
                childNode = buildNode(childNode, sb.toString());
                aNode.setNodes(childNode);

            }
            // this is a property
            else {
                if (!line.isEmpty() && !line.equals("}")) {
                    String[] prop = line.split("[\\s]+");
                    try {
                        aNode.setProperties(prop[0], prop[1].substring(0, prop[1].indexOf(";")));
                    } catch (Exception e) {
                        throw new RuntimeException(
                           "Malformatted property is defined in the configuration file. " +
                                                       line);
                    }
                }
            }
        }

        aNode.setFullyConstructed(true);
        return aNode;

    }
}
