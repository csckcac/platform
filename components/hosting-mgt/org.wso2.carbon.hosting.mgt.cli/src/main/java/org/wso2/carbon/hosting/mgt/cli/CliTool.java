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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 *
 */

public class CliTool
{
   private static Options options = new Options();

   /**
    * Apply Apache Commons CLI PosixParser to command-line arguments.
    *
    * @param commandLineArguments Command-line arguments to be processed with
    *    Posix-style parser.
    */
    public void usePosixParser(final String[] commandLineArguments) {
        final CommandLineParser cmdLinePosixParser = new PosixParser();
        final Options posixOptions = constructPosixOptions();
        CommandLine commandLine;
        CliCommandManager cliCommandManager = new CliCommandManager();
        try
        {
            if (cliCommandManager.loggingToRemoteServer (System.getenv("STRATOS_ADS_HOST"), System.getenv("STRATOS_ADS_PORT"),
                                                       System.getenv("STRATOS_TENANT_USERNAME"), System.getenv("STRATOS_TENANT_PASSWORD"),
                                                       System.getenv("STRATOS_TENANT_DOMAIN") )) {
                System.out.println("Successfully logged in");

                displayBlankLines(1, System.out);
                commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);

                //handle application upload when cartridge is already created
                if ( commandLine.hasOption("upload") )
                {
                    if(commandLine.hasOption("applications") && commandLine.hasOption("cartridge") ){
                        cliCommandManager.uploadApps(commandLine.getOptionValue("applications"),
                                                     commandLine.getOptionValue("cartridge"));
                    } else{
                        System.out.println("Not enough arguments !");
                    }
                }

                //handle application listing (after cartridge is already created)
                else if ( commandLine.hasOption("list_apps") )
                {
                    if(commandLine.hasOption("cartridge") ){
                        cliCommandManager.listApps(commandLine.getOptionValue("cartridge"));
                    } else{
                        System.out.println("Not enough arguments !");
                    }
                }

                //handle application deleting (after cartridge is already created)
                else if ( commandLine.hasOption("remove_apps") )
                {
                    if(commandLine.hasOption("applications") && commandLine.hasOption("cartridge") ){
                        cliCommandManager.deleteApps(commandLine.getOptionValue("applications"),
                                                     commandLine.getOptionValue("cartridge"));
                    } else if (commandLine.hasOption("cartridge")){
                        cliCommandManager.deleteAllApps( commandLine.getOptionValue("cartridge"));
                    } else{
                        System.out.println("Not enough arguments !");
                    }
                }

                //Print available cartridge type that can be used to create a cartridge
                else if(commandLine.hasOption("list_types")){
                    cliCommandManager.getCartridges();
                }

                //create cartridge with instances, applications (optional) will be uploaded to ADS
                else if(commandLine.hasOption("create")){
                    //Check for mandatory option create

                    String  min = null, max = null, attachVolume = null;
                    //to get optional values

                    if(commandLine.hasOption("min")){  //assign min
                        min = commandLine.getOptionValue("min");
                    }
                    if(commandLine.hasOption("max")){  //assign max
                        max = commandLine.getOptionValue("max");
                    }
                    if(commandLine.hasOption("volume")){
                        attachVolume = commandLine.getOptionValue("volume");
                    }
                    cliCommandManager.register(commandLine.getOptionValue("create"),
                                               min,
                                               max,
                                               System.getenv("STRATOS_TENANT_SVN_PASSWORD"),
                                               attachVolume);

                    if(commandLine.hasOption("applications") ){
                        cliCommandManager.uploadApps(commandLine.getOptionValue("applications"),
                                                     commandLine.getOptionValue("create"));
                    }
                } else if(commandLine.hasOption("help")){//if asking for help : display possible options

//                    printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true,
//                              System.out);
                }
            }
        }
        catch (ParseException parseException)  // checked exception
        {
//          printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true,
//                                        System.out);
         System.err.println(
                 "Encountered exception while parsing using PosixParser:\n"
                 + parseException.getMessage());
        }
    }


   /**
    * Construct and provide Options.
    *
    * @return Options expected from command-line.
    */
   public Options constructPosixOptions()
   {
      final Options posixOptions = new Options();
       //print help
       posixOptions.addOption("h", "help", true, "Show help.");

       //List cartridge types
       posixOptions.addOption("l", "list_types", false, "List the cartridge types");

       //crete cartridge (first instance startup), optionally upload apps
       posixOptions.addOption("create", true, "Register the cartridge");
       posixOptions.addOption("n", "min", true, "Minimum number of instances of the cartridge");
       posixOptions.addOption("x", "max", true, "Maximum number of instances of the cartridge");
       posixOptions.addOption("v", "volume", true, "Whether to add persistent volume to the instances " +
                                                 "created. Default is false");

       //only upload apps
       posixOptions.addOption( "upload", false, "Upload the apps.");
       posixOptions.addOption("a", "applications", true, "Apps to be uploaded.");
       posixOptions.addOption("c", "cartridge", true, "Cartridge of apps which are uploading.");

       //List the apps
       posixOptions.addOption( "list_apps", false, "List the apps.");
       //User must provide cartridge option to be listed

       //Remove the apps
       posixOptions.addOption( "remove_apps", false, "Remove the apps.");
       //User must provide cartridge option to be listed

      return posixOptions;
   }

   /**
    * Display command-line arguments without processing them in any further way.
    *
    * @param commandLineArguments Command-line arguments to be displayed.
    */
   public void displayProvidedCommandLineArguments( final String[] commandLineArguments,
                                                           final OutputStream out) {
      final StringBuffer buffer = new StringBuffer();
      for ( final String argument : commandLineArguments )
      {
         buffer.append(argument).append(" ");
      }
      try
      {
         out.write((buffer.toString() + "\n").getBytes());
      }
      catch (IOException ioEx)
      {
         System.err.println(
            "WARNING: Exception encountered trying to write to OutputStream:\n"
            + ioEx.getMessage() );
         System.out.println(buffer.toString());
      }
   }

    /**
     * Write the provided number of blank lines to the provided OutputStream.
     *
     * @param numberBlankLines Number of blank lines to write.
     * @param out OutputStream to which to write the blank lines.
     */
    public void displayBlankLines( final int numberBlankLines, final OutputStream out) {
       try {
          for (int i=0; i<numberBlankLines; ++i) {
             out.write("\n".getBytes());
          }
       }
       catch (IOException ioEx) {
          for (int i=0; i<numberBlankLines; ++i) {
             System.out.println();
          }
       }
    }


   /**
    * Print usage information to provided OutputStream.
    *
    * @param applicationName Name of application to list in usage.
    * @param options Command-line options to be part of usage.
    * @param out OutputStream to which to write the usage information.
    */
   public static void printUsage( final String applicationName, final Options options,
                                  final OutputStream out) {
      final PrintWriter writer = new PrintWriter(out);
      final HelpFormatter usageFormatter = new HelpFormatter();
      usageFormatter.printUsage(writer, 80, applicationName, options);
      writer.flush();
   }

   /**
    * Write "help" to the provided OutputStream.
    */
