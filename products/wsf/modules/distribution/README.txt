WSO2 WSF Java ${wsf.java.version}
------------------

${buildNumber}

Welcome to the WSO2 WSF/Java ${wsf.java.version} release

WSO2 Web Services Framework for Java (WSF/Java) provides the complete framework
for developing Web Services or Web Service clients. Basically this contains all
the libraries you need to write a service or client.

WSF/Java includes all WS-Security (Rampart), WS-ReliablieMessage (Sandesha) etc.
libraries and Axis2 modules (.mar archives) which you need in developing complex
services and clients.

WSF/Java Binary Distribution Directory Structure
------------------------------------------------

    WSF_JAVA_HOME
        |- lib <folder>
        |- repository <folder>
        |--- modules <folder>
        |- resources <folder>
        |- samples <folder>
        |- LICENSE.txt <file>
        |- README.txt <file>
        |- release-notes.html <file>

    - lib
      Contains all the libraries.

    - repository
      This is the Axis2 client side repository. By default it contains all Axis2 modules.

    - resources
      Contains additional resources that may be required. Ex: key stores.

    - samples
      Contains some client samples that demonstrate how to develop
      various clients with security, RM ect.

    - LICENSE.txt
      Lists all libraries used by WSF/Java with their licenses.

    - README.txt
      This document.

    - release-notes.html
      Release information for WSO2 WSF/Java ${wsf.java.version}

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 WSF/Java, visit the WSO2 Oxygen Tank (http://wso2.org)

Crypto Notice
-------------

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  Before using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:

Apacge Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

---------------------------------------------------------------------------
(c) Copyright 2010 WSO2 Inc.
