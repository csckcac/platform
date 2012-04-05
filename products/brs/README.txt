================================================================================
                        WSO2  Business Rules Server 1.2.0
================================================================================

Welcome to the WSO2 BRS 1.2.0 release

WSO2 BRS is a lightweight and easy-to-use Open Source  Business Rules Server
(BRS) is available under the Apache Software License v2.0. and WSO2 BRS supports
exposing business decisions as secure and reliable web services and integrating 
business decisions to the organizations’ application integration infrastructure.

This is based on the revolutionary WSO2 Carbon framework. All the major features
have been developed as pluggable Carbon components.

Key Features of WSO2 BRS
==================================
* Ability to write scripts for Drools rule engine
* Support any JSR94 based rule engine
* Wizard for service creation
* WS-Security support for external services
* WS-Security support for business rule services.
* System monitoring.
* Try-it for business rule services.
* SOAP Message Tracing.
* Web Services tooling support such as WSDL2Java, Java2WSDL and WSDL Converter.
* Customizable server - You can customize the BRS to fit into your
  exact requirements, by removing certain features or by adding new
  optional features.

System Requirements
==================================

1. Minimum memory - 1 GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. Java SE Development Kit 1.6.0_21 or higher
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
6. To build WSO2 BRS from the Source distribution, it is necessary that you have
   JDK 1.6 or higher version and Maven 2.1.0 or later

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements

Installation & Running
==================================

1. Extract the wso2brs-1.2.0.zip and go to the extracted directory
2. Run the wso2server.sh or wso2server.bat as appropriate
3. Point your favourite browser to

    https://localhost:9443/carbon

4. Use the following username and password to login

    username : admin
    password : admin


WSO2 BRS 1.2.0 distribution directory structure
=============================================

	CARBON_HOME
		|-- bin <folder>
		|-- dbscripts <folder>
		|-- lib <folder>
		|-- repository <folder>
		|   |-- components <folder>
		|   |-- conf <folder>
		|   |-- database <folder>
		|   |-- deployment <folder>
		|   |-- logs <folder>
		|   |-- tenants <folder>
		|   |-- resources <folder>
		|       |-- security <folder>
		|-- tmp <folder>
		|-- LICENSE.txt <file>
		|-- INSTALL.txt <file>
		|-- README.txt <file>
		`-- release-notes.html <file>

    - bin
	  Contains various scripts .sh & .bat scripts

    - dbscripts
      Contains the SQL scripts for setting up the database on a variety of
      Database Management Systems, including H2, Derby, MSSQL, MySQL abd
      Oracle.

    - lib
      Contains the basic set of libraries required to start-up  WSO2 BRS
      in standalone mode

    - repository
      The repository where services and modules deployed in WSO2 BRS
      are stored.

        - components
          Contains OSGi bundles and configurations
      
        - conf
          Contains configuration files
         
        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details
          
        - logs
          Contains all log files created during execution

        - tenants
          Contains tenant details

        - resources
          Contains additional resources that may be required

        - security
          Contains security resources

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - LICENSE.txt
      Apache License 2.0 under which WSO2 BRS is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 BRS

    - release-notes.html
      Release information for WSO2 BRS 1.2.0


Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 BRS, visit the WSO2 Oxygen Tank (http://wso2.org)

Known issues of WSO2 BRS 1.2.0-SNAPSHOT
==================================

 * Dependency management within the configuration is not handled properly

Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/BRSJAVA

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

