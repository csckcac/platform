/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.api.processes;

import org.wso2.carbon.governance.api.exception.GovernanceException;

/**
 * This provides the management functionality for process artifacts stored on the registry.
 */
public class ProcessManager {

/*
    private ServiceManager manager;

    */
/**
     * Constructor accepting an instance of the registry to use.
     *
     * @param registry the instance of the registry.
     *//*

    public ProcessManager(Registry registry) {
        this.manager = new ServiceManager(registry, GovernanceConstants.PROCESS_MEDIA_TYPE,
                "process") {};
    }

    */
/**
     * Creates a new process artifact from the given qualified name.
     *
     * @param qName the qualified name of this process.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public Process newProcess(QName qName) throws GovernanceException {
        return new Process(manager.newService(qName)) {};
    }

    */
/**
     * Creates a new process artifact from the given content.
     *
     * @param content the process content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     *//*

    public Process newProcess(OMElement content) throws GovernanceException {
        return new Process(manager.newService(content)) {};
    }

    */
/**
     * Adds the given process artifact to the registry.
     *
     * @param process the process artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void addProcess(Process process) throws GovernanceException {
        manager.addService(process);
    }

    */
/**
     * Updates the given process artifact on the registry.
     *
     * @param process the process artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void updateProcess(Process process) throws GovernanceException {
        manager.updateService(process);
    }

    */
/**
     * Fetches the given process artifact on the registry.
     *
     * @param processId the identifier of the process artifact.
     *
     * @return the process artifact.
     * @throws GovernanceException if the operation failed.
     *//*

    public Process getProcess(String processId) throws GovernanceException {
        return new Process(manager.getService(processId)) {};
    }

    */
/**
     * Removes the given process artifact from the registry.
     *
     * @param processId the identifier of the process artifact.
     *
     * @throws GovernanceException if the operation failed.
     *//*

    public void removeProcess(String processId) throws GovernanceException {
        manager.removeService(processId);
    }

    */
/**
     * Finds all process artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the process artifacts that match.
     * @throws GovernanceException if the operation failed.
     *//*

    public Process[] findProcesses(ProcessFilter criteria) throws GovernanceException {
        List<Process> processes = new ArrayList<Process>();
        for (Process process : getAllProcesses()) {
            if (process != null) {
                if (criteria.matches(process)) {
                    processes.add(process);
                }
            }
        }
        return processes.toArray(new Process[processes.size()]);
    }

    // Method to obtain processes from services.
    private Process[] getProcesses(Service[] services) {
        List<Process> processes = new ArrayList<Process>(services.length);
        for (Service service : services) {
            processes.add(new Process(service) {});
        }
        return processes.toArray(new Process[processes.size()]);
    }

    */
/**
     * Finds all process artifacts on the registry.
     *
     * @return all process artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     *//*

    public Process[] getAllProcesses() throws GovernanceException {
        return getProcesses(manager.getAllServices());
    }

    */
/**
     * Finds all identifiers of the process artifacts on the registry.
     *
     * @return an array of identifiers of the process artifacts.
     * @throws GovernanceException if the operation failed.
     *//*

    public String[] getAllProcessIds() throws GovernanceException {
        return manager.getAllServiceIds();
    }
*/
}
