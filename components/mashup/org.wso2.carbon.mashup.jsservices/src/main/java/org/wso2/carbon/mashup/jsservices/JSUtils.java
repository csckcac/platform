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
package org.wso2.carbon.mashup.jsservices;

import org.apache.axis2.util.XMLChar;
import org.apache.axis2.AxisFault;

public class JSUtils {

    /**
     * Given a object obtained from javascript this method can be used to figure out whether the value if true or false.
     * These are the javascript rules to determine whethere an object is true or false.
     * Undefined =  false
     * Null = false
     * Boolean = the input argument (no conversion).
     * Number = false if the argument is +0, −0, or NaN; otherwise the result is true.
     * String = false if the argument is the empty string (its length is zero); otherwise the result is true.
     * Object = true
     *
     * @param defaultValue - The default value of the return in case the property was not defined true/false.
     * @param jsObject     - The jsObject the represents the value that need to be checked
     * @return boolean indicating whether the given value if true or false infered according to the ruls given above.
     */
    public static boolean isJSObjectTrue(boolean defaultValue, Object jsObject) {
        boolean isTRue = defaultValue;
        if (jsObject instanceof Boolean) {
            isTRue = (Boolean) jsObject;
            return isTRue;
        }
        if (jsObject instanceof Number) {
            Number number = (Number) jsObject;
            if (number.doubleValue() == 0 || ((Double) number).isNaN()) {
                isTRue = false;
            }
            return isTRue;
        }
        if (jsObject instanceof String) {
            String str = (String) jsObject;
            if ("false".equalsIgnoreCase(str)) {
                isTRue = false;
            }
            return isTRue;
        }
        return isTRue;
    }

    /**
     * Method used to validate serviceNames and Operation names. The legal characters are specified at
     * https://wso2.org/jira/browse/MASHUP-272
     *
     * @param ncName - The name that needs to be validated
     * @param name   - Used to construct the error message. ServiceName or OperationName
     * @throws AxisFault - Thrown in case validation fails
     */
    public static void validateName(String ncName, String name)
            throws AxisFault {
        if (ncName.length() == 0) {
            throw new AxisFault(
                    "A " + name + " cannot be empty. The " + name + " " + ncName + " was empty");
        }
        char ch = ncName.charAt(0);
        if (!XMLChar.isNCNameStart(ch) || ch == '\u212E') {
            throw new AxisFault(" A " + name + " cannot start with the character " + ch +
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
                throw new AxisFault("The character " + ch + " found at location " + i +
                        " cannot be used in a " + name
                        + ". " + ncName + " is not valid " + name);
            }
        }
    }
}
