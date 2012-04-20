/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
*/
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.bpel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.Activity;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityComplex;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityRoot;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.Link;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to parse the BPEL documents. Only the {@link Activity}s of the {@link ProcessModel} will
 * be parsed.
 *
 * @author Gregor Latuske
 */
public final class BPELParser {

    /**
     * The cached data.
     */
    private static final Map<ProcessModel, ActivityRoot> DATA_MAP = new HashMap<ProcessModel, ActivityRoot>();

    private BPELParser() {
    }

    /**
     * Parses the BPEL document of the given {@link ProcessModel} and returns the {@link ActivityRoot}.
     *
     * @param processModel The {@link ProcessModel} to parse.
     * @return The created {@link ActivityRoot}.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static ActivityRoot parse(ProcessModel processModel)
            throws IOException, ParserConfigurationException, SAXException {

        if (DATA_MAP.get(processModel) == null) {
            DATA_MAP.put(processModel, parseRootNode(processModel));
        }

        return DATA_MAP.get(processModel);
    }

    /**
     * Parses the given {@link Node}, creates an {@link ActivityRoot} and parses the child {@link Node}s
     * recursively.
     *
     * @param processModel The associated {@link ProcessModel}.
     * @return Root Activity
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private static ActivityRoot parseRootNode(ProcessModel processModel)
            throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;

        try {
            if (processModel.getDocument() != null) {
                is = processModel.getDocument().openStream();
            } else {
                is = new ByteArrayInputStream(processModel.getDefinition().getBytes());
            }

            // Create and configure DocumentFactory
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            // Create Document and load DocumentElement
            Document document = builderFactory.newDocumentBuilder().parse(is);
            Element element = document.getDocumentElement();

            ActivityRoot root = new ActivityRoot(processModel.getPid());
            parseChildNodes(element, root, root);

            return root;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Parses the given {@link Node}, creates an {@link Activity} and parses the child {@link Node}s
     * recursively.
     *
     * @param node   The {@link Node} to parse.
     * @param parent The parent {@link Activity}.
     * @param root   The {@link ActivityRoot}.
     */
    private static void parseNode(Node node, ActivityComplex parent, ActivityRoot root) {
        Activity activity = BPELActivityFactory.createActivity(getType(node), getName(node), parent, root);

        // Parse children
        if (activity != null) {
            parseChildNodes(node, activity, root);
        }
    }

    /**
     * Parses the child nodes and creates child activities or add links.
     *
     * @param node     The {@link Node} with the children to parse.
     * @param activity The created {@link Activity}.
     * @param root     The {@link ActivityRoot}.
     */
    private static void parseChildNodes(Node node, Activity activity, ActivityRoot root) {
        // Iterate over child nodes
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {

            Node child = nodeList.item(i);
            String tag = child.getLocalName();

            // Parse child node
            if (tag != null && !tag.isEmpty()) {

                if (tag.equalsIgnoreCase("sources") || tag.equalsIgnoreCase("targets")) {
                    addLinks(child, activity, root);
                } else if (activity instanceof ActivityComplex) {
                    parseNode(nodeList.item(i), (ActivityComplex) activity, root);
                }
            }
        }
    }

    /**
     * Determines the name of the node.
     *
     * @param node The given node.
     * @return The name of the node.
     */
    private static String getName(Node node) {
        String name = "";
        if (node.getAttributes() != null) {

            Node nameNode = node.getAttributes().getNamedItem("name");
            if (nameNode != null) {
                name = nameNode.getTextContent();
            }
        }

        return name;
    }

    /**
     * Determines the type (tag name) of the node.
     *
     * @param node The given node.
     * @return The type (tag name) of the node.
     */
    private static String getType(Node node) {
        return node.getLocalName() == null ? "" : node.getLocalName();
    }

    /**
     * Adjust the links of the flow activities (set the targets or sources).
     *
     * @param node     The node with the targets or sources.
     * @param activity The activity that is the target or source.
     * @param root     Activity Root
     */
    private static void addLinks(Node node, Activity activity, ActivityRoot root) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {

            // Get target node
            Node child = nodeList.item(i);
            String name = child.getLocalName();
            if (name != null && child.getAttributes() != null) {

                // Link name node
                Node linkNameNode = child.getAttributes().getNamedItem("linkName");
                if (linkNameNode != null) {

                    // Link name
                    String linkName = linkNameNode.getTextContent();
                    if (linkName != null && !linkName.isEmpty()) {

                        Link link = new Link(linkName);
                        boolean contains = false;

                        // Check for new link
                        for (Link existingLink : root.getLinks()) {
                            if (existingLink.equals(link)) {
                                link = existingLink;
                                contains = true;
                            }
                        }

                        // Add link if not exists
                        if (!contains) {
                            root.getLinks().add(link);
                        }

                        // Set source or target
                        if (name.equalsIgnoreCase("source")) {
                            link.setSource(activity);
                        } else if (name.equalsIgnoreCase("target")) {
                            link.setTarget(activity);
                        }
                    }
                }
            }
        }
    }

}
