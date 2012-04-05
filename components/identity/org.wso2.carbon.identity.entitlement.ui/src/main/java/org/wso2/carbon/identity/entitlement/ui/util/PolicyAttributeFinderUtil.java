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

package org.wso2.carbon.identity.entitlement.ui.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.dto.AttributeValueTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreationException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import java.util.*;

/**
 * This util class helps to create attribute value names for given attribute type
 */
public class PolicyAttributeFinderUtil {

    private static final Log log = LogFactory.getLog(PolicyAttributeFinderUtil.class);
    /**
     * This method returns the operation list of the wsdl for a given service Uri
     * @param  serviceUri
     * @return Operation List
     * @throws org.apache.axis2.AxisFault
     * @throws org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreationException
     */
    public static List<String> getOperationListFromServiceUri(String serviceUri) throws AxisFault,
            EntitlementPolicyCreationException {

        List<String> operationList = new ArrayList<String>();
        CodeGenConfiguration codeGenConfiguration = null;
        AxisService axisService = null;
        
        try{
            CodeGenerationEngine codeGenerationEngine  = new CodeGenerationEngine(codeGenConfiguration);
            Definition wsdl4jDef = codeGenerationEngine.readInTheWSDLFile(serviceUri + "?wsdl");
            WSDL11ToAxisServiceBuilder wsdl11ToAxisServiceBuilder = new WSDL11ToAxisServiceBuilder
                    (wsdl4jDef, null, null, false);
            axisService = wsdl11ToAxisServiceBuilder.populateService();
        } catch (WSDLException e) {
            log.warn("Operation List can not be created for given wsdl uri  " + serviceUri + "?wsdl");
        } catch (CodeGenerationException e) {
            log.warn("Operation List can not be created for given wsdl uri  " + serviceUri + "?wsdl");
        }

        if(axisService != null) {
            Iterator iterator = axisService.getOperations();    
            while(iterator.hasNext()) {
                AxisOperation operation  =  (AxisOperation) iterator.next();
                operationList.add(operation.getName().getLocalPart());
            }
        }
        return operationList;
    }

}

