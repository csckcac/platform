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
   public void usePosixParser(final String[] commandLineArguments)
   {
      final CommandLineParser cmdLinePosixParser = new PosixParser();
      final Options posixOptions = constructPosixOptions();
      CommandLine commandLine;
       CliCommandManager cliCommandManager = new CliCommandManager();
      try
      {
         commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);
         if ( commandLine.hasOption("upload") )
         {
            if(commandLine.hasOption("applications") && commandLine.hasOption("cartridge-type") ){
                if (cliCommandManager.loggingToRemoteServer (System.getenv("host"),
                                                             System.getenv("port"),
                                                             System.getenv("username"),
                                                             System.getenv("password"),
                                                             System.getenv("domain") )) {
                    System.out.println("Successfully logged in");
                    displayBlankLines(1, System.out);
                    cliCommandManager.uploadApps(commandLine.getOptionValue("applications"),
                                                 commandLine.getOptionValue("cartridge-type"));
                }
            }
         } else if(commandLine.hasOption("help")){
             printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true,
                                                     System.out);
         }
      }
      catch (ParseException parseException)  // checked exception
      {
          printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true,
                                        System.out);
         System.err.println(
              "Encountered exception while parsing using PosixParser:\n"
            + parseException.getMessage() );
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
       posixOptions.addOption("h", "help", true, "Show help.");
       posixOptions.addOption( "upload", false, "Upload the apps.");
       posixOptions.addOption("a", "applications", true, "Apps to be uploaded.");
       posixOptions.addOption("c", "cartridge-type", true, "Type of the cartridge of apps.");
       posixOptions.addOption("login", false, "login");
       posixOptions.addOption("host", true, "Server host");
       posixOptions.addOption("port", true, "Server port");
       posixOptions.addOption("username", true, "User name of tenant");
       posixOptions.addOption("password", true, "Password of tenant user");
       posixOptions.addOption("domain", true, "Tenant domain");

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
   public void printHelp( final Options options, final int printedRowWidth, final String header,
                          final String footer, final int spacesBeforeOption,
                          final int spacesBeforeOptionDescription, final boolean displayUsage,
                          final OutputStream out) {
      final String commandLineSyntax = "java -cp ApacheCommonsCLI.jar";
      final PrintWriter writer = new PrintWriter(out);
      final HelpFormatter helpFormatter = new HelpFormatter();

      helpFormatter.printHelp( writer, printedRowWidth, commandLineSyntax, header, options,
                               spacesBeforeOption, spacesBeforeOptionDescription, footer, displayUsage);
      writer.flush();
   }

   /**
    * Main executable method used to call from CLI.
    *
    * @param commandLineArguments Commmand-line arguments.
    */
    public static void main(final String[] commandLineArguments) {

        System.setProperty("javax.net.ssl.trustStore",
                           "/home/lahiru/work/phpHosting/custom/cartridge/wso2stratos-hosting-1.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
                   System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore",
                           "/home/lahiru/work/phpHosting/custom/cartridge/wso2stratos-hosting-1.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
                   System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        CliTool cliTool = new CliTool();
        final String applicationName = "CliTool";
        cliTool.displayBlankLines(1, System.out);
        if (commandLineArguments.length < 1)
        {
            System.out.println("-- HELP --");
            cliTool.printHelp(cliTool.constructPosixOptions(), 80, "HELP", "End of Help", 3, 5, true,
                              System.out);
            cliTool.displayBlankLines(2, System.out);
        }

        cliTool.usePosixParser(commandLineArguments);
        cliTool.displayBlankLines(1, System.out);



    }
}