//   public void printHelp( final Options options, final int printedRowWidth, final String header,
//                          final String footer, final int spacesBeforeOption,
//                          final int spacesBeforeOptionDescription, final boolean displayUsage,
//                          final OutputStream out) {
//      final String commandLineSyntax = "java -cp ApacheCommonsCLI.jar";
//      final PrintWriter writer = new PrintWriter(out);
//      final HelpFormatter helpFormatter = new HelpFormatter();
//
//      helpFormatter.printHelp( writer, printedRowWidth, commandLineSyntax, header, options,
//                               spacesBeforeOption, spacesBeforeOptionDescription, footer, displayUsage);
//      writer.flush();
//   }

   /**
    * Main executable method used to call from CLI.
    *
    * @param commandLineArguments Commmand-line arguments.
    */
    public static void main(final String[] commandLineArguments) throws CliToolException {

        System.setProperty("javax.net.ssl.trustStore",
                           "/home/lahiru/work/phpHosting/custom/cartridge/wso2stratos-hosting-1.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
                   System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore",
                           "/home/lahiru/work/phpHosting/custom/cartridge/wso2stratos-hosting-1.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
                   System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        CliTool cliTool = new CliTool();
        final String applicationName = "CliTool";
        cliTool.displayBlankLines(1, System.out);
//        if (commandLineArguments.length < 1)
//        {
//            System.out.println("-- HELP --");
//            cliTool.printHelp(cliTool.constructPosixOptions(), 80, "HELP", "End of Help", 3, 5, true,
//                              System.out);
//            cliTool.displayBlankLines(2, System.out);
//        }

        cliTool.usePosixParser(commandLineArguments);
        cliTool.displayBlankLines(1, System.out);



    }
}
