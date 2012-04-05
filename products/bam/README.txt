@product.name@, v@product.version@
================================

15th April 2010

Welcome to the @product.name@, v@product.version@ release

WSO2 Business Activity Monitor (WSO2 BAM) is a tool designed to
exercise Business Activity Monitoring (BAM). WSO2 BAM is intended to
serve the needs of both business and IT domain experts to monitor and
understand business activities within a SOA deployment. It is
specifically designed for monitoring SOA deployments, and can be
extended to cater for other general monitoring requirements as well.

WSO2 BAM is powered by WSO2 Carbon, the SOA middleware component
platform. 

Features
========

* Support for collecting data on service invocations and message 
  mediations
* Straight through processing - Polling and Eventing based models
  for automated data collection wihtout manual intervention
* Real time activity monitoring with zero latency - No time gap between
  data collection and availability of data for monitoring
* Analytics for historical data on service invocations and message
  mediations summarized over various time dimension intervals
* Data visualization with dashboards and reports
* Ability to define and monitor key performance indicators (KPI) with
  dashboards and reports
* Provision to extend monitoring capabilities by customizing dashboard
  gadgets
* Built-in support for monitoring WSO2 WSAS and WSO2 ESB
* Ability to extend the current monitoring model with user defined data
  by collecting custom data formats for monitoring
* Multiple DBMS support (H2 - default, MySQL and MSSQL)
* Message level data correlation using Activity ID (Experimental)

System Requirements
===================

1. Minimum memory - 256MB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. The Management Console requires full Javascript enablement of the
   Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium
     security level in Internet Explorer 6.x.

Installation and Running
========================

1. Extract the downloaded zip file
2. Go to the bin directory in the extracted folder
3. On Linux/Unix, run wso2server.sh script. On Microsoft Windows, run 
   wso2server.bat file.
4. Point your browser to the URL https://localhost:9443/carbon
5. Use username admin, and password admin to login as the administrator.
   NOTE: It is strongly recommended that you change your administrator 
       password from the admin to a more secure one at this point. To do
       this click on 'Configure->User Managment' link on the ledt menu.
6. Add servers to be monitored using left menu 'Configure->Monitored Servers'.
7. Monitor data using various dashboards present under 'Dashboard'
   section of left menu.
   NOTE: You might have to wait some time till there is data available 
       from the servers being monitored. To see summary data, you have 
       to wait at least one hour, before the first summary run happens.
8. Have a look at the documentation for more information on data
   collection and monitoring capabilities of WSO2 BAM

Training
========

WSO2 Inc. offers a variety of professional Training Programs, including
training on general Web services as well as @product.name@ and number of 
other products.

For additional support information please refer to
http://wso2.com/training/course-catalog/


Support
=======

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on @product.name@, visit the WSO2 Oxygen Tank (http://wso2.org)


Known issues of @product.name@ @product.version@
==============================================

All known issues have been recorded at https://wso2.org/jira/browse/CARBON
& https://wso2.org/jira/browse/BAM


@product.name@ Binary Distribution Directory Structure
=======================================================

	CARBON_HOME
		|- bam <folder>
		|- bin <folder>
		|- conf <folder>
		|- database <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- logs <folder>
		|- repository <folder>
		|- resources <folder>
		|- tmp <folder>
		|- webapps <folder>
		|-- LICENSE.txt <file>
		|-- README.txt <file>
		|-- INSTALL.txt <file>
		|-- release-notes.html <file>

	- bam
	  Contains the default BAM database and the database creation scripts
	  for various supported DBMS.
	
	- bin
	  Contains various scripts .sh & .bat scripts

	- conf
	  Contains configuration files

	- database
	  Contains the WSO2 Registry & User Manager database

	- dbscripts
	  Contains the database creation & seed data population SQL scripts for
	  various supported databases for the WSO2 Registry & User Manager database
      
	- lib
	  Contains the basic set of libraries required to startup @product.name@
	  in standalone mode

	- logs
	  Contains all log files created during execution

	- repository
	  The repository where Carbon artifacts &
	  Axis2 services and modules deployed in @product.name@
	  are stored. In addition to this other custom deployers such as
	  dataservices, axis1services and pojoservices are also stored.

	- resources
	  Contains additional resources that may be required

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- webapps
	  Contains the @product.name@ webapp. Any other webapp also can be deployed
	  in this directory

	- LICENSE.txt
	  Apache License 2.0 under which @product.name@ is distributed.

	- README.txt
	  This document.

	- INSTALL.txt
          This document will contain information on installing @product.name@

	- release-notes.html
	  Release information for @product.name@, v@product.version@

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

   Apacge Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

For further details, see the WSO2 Carbon documentation at
http://wso2.org/wiki/display/carbon/2.0

---------------------------------------------------------------------------
(c) @copyright.year@, WSO2 Inc.


