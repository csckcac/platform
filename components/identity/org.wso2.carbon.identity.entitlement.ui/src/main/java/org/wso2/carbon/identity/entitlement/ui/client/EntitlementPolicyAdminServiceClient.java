/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.ui.client;

import java.lang.Exception;
import java.util.List;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.*;


public class EntitlementPolicyAdminServiceClient {

    private EntitlementPolicyAdminServiceStub stub;
    private static final Log log = LogFactory.getLog(EntitlementPolicyAdminServiceClient.class);

    /**
     * Instantiates EntitlementServiceClient
     * 
     * @param cookie For session management
     * @param backendServerURL URL of the back end server where EntitlementPolicyAdminService is
     *        running.
     * @param configCtx ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public EntitlementPolicyAdminServiceClient(String cookie, String backendServerURL,
            ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "EntitlementPolicyAdminService";
        stub = new EntitlementPolicyAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     *
     * @param policyTypeFilter
     * @param policySearchString
     * @param pageNumber
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies that reside in the
     * given page.
     * @throws AxisFault
     */
    public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter,String policySearchString,
                                                int pageNumber) throws AxisFault {
        try {
            return stub.getAllPolicies(policyTypeFilter, policySearchString, pageNumber);
        } catch (Exception e) {
            String message = "Error while loading all policies from backend service";
            handleException(message, e);
        }
        PaginatedPolicySetDTO paginatedPolicySetDTO =  new PaginatedPolicySetDTO();
        paginatedPolicySetDTO.setPolicySet(new PolicyDTO[0]);
        return paginatedPolicySetDTO;
    }

    /**
	 * Gets policy DTO for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getPolicy(String policyId) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getPolicy(policyId);
            dto.setPolicy(dto.getPolicy().trim().replaceAll("><", ">\n<"));            
        } catch (Exception e) {
            String message = "Error while loading the policy from backend service";
            handleException(message, e);
        }
        return dto;
    }


    /**
	 * Gets light weight policy DTO for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getLightPolicy(String policyId) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getLightPolicy(policyId);
        } catch (Exception e) {
            String message = "Error while loading the policy from backend service";
            handleException(message, e);
        }
        return dto;
    }


    /**
	 * Gets light weight policy DTO with attribute Meta data for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy DTO
     * @throws AxisFault throws
     */
    public PolicyDTO getMetaDataPolicy(String policyId) throws AxisFault {
        PolicyDTO dto = null;
        try {
            dto = stub.getMetaDataPolicy(policyId);
        } catch (Exception e) {
            String message = "Error while loading the policy from backend service";
            handleException(message, e);
        }
        return dto;
    }
    /**
     * 
     * @param policy
     * @throws AxisFault
     */
    public void removePolicy(PolicyDTO policy) throws AxisFault {
        try {
            stub.removePolicy(policy);
        } catch (Exception e) {
            String message = "Error while removing the policy from backend service";
            handleException(message, e);
        }
    }

    /**
     * 
     * @param policy
     * @throws AxisFault
     */
    public void updatePolicy(PolicyDTO policy) throws AxisFault {
        try {
            if(policy.getPolicy() != null && policy.getPolicy().trim().length() > 0){
                policy.setPolicy(policy.getPolicy().trim().replaceAll(">\\s+<", "><"));                
            }
            stub.updatePolicy(policy);
        } catch (Exception e) {
            String message = "Error while updating the policy at the backend service";
            handleException(message, e);
        }
    }

    /**
     * 
     * @param policy
     * @throws AxisFault
     */
    public void addPolicy(PolicyDTO policy) throws AxisFault {
        try {
            policy.setPolicy(policy.getPolicy().trim().replaceAll(">\\s+<", "><"));            
            stub.addPolicy(policy);
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
    }

    /**
     * adding an entitlement policy which is extracted using file upload executor
     * @param content  content of the policy as a <code>String</code> Object
     * @throws AxisFault, throws if fails
     */
    public void uploadPolicy(String content) throws AxisFault {

        PolicyDTO dto = new PolicyDTO();
        dto.setPolicy(content);
        dto.setPolicy(dto.getPolicy().trim().replaceAll(">\\s+<", "><"));
        try {
            stub.addPolicy(dto);
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
    }

    /**
     * Import XACML policy from registry
     * @param policyRegistryPath registry path
     * @throws AxisFault
     */
    public void importPolicyFromRegistry(String policyRegistryPath) throws AxisFault {

        try {
            stub.importPolicyFromRegistry(policyRegistryPath);
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);            
        }
    }
    
    /**
     * Clears the decision cache maintained by the PDP.
     * @throws AxisFault
     */
    public void clearDecisionCache() throws AxisFault {

        try {
            stub.clearDecisionCache();
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);            
        }
    }

    /**
     * Clears the attribute cache maintained by the PDP.
     * @throws AxisFault
     */
    public void clearAttributeCache() throws AxisFault {

        try {
            stub.clearAllAttributeCaches();
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
    }

    /**
     * Pre-defined Attributes related with XACML Policy is fetched from registry.
     * @param resourceName Resource Name of the Registry
     * @return
     * @throws org.apache.axis2.AxisFault
     */

    public String [] getEntitlementPolicyDataFromRegistry (String resourceName) throws AxisFault {

        try {
            return stub.getEntitlementPolicyDataFromRegistry(resourceName);
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
        return null;
    }

    /**
     * Returns the list of policy set ids available in PDP
     * @return list of policy set ids
     * @throws AxisFault
     */
    public String[] getAllPolicyIds() throws AxisFault {

        try {
            return stub.getAllPolicyIds();
        } catch (Exception e) {
            String message = e.getMessage();
            handleException(message, e);
        }
        return null;
    }

    /**
     *  Get  globally defined policy combining algorithm
     * @return policy combining algorithm as a String
     * @throws AxisFault
     */
    public String getGlobalPolicyAlgorithm() throws AxisFault {
        try {
            return stub.getGlobalPolicyAlgorithm();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Set policy combining algorithm globally
     * @param policyAlgorithm policy combining algorithm as a String
     * @throws AxisFault
     */
    public void setGlobalPolicyAlgorithm(String policyAlgorithm) throws AxisFault {
        try {
            stub.setGlobalPolicyAlgorithm(policyAlgorithm);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }
    
     /**
     * 
     * @param requestContext
     * @return
     * @throws FileUploadException
     */
    private List parseRequest(ServletRequestContext requestContext) throws FileUploadException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        return upload.parseRequest(requestContext);
    }

   /**
    * Gets user or role entitled resources
    * @param subjectName user or role name
    * @param resourceName resource name
    * @param subjectId  attribute id of the subject, user or role
    * @param action action name 
    * @param enableChildSearch whether search is done for the child resources under the given resource name
    * @param useApplicablePolices whether search is done only for applicable policies in given search parameters
    * @throws org.apache.axis2.AxisFault  throws
    * @return entitled resources as String array
    */
    public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                                      String subjectId, String action, boolean enableChildSearch,
                                                      boolean useApplicablePolices)
            throws AxisFault {
        try {
           return  stub.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                                              enableChildSearch, useApplicablePolices);
        } catch (Exception e) {
           handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Gets attribute value tree for given attribute type
     * @return attribute value tree
     * @throws AxisFault throws
     */
    public PolicyAttributeDTO[] getPolicyAttributeValues()
            throws AxisFault {
        try {
           return  stub.getPolicyAttributeValues();
        } catch (Exception e) {
           handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Logs and wraps the given exception.
     * 
     * @param msg Error message
     * @param e Exception
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

}
