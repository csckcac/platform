/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.csg.common;

public class CSGConstant {

    /**
     * The csg thirft server port
     */
    public static final String THRIFT_SERVER_PORT = "csg-thrift-server-port";

    /**
     * Host name in which csg thrift server is running
     */
    public static final String THRIFT_SERVER_HOST_NAME = "csg-thrift-server-hostname";

    public static final String INITIAL_RECONNECT_DURATION = "csg-thirft-re-connect-duration";

    /**
     * Defines the timeout parameter for thrift server
     */
    public static final String CSG_THRIFT_CLIENT_TIMEOUT = "csg-thrift-timeout";

    public static final int DEFAULT_PORT = 15001;

    public static final int DEFAULT_TIMEOUT = 60 * 15 * 1000;

    /**
     * The type of csg thrift server
     */
    public static final String SERVER_TYPE = "csg-thrift-server-type";

    /**
     * The type of protocol for thrift server
     */
    public static final String PROTOCOL_TYPE = "csg-thrift-protocol-type";

    /**
     * The type of transport for thrift server
     */
    public static final String TRANSPORT_TYPE = "csg-thrift-server-transport-type";

    /**
     * The time that the csg transport should block until a response comes
     */
    public static final String CSG_SEMAPHORE_TIMEOUT = "csg-so-timeout";

    // worker thread pool default values
    public static final int WORKERS_CORE_THREADS = 20;
    public static final int WORKERS_MAX_THREADS = 500;
    public static final int CSG_WORKERS_MAX_THREADS = 500;
    public static final int WORKER_KEEP_ALIVE = 5;
    public static final int WORKER_BLOCKING_QUEUE_LENGTH = -1;

    // csg transport sender thread pool param
    public static final String CSG_T_CORE = "csg-t-core";
    public static final String CSG_T_MAX = "csg-t-max";
    public static final String CSG_T_ALIVE = "csg-t-alive-sec";
    public static final String CSG_T_QLEN = "csg-t-qlen";

    // csg thrift transport thread pool param
    public static final String CSG_THRIFT_T_CORE = "csg-thrift-t-core";
    public static final String CSG_THRIFT_T_MAX = "csg-thrift-t-max";
    public static final String CSG_THRIFT_T_ALIVE = "csg-thrift-t-alive";
    public static final String CSG_THRIFT_T_QLEN = "csg-thrift-t-qlen";

    /**
     * No of concurrent consumers to poll the server from csg thrift transport receiver
     */
    public static final String NO_OF_CONCURRENT_CONSUMERS = "csg-thrift-t-c-c";

    /**
     * The no of messages that the  this client should read from the server
     */
    public static final String MESSAGE_BLOCK_SIZE = "csg-thrift-t-m-s";

    /**
     * The size of response message block that csg thrift transport should send to
     * client
     */
    public static final String RESPONSE_MESSAGE_BLOCK_SIZE = "csg-thrift-r-m-s";

    /**
     * Use this no of message block for processing
     */
    public static final String MESSAGE_PROCESSING_BLOCK_SIZE = "csg-thrift-p-r-m-s";

    public static final int DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE = 5;

    /**
     * The csg thrift server's buffer key
     */
    public static final String CSG_POLLING_TRANSPORT_BUF_KEY = "CSG_POLLING_TRANSPORT_BUF_KEY";

    public static final String CSG_WEB_APP_BUF_KEY = "CSG_WEB_APP_BUF_KEY";

    public static final String CSG_WEB_APP_WORKER_POOL = "CSG_WEB_APP_WORKER_POOL";

    public static final String PROGRESSION_FACTOR = "csg-progression-factor";

    public static final String TIME_UNIT = "csg-time-unit";

    public static final String NO_OF_SCHEDULER_TIME_UNITS = "no-of-csg-scheduler-time-units";

    public static final String NO_OF_IDLE_MESSAGE_TIME_UNITS = "no-of-idle-msg-time-units";

    public static final String MILLISECOND = "millisecond";

    public static final String SECOND = "second";

    public static final String MINUTE = "minute";

    public static final String HOUR = "hour";

    public static final String DAY = "day";

    public static final String CSG_CORRELATION_KEY = "CSG_CORRELATION_KEY";

    public static final String CSG_TRANSPORT_PREFIX = "csg://";

    public static final String CSG_THRIFT_TRANSPORT_PREFIX = "csgthrift:/";

    public static final String CSG_SERVER_HOST = "host";

    public static final String CSG_SERVER_PORT = "port";

    public static final String CSG_SERVER_USER_NAME = "username";

    public static final String CSG_SERVER_PASS_WORD = "password";

    public static final String CSG_SERVER_NAME = "name";

    public static final String CSG_SERVER_DOMAIN_NAME = "domain";

    /**
     * The registry path for CSG storage
     */
    public static final String REGISTRY_CSG_RESOURCE_PATH =
            "/repository/components/org.wso2.carbon.cloud.csg/";

