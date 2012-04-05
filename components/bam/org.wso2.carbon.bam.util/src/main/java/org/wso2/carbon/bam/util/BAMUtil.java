package org.wso2.carbon.bam.util;

import java.io.IOException;
import java.net.Socket;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class BAMUtil {

    public static boolean isServerUpAndRunning(String serverUrlWithPort) {

        boolean isServerUp;
        int firstElement = 0;
        int secondElement = 1;

        String[] urlAndPort = serverUrlWithPort.split("://")[secondElement].split(":");
        String url = urlAndPort[firstElement];
        String port;
        if (urlAndPort[secondElement].contains("/")) {
            port = urlAndPort[secondElement].split("/")[firstElement];
        } else {
            port = urlAndPort[secondElement];
        }
        try {
            Socket socket = new Socket(url, Integer.parseInt(port));
            isServerUp = true;
        } catch (IOException e) {
            isServerUp = false;
        }
        return isServerUp;
    }
}
