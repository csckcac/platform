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
package org.wso2.carbon.governance.api.processes.dataobjects;

import org.wso2.carbon.governance.api.exception.GovernanceException;

/**
 * This represents a process artifact stored on the Registry. Process artifacts are created as a
 * result of adding a new process.
 */
public class Process{ /*//extends Service {

    private static final Log log = LogFactory.getLog(Process.class);

    *//**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param qName the qualified name.
     *//*
    public Process(String id, QName qName) {
        super(id, qName);
    }

    *//**
     * Constructor accepting resource identifier and the process content.
     *
     * @param id                    the resource identifier.
     * @param processContentElement an XML element containing the process content.
     *
     * @throws GovernanceException if the construction fails.
     *//*
    public Process(String id, OMElement processContentElement) throws GovernanceException {
        super(id, processContentElement);
    }

    *//**
     * Copy constructor used to create a process out of a service.
     *
     * @param service the object to be copied.
     *//*
    protected Process(Service service) {
        super(service);
    }

    *//**
     * Attach a service artifact to a process artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param service the service to attach.
     *
     * @throws GovernanceException if the operation failed.
     *//*
    public void attachService(Service service) throws GovernanceException {
        attach(service);
    }

    *//**
     * Detach a service artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param serviceId the identifier of the service to detach.
     *
     * @throws GovernanceException if the operation failed.
     *//*
    @SuppressWarnings("unused")
    public void detachService(String serviceId) throws GovernanceException {
        detach(serviceId);
    }

    *//**
     * Method to retrieve all services attached to this service artifact.
     *
     * @return all services attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     *//*
    public Service[] getAttachedServices() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Service> services = new ArrayList<Service>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof Service) {
                    services.add((Service) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting attached service from the artifact at path: " + path +
                    ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return services.toArray(new Service[services.size()]);
    }

    *//**
     * Attach a BPEL artifact to a workflow artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param bpel the BPEL to attach.
     *
     * @throws GovernanceException if the operation failed.
     *//*
    public void attachBPEL(Bpel bpel) throws GovernanceException {
        attach(bpel);
        addAttribute(GovernanceConstants.PROCESS_BPEL_ATTRIBUTE, bpel.getPath());
    }

    *//**
     * Detach a BPEL artifact from a workflow artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param bpelId the identifier of the BPEL to detach.
     *
     * @throws GovernanceException if the operation failed.
     *//*
    @SuppressWarnings("unused")
    public void detachBPEL(String bpelId) throws GovernanceException {
        detach(bpelId);
    }

    *//**
     * Method to retrieve all BPELs attached to this service artifact.
     *
     * @return all BPELs attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     *//*
    public Bpel[] getAttachedBPELs() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Bpel> bpels = new ArrayList<Bpel>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof Bpel) {
                    bpels.add((Bpel) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting attached BPELs from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return bpels.toArray(new Bpel[bpels.size()]);
    }*/
}
