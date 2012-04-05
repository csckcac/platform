WSO2 Mashup Server ${mashup_version}
========================

Welcome to the WSO2 Mashup Server ${mashup_version} release (${buildNumber})

"Create, deploy, and consume Web services Mashups in the simplest fashion."

The WSO2 Mashup Server is a powerful yet simple and quick way to tailor 
Web-based information to the personal needs of individuals and organizations. 
It is a platform for acquiring data from a variety of sources including Web 
Services, HTML pages, feeds and data sources, and process and combine it
with other data using JavaScript with E4X XML extensions. The result is then 
exposed as a new Web service with rich metadata and artifacts to simplify 
the creation of rich user interfaces

Features
========

* Hosting of mashup services written using JavaScript with E4X XML extension
    - Simple file based deployment model
* JavaScript annotations to configure the deployed services
* Auto generation of metadata and runtime resources for the deployed mashups
    - JavaScript stubs that simplify client access to the mashup service
    - Code templates for developing rich HTML or Google Gadget interfaces
    - TryIt functionality to invoke the mashup service through a web browser
    - WSDL 1.1/WSDL 2.0/XSD documents to describe the mashup service
    - API documentation 
* Ability to bundle a custom user interface for the mashups
* Many useful Javascript Host objects that can be used when writing mashups
    - WSRequest: invoke Web services from mashup services
    - File: File storage/manipulation functionality
    - System: Set of system specific utility functions
    - Session: Ability to share objects across different service invocations
    - Scraper: Extract data from HTML pages and present in XML format 
    - APPClient: Atom Publishing Protocol client to retrieve/publish Atom
                 feeds with APP servers
    - Feed: A generic set of host objects to transparently read and create 
            Atom and RSS feeds
    - Request: Ability get information regarding a request received
    - HttpClient : A hostobject equvalent with Apache HttpClient, which 
		help you to do HTTP related stuff

* Support for recurring and longer-running tasks
* Support for service lifecycles
* Ability to secure hosted mashups using a set of commonly used security 
  scenarios
* Management console to easily manage the mashups
* Equinox P2 based provisioning support -
   extend your Mashup Server instance by installing new P2 features. See
   https://wso2.org/wiki/display/carbon/p2-based-provisioning-support
* User based mashup deployment model
* Ability to upload mashups with it's resources as a zip



System Requirements
===================

1. Minimum memory - 1GB

2. Processor      - Pentium 800MHz or equivalent at minimum

3. Java SE Development Kit 1.6.0_24 or higher

4. The Management Console requires you to enable JavaScript of the Web browser,
   with MS IE 6 and 7. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to medium or lower.

   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x and the default medium security setting with
     IE does not have sufficient levels of JavaScript and ActiveX enabled for the management
     console to run.

5. To build Mashup Server from the Source distribution, it is also necessary that you
   have Maven 2.1.0 or later.

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements


Project Resources
=================

* Home page          : http://wso2.com/products/mashup-server
* Library            : http://wso2.org/library/mashup
* Wiki               : http://wso2.org/wiki/display/mashup
* JIRA-Issue Tracker : https://wso2.org/jira/browse/MASHUP
                     : https://wso2.org/jira/browse/CARBON
     All Open Issues : https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10537
* QA Artifacts       
     Wiki            : http://wso2.org/wiki/display/mashup/Quality+Assurance
     SVN             : http://wso2.org/repos/wso2/trunk/commons/qa/mashup
* Forums             : http://wso2.org/forum/226
* Mailing Lists
     Developer List  : carbon-dev@wso2.org 
     User List       : mashup-user@wso2.org 
     Subscribe       : http://wso2.org/mail#mashup


Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:9443/carbon/
4. For more information, see the Installation Guide

Issues Fixed in This Release
============================

 * WSO2 Mashup Server -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10536
 * Mashup Components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10542


Known issues of WSO2 Mashup Server
==================================

 * WSO2 Mashup Server -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10537
 * Mashup Components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10538


WSO2 Mashup Server Binary Distribution Directory Structure
==========================================================

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
        |   `-- tenants <folder>
        |-- repository <folder>
	|    |-- resources <folder>
	|	|-- dashboard <folder>
        |   	`-- security <folder>
        |-- samples <folder>
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
      Contains the basic set of libraries required to start-up Mashup Server
      in standalone mode

    - repository
      The repository where services and modules deployed in Mashup Server
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
      Contains some sample applications that demonstrate the functionality
      and capabilities of Mashup Server

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - webapps
      Contains the Mashup Server webapp. Any other webapp also can be deployed
      in this directory

    - LICENSE.txt
      Apache License 2.0 under which Mashup Server is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing Mashup Server

    - release-notes.html
      Release information for Mashup Server ${mashup_version}


Support
=======

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit http://wso2.com/support/

For more information about WSO2 Mashup Server please see http://wso2.com/products/mashup-server
Visit the WSO2 Oxygen Tank developer portal for additional resources.


Crypto Notice
=============

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

For further details, see the WSO2 Carbon documentation at
http://wso2.org/wiki/display/carbon/3.0

---------------------------------------------------------------------------
(c) Copyright 2011, WSO2 Inc.

