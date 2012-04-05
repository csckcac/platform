@product.name@, v@product.version@
================================

Welcome to the @product.name@, version @product.version@

New Features In This Release
============================

1. Based on Carbon @carbon.version@
2.Optimized Performance
3.Search Enabled Gadget Repository
4.Improved UI to Pick Gadgets
5.Enhanced Permission Model for Theming
6.Improved Gadget Source Editor

Features
========
1. Enterprise class Portal interface
2. Client Side Gadgets
3. Enterprise Gadget Repository
4. Anonymous Mode
5. OAuth Support for Gadgets
6. Inter-gadget Communication
7. I18n Support for Gadget
8. User registration
9. external user stores
10.Management Console
11. Custom Layouts for Tabs
12. Role Based Permissions
13. Make SOAP Request Support
14. Theming Support
15. Gadget Archive Deployment

Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:8443/carbon/ for mgt console or 
   http://localhost:8080/portal for GS portal
4. For more information, see the Installation Guide:
     Locally    : INSTALL.txt
     On the web : http://wso2.org/project/registry/@product.version@/docs/installation_guide.html

System Requirements
===================

1. Minimum memory - 1GB

2. Processor      - Pentium 800MHz or equivalent at minimum

3. Java SE Development Kit 1.6.0_24 or higher

4. The Management Console requires you to enable JavaScript of the Web browser,
   with MS IE 6, 7, 8 and 9. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to medium or lower.

   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x and the default medium security setting with
     IE does not have sufficient levels of JavaScript and ActiveX enabled for the management
     console to run.

5. To build @product.name@ from the Source distribution, it is also necessary that you
   have Maven 2.1.0 or later.

For more details see
    http://wso2.org/wiki/display/carbon/System+Requirements


Issues Fixed in This Release
============================

 * WSO2 Gadget Server -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10543
 * Gadget Server Components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10546


Known issues of WSO2 Gadget Server
==================================

 * WSO2 Gadget Server -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10544
 * Gadget Server Components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10545


WSO2 Gadget Server Binary Distribution Directory Structure
-----------------------------------------------------

	CARBON_HOME
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- docs <folder>
		|- repository <folder>
        |   |-- components <folder>
        |   |-- conf <folder>
        |   |-- data <folder>
        |   |-- database <folder>
        |   |-- deployment <folder>
        |   |-- lib <folder>
        |   |-- logs <folder>
        |   |-- resources <folder>
        |   |   |-- gadget-repo <folder>
        |   |   |-- gs-themes <folder>
        |   |   `-- security <folder>
        |   `-- tenants <folder>
		|- tmp <folder>
		|-- LICENSE.txt <file>
		|-- README.txt <file>
		|-- INSTALL.txt <file>
		|-- release-notes.html <file>

    	- bin
	  Contains various scripts .sh & .bat scripts

	- conf
	  Contains configuration files

        - database
          Contains the database

	- lib
	  Contains the basic set of libraries required to startup GS
	  in standalone mode

	- logs
	  Contains all log files created during execution

	- repository
      The repository where services and modules deployed in @product.name@
      are stored.

        - components
          Contains OSGi bundles and configurations

        - conf
          Contains configuration files

        - data
          Contains server runtime data

        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details

        - lib
          Contains all client side libraries

        - logs
          Contains all log files created during execution

        - resources
          Contains additional resources that may be required

            - gadget-repo
              Contains default gadget resources

            - gs-themes
              Contains default theme resources

            - security
              Contains security resources

        - tenants
          Contains tenant details

	- resources
	  Contains additional resources that may be required

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- LICENSE.txt
	  Apache License 2.0 under which WSO2 GS is distributed.

	- README.txt
	  This document.

	- INSTALL.txt
          This document will contain information on installing WSO2 GS

	- release-notes.html
	  Release information for WSO2 GS 1.4.0


Support
-------

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit
http://wso2.com/support/

For more information about @product.name@ please see
http://wso2.com/products/gadget-server/ visit the WSO2 Oxygen Tank developer portal for
additional resources.

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
http://wso2.org/wiki/display/carbon/3.0

---------------------------------------------------------------------------
(c) @copyright.year@, WSO2 Inc.
