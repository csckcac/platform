@product.name@, v@product.version@
================================

12th March 2012

Welcome to the @product.name@, v@product.version@ release

WSO2 Business Activity Monitor (WSO2 BAM) is a comprehensive framework designed to solve the problems in the wide area of business activity monitoring. WSO2 BAM comprises of many modules to give the best of performance, scalability and customizability. This allows to achieve requirements of business users, dev ops, CEOs without spending countless months on customizing the solution without sacrificing performance or the ability to scale. 

WSO2 BAM is powered by WSO2 Carbon, the SOA middleware component
platform. 

Features
========

* Data Agents
        A re-usable Agent API to publish events to the BAM server from any application (samples included)
        Apache Thrift based Agents to publish data at extremely high throughput rates
        Option to use Binary or HTTP protocols
* Event Storage
        Apache Cassandra based scalable data architecture for high throughput of writes and reads
        Carbon based security mechanism on top of Cassandra
* Analytics
        An Analyzer Framework with the capability of writing and plugging in any custom analysis tasks
        Built in Analyzers for common operations such as get, put aggregate, alert, fault detection, etc.
        Scheduling capability of analysis tasks
* Visualization
        Drag and drop gadget IDE to visualize analyzed data with zero code
        Capability to plug in additional UI elements and Data sources to Gadget IDE
        Google gadgets based dashboard

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
6. Publish events to BAM server using data agents.
7. Write anaylizer sequence according to your requirement on the collected data by the BAM.
8. Create your preferred gadget from the Gadget-Ide. 
9. Publish the gadgets to the BAM dashboard. There you can view the created gadget from the BAM dashboard.
10.There are many four samples which addresses the how to work with BAM and the features of BAM. 

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
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- repository <folder>
		|- tmp <folder>
		|- samples <folder>
			|- cluster-monitor<folder>
			|- end-to-end-messageTracing<folder>
			|- fault-detection-and-alerting<folder>
			|- kpi-definition<folder>		
		|-- LICENSE.txt <file>
		|-- README.txt <file>
		|-- INSTALL.txt <file>
		|-- release-notes.html <file>

	
	- bin
	  Contains various scripts .sh & .bat scripts

	- dbscripts
	  Contains the database creation & seed data population SQL scripts for
	  various supported databases for the WSO2 Registry & User Manager database
      
	- lib
	  Contains the basic set of libraries required to startup @product.name@
	  in standalone mode

	- repository
	  The repository where Carbon artifacts &
	  Axis2 services and modules deployed in @product.name@
	  are stored. In addition to this other custom deployers such as
	  dataservices, axis1services and pojoservices are also stored.

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- samples
          Contains the samples which describes the usage and fetures of 
	  @product.name@. This includes four samples: 

           	- cluster-monitor 
		  This sample explains the scenario where you can monitor the cluster 
		  of ESB and AS via  @product.name@.

	        - end-to-end-messageTracing 
		  This sample exaplains about the scenario of using @product.name@ when 
                  tranferring message from one end to another end. 

	        - fault-detection-and-alerting 
		  This sample exaplains about the scenario of using @product.name@ to 
 		  send e-mail alert in the failure.

	        - kpi-definition 
		  This sample exaplains about the scenario of using @product.name@ to 
 		  in monitoring Key performance Indicators.

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


