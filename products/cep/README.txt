================================================================================
                        WSO2 Complex Event Processing Server 1.0.0-SNAPSHOT
================================================================================

Welcome to the WSO2 CEP 1.0.0-SNAPSHOT release

WSO2 CEP is a lightweight and easy-to-use Open Source Complex Event Processing
Server (CEP) is available under the Apache Software License v2.0. WSO2 CEP supports
analyzing complex events triggered by various event sources and triggering 
new events according to the conditions specified in CEP queries.


This is based on the revolutionary WSO2 Carbon framework. All the major features
have been developed as pluggable Carbon components.

Key Features of WSO2 CEP
==================================
*Plugable back end rutime support - WSO2 CEP supports following back end run time engines.
 				
		1. Drools Fusion - This back end rintime engine is distributed with the CEP pack
		2. Esper	 - This back end runtime is availble at WSO2 GPL P2 repository : http://dist.wso2.org/wso2-gpl-p2/carbon/releases/4.0.0-SNAPSHOT/ 
 				    and can be added as a feature with WSO2 Carbon Feature Management. 

* Support Multiple Broker Types - WSO2 CEP supports WS-Event and JMS-Qpic broker types
* GUI Support -  WSO2 CEP supports create,edit,delete operations on Buckets,Inputs and Queries.
* Use Registry resources - WSO2 CEP supports using resources stored in registry (Queries) to create buckets
* Persistance   - WSO2 CEP supports persisting created buckets in the registry.
* System monitoring.
* SOAP Message Tracing.
* Web Services tooling support such as WSDL2Java, Java2WSDL and WSDL Converter.
* Customizable server - You can customize the CEP to fit into your
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
5. To build WSO2 CEP from the Source distribution, it is necessary that you have
   JDK 1.6 or higher version and Maven 2.1.0 or later

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements

Installation & Running
==================================

1. Extract the wso2cep-1.0.0-SNAPSHOT.zip and go to the extracted directory
2. Run the wso2server.sh or wso2server.bat as appropriate
3. Point your favourite browser to

    https://localhost:9443/carbon

4. Use the following username and password to login

    username : admin
    password : admin

5. Installing Esper Back End Runtime

   You can find the Esper Back End Runtime GPL repository in :
"http://dist.wso2.org/wso2-gpl-p2/carbon/releases/4.0.0-SNAPSHOT/"

You can use the carbon Feature management Console and add Esper back end runtime
 feature to CEP.  
   

WSO2 CEP 1.0.0-SNAPSHOT distribution directory structure
=============================================

	CARBON_HOME
		|-- bin <folder>
		|-- dbscripts <folder>
		|-- lib <folder>
		|-- repository <folder>
		|   |-- components <folder>
		|   |-- conf <folder>
                |   |-- data <folder>
		|   |-- database <folder>
		|   |-- deployment <folder>
		|   |-- logs <folder>
		|   |-- tenants <folder>
		|   |-- resources <folder>
		|       |-- security <folder>
		|-- tmp <folder>
                |-- samples <folder>
		|-- LICENSE.txt <file>
		|-- INSTALL.txt <file>
		|-- README.txt <file>
		`-- release-notes.html <file>

    - bin
	  Contains various scripts .sh & .bat scripts

    - dbscripts
      Contains the SQL scripts for setting up the database on a variety of
      Database Management Systems, including H2, Derby, MSSQL, MySQL and
      Oracle.

    - lib
      Contains the basic set of libraries required to start-up  WSO2 CEP
      in standalone mode

    - repository
      The repository where services and modules deployed in WSO2 CEP
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
	    
     - samples
      Contains additional resources that are required to test samples

        - lib
          Contains libraries required for samples testing
        - services
          Contains axis2services required for samples testing

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - LICENSE.txt
      Apache License 2.0 under which WSO2 CEP is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 CEP

    - release-notes.html
      Release information for WSO2 CEP 1.0.0-SNAPSHOT


Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 CEP, visit the WSO2 Oxygen Tank (http://wso2.org)


Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/CEP

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
(c) Copyright 2010 WSO2 Inc.

