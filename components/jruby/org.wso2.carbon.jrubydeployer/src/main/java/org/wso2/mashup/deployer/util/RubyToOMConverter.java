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
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.types.NonPositiveInteger;
import org.apache.axis2.databinding.types.NonNegativeInteger;
import org.apache.axis2.databinding.types.PositiveInteger;
import org.apache.axis2.databinding.types.NegativeInteger;
import org.jruby.RubyFloat;
import org.jruby.RubyInteger;
import org.jruby.RubyBignum;
import org.jruby.RubyBoolean;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 13, 2009
 * Time: 10:28:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class RubyToOMConverter {


    public static String convertToString(Object rubyObject) {
           return rubyObject.toString();
       }

       public static String convertToFloat(Object rubyObject) throws AxisFault {
           try {

               if(rubyObject instanceof RubyFloat){
               RubyFloat floatObject = (RubyFloat) rubyObject;
               return floatObject.toString();
               }
               return Double.toString(RubyFloat.num2dbl((IRubyObject)rubyObject)) ;
              // return rubyObject.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to float");
           }


       }




       public static String convertToInteger(Object rubyObject) throws AxisFault {
           try {
               if(rubyObject instanceof RubyInteger){
               RubyInteger intObject = (RubyInteger) rubyObject;
               return intObject.toString();
               }
               return Integer.toString(RubyInteger.num2int((IRubyObject)rubyObject)) ;
              // return rubyObject.toString();
               
           } catch (Exception e) {
               throw new AxisFault("/Unable to convert the return value to integer");
           }
       }

       public static String convertToInt(Object rubyObject) throws AxisFault {


               return convertToInteger(rubyObject);


       }

       public static String convertToNonPositiveInteger(Object rubyObject) throws AxisFault {
           try {
               NonPositiveInteger integer =
                       ConverterUtil.convertToNonPositiveInteger(rubyObject.toString());
               return integer.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to non positive integer");
           }
       }

       public static String convertToNonNegativeInteger(Object rubyObject) throws AxisFault {
           try {
               NonNegativeInteger integer =
                       ConverterUtil.convertToNonNegativeInteger(rubyObject.toString());
               return integer.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to non negative integer");
           }
       }

       public static String convertToPositiveInteger(Object rubyObject) throws AxisFault {
           try {
               PositiveInteger integer = ConverterUtil.convertToPositiveInteger(rubyObject.toString());
               return integer.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to positive integer");
           }
       }

       public static String convertToNegativeInteger(Object rubyObject) throws AxisFault {
           try {
               NegativeInteger integer = ConverterUtil.convertToNegativeInteger(rubyObject.toString());
               return integer.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to negative integer");
           }
       }

       public static String convertToLong(Object rubyObject) throws AxisFault {
           try {
                if(rubyObject instanceof RubyInteger){
               RubyBignum longObject = (RubyBignum) rubyObject;
               return longObject.toString();
               }
                return Long.toString(RubyBignum.num2long((IRubyObject)rubyObject)) ;
               //return rubyObject.toString();
           } catch (Exception e) {
               throw new AxisFault("Unable to convert the return value to long");
           }
       }

    public static String convertToBoolean(Object rubyObject) throws AxisFault {
               try {
                    if(rubyObject instanceof RubyInteger){
                   RubyBoolean boolObject = (RubyBoolean) rubyObject;
                   return boolObject.toString();
                   }
                   return rubyObject.toString();
               } catch (Exception e) {
                   throw new AxisFault("Unable to convert the return value to long");
               }
           }



}
