WSO2 AppServer ${appserver.version}
---------------

${buildNumber}

Welcome to the WSO2 AppServer ${appserver.version} release

WSO2 AppServer is an Enterprise ready application server based on the award-winning WSO2 Carbon framework. Being the
successor of the WSO2 Web Services Application Server(WSAS), WSO2 Application Server(AS) now supports
web application deployment and management in addition to its award winning web services management capabilities.
Coupled with WSO2 Carbon Authentication/Authorization feature, now users can manage their applications that ranges
from web services, business processes to web applications in a unified manner within the AppServer management console
itself. WSO2 AppServer uses Apache Tomcat, the widely used servlet container as its underlying web application contaner.

AppServer provides a comprehensive Web services server platform, using Axis2 as its Web services framework
and provide many value additions on top of Axis2. It can expose services using both SOAP and REST models and supports
a comprehensive set of WS-* specifications such as WS-Security, WS-Trust, WS-SecureConversation, WS-Reliable Messaging,
WS-Addressing, WS-Policy, WS-SecurityPolicy, etc.

New Features In This Release
----------------------------

1. Inbuilt Data Services support
2. Server Roles Management feature
3. Improved JAX-WS Support
4. Improved SOAP Tracer
5. Improvements in Service listing page
    -- Two lists for Services and Service Groups
    -- Security Indicators within service list
6. Improved Samples and documentation
7. Improved Class loading for Web apps and Web Services
8. Embedded Tomcat 7.0.14 support
9. Servlet API 3.0 support

Key Features
------------
* Web Application deployment and management within the App Server
* AppServer tooling - AppServer related artifacts can be easily generated using WSO2 Carbon Studio
* Clustering support for High Availability & High Scalability
* Full support for WS-Security, WS-Trust, WS-Policy and WS-Secure Conversation
* JAX-WS support - Deploy any JAX-WS annotated service and engage WS-* protocols through the management console.
* JMX & Web interface based monitoring and management
* WS-* & REST support
* GUI, command line & IDE based tools for Web service development
* Equinox P2 based provisioning support
* WSDL2Java/Java2WSDL/WSDL 1.1 & Try it(invoke any remote Web service)

Issues Fixed in This Release
----------------------------

* AppServer related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=${fixed.isssues.filter.id}

Installation & Running
----------------------
1. extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

For more details, see the Installation Guide

System Requirements
-------------------

1. Minimum memory - 1 GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

For more details see the Installation guide or,
http://wso2.org/wiki/display/carbon/System+Requirements

Known Issues in This Release
----------------------------

* AppServer related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=${known.isssues.filter.id}

Including External Dependencies
--------------------------------
For a complete guide on adding external dependencies to WSO2 AppServer & other carbon related products refer to the article:
http://wso2.org/library/knowledgebase/add-external-jar-libraries-wso2-carbon-based-products

AppServer Binary Distribution Directory Structure
--------------------------------------------

    CARBON_HOME
        |-- bin <folder>
        |-- dbscripts <folder>
        |-- lib <folder>
        |-- repository <folder>
        |   |-- components <folder>
        |   |-- conf <folder>
        |   |-- resources <folder>
        |       `-- security <folder>
        |   |-- database <folder>
        |   |-- deployment <folder>
        |   `-- logs <folder>
        |-- samples <folder>
        |-- tmp <folder>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        `-- release-notes.html <file>


    - bin
      Contains various scripts .sh & .bat scripts

    - conf
      Contains configuration files

    - database
      Contains the WSO2 Registry & User Manager database

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases

    - lib
      Contains the basic set of libraries required to startup AppServer
      in standalone mode

    - logs
      Contains all log files created during execution

    - resources
      Contains additional resources that may be required

    - samples
      Contains some sample applications that demonstrate the functionality
      and capabilities of WSO2 AppServer

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - LICENSE.txt
      Apache License 2.0 under which WSO2 AppServer is distributed.

    - README.txt
      This document.

    - INSTALL.txt
          This document will contain information on installing WSO2 AppServer

    - release-notes.html
      Release information for WSO2 AppServer 4.5.0-SNAPSHOT


Support
-------

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 AppServer, visit the WSO2 Oxygen Tank (http://wso2.org)

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


For further details, see the WSO2 Carbon documentation at
http://wso2.com/products/carbon/

---------------------------------------------------------------------------
(c) Copyright 2010 WSO2 Inc.
