   EntitlementFilter Sample
##########################################

Pre Requests
---------------------------------------
  * Start WSO2 IS
  * Import the sample XACML policy to the IS, which is given in src/main/Resources
  * Please see the web.xml file given in the src/main/webapp/WEB-INF/web.xml
  * If you are running WSO2 IS in a  configuration other than default please edit the web.xml as needed.

Building and running the demo using Maven
---------------------------------------

From the base directory of this sample (i.e., where this README file is
located), the pom.xml file is used to build and run the demo.

Using either UNIX or Windows:

  * mvn clean install you can build the EntitlementFilter Web App and the Client
  * Start the server (run bin/wso2server.sh/.bat)
  * mvn -Pdeploy (deploys the generated WAR file on WSO2 AS with related logs on the console)
  * mvn -Pclient (runs the client)
  * See both consoles of your and WSO2 AS, It will show you how the EntitlementFilter is Working

To remove the code generated from the WSDL file and the .class
files, run "mvn clean".
