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
package org.wso2.mashup.deployer.util;

import org.apache.axis2.AxisFault;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: buddhika
 * Date: Nov 16, 2007
 * Time: 1:28:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class RubyScriptReader {
    private String metaString="",targetString="";

    

    public String getScript(){

        return targetString;
    }

   
    public String readScript(File file) throws AxisFault {

        
        String script="";


        try{
        FileInputStream fileInputStream;
        fileInputStream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        script=br.readLine();
        targetString="";

         while(script!=null){
            targetString += script + "\n";
            script=br.readLine();
         }

        }

        catch(IOException e){
              throw new AxisFault("Can't deploy Service..Service File missing...");
        }

        return targetString;
    }

}
