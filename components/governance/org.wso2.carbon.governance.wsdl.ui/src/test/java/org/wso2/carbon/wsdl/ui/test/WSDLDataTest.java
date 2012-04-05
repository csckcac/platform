/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.wsdl.ui.test;

import junit.framework.TestCase;
import org.wso2.carbon.governance.wsdl.ui.TreeNodeBuilder;
import org.wso2.carbon.registry.common.ui.utils.TreeNode;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class WSDLDataTest extends TestCase {
    /*public void testRetrieveWSDLData() throws Exception {
        String wsdl11Content = getFileContent("src/test/resources/sample_wsdl_11.wsdl");
        Map<String, Set<String>> wsdlData = UIUtil.retrieveWSDLData(wsdl11Content);

        assertEquals(wsdlData.get("WSDL Version").toArray()[0], "1.1");

        assertEquals(wsdlData.get("Schema complex type(s)").toArray()[0],"address");
        assertEquals(wsdlData.get("Schema complex type(s)").toArray()[1],"productInfo");
        assertEquals(wsdlData.get("Binding(s)").toArray()[0], "ShippingTradeSoapBinding");


        String wsdl20Content = getFileContent("src/test/resources/sample_wsdl_20.wsdl");
        Map<String, Set<String>> wsdl2Data = UIUtil.retrieveWSDLData(wsdl20Content);

        assertEquals(wsdl2Data.get("WSDL Version").toArray()[0], "2.0");

        assertEquals(wsdl2Data.get("Binding(s)").toArray()[0], "MyServiceBinding");
    } */

    private String getFileContent(String filename) throws Exception {
        File file = new File(filename);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int)file.length()];
        fileInputStream.read(fileContents);
        return new String(fileContents);
    }

    public void testRetrieveWSDLDataFull() throws Exception {
        String wsdlFileName = "src/test/resources/InstanceRegIdRetriever.wsdl";
        String wsdl11Content = getFileContent(wsdlFileName);
        TreeNodeBuilder builder = new TreeNodeBuilder(wsdlFileName, wsdl11Content);

        TreeNode wsdlTreeNode = builder.buildTree();
        assertEquals(wsdlTreeNode.getKey(), "Wsdl<em>: InstanceRegIdRetriever.wsdl</em>");

        TreeNode[] rootChilds = getTreeChildNodes(wsdlTreeNode);
        assertEquals(rootChilds[0].getKey(), "Documentation");
        assertEquals(rootChilds[1].getKey(), "Version");
        assertEquals(rootChilds[2].getKey(), "Service<em>: InstanceRegIdRetriever [http://instanceid.services.core.carbon.wso2.org]</em>");

        // finding service childs
        TreeNode[] serviceChilds = getTreeChildNodes(rootChilds[2]);
        assertEquals(serviceChilds[0].getKey(), "Port<em>: InstanceRegIdRetrieverHttpsSoap11Endpoint [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] portChilds = getTreeChildNodes(serviceChilds[0]);
        assertEquals(portChilds[1].getKey(), "SOAP Version");
        assertEquals(portChilds[1].getChildNodes()[0].getKey(), "SOAP 1.1");

        TreeNode[] portTypeChilds = getTreeChildNodes(portChilds[4]);
        assertEquals(portTypeChilds[0].getKey(), "Operation(s)");
        assertEquals(portTypeChilds[0].getChildNodes()[0].getKey(), "retrieveRegId");
    }

    public void testRetrieveWSDLDataFullNoTns() throws Exception {
        String wsdlFileName = "src/test/resources/InstanceRegIdRetrieverNoTns.wsdl";
        String wsdl11Content = getFileContent(wsdlFileName);
        TreeNodeBuilder builder = new TreeNodeBuilder(wsdlFileName, wsdl11Content);

        TreeNode wsdlTreeNode = builder.buildTree();
        assertEquals(wsdlTreeNode.getKey(), "Wsdl<em>: InstanceRegIdRetrieverNoTns.wsdl</em>");

        TreeNode[] rootChilds = getTreeChildNodes(wsdlTreeNode);
        assertEquals(rootChilds[0].getKey(), "Documentation");
        assertEquals(rootChilds[1].getKey(), "Version");
        assertEquals(rootChilds[2].getKey(), "Service<em>: InstanceRegIdRetriever [http://instanceid.services.core.carbon.wso2.org]</em>");

        // finding service childs
        TreeNode[] serviceChilds = getTreeChildNodes(rootChilds[2]);
        assertEquals(serviceChilds[0].getKey(), "Port<em>: InstanceRegIdRetrieverHttpsSoap11Endpoint [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] portChilds = getTreeChildNodes(serviceChilds[0]);
        assertEquals(portChilds[1].getKey(), "SOAP Version");
        assertEquals(portChilds[1].getChildNodes()[0].getKey(), "SOAP 1.1");

        TreeNode[] portTypeChilds = getTreeChildNodes(portChilds[4]);
        assertEquals(portTypeChilds[0].getKey(), "Operation(s)");
        assertEquals(portTypeChilds[0].getChildNodes()[0].getKey(), "retrieveRegId");
    }

    public void testRetrieveWSDLDataNoServices() throws Exception {
        String wsdlFileName = "src/test/resources/InstanceRegIdRetriever-NoServices.wsdl";
        String wsdl11Content = getFileContent(wsdlFileName);
        TreeNodeBuilder builder = new TreeNodeBuilder(wsdlFileName, wsdl11Content);

        TreeNode wsdlTreeNode = builder.buildTree();
        assertEquals(wsdlTreeNode.getKey(), "Wsdl<em>: InstanceRegIdRetriever-NoServices.wsdl</em>");

        TreeNode[] rootChilds = getTreeChildNodes(wsdlTreeNode);
        assertEquals(rootChilds[0].getKey(), "Documentation");
        assertEquals(rootChilds[1].getKey(), "Version");
        assertEquals(rootChilds[2].getKey(), "Binding<em>: InstanceRegIdRetrieverSoap11Binding [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] bindingChilds = getTreeChildNodes(rootChilds[2]);
        assertEquals(bindingChilds[0].getKey(), "Port Type<em>: InstanceRegIdRetrieverPortType [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] portTypeChilds = getTreeChildNodes(bindingChilds[0]);
        assertEquals(portTypeChilds[0].getKey(), "Operation(s)");
        assertEquals((portTypeChilds[0].getChildNodes()[0].getKey()), "retrieveRegId");
    }


    public void testRetrieveWSDLDataNoServicesNoBindings() throws Exception {
        String wsdlFileName = "src/test/resources/InstanceRegIdRetriever-NoServices-NoBindings.wsdl";
        String wsdl11Content = getFileContent(wsdlFileName);
        TreeNodeBuilder builder = new TreeNodeBuilder(wsdlFileName, wsdl11Content);

        TreeNode wsdlTreeNode = builder.buildTree();
        assertEquals(wsdlTreeNode.getKey(),
                "Wsdl<em>: InstanceRegIdRetriever-NoServices-NoBindings.wsdl</em>");

        TreeNode[] rootChilds = getTreeChildNodes(wsdlTreeNode);
        assertEquals(rootChilds[2].getKey(), "Port Type<em>: InstanceRegIdRetrieverPortType [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] portTypeChilds = getTreeChildNodes(rootChilds[2]);
        assertEquals(portTypeChilds[0].getKey(), "Operation(s)");
        assertEquals(portTypeChilds[0].getChildNodes()[0].getKey(), "retrieveRegId");
    }


    public void testRetrieveWSDLDataNoBindings() throws Exception {
        String wsdlFileName = "src/test/resources/InstanceRegIdRetriever-NoBindings.wsdl";
        String wsdl11Content = getFileContent(wsdlFileName);
        TreeNodeBuilder builder = new TreeNodeBuilder(wsdlFileName, wsdl11Content);

        TreeNode wsdlTreeNode = builder.buildTree();
        assertEquals(wsdlTreeNode.getKey(),
                "Wsdl<em>: InstanceRegIdRetriever-NoBindings.wsdl</em>");

        TreeNode[] rootChilds = getTreeChildNodes(wsdlTreeNode);
        assertEquals(rootChilds[2].getKey(), "Service<em>: InstanceRegIdRetriever [http://instanceid.services.core.carbon.wso2.org]</em>");
        assertEquals(rootChilds[3].getKey(), "Port Type<em>: InstanceRegIdRetrieverPortType [http://instanceid.services.core.carbon.wso2.org]</em>");

        // finding service childs
        TreeNode[] serviceChilds = getTreeChildNodes(rootChilds[2]);
        assertEquals(serviceChilds[0].getKey(), "Port<em>: InstanceRegIdRetrieverHttpsSoap11Endpoint [http://instanceid.services.core.carbon.wso2.org]</em>");

        TreeNode[] portChilds = getTreeChildNodes(serviceChilds[0]);
        assertEquals(portChilds[1].getKey(), "SOAP Version");
        assertEquals(portChilds[1].getChildNodes()[0].getKey(), "SOAP 1.1");

        TreeNode[] portTypeChilds = getTreeChildNodes(rootChilds[3]);
        assertEquals(portTypeChilds[0].getKey(), "Operation(s)");
        assertEquals(portTypeChilds[0].getChildNodes()[0].getKey(), "retrieveRegId");
    }

    private static TreeNode[] getTreeChildNodes(TreeNode wsdlTreeNode) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        Object[] treeChilds = wsdlTreeNode.getChildNodes();
        for (Object treeChild: treeChilds) {
            if (treeChild instanceof TreeNode) {
                treeNodes.add((TreeNode)treeChild);
            }
        }
        return treeNodes.toArray(new TreeNode[treeNodes.size()]);
    }
}
