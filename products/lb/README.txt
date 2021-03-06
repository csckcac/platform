================================================================================
                        WSO2 Elastic Load Balancer 2.0.0-SNAPSHOT
================================================================================

Welcome to the WSO2 Elastic Load Balancer 2.0.0-SNAPSHOT release

WSO2 ELB is a lightweight and easy-to-use Open Source Elastic Load Balancer
(ELB) available under the Apache Software License v2.0. WSO2 ELB allows
administrators to simply configure message load balancing, failover routing,
and auto scaling. The runtime has been designed to be completely
asynchronous, non-blocking and streaming based on the Apache Synapse core.

This is based on the revolutionary WSO2 Carbon [Middleware a' la carte]
framework. All the major features have been developed as pluggable Carbon
components. WSO2 Elastic Load Balancer is a compact version doing the load balancing
features of WSO2 Enterprise Service Bus.


Key Features of WSO2 ELB
========================

1. Non-blocking HTTP/S transports based on Apache HttpCore for ultrafast
   execution and support for thousands of connections at high concurreny with
   constant memory usage.
2. Load-balancing (with or without sticky sessions)/Fail-over, and clustered
   Throttling and Caching support.
3. Built in support for scheduling tasks using the Quartz scheduler.
4. Automatically scaling the system according to the load across the nodes in
   the service nodes that are in the cluster.
5. Service aware dynamic load balancing - A single load balancer
   can centrally manage the load across the nodes of different service clusters.
6. Tenant aware load balancing - Tenants can be loaded and unloaded dynamically.
   Tenants can be partitioned to different service clusters.
7. Lightweight, XML and Web services centric messaging model
8. Administrator friendly - Configurations are simple and easy.

System Requirements
==================================

1. Minimum memory - 1GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. Java SE Development Kit 1.6.21 or higher

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements

Installation & Running
==================================

1. Extract the wso2elb-2.0.0-SNAPSHOT.zip and go to the extracted directory
2. Run the wso2server.sh or wso2server.bat as appropriate
3. Configure the load balancer to manage the load across the instants to be balanced.

WSO2 ELB 2.0.0-SNAPSHOT distribution directory structure
===============================================

    CARBON_HOME
	|- bin <folder>
  |- dbscripts <folder>
	|- lib <folder>
	|- repository <folder>
  |- samples <folder>
	|- tmp <folder>
	|- LICENSE.txt <file>
	|- README.txt <file>
	|- INSTALL.txt <file>		
	|- release-notes.html <file>

    - bin
	  Contains various scripts, .sh & .bat files

    - dbscripts
    Contains all the database scripts

    - lib
	  Contains the basic set of libraries required to startup ELB
	  in standalone mode

    - repository
	  The repository where services and modules deployed in WSO2 ELB
	  are stored. In addition to this, the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators, third party libraries etc. All
	  global and LB specific configuration files, generated log files
	  and other deployed artifacts are also housed under this directory.

    - samples
	  Contains some sample services and client applications that demonstrate
	  the functionality and capabilities of WSO2 ELB

    - tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

    - LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 ELB is distributed.

    - README.txt
	  This document.

    - INSTALL.txt
    This document will contain information on installing WSO2 ELB

    - release-notes.html
	  Release information for WSO2 ELB 2.0.0-SNAPSHOT

Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 ELB, visit the WSO2 Oxygen Tank (http://wso2.org)

Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/LB

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
