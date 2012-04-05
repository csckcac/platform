WSO2 Mashup Server ${mashup_version}
----------------------------------------------------------------------------

Welcome to the WSO2 Mashup Server ${mashup_version} release (${buildNumber})

"Create, deploy, and consume Web services Mashups in the simplest fashion."

The WSO2 Mashup Server is a powerful yet simple and quick way to tailor 
Web-based information to the personal needs of individuals and organizations. 
It is a platform for acquiring data from a variety of sources including Web 
Services, HTML pages, feeds and data sources, and process and combine it
with other data using JavaScript with E4X XML extensions. The result is then 
exposed as a new Web service with rich metadata and artifacts to simplify 
the creation of rich user interfaces


Key Features
------------

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
* Support for recurring and longer-running tasks
* Support for service lifecycles
* Ability to secure hosted mashups using a set of commonly used security 
  scenarios
* Management console to easily manage the mashups


New Features In This Release
----------------------------

The ${mashup_version} version of the Mashup Server is built on top of the Award Winning WSO2 Carbon
Platform. All the major features have been developed as pluggable Carbon components.


Installation & Running
----------------------
1. extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

For more details, see the Installation Guide


System Requirements
-------------------

1. Minimum memory - 256MB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

For more details see
http://wso2.org/wiki/display/carbon/System+Requirements


WSO2 Mashup Server Binary Distribution Directory Structure
----------------------------------------------------------
  CARBON_HOME   
	   |-bin	   
	   |-dbscripts
	   |-docs	  
	   |-lib	   
	   |-logs
	   |-repository	  
	   |---dataservices	  	   
	   |---scripts	   
	   |---conf	 
	   |---database 
	   |-resources	   
	   |---security
	   |-tmp	   
	   |-webapps	   
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
	  Contains the basic set of libraries required to startup WSO2 Mashup Server
	  in standalone mode

	- logs
	  Contains all log files created during execution

	- repository
	  The repository where Carbon artifacts &
	  Axis2 services and modules deployed in WSO2 Mashup Server
	  are stored. In addition to this other custom deployers such as
	  javascript, dataservices, axis1services and pojoservices are also stored.
	  
			- dataservices
			  Contains the Data Services hosted in the Mashup Server.
			  
			- scripts
			  Contains the Javascript Services (Mashups) hosted in the Mashup Server.
			  
			- services
			  Contains the Java Services hosted in the Mashup Server.

	- resources
	  Contains additional resources that may be required

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- webapps
	  Contains the WSO2 Mashup Server webapp. Any other webapp also can be deployed
	  in this directory

	- LICENSE.txt
	  Apache License 2.0 under which WSO2 Mashup Server is distributed.

	- README.txt
	  This document.

	- INSTALL.txt
          This document will contain information on installing WSO2 Mashup Server

	- release-notes.html
	  Release information for WSO2 Mashup Server ${mashup_version}


Training
--------

WSO2 Inc. offers a variety of professional Training Programs, including
training on general Web services as well as WSO2 Mashup Server, Apache Axis2,
Data Services and a number of other products.

For additional support information please refer to
http://wso2.com/training/course-catalog/


Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Mashup Server, visit the WSO2 Oxygen Tank 
(http://wso2.org)


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
http://wso2.org/wiki/display/carbon/1.0

---------------------------------------------------------------------------
(c) Copyright WSO2 Inc.
