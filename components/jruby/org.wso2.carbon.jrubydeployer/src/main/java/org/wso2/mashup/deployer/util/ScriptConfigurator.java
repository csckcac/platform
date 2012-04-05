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
import org.wso2.mashup.deployer.AppProperties;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 4, 2009
 * Time: 3:15:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptConfigurator {



    public static String appendMetaDataHeaders(String script){
      String header =    "class Class\n" +
                   "Seen_classes = []\n" +
                   "\n" +
                   "  def inherited(class_obj)\n" +
                   "    Seen_classes << class_obj.name\n" +
                   "  end\n" +
                   "\n" +
                   "  def seen_classes\n" +
                   "    Seen_classes.each do |class_name|\n" +
                   "\tputs class_name\n" +
                   "    end\n" +
                   "  end\n" +
                   "end\n";


       return header+script;

    }


     public static String appendAnnotationHeaders(String script) throws AxisFault {
        String headersPlusScript = appendRequiredHeaders(script);

        String scr = "_anno_out.getMap \n"    ;

        return headersPlusScript+scr;
    }

    public static String appendRequiredHeaders(String script) throws AxisFault {
      /*   String headers = "require '/home/usw/my/carbon/axis2-1.4.1-wso2/repository/my/descript'\n"+
                                        "include Annotation \n";
        */
        String headers = null;
        try {
            headers = readHeaders();
        } catch (IOException e) {
            throw new AxisFault("required headers missing inorder to read the ruby script Annotations..");
        }

        return headers+script;

    }

    public static String readHeaders() throws IOException {

       // InputStream in = Thread.currentThread().getContextClassLoader().getSystemResourceAsStream("org/wso2/mashup/deployer/conf/descript.rb")  ;
        InputStream in = ScriptConfigurator.class.getClassLoader().getResourceAsStream("org/wso2/mashup/deployer/conf/descript.rb")  ;

        InputStreamReader inr = new InputStreamReader(in);

        BufferedReader br;
        br = new BufferedReader(inr);
        String descript="";
        String temp;
        do{
              temp = br.readLine();
                if(temp!=null)
                descript = descript + temp + "\n";
        }
        while(temp !=null) ;

        if(AppProperties.isDebug_MODE)
            System.out.println("[---Header---] : \n" + descript);
        return descript;
        
    }
    

}
