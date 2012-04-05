MTOM Demo for SWA & XOP
=======================

This demo illustrates the use of a SOAP message 
with an attachment and XML-binary Optimized Packaging.

Please review the README in the samples directory before
continuing.


Building and running the demo using Maven
---------------------------------------
From the base directory of this sample (i.e., where this README file is
located), the maven pom.xml file can be used to build and run the demo. 

Using either UNIX or Windows:

  * mvn clean install (builds the demo and creates a WAR file)
  * Start the server (run bin/wso2server.sh/.bat)
  * mvn -Pdeploy (deploys the generated WAR file on WSO2 AS with related logs on the console)
  * mvn -Pclient (runs the client)
    
To remove the code generated from the WSDL file and the .class
files, run mvn clean".


