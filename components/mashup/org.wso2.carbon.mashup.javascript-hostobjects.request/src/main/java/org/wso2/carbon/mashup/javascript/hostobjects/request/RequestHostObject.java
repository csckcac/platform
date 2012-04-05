/*
 * Copyright 2007,2008 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mashup.javascript.hostobjects.request;

import org.apache.axis2.transport.http.HTTPConstants;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonException;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.rampart.RampartMessageData;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class RequestHostObject extends ScriptableObject {

    public String getClassName() {
        return "Request";
    }

    /**
     * <p/> This is a JavaScript Rhino host object aimed to provide information regarding the
     * request it received and hence it consists of a set of readOnly properties. </p> <pre/>
     * Example Usage: function securedUsingUTOverHTTPS() { system.log("This mashup was invoked by
     * the following User : " + request.authenticatedUser ); system.log("This mashup was invoked
     * from the following IP : " + request.remoteIP ); system.log("This mashup was invoked with the
     * following URL : " + request.address ); } </pre>
     */
    public void jsConstructor() {
    }

    /**
     * <p/> Appplicable to Mashups that are secured using scecurity scenario 1 (UsernameToken with
     * Timestamp over HTTPS). This method will return the username of the user who called this
     * mashup. </p>
     * <p/>
     * <pre>
     * var authenticatedUser = system.authenticatedUser;
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public String jsGet_authenticatedUser() throws CarbonException {
        // Getting the current MessageContext
        MessageContext msgContext = MessageContext.getCurrentMessageContext();

        // Getting HttpServletRequest from Message Context
        String username = (String) msgContext.getProperty(RampartMessageData.USERNAME);
        if (username == null) {
            throw new CarbonException("Username not present in the request");
        }
        return username;
    }

    /**
     * <p/> Appplicable to Mashups that are secured using scecurity scenario 1 (UsernameToken with
     * Timestamp over HTTPS). This method will return the username of the user who called this
     * mashup. </p>
     * <p/>
     * <pre>
     *     var remoteIP = system.remoteIP;
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public String jsGet_remoteIP() throws CarbonException {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        return (String) messageContext.getProperty(MessageContext.REMOTE_ADDR);
    }

    /**
     * <p/> Returns the remoteIP that the request was made from. </p>
     * <p/>
     * <pre>
     *     var requestURL = system.address;
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public String jsGet_address() throws CarbonException {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        EndpointReference to = messageContext.getTo();
        String address = null;
        if (to != null) {
            address = to.getAddress();
        }
        return address;
    }

    /**
     * Return the HTTP headers of the current request
     *
     * @param cx
     * @param thisObj
     * @param arguments
     * @param funObj
     *
     * @throws CarbonException
     */
    public static NativeArray jsFunction_getHeaders(Context cx, Scriptable thisObj,
                                                       Object[] arguments, Function funObj)
            throws CarbonException {
        if (arguments.length != 0) {
            throw new CarbonException(
                    "Invalid number of parameters for request.getHeader() method.");
        }

        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpServletRequest request =
                (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        Enumeration headerEnums = request.getHeaderNames();
        List<NativeObject> headers = new ArrayList<NativeObject>();
        while (headerEnums.hasMoreElements()) {
            NativeObject header = new NativeObject();
            String headerName = (String) headerEnums.nextElement();
            header.put("name", header, headerName);
            header.put("value", header, request.getHeader(headerName));
            headers.add(header);
        }
        return new NativeArray(headers.toArray());
    }

    /**
     * Return an HTTP header of the current request
     *
     * @param cx
     * @param thisObj
     * @param arguments
     * @param funObj
     *
     * @throws CarbonException
     */
    public static String jsFunction_getHeader(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj) throws CarbonException {
        if (arguments.length != 1) {
            throw new CarbonException(
                    "Invalid number of parameters for request.getHeader() method.");
        }
        if (arguments[0] instanceof String) {
            MessageContext msgCtx = MessageContext.getCurrentMessageContext();
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            return request.getHeader((String) arguments[0]);
        } else {
            throw new CarbonException(
                    "Invalid parameter for request.getHeader() method. Parameter should be a string");
        }
    }
}
