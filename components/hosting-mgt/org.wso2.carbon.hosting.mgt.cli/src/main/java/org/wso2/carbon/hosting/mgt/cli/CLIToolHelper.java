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

package org.wso2.carbon.hosting.mgt.cli;



import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.PosixParser;

/**
 * Created by IntelliJ IDEA.
 * User: lahiru
 * Date: 8/21/12
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CLIToolHelper {
    private boolean isLogged = false;
    
    public CLIToolHelper(){
        init("tenantName", "tenantDomain", "password");
    }
    
//    public static void main(String[] args) {

           // create Options object
//        Options options = new Options();
//
//        // add t option
//        options.addOption("t", false, "display current time");
//
//        CommandLineParser parser = new PosixParser();
//
//        try {
//            CommandLine cmd = parser.parse( options, args);
//
//            if(cmd.hasOption("t")) {
//                System.out.println("has t");
//                // print the date and time
//            }
//            else {
//                System.out.println("No t");
//                // print the date
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//
//



//        System.out.println(args[0]);
//        CLIToolHelper cliTestHelper = new CLIToolHelper();
//        cliTestHelper.createCartridge("PHP");
//    }

//
private static Options createOptions() {
   Options options = new Options();
   options.addOption("h", "help", false, "print this message and exit");
   options.addOption("i", "input", true, "input file");
   return options;
 }

 private static void showHelp(Options options) {
   HelpFormatter h = new HelpFormatter();
   h.printHelp("help", options);
   System.exit(-1);
 }

 public static void main(String[] args) {
   Options options = createOptions();
   try {
     CommandLineParser parser = new PosixParser();
     CommandLine cmd = parser.parse(options, args);
     @SuppressWarnings("unused")
     String inputPath = cmd.getOptionValue("i");
   } catch (Exception e) {
     e.printStackTrace();
     showHelp(options);
   }
 }



    public void init(String tenantName, String tenantDomain, String password) {
        System.out.println("Initialized cartridge client");
        //TODO create session
    }

    public void createCartridge(String cartridgeName){

        System.out.println(cartridgeName);
    }

}
