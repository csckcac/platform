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
package org.wso2.mashup.deployer;

import java.util.Properties;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 17, 2009
 * Time: 4:57:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppProperties {
   private static Properties props = new Properties();

    static{

        try {
            props.load(AppProperties.class.getClassLoader().getResourceAsStream("org/wso2/mashup/deployer/conf/app.properties"));

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    private static String DEBUG = (String) props.get("debug.on");
    public final static boolean isDebug_MODE = new Boolean(DEBUG);


    public static void main(String[] args) {
       if(isDebug_MODE)
       System.out.println(DEBUG);
    }

}
