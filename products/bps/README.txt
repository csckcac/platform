WSO2 Business Process Server v@product.version@
-------------------------------------------

Welcome to the WSO2 BPS @product.version@ release

WSO2 Business Process Server (BPS) is an easy-to-use open source
business process server that executes business processes written using
the WS-BPEL standard. It is powered by Apache ODE and provides a
complete Web-based graphical console to deploy, manage and view
processes in addition to managing and viewing process instances.

WSO2 BPS is developed on top of the WSO2 Carbon platform and all the
existing capabilities of Enterprise Service Bus(ESB) and 
Web Services Application Server(WSAS) can be applied to business processes. 
For example securing business processes and throttle requests come to business processes.
 

Key Features
------------
* Deploying Business Processes written in compliance with WS-BPEL 2.0 Standard and BPEL4WS 1.1 standard.
* Managing BPEL packages, processes and process instances.
* BPEL Extensions and XPath extensions support.
* Instance recovery(Only supports 'Invoke' activity) support through management console
* OpenJPA based Data Access Layer
* WS-Security support for external services
* WS-Security support for business processes.
* Clustering support
* BPEL Package hot update which facilitate Versioning of BPEL Packages
* E4X based data manipulation support for BPEL assignments
* Configure external data base system as the BPEL engine's persistence storage
* Caching support for business processes.
* Throttling support for business processes.
* Transport management.
* Internationalized web based management console.
* System monitoring.
* Try-it for business processes.
* SOAP Message Tracing.
* New end-point configuration mechanism based on WSO2 Unified Endpoints.
* Customizable server - You can customize the BPS to fit into your
  exact requirements, by removing certain features or by adding new
  optional features.


New Features In This Release
----------------------------

* Clustering support
* Improved BPS home page
* "Remember me" feature


Issues Fixed In This Release
----------------------------

* WSO2 BPS related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10530


Known Issues
-----------

* WSO2 BPS related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10529
* Human Tasks feature is discontinued util the tool is available for Carbon Studio


Installation & Running
----------------------
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

For more details, see the Installation Guide


System Requirements
-------------------

1. Minimum memory - 1GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

For more details see
http://wso2.org/wiki/display/carbon/System+Requirements

 

BPS Binary Distribution Directory Structure
--------------------------------------------

	WSO2BPS_HOME
	|-- bin <folder>
	|-- dbscripts <folder>
	|-- lib <folder>
	|-- repository <folder>
	|   |-- components <folder>
	|   |-- conf <folder>
	|   |-- data <folder>
	|   |-- database <folder>
	|   |-- deployment <folder>
	|   |-- lib <folder>	
	|   |-- logs <folder>
	|   |-- resources <folder>
	|   |   `-- security <folder>
	|   `-- tenants <folder>
	|-- samples <folder>
	|-- tmp <folder>
	|-- LICENSE <file>
	|-- README <file>
	|-- INSTALL <file>
	`-- release-notes.html <file>

    -	bin
	Contains various scripts to start/stop BPS server, change user
	password, run tcpmon, java2wsdl, wsdl2java and admin scripts for cluster
	node management.

    -   dbscripts
	Contains dbscripts required to configure external databases instead of default derby database.

    -	lib
	Contains BPS and third party libraries required for the start-up of
	WSO2 BPS in standalone mode.

    - 	repository
	The repository where BPEL packages, services and modules deployed in WSO2 BPS
	are stored. In addition to this other custom deployers such as
	dataservices, axis1services and pojoservices are also stored.
	- components
          Contains OSGi bundles and configurations
      
        - conf
          Contains configuration files

        - data
          Contains the schema and data directories related to embedded ldap
         
        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details

        - lib
          Contains the 3rd party libraries required for the functionalities of WSO2 BPS
          
        - logs
          Contains all log files created during execution

        - resources
	  	  Contains additional resources that may be required

          - security
            Contains security resources

        - tenants
          Contains tenant details

    -   samples
        Contains the simple axis2 server that is requred to run LoanProcess sample
        
    - 	tmp
	  	Used for storing temporary files, and is pointed to by the
	  	java.io.tmpdir System property

    - 	LICENSE
	  	Apache License 2.0 under which WSO2 BPS is distributed.

    -	README
		This document.

    -   INSTALL
        This document will contain information on installing WSO2 BPS @product.version@ in different modes.

    -	release-notes.html
		Release information for WSO2 BPS @product.version@.


Library
-------
For more information on WSO2 BPS, Visit the "WSO2 Oxygen Tank" at
http://wso2.org/library


Support
-------

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit http://wso2.com/support/

For more information about WSO2 BPS please see http://wso2.com/products/business-process-server/
or visit the WSO2 Oxygen Tank developer portal for addition resources.


Crypto Notice
-------------

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

Apacge Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/


For further details, see the WSO2 Carbon documentation at
http://wso2.com/products/carbon/

---------------------------------------------------------------------------
 Copyright 2011 WSO2 Inc.


