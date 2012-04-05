================================================================================
                        WSO2 Cloud Services Gateway 1.0.0-alpha
================================================================================

Welcome to the WSO2 CSG 1.0.0 release

WSO2 Cloud Services Gateway(or CSG for short) is a framework that can be used to
expose a private service that is behind a firewall to the outside world in a
secure manner. The framework consists of CSG server and the CSG Agent component.
CSG Agent component should be installed in a service hosting product( such as
WSO2 Application Server).

This is based on the revolutionary WSO2 Carbon [Middleware a' la carte]
framework. All the major features have been developed as pluggable Carbon
components.

Key Features of WSO2 CSG
========================

1. Ability to expose a private SOAP service securely and configurable manner.
2. Ability to expose a private REST service securely and configurable manner.
3. Ability to expose a private JSON service securely and configurable manner.
4. Multi-tenancy support(Ability to expose a private service with same name by two different
tenants).
5. Ability to configure dead messages clean up task.

System Requirements
==================================

1. Minimum memory - 1GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. Java SE Development Kit 1.6.24 or higher
4. The Management Console requires you to enable Javascript of the Web browser,
   with MS IE 7. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to
   medium or lower.
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x and the default medium security setting with
     IE does not allow sufficient level of JS or ActiveX enablement for the
     management console to run.
5. To compile and run the sample clients, an Ant version is required. Ant 1.7.0
   version is recommended
6. To build WSO2 CSG from the Source distribution, it is necessary that you have
   JDK 1.6.x version and Maven 2.1.0 or later

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements

Installation & Running
==================================

1. Extract the wso2csg-1.0.0-alpha.zip and go to the extracted directory
2. Run the wso2server.sh{bat} inside the bin folder.

WSO2 CSG 1.0.0-alpha distribution directory structure
======================================================

	CARBON_HOME
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- logs <folder>
		|- repository <folder>
		|--- conf <folder>
		|--- database <folder>
		|- resources <folder>
		|- tmp <folder>
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
	  Contains the basic set of libraries required to startup CSG
	  in standalone mode

	- logs
	  Contains all log files created during execution

	- repository
	  Contains the run time configuration of the CSG server and
	  the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators third party libraries and so on..

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 CSG is distributed.

	- README.txt
	  This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 CSG

	- release-notes.html
	  Release information for WSO2 CSG 1.0.0-alpha

Support
=======

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 CSG, visit the WSO2 Oxygen Tank (http://wso2.org)

Known issues of WSO2 CSG 1.0.0-alpha
====================================

 * https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10664
 * https://wso2.org/jira/secure/IssueNavigator.jspa?requestId=10524
 * https://wso2.org/jira/secure/IssueNavigator.jspa?requestId=10525

Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON

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

