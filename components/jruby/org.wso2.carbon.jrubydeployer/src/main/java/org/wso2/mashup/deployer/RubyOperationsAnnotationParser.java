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

import org.jruby.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.wso2.mashup.deployer.util.ScriptConfigurator;
import org.apache.axis2.AxisFault;

import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: buddhika
 * Date: Nov 19, 2007
 * Time: 9:41:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class RubyOperationsAnnotationParser {








     public static Map parseRubyOperationsAnnotation(String script) throws AxisFault {

        Map annotations = new HashMap();

        Ruby runtime = Ruby.getDefaultInstance();

        String script_mod = ScriptConfigurator.appendAnnotationHeaders(script);

         if(AppProperties.isDebug_MODE)
             System.out.println("[---Generated Script ---] : \n"+script_mod);

        IRubyObject output = runtime.evalScriptlet(script_mod);

        RubyHash temp = output.convertToHash();

       // annotations = convert(temp);
        annotations = temp;


         return annotations;
    }


    private static HashMap convert(RubyHash temp){

        int size = temp.keys().getLength();
        HashMap<String,Object> map =new HashMap();
        RubyArray keySet = temp.keys();
        Object key,val;

        for(int i=0; i<(size);i++){
             key =  keySet.get(i);
             val =  temp.get(key);

             if(val instanceof RubyHash){
                 String modKey = convertToString(key);
                 map.put(modKey,convert((RubyHash)val));

             }
            else{
                 String modKey = convertToString(key);
                 String modval    = convertToString(val);
                 map.put(modKey,modval);
             }


          //   map.put(key,val);
        }

        return map;
    }


    public static String  convertToString(Object key){
        if(key instanceof RubySymbol){
            return ((RubySymbol) key).asString().toString();
        }
        else if(key instanceof RubyString){
            return ((RubyString) key).toString(); 
        }
        else{
            return key.toString();
        }


    }

}
