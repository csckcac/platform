/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.version;

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistryNode;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryVersionManager implements VersionManager {

    private static long versionCounter = 0;
    private Session session;
    private Map<String, VersionHistory> versionHistories = new HashMap<String, VersionHistory>();
    private String currentActivityNodePath = "";
    private int configNodeCount = 0;


    public RegistryVersionManager(Session session) {

        this.session = session;
//        loadActivityNodes();
    }

    private void loadActivityNodes() {


        try {

            if ((!(((RegistrySession) session).itemExists("/jcr:system")) && ((RegistrySession) session).itemExists("/jcr:system/jcr:activities"))) {
                ((RegistrySession) session).getRootNode().addNode("jcr:system").addNode("jcr:activities");
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }

    private Version createVersionOnNode(String nodePath) throws RegistryException {
        String latestVersionPath = "";
        Version version = null;

        ((RegistrySession) session).getUserRegistry().createVersion(nodePath);
        String[] regVerPaths = ((RegistrySession) session).getUserRegistry().getVersions(nodePath);
        //NOTE: Here the latest version path is given by the last element of tye version array
        latestVersionPath = regVerPaths[0];
        addVersionToHistory(nodePath, latestVersionPath);
        version = new RegistryVersion(latestVersionPath, System.currentTimeMillis(), nodePath, session);

        if (versionHistories.get(nodePath) != null) {
            ((RegistryVersionHistory) versionHistories.
                    get(nodePath)).getVersionList().add(version);
            ((RegistryVersion) version).setVersionHistory(
                    (RegistryVersionHistory) versionHistories.get(nodePath));
        } else {
            RegistryVersionHistory vh = new RegistryVersionHistory(session);
            vh.getVersionList().add(version);
            ((RegistryVersion) version).setVersionHistory(vh);
            versionHistories.put(nodePath, vh);
        }
        return version;
    }

    public Version checkin(String s) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {

        Version version = null;
        String latestVersionPath = "";

        if (!isNodeTypeVersionable(s)) {
            throw new UnsupportedRepositoryOperationException("Cannot apply checkin for non versionalbe nodes .!!!");
        }

        try {
            RegistryJCRItemOperationUtil.validateSessionSaved((RegistrySession) session);
            Value propVal = ((Node) session.getItem(s)).getProperty("jcr:checkedOut").getValue();
            if ((propVal != null) && (propVal.getString().equals("true"))) {
                CollectionImpl vnode = ((CollectionImpl) ((RegistrySession) session).getUserRegistry().get(s));
                vnode.setProperty("jcr:checkedOut", "false");
                vnode.setProperty("jcr:isCheckedOut", "false");
                ((RegistrySession) session).getUserRegistry().put(s, vnode);
                version = createVersionOnNode(s);
//                latestVersionPath = createVersionOnNodeAndGetVersionPath(s);
//                if (((RegistrySession) session).getUserRegistry().get(s) instanceof CollectionImpl) {
//                  addVersionToHistory(s,latestVersionPath);
//                }
//                version = new RegistryVersion(latestVersionPath, System.currentTimeMillis(), s, session);
//
//                if (versionHistories.get(s) != null) {
//
//                    ((RegistryVersionHistory) versionHistories.get(s)).getVersionList().add(version);
//                    ((RegistryVersion) version).setVersionHistory((RegistryVersionHistory) versionHistories.get(s));
//
//                } else {
//
//                    RegistryVersionHistory vh = new RegistryVersionHistory(session);
//                    vh.getVersionList().add(version);
//                    ((RegistryVersion) version).setVersionHistory(vh);
//                    versionHistories.put(s, vh);
//                }
            } else {
                // node already checked in
                List<String> list = getVersionList(s);
                version = new RegistryVersion(list.get(list.size() - 1), System.currentTimeMillis(), s, session);

            }
        } catch (PathNotFoundException e) {
            version = getBaseVersion(s);
        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred at registry level..!!" + e.getMessage());
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Cannot do checkin to unsaved nodes..!!!");
        }


        return version;
    }

    private void addVersionToHistory(String nodePath, String nodeVersion) {
        try {

            String confPath = RegistryJCRSpecificStandardLoderUtil.
                    getSystemConfigVersionPath((RegistrySession) session);
            Resource resource = ((RegistrySession) session).getUserRegistry().get(confPath);

            if (resource.getProperty(nodePath) != null) {
                resource.getPropertyValues(nodePath).add(nodeVersion);
            } else {
                List<String> list = new ArrayList<String>();
                list.add(nodeVersion);
                resource.setProperty(nodePath, list);
            }
            ((RegistrySession) session).getUserRegistry().put(confPath, resource);

        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private List<String> getVersionList(String nodePath) throws RegistryException {
        String confPath = RegistryJCRSpecificStandardLoderUtil.
                getSystemConfigVersionPath((RegistrySession) session);
        return ((RegistrySession) session).getUserRegistry().get(confPath).getPropertyValues(nodePath);
    }

    private boolean isNodeTypeVersionable(String s) throws RepositoryException {
        if (session.getNode(s).isNodeType("mix:versionable") ||
                session.getNode(s).isNodeType("mix:simpleVersionable")) {
            return true;
        } else {
            return false;
        }
    }

    public void checkout(String s) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {

        if (!isNodeTypeVersionable(s)) {
            throw new UnsupportedRepositoryOperationException("Cannot apply checkout for non versionalbe nodes .!!!");
        }
        try {
            Resource resource = ((RegistrySession) session).getUserRegistry().get(s);
            resource.setProperty("jcr:checkedOut", "true");   // no need both.But as in JCR spec there are two properties to set
            resource.setProperty("jcr:isCheckedOut", "true");
            ((RegistrySession) session).getUserRegistry().put(s, resource);

        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred at Registry level");
        }

    }

    public Version checkpoint(String s) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {

        checkout(s);

        return checkin(s);
    }

    public boolean isCheckedOut(String s) throws RepositoryException {
        boolean isCheckedOut = false;
//        if (!((Node) session.getItem(s)).isNodeType("mix:simpleVersionable")) {
//            isCheckedOut = true;
//        }

        Value propVal = ((Node) session.getItem(s)).getProperty("jcr:checkedOut").getValue();
        Value propVal1 = ((Node) session.getItem(s)).getProperty("jcr:isCheckedOut").getValue();

        if (propVal.getString().equals("true")
                || (propVal1.getString().equals("true"))) {
            isCheckedOut = true;
        }
        return isCheckedOut;
    }

    public VersionHistory getVersionHistory(String s) throws UnsupportedRepositoryOperationException, RepositoryException {
        if (versionHistories.get(s) == null) {
           versionHistories.put(s, new RegistryVersionHistory(session));
        }

        return versionHistories.get(s);

    }

    public Version getBaseVersion(String s) throws UnsupportedRepositoryOperationException, RepositoryException {

        if (((RegistryVersionHistory) versionHistories.get(s) != null) &&
                (((RegistryVersionHistory) versionHistories.get(s)).getVersionList().size() > 0)) {
            List<Version> list = ((RegistryVersionHistory) versionHistories.get(s)).getVersionList();
            return list.get(list.size() - 1);
        } else {
            return null;
        }
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        //TODO
        for (Version version : versions) {
            restore(version, b);
        }

    }

    private boolean isVersionInNodeVersionHistory(String nodePth, String vPath) throws RegistryException, VersionException {
        if (((getVersionList(nodePth).contains(vPath)))) {
            return true;
        } else {
            throw new VersionException("No such version in node's version history" + nodePth);
        }
    }

    private boolean isValidVersionName(String nodePath) throws UnsupportedRepositoryOperationException {

        try {
            session.getItem(nodePath);
        } catch (PathNotFoundException e) {
            throw new UnsupportedRepositoryOperationException("Unsupported : non versionable node " + nodePath);
        } catch (RepositoryException e) {
            throw new UnsupportedRepositoryOperationException("Unsupported : non versionable node " + nodePath);
        }
        return true;
    }

    public void restore(String s, String s1, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {

        // Check node is versionable
        if (!isNodeTypeVersionable(s)) {
            throw new UnsupportedRepositoryOperationException("Cannot do restore on non versionable nodes");
        }

        if (s1 != null && s1.equals(getVersionHistory(s).getRootVersion().getName())) {
            throw new VersionException("Cannot do restore opeartion on jcr:rootVersion ..!!!");
        }

        try {
            RegistryJCRItemOperationUtil.validateSessionSaved((RegistrySession) session);
            if (isNodeTypeVersionable(s)
                    && isValidVersionName(s)
                    && isVersionInNodeVersionHistory(s, s1)
                    ) {
                ((RegistrySession) session).getUserRegistry().restoreVersion(s1);
                Resource resource = ((RegistrySession) session).getUserRegistry().get(s);
                resource.setProperty("jcr:isCheckedOut", "false");
                resource.setProperty("jcr:checkedOut", "false");
                ((RegistrySession) session).getUserRegistry().put(s, resource);
                createVersionOnNode(s);

            }
        } catch (VersionException e) {
            throw new VersionException("No such version in node's version history" + s);
        } catch (UnsupportedRepositoryOperationException e) {
            throw new UnsupportedRepositoryOperationException("Node type not Versionable : " + s);
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        } catch (RegistryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }

    }

    private String getNodePathFromVersionName(String s) {
        if (!s.contains("/")) {
            return s;
        }
        return s.substring(0, s.lastIndexOf("/"))
                + "/"
                + s.split("/")[s.split("/").length - 1].split(";")[0];

    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {

        // Check node is versionable
        if (!isNodeTypeVersionable(getNodePathFromVersionName(version.getName()))) {
            throw new UnsupportedRepositoryOperationException("Cannot do restore on non versionable nodes");
        }

        if (version != null && version.getName().equals(getVersionHistory(
                getNodePathFromVersionName(version.getName())).
                getRootVersion().getName())) {
            throw new VersionException("Cannot do restore opeartion on jcr:rootVersion ..!!!");
        }


        try {
            RegistryJCRItemOperationUtil.validateSessionSaved((RegistrySession) session);
            if ((version != null)
                    && isValidVersionName(
                    getNodePathFromVersionName(version.getName()))
                    && (isVersionInNodeVersionHistory(
                    getNodePathFromVersionName(version.getName()), version.getName()))
                    ) {
                ((RegistrySession) session).getUserRegistry().restoreVersion(version.getName());
//                version.getFrozenNode().setProperty("jcr:isCheckedOut", "false");
                Resource resource = ((RegistrySession) session).getUserRegistry().
                        get(getNodePathFromVersionName(version.getName()));
                resource.setProperty("jcr:isCheckedOut", "false");
                resource.setProperty("jcr:checkedOut", "false");

                ((RegistrySession) session).getUserRegistry().put(
                        getNodePathFromVersionName(version.getName()), resource);
                //create a new version at restore in simple versioning
                createVersionOnNode(getNodePathFromVersionName(version.getName()));

            }
        } catch (VersionException e) {
            throw new VersionException("No such version in node's version history" +
                    getNodePathFromVersionName(version.getName()));
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        } catch (RegistryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }

    }

    public void restore(String s, Version version, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {

        if (version != null && version.getName().equals(getVersionHistory(s).getRootVersion().getName())) {
            throw new VersionException("Cannot do restore opeartion on jcr:rootVersion ..!!!");
        }

        // Check node is versionable
        if (!isNodeTypeVersionable(s)) {
            throw new UnsupportedRepositoryOperationException("Cannot do restore on non versionable nodes");
        }


        try {
            RegistryJCRItemOperationUtil.validateSessionSaved((RegistrySession) session);
            Item i = session.getItem(s);
            throw new VersionException("There must be no existing node at absPath " + s);
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        } catch (PathNotFoundException e) {
            //success
        }

        try {
            if ((version != null)
                    && isNodeTypeVersionable(s)
                    && isValidVersionName(s)
                    && (isVersionInNodeVersionHistory(s, version.getName()))
                    ) {
                ((RegistrySession) session).getUserRegistry().restoreVersion(version.getName());
                Resource resource = ((RegistrySession) session).getUserRegistry().
                        get(getNodePathFromVersionName(version.getName()));
                resource.setProperty("jcr:isCheckedOut", "false");
                resource.setProperty("jcr:checkedOut", "false");
                ((RegistrySession) session).getUserRegistry().put(
                        getNodePathFromVersionName(version.getName()), resource);
                createVersionOnNode(getNodePathFromVersionName(version.getName()));

            }
        } catch (VersionException e) {
            throw new VersionException("No such version in node's version history" + s);
        } catch (RegistryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }

    }

    public void restoreByLabel(String s, String s1, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {

        String verPAth = ((Node) ((RegistrySession) session).getItem("/jcr:system/jcr:gregVersionLabels")).getProperty(s1).getString();

        try {

            ((RegistrySession) session).getUserRegistry().restoreVersion(verPAth);
        } catch (RegistryException e) {
            e.printStackTrace();
        }


    }

    // TODO :All merging stuff

    public NodeIterator merge(String s, String s1, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {

        return null;
    }

    public NodeIterator merge(String s, String s1, boolean b, boolean b1) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;
    }

    public void doneMerge(String s, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {

    }

    public void cancelMerge(String s, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {

    }

    public Node createConfiguration(String s) throws UnsupportedRepositoryOperationException, RepositoryException {

        /*
          Calling createConfiguration on the node N at absPath creates, in the configuration storage, a new
          nt:configuration node whose root is N. A reference to N is recorded in the jcr:root
          property of the new configuration, and a reference to the new configuration
          is recorded in the jcr:configuration property of N
         */

        String configNodeName = "jcr:configNode" + configNodeCount++;

        ((RegistryNode) session.getItem(s)).addNode(configNodeName, "nt:configuration").setProperty("jcr:root", s);
        ((RegistryNode) session.getItem(s)).setProperty("jcr:configuration", s + "/" + configNodeName);

        return (RegistryNode) session.getItem(s + "/" + configNodeName);
    }

    public Node setActivity(Node node) throws UnsupportedRepositoryOperationException, RepositoryException {

        RegistryNode acNode = null;

        if (node == null) {

            currentActivityNodePath = "";

            if (((RegistryNode) session.getItem("/jcr:system/jcr:activities")).getNodes().hasNext()) {

                acNode = (RegistryNode) ((RegistryNode) session.getItem("/jcr:system/jcr:activities")).getNodes().next();
            }

        } else {

            currentActivityNodePath = node.getPath();
            acNode = (RegistryNode) createActivity(node.getPath());

        }

        return acNode;
    }

    public Node getActivity() throws UnsupportedRepositoryOperationException, RepositoryException {

        return (Node) ((RegistrySession) session).getItem(currentActivityNodePath);
    }

    public Node createActivity(String s) throws UnsupportedRepositoryOperationException, RepositoryException {

        currentActivityNodePath = "/jcr:system/jcr:activities" + s;

        return ((RegistryNode) ((RegistrySession) session).getItem("/jcr:system/jcr:activities")).addNode(s, "nt:activity");

    }

    public void removeActivity(Node node) throws UnsupportedRepositoryOperationException, VersionException, RepositoryException {

        ((RegistrySession) session).removeItem(node.getPath());
    }

    public NodeIterator merge(Node node) throws VersionException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {

        return null;
    }
}
