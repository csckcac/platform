/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.mashup.javascript.hostobjects.im;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.mashup.imwrapper.core.IMWrapper;
import org.wso2.carbon.mashup.imwrapper.core.IMWrapperFactory;
import org.wso2.carbon.mashup.imwrapper.core.IMException;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.utils.MashupConstants;

/**
 * <p/>
 * The IM host object is a generic host object designed to send Instant Messages (IM) using various
 * IM protocols. The underlying protocol implementation is kept transparent. MSN, Yahoo, AIM, ICQ and
 * Jabber are the supporetd protocols.
 * <p/>
 * Notes:
 * <p/>
 * 1. When instantiating a IM object, the user is expected to pass in the IM protocol expected to be
 * used.Valid values are msn, yahoo, aim, icq and jabber
 * 2. When the jabber protocol is used the userID should be of the form username@server e.g. username@gmail.com
 * 3. The login function can be called with or without user credentials. If its called with credentials
 * they are used to authenticate the user. If the function is called without credentials the details
 * are taken from the server.xml found under conf directory where the mashup server is located.
 * So if you wish to keep the credentials in server.xml please update it with the needed usernames
 * and passwords. The section that corresponds to this is as follows.
 *
 * <p>
 * <IMConfig>
 *          <!--Account details of the Yahoo account that will be used to send IMs-->
            <Yahoo>
                <username>username</username>
                <password>password</password>
            </Yahoo>
 *           <!--Account details of the MSN account that will be used to send IMs-->
 *          <MSN>
 *               <username>username</username>
 *               <password>password</password>
 *           </MSN>
 *           <!--Account details of the AIM account that will be used to send IMs-->
 *           <AIM>
 *               <username>username</username>
 *               <password>password</password>
 *           </AIM>
 *           <!--Account details of the ICQ account that will be used to send IMs-->
 *           <ICQ>
 *               <username>username</username>
 *               <password>password</password>
 *           </ICQ>
 *           <!--Account details of the Jabber account that will be used to send IMs-->
 *           <Jabber>
 *               <username>username@jabberServer</username>
 *               <password>password</password>
 *           </Jabber>
 *       </IMConfig> 
 * <p/>
 * </p>
 * <p/>
 * <pre>
 * eg:
 *     //Sending an IM using MSN.
 * <p/>
 *     function sendIM(){
 *          var im = new IM("msn");
 *          im.login("username","password");
 *          im.sendMessage("yourFriendsID","hi! This IM was sent using the WSO2 Mashup Server");
 *          im.disconnect();
 *     }
 * <p/>     
 * </pre>
 * </p>
 */
public class IMHostObject extends ScriptableObject {

    private IMWrapper imWrapper;
    private String protocol;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {
        IMHostObject im = new IMHostObject();

        if (args.length == 1 & args[0] instanceof String) {
            String protocol = (String) args[0];
            im.protocol = protocol;
            try {
                im.imWrapper = IMWrapperFactory.createIMProtocolImpl(protocol);
            } catch (IMException e) {
                throw new CarbonException(e);
            }
        } else {
            throw new CarbonException("Invalid parameters. The name of the IM protocol to be used " +
                    "should be parsed in as a parameter.");
        }
        return im;
    }

    /**
     * Return the name of the class.
     * <p/>
     * This is typically the same name as the constructor.
     * Classes extending ScriptableObject must implement this abstract
     * method.
     */
    public String getClassName() {
        return "IM";
    }

    public static void jsFunction_login(Context cx, Scriptable thisObj, Object[] args,
                                        Function funObj)
            throws CarbonException {
        IMHostObject im = (IMHostObject) thisObj;
        String username;
        String password;

        if (args.length == 0) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String key = MashupConstants.IM_CONFIG + "." + im.protocol;
            username = serverConfig.getFirstProperty(key + "." +
                    MashupConstants.USERNAME);
            password = serverConfig.getFirstProperty(key + "." +
                    MashupConstants.PASSWORD);
        } else if (args.length == 2) {
            username = (String) args[0];
            password = (String) args[1];
        } else {
            throw new CarbonException("Incorrect number of arguments. If you want to use login " +
                    "details specified in the server.xml call this method with 0 parameters else " +
                    "pass in the username and password");
        }
        try {
            im.imWrapper.login(username, password);
        } catch (IMException e) {
            throw new CarbonException(e);
        }
    }

    public void jsFunction_sendMessage(String to, String message) throws CarbonException {
        try {
            imWrapper.sendMessage(to, message);
        } catch (IMException e) {
            throw new CarbonException(e);
        }
    }

    public void jsFunction_disconnect() throws CarbonException {
        try {
            imWrapper.disconnect();
        } catch (IMException e) {
            throw new CarbonException(e);
        }
    }
}
