package org.wso2.carbon.eventbridge.streamdefn.cassandra.internal.util;

import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.subscriber.BAMEventSubscriber;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

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
    private static RealmService realmService;
    private static AuthenticationService authenticationService;
    private static RegistryService registryService;
    private static DataAccessService dataAccessService;

    private static CassandraConnector cassandraConnector;

    private static BAMEventSubscriber bamEventSubscriber;


    public static CassandraConnector getCassandraConnector() {
        return cassandraConnector;
    }

    public static void setCassandraConnector(CassandraConnector cassandraConnector) {
        Utils.cassandraConnector = cassandraConnector;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        Utils.dataAccessService = dataAccessService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        Utils.realmService = realmService;
    }

    public static AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static void setAuthenticationService(AuthenticationService authenticationService) {
        Utils.authenticationService = authenticationService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        Utils.registryService = registryService;
    }

    public static BAMEventSubscriber getBamEventSubscriber() {
        return bamEventSubscriber;
    }

    public static void setBamEventSubscriber(BAMEventSubscriber bamEventSubscriber) {
        Utils.bamEventSubscriber = bamEventSubscriber;
    }

}