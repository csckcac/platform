package org.wso2.carbon.hostobjects.extra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

/**
 * TwitterConnect Test Suite
 */
public class TwitterConnectHostObjectTest {
    private static final Log log = LogFactory.getLog(TwitterConnectHostObjectTest.class);

    public void testTwitterConnect() {
        //These are some test keys taken from scribe OAuth library
/*        String[] args = {"6icbcAXyZx67r8uTAUM5Qw", "SCCAdUUc6LXxiazxH3N0QfpNUvlUy84mZ2XZKiv39s"};
        try {
            TwitterConnectHostObject tcho = (TwitterConnectHostObject) TwitterConnectHostObject.jsConstructor(null, args, null, true);
            tcho.jsFunction_getRequestToken();
            assert tcho.jsFunction_getAuthorizationUrl().length() != 0;
            log.info("Twitter Authorization URL Created : " + tcho.jsFunction_getAuthorizationUrl());
        } catch (ScriptException e) {
            log.error(e);
        }*/
    }
}
