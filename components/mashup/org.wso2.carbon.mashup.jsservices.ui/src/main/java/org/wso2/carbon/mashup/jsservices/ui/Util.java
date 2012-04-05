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
package org.wso2.carbon.mashup.jsservices.ui;

import org.wso2.carbon.CarbonError;
import org.wso2.carbon.CarbonException;
import org.apache.axis2.util.XMLChar;

import javax.servlet.http.HttpServletRequest;

public class Util {

    public static void handleException(HttpServletRequest request, String message) {
        CarbonError carbonError = new CarbonError();
        carbonError.addError(message);
        request.setAttribute(CarbonError.ID, carbonError);
    }

    /**
     * Method used to validate serviceNames and Operation names. The legal characters are specified at
     * https://wso2.org/jira/browse/MASHUP-272
     *
     * @param ncName - The name that needs to be validated
     * @param name   - Used to construct the error message. ServiceName or OperationName
     * @throws CarbonException - Thrown in case validation fails
     */
    public static void validateName(String ncName, String name)
            throws CarbonException {
        if (ncName.length() == 0) {
            throw new CarbonException(
                    "A " + name + " cannot be empty. The " + name + " " + ncName + " was empty");
        }
        char ch = ncName.charAt(0);
        if (!XMLChar.isNCNameStart(ch) || ch == '\u212E') {
            throw new CarbonException(" A " + name + " cannot start with the character " + ch +
                    ". The " + name + " " +
                    ncName + " is not valid");
        }
        for (int i = 1; i < ncName.length(); i++) {
            ch = ncName.charAt(i);
            if (!XMLChar.isNCName(ch) || ch == '\u212E' || ch == '\u00B7' || ch == '\u0387' ||
                    ch == '\u06DD' ||
                    ch == '\u06DE'
                    || ch == '\u002D' || ch == '\u002E') {
                i++;
                throw new CarbonException("The character " + ch + " found at location " + i +
                        " cannot be used in a " + name
                        + ". " + ncName + " is not a valid " + name);
            }
        }
    }
}
