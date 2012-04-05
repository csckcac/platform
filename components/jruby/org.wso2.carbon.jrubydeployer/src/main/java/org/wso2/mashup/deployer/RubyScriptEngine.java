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

import org.wso2.mashup.deployer.util.RubyScriptReader;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.apache.axis2.AxisFault;

import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 12, 2009
 * Time: 12:41:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class RubyScriptEngine {

     public static void main(String[] args) throws AxisFault {
               RubyScriptReader reader = new RubyScriptReader();
               String s = reader.readScript(new File("/home/usw/jRuby/testRuby/test.rb"));
                System.out.println(s);
                Ruby runtime = Ruby.getDefaultInstance();
                System.out.println(s +"func2 10,15 ");

                List params = new ArrayList();
                List a =new ArrayList();
                a.add(10);
                a.add(15);



                int b = 1200;

                params.add(a);
                params.add(b);

                //IRubyObject ob =runtime.evalScript(s +getMethodHeader("My.func")+getParamHeader(params.toArray()));
         IRubyObject ob  = null;
         try {
             ob = invokeMethod(s ,"func2",params.toArray());
         } catch (AxisFault axisFault) {
             axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         System.out.println(ob.toString());

          }
    

    public static IRubyObject invokeMethod(String script,String methodName,Object args)throws AxisFault {
       try{
           boolean isArray=false,hasArgs=false;
           int argLen = 0;
        if (args instanceof Object[]){
              isArray =true;
              argLen = ((Object[])args).length;
        }
        if(args!=null && argLen!=0 ){
              hasArgs = true;
        }

        if(isArray && hasArgs){
                 Ruby runtime = Ruby.getDefaultInstance();
                 IRubyObject ob =runtime.evalScriptlet(script +getMethodHeader(methodName)+getParamHeader((Object[])args));
                 return ob;
        }
        else if(!hasArgs){

                 Ruby runtime = Ruby.getDefaultInstance();
                 IRubyObject ob =runtime.evalScriptlet(script +getMethodHeader(methodName));
                 return ob;
        }
        else if(argLen<=0){

                 Ruby runtime = Ruby.getDefaultInstance();
                 IRubyObject ob =runtime.evalScriptlet(script +getMethodHeader(methodName));
                 return ob;
        }
       }
       catch(Exception e){
              throw new AxisFault("Could not invoke the method requested.",e);
        }

       return null;
    }

    private static String getParamHeader(Object[] list){
        int length = list.length;
        String str ="";
        for(int i=0;i<length;i++){
             str += construct(list[i])+",";

        }
        return str.substring(0,str.length()-1);


    }

    private static String getMethodHeader(String methodName){
          int dIndex = methodName.indexOf('.');
        if(dIndex > -1) {
                 String className = methodName.substring(0,dIndex);
                 String subMethodName = methodName.substring(dIndex+1,methodName.length());
                 String constructor = className+".new."+subMethodName+" ";


            return constructor;
        }

        return methodName+" ";
    }

    /*this will construct each parameter individually
      booleans,int primitives do not need modification
      but arrays and lists ,etc should be converted to ruby types


    */
    public static Object construct(Object arg){
        if(arg instanceof int[]){
            int[] a = (int[]) arg;
            String str = "[" ;
            for(int i=0;i<a.length;i++){

                  str += a[i] +",";
            }
            str += "]";
            return str;
        }
        else if(arg instanceof List){
              List temp = (List) arg;
              Iterator it = temp.iterator();
              String str = "[" ;
              while(it.hasNext()){
                  Object ob = it.next();
                  str += ob.toString() +",";

              }
               str += "]";

        }

        return arg;
    }

}
