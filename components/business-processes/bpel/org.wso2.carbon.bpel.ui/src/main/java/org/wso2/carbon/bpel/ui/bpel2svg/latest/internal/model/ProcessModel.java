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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityRoot;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessModelStatus;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a process model.
 */
public class ProcessModel
        implements Serializable, ProcessItem<ProcessModelStatus> {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -7112469838121594891L;

    /**
     * The id of the process model.
     */
    private final String pid;

    /**
     * The name of the process model.
     */
    private final String name;

    /**
     * The version of the process model.
     */
    private final long version;

    /**
     * The status of the process model.
     */
    private ProcessModelStatus status;

    /**
     * The URL to the document (e.g BPLE file) of the process model.
     */
    private URL document;

    /**
     * The list of the process instances of the process model.
     */
    private List<ProcessInstance> processInstances;

    /**
     * The associated root activity.
     */
    private ActivityRoot activityRoot;


    private String definition;

    /**
     * Constructor of ProcessModell.
     *
     * @param pid      The id of the process model.
     * @param name     The name of the process model.
     * @param version  The version of the process model.
     * @param status   The status of the process model.
     * @param document The URL to the document of the process model.
     */
    public ProcessModel(String pid, String name, long version, ProcessModelStatus status, URL document) {
        this.pid = pid;
        this.name = name;
        this.version = version;
        this.status = status;
        this.document = document;
        this.definition = null;
        this.processInstances = new ArrayList<ProcessInstance>();
    }

    public ProcessModel(String pid, String name, long version, ProcessModelStatus status, String definition) {
        this.pid = pid;
        this.name = name;
        this.version = version;
        this.status = status;
        this.document = null;
        this.definition = definition;
        this.processInstances = new ArrayList<ProcessInstance>();
    }

    public String getDefinition() {
        return definition;
    }

    /**
     * Returns the {@link ProcessInstance} with the given IID or <code>null</code>, if there does not exist a
     * {@link ProcessInstance} with the given ID.
     *
     * @return The {@link ProcessInstance} with the given IID or <code>null</code>, if there does not exist a
     *         {@link ProcessInstance} with the given ID.
     */
    public ProcessInstance getProcessInstance(String iid) {
        for (ProcessInstance instance : getProcessInstances()) {
            if (instance.getIid().equals(iid)) {
                return instance;
            }
        }

        return null;
    }

    /**
     * Returns the latest active {@link ProcessInstance} or <code>null</code>, if there does not exist a
     * {@link ProcessInstance}.
     *
     * @return The latest active {@link ProcessInstance} or <code>null</code>, if there does not exist a
     *         {@link ProcessInstance}.
     */
    public ProcessInstance getLatestProcessInstance() {
        ProcessInstance latestInstance = null;
        for (ProcessInstance instance : getProcessInstances()) {
            if (latestInstance == null
                    || instance.getLastActivityDate().after(latestInstance.getLastActivityDate())) {
                latestInstance = instance;
            }
        }

        return latestInstance;
    }

    /**
     * Returns the number of {@link ProcessInstance}s associated with this {@link ProcessModel}.
     *
     * @return The number of {@link ProcessInstance}s associated with this {@link ProcessModel}.
     */
    public int getProcessInstanceCount() {
        return this.processInstances.size();
    }

    /**
     * Returns the value of pid.
     *
     * @return The value of pid.
     */
    public String getPid() {
        return this.pid;
    }

    /**
     * Returns the value of name.
     *
     * @return The value of name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of version.
     *
     * @return The value of version.
     */
    public long getVersion() {
        return this.version;
    }

    /**
     * Returns the value of status.
     *
     * @return The value of status.
     */
    public ProcessModelStatus getStatus() {
        return this.status;
    }

    /**
     * Set the value of status to status.
     *
     * @param status The new value of status.
     */
    public void setStatus(ProcessModelStatus status) {
        this.status = status;
    }

    /**
     * Returns the value of document.
     *
     * @return The value of document.
     */
    public URL getDocument() {
        return this.document;
    }

    /**
     * Set the value of bpelDocument to document.
     *
     * @param document The new value of document.
     */
    public void setDocument(URL document) {
        this.document = document;
    }

    /**
     * Returns the value of processInstances.
     *
     * @return The value of processInstances.
     */
    public List<ProcessInstance> getProcessInstances() {
        return this.processInstances;
    }

    /**
     * Sets the value of processInstances to processInstances.
     *
     * @param processInstances The new value of processInstances.
     */
    public void setProcessInstances(List<ProcessInstance> processInstances) {
        this.processInstances = processInstances;
    }

    /**
     * Returns the value of activityRoot.
     *
     * @return The value of activityRoot.
     */
    public ActivityRoot getActivityRoot() {
        return this.activityRoot;
    }

    /**
     * Sets the value of activityRoot to activityRoot.
     *
     * @param activityRoot The new value of activityRoot.
     */
    public void setActivityRoot(ActivityRoot activityRoot) {
        this.activityRoot = activityRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessModel && getPid() != null) {
            ProcessModel process = (ProcessModel) obj;
            return getPid().equals(process.getPid());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getPid() == null) {
            return 0;
        }

        return getPid().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PID: " + getPid() + ", Name: " + getName() + ", Version: " + getVersion() + ", Status: "
                + getStatus();
    }

}
