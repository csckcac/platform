================================================================================
                        WSO2 Enterprise Service Bus 4.0.0-SNAPSHOT
================================================================================

Welcome to the WSO2 ESB 4.0.0 release

WSO2 ESB is a lightweight and easy-to-use Open Source Enterprise Service Bus
(ESB) available under the Apache Software License v2.0. WSO2 ESB allows
administrators to simply and easily configure message routing, intermediation,
transformation, logging, task scheduling, load balancing, failover routing,
event brokering, etc.. The runtime has been designed to be completely
asynchronous, non-blocking and streaming based on the Apache Synapse core.

This is based on the revolutionary WSO2 Carbon [Middleware a' la carte]
framework. All the major features have been developed as pluggable Carbon
components.

Key Features of WSO2 ESB
==================================

1. Proxy services - facilitating synchronous/asynchronous transport, interface
   (WSDL/Schema/Policy), message format (SOAP 1.1/1.2, POX/REST, Text, Binary),
   QoS (WS-Addressing/WS-Security/WS-RM) and optimization switching (MTOM/SwA).
2. Non-blocking HTTP/S transports based on Apache HttpCore for ultrafast
   execution and support for thousands of connections at high concurreny with
   constant memory usage.
3. Built in Registry/Repository, facilitating dynamic updating and reloading
   of the configuration and associated resources (e.g. XSLTs, XSD, WSDL,
   Policies, JS, Configurations ..)
4. Easily extendable via custom Java class (mediator and command)/Spring
   mediators, or BSF Scripting languages (Javascript, Ruby, Groovy, etc.)
5. Built in support for scheduling tasks using the Quartz scheduler.
6. Load-balancing (with or without sticky sessions)/Fail-over, and clustered
   Throttling and Caching support
7. WS-Security, WS-Reliable Messaging, Caching & Throttling configurable via
   (message/operation/service level) WS-Policies
8. Lightweight, XML and Web services centric messaging model
9. Support for industrial standards (Hessian binary web service protocol/
   Financial Information eXchange protocol and optional Helth Level-7 protocol)
10. Enhanced support for the VFS(File/FTP/SFTP)/JMS/Mail transports with
    optional TCP/UDP transports and transport switching for any of the above
    transports
11. Support for message splitting & aggregation using the EIP and service
    callouts
12. Database lookup & store support with DBMediators with reusable database
    connection pools
13. WS-Eventing support with event sources and event brokering
14. Rule based mediation of the messages using the Drools rule engine
15. Transactions support via the JMS transport and Transaction mediator for
    database mediators
16. Internationalized GUI management console with user/permission management for
    configuration development and monitoring support with statistics,
    configurable logging and tracing
17. JMX monitoring support and JMX management capabilities like,
    Gracefull/Forcefull shutdown/restart

New Features of the WSO2 ESB 4.0.0-SNAPSHOT
==================================

1. This ESB release is based on Carbon "Middleware a' la carte" which is an OSGi
   based SOA platform version 2.0 by WSO2 Inc.
2. Rule based mediation via Drools
3. Fine grained autherization for services via the Entitlement mediator
4. Reliable-Messaging specification 1.1 support
5. Enhanced WS-Eventing support and Event Sources making it an even broker
6. Enhanced AJAX based sequence, endpoint and proxy service editors
7. Enhanced transport configuration management through the graphical console
8. Enhanced integrated registry and search functionalities with versionning,
   notifications, rating of resources, and commenting
9. Enhanced remote registry support
10. Default persistence to the registry for the configuration elements
11. Enhanced permission model with the user management
12. Enhanced REST/GET and other HTTP method support
13. P2 based OSGi feature support, for optional features like service
    management, runtime governance and so on..

System Requirements
==================================

1. Minimum memory - 256MB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. Java SE Development Kit 1.5.13 or higher
4. The Management Console requires you to enable Javascript of the Web browser,
   with MS IE 6 and 7. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to
   medium or lower.
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x and the default medium security setting with
     IE does not allow sufficient level of JS or ActiveX enablement for the
     management console to run.
5. To compile and run the sample clients, an Ant version is required. Ant 1.7.0
   version is recommended
6. To build WSO2 ESB from the Source distribution, it is necessary that you have
   JDK 1.5.x version and Maven 2.1.0 or later

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements

Installation & Running
==================================

1. Extract the wso2esb-4.0.0-SNAPSHOT.zip and go to the extracted directory
2. Run the wso2server.sh or wso2server.bat as appropriate
3. Point your favourite browser to

    https://localhost:9443/carbon

4. Use the following username and password to login

    username : admin
    password : admin

5. Smaple configurations can be started with wso2esb-samples.sh or
   wso2esb-samples.bat as appropriate specifying the sample number with the -sn
   option, for example to run sample 0 the command is

    ./wso2esb-samples.sh -sn 0
    ./wso2esb-samples.bat -sn 0

WSO2 ESB 4.0.0-SNAPSHOT distribution directory structure
=============================================

	CARBON_HOME
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- logs <folder>
		|- repository <folder>
		|--- conf <folder>
		|--- database <folder>
		|- resources <folder>
		|- samples <folder>
		|- tmp <folder>
		|- webapps <folder>
		|- LICENSE.txt <file>
		|- README.txt <file>
		|- INSTALL.txt <file>		
		|- release-notes.html <file>

    - bin
	  Contains various scripts .sh & .bat scripts

	- conf
	  Contains configuration files

	- database
      Contains the database

    - dbscripts
      Contains all the database scripts

    - lib
	  Contains the basic set of libraries required to startup ESB
	  in standalone mode

	- logs
	  Contains all log files created during execution

	- repository
	  The repository where services and modules deployed in WSO2 ESB
	  are stored. In addition to this the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators third party libraries and so on..

	- resources
	  Contains additional resources that may be required, including sample
	  configuration and sample resources

	- samples
	  Contains some sample services and client applications that demonstrate
	  the functionality and capabilities of WSO2 ESB

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- webapps
	  Contains the WSO2 ESB webapp. Any other webapp also can be deployed
	  in this directory

	- LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 ESB is distributed.

	- README.txt
	  This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 ESB

	- release-notes.html
	  Release information for WSO2 ESB 4.0.0-SNAPSHOT

Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 ESB, visit the WSO2 Oxygen Tank (http://wso2.org)

Known issues of WSO2 ESB 4.0.0
==================================

 * Dependency management within the configuration is not handled properly

Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/ESBJAVA

Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
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

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

--------------------------------------------------------------------------------
(c) Copyright 2011 WSO2 Inc.