    /**
     * The CSG_TRANSPORT_NAME server collection for storing CSG server information
     */
    public static final String REGISTRY_SERVER_RESOURCE_PATH =
            REGISTRY_CSG_RESOURCE_PATH + "servers";

    /**
     * The CSG flag collection for keeping track of published services etc..
     */
    public static final String REGISTRY_FLAG_RESOURCE_PATH = REGISTRY_CSG_RESOURCE_PATH + "flags";

    /**
     * The path where WSDLs of published services are stored.
     */
    public static final String REGISTRY_CSG_WSDL_RESOURCE_PATH = "/trunk/services/wsdls";

    /**
     * Client axis2.xml for admin services when using with ESB
     */
    public static final String CLIENT_AXIS2_XML = "repository/conf/axis2/axis2_client.xml";

    /**
     * CSG Transport name
     */
    public static final String CSG_TRANSPORT_NAME = "csg";

    /**
     * The CSG Thrift transport name
     */
    public static final String CSG_POLLING_TRANSPORT_NAME = "csgpolling";

    public static final String CSG_SERVICE_STATUS_PUBLISHED = "Published";

    public static final String CSG_SERVICE_STATUS_UNPUBLISHED = "Unpublished";

    public static final String CSG_SERVICE_STATUS_AUTO_MATIC = "AutoMatic";

    public static final String CSG_SERVICE_ACTION_PUBLISH = "publish";

    public static final String CSG_SERVICE_ACTION_UNPUBLISH = "unpublish";

    public static final String CSG_SERVICE_ACTION_AUTOMATIC = "automatic";

    public static final String CSG_SERVICE_ACTION_MANUAL = "manual";

    public static final String CSG_SERVICE_ACTION_RESTART = "restart";

    public static final String TOKEN = "token";

    /**
     * The csg server component connection read time out to csg agent when reading the private
     * service's WSDL
     */
    public static final String READTIMEOUT = "csg-connection-read-timeout";

    /**
     * Default value of {@link READTIMEOUT}
     */
    public static final int DEFAULT_READTIMEOUT = 100000;

    /**
     * The csg server component connection timeout to csg agent when reading the private
     * service's WSDL
     */
    public static final String CONNECTTIMEOUT = "csg-connection-connect-timeout";

    /**
     * Default value of {@link CONNECTTIMEOUT}
     */
    public static final int DEFAULT_CONNECTTIMEOUT = 200000;

    public static final int MAX_MESSAGE_PROCESSING_BLOCK_SIZE = 200;

    public static final String CSG_PROXY_PREFIX = "csg-proxy-prefix";

    public static final String CSG_PROXY_DELIMITER = "csg-proxy-delimiter";

    /**
     * The parameter that need to set in carbon.xml to provide the port of the thrift server
     */
    public static final String CSG_CARBON_PORT = "Ports.CSG";

    public static final String CSG_SERVER_BEAN = "CSG_SERVER_BEAN";

    /**
     * The no of worker that need to run for processing
     */
    public static final String NO_OF_DISPATCH_TASK = "csg-no-of-dispatch-worker";

    public static final String DEFAULT_CONTENT_TYPE = "text/xml";

    public enum DEPLOYMENT_TYPE {SERVICE, WEBAPP}

    public static final String CSG_USER_NAME = "csg-user-name";

    public static final String DEFAULT_CSG_USER = "csguser";

    public static final String CSG_USER_PASSWORD = "csg-user-password";

    public static final String DEFAULT_CSG_USER_PASSWORD = "csguser";

    public static final String CSG_USER_PERMISSION_LIST = "csg-user-permission-list";

    public static final String ADMIN_PERMISSION_STRING = "/permission/admin";

    public static final String MANAGE_MEDIATION_PERMISSION_STRING =
            "/permission/admin/manage/mediation";

    public static final String MANAGE_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/modify/service";

    public static final String ADMIN_LOGIN_PERMISSION_STRING =
            "/permission/admin/login";

    public static final String ADMIN_PUBLISH_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/publish";

    public static final String ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING =
            "/permission/admin/manage/un-publish";

    public static final String[] CSG_PUBLISH_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING,
                    ADMIN_PUBLISH_SERVICE_PERMISSION_STRING,
            };

    public static final String[] CSG_UNPUBLISH_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING,
                    ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING,
            };

    public static final String[] CSG_USER_DEFAULT_PERMISSION_LIST = new String[]
            {
                    ADMIN_LOGIN_PERMISSION_STRING,
                    MANAGE_MEDIATION_PERMISSION_STRING,
                    MANAGE_SERVICE_PERMISSION_STRING
            };

    public static final String CSG_ROLE_NAME = "csg-role-name";

    public static final String DEFAULT_CSG_ROLE_NAME = CSG_ROLE_NAME;

    public static final String CSG_PUBLISH_ROLE_NAME = "publish";

    public static final String CSG_UNPUBLISH_ROLE_NAME = "un-publish";

    /**
     * Prevents instantiation of this class.
     */
    private CSGConstant() {

    }

}
