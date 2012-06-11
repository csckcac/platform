package org.wso2.carbon.eventbridge.restapi.internal;

import org.wso2.carbon.eventbridge.core.engine.Engine;
import org.wso2.carbon.eventbridge.core.receiver.AbstractEventReceiver;
import org.wso2.carbon.identity.authentication.AuthenticationService;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Utils {

    private static AuthenticationService authenticationService;

    private static Engine engine;

    private static AbstractEventReceiver eventReceiver;

    public static AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static void setAuthenticationService(AuthenticationService authenticationService) {
        Utils.authenticationService = authenticationService;
    }

    public static Engine getEngine() {
        return engine;
    }

    public static void setEngine(Engine engine) {
        Utils.engine = engine;
    }

    public static AbstractEventReceiver getEventReceiver() {
        return eventReceiver;
    }

    public static void setEventReceiver(AbstractEventReceiver eventReceiver) {
        Utils.eventReceiver = eventReceiver;
    }
}
