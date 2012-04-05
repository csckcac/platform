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
package org.wso2.carbon.mashup.imwrapper.core;

import org.wso2.carbon.mashup.imwrapper.core.internal.Activator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IMWrapperFactory {

    private static final Log log = LogFactory.getLog(IMWrapperFactory.class);

    public static IMWrapper createIMProtocolImpl(String protocol) throws IMException {
        String className = Activator.getIMImplementatios().get(protocol);
        if (className == null) {
            String message = "Cannot find implementation class for IM protocol " + protocol;
            log.error(message);
            throw new IMException(message);
        }
        Class aClass;
        try {
            aClass = Class.forName(className);
            return (IMWrapper) aClass.newInstance();
        } catch (ClassNotFoundException e) {
            String message = "Cannot find implementation class for IM protocol " + protocol;
            log.error(message, e);
            throw new IMException(message);
        } catch (IllegalAccessException e) {
            String message = "Cannot instantiate class for IM protocol " + protocol;
            log.error(message, e);
            throw new IMException(message);
        } catch (InstantiationException e) {
            String message = "Cannot instantiate class for IM protocol " + protocol;
            log.error(message, e);
            throw new IMException(message);
        }
    }
}
