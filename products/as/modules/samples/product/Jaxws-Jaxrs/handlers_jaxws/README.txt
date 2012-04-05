JAX-WS Handler Demo
===================

This demo shows how JAX-WS handlers are used.  The server uses a
SOAP protocol handler which logs incoming and outgoing messages
to the console.  

The server code registers a handler using the @HandlerChain annotation
within the service implementation class. For this demo, LoggingHandler
is SOAPHandler that logs the entire SOAP message content to stdout.

The client includes a logical handler that checks the parameters on
outbound requests and short-circuits the invocation in certain
circumstances. This handler is specified programatically.

Building and running the demo using Maven
---------------------------------------

From the base directory of this sample (i.e., where this README file is
located), the pom.xml file is used to build and run the demo. 

Using either UNIX or Windows:

  * mvn clean install (builds the demo and creates a WAR file)
  * Start the server (run bin/wso2server.sh/.bat)
  * mvn -Pdeploy (deploys the generated WAR file on WSO2 AS with related logs on the console)
  * mvn -Pclient (runs the client)

To remove the code generated from the WSDL file and the .class
files, run "mvn clean".

IMPORTANT
=========

When specifying the handler file path in the @HandlerChain annotation, complete path
from the root should be given as follows.

@HandlerChain(file = "/demo/handlers/common/demo_handlers.xml")

Relative paths (Ex : "../common/demo_handlers.xml") won't work in WSO2 Carbon. You have
to give the full package hierarchy.

