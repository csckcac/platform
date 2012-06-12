/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.runtime.xpath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.utils.HTNameSpaces;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.List;

/**
 * The XPath function resolver for Human Task specific evaluation methods.
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {

    private static final Log log = LogFactory.getLog(JaxpFunctionResolver.class);

    private EvaluationContext evalCtx;

    public XPathFunction resolveFunction(QName functionName, int arity) {
        if (log.isDebugEnabled()) {
            log.debug("Resolving function: " + functionName);
        }

        if (functionName.getNamespaceURI() == null) {
            throw new NullPointerException("Undeclared namespace for " + functionName);
        } else if (HTNameSpaces.HTD_NS.equals(functionName.getNamespaceURI())) {
            String localPart = functionName.getLocalPart();
            if (XPath2Constants.FUNCTION_GET_POTENTIAL_OWNERS.equals(localPart)) {
                return new GetPotentialOwners();
            } else {
                String errMsg = "This operation is not currently supported in this version of WSO2 BPS.";
                if (XPath2Constants.FUNCTION_GET_ACTUAL_OWNER.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_BUSINESS_ADMINISTRATORS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_EXCLUDED_OWNERS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_INPUT.equals(localPart)) {
                    return new GetInput();
                } else if (XPath2Constants.FUNCTION_GET_TASK_INITIATOR.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_TASK_PRIORITY.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_TASK_STAKEHOLDERS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_LOGICAL_PEOPLE_GROUP.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_INTERSECT.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_UNION.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_EXCEPT.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else {
                    throw new IllegalArgumentException("Unknown Human Task Function: " + localPart);
                }
            }

        }
        return null;
    }

    public JaxpFunctionResolver(EvaluationContext evalCtx) {
        this.evalCtx = evalCtx;
    }

    public class GetInput implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            MessageDAO inputMsg = evalCtx.getInput();
            String partName = (String) args.get(0);
            Node matchingElement = null;
            if (StringUtils.isNullOrEmpty(partName)) {
                throw new HumanTaskRuntimeException("The getInput function should be provided with" +
                        " the part name");
            }

            if (inputMsg.getBodyData().hasChildNodes()) {
                NodeList nodeList = inputMsg.getBodyData().getChildNodes();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (partName.trim().equals(nodeList.item(i).getNodeName())) {
                        matchingElement = nodeList.item(i);
                    }
                }
            }

            if (matchingElement == null || matchingElement.getFirstChild() == null) {
                throw new HumanTaskRuntimeException("Cannot find a matching Element for " +
                        "expression evaluation: getInput");
            }

            return matchingElement.getFirstChild();
        }
    }

    public static class GetPotentialOwners implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            return null;
        }
    }
}
