Jaggery ${carbon.product.version}
-----------------

${buildNumber}

Welcome to the Jaggery ${carbon.product.version} release

Jaggery is a framework to write webapps and HTTP-focused web services
for all aspects of the application: front-end, communication,
Server-side logic and persistence in pure Javascript. One of the
intents of this framework is to reduce the gap between writing web
apps and web services.

This Framework uses Mozilla Rhino to process Javascript at the
server and also contains a powerful caching layer with the support of
Rhino compiled scripts; so its as fast as the JVM. As few key features,
Jaggery has native JSON support and also E4X support for XML manipulation.


** Bug Fixes
    * [JAGGERY-32] - NullPointerException from 'shout' sample
    * [JAGGERY-40] - &lt and < are not getting resolved in try-it client (response)
    * [JAGGERY-80] - Gives an error when I try to print a table within <% tags
    * [JAGGERY-83] - META-INF folder in jaggery apps
    * [JAGGERY-91] - tryi-it breaks if you execute a client-side javascript code
    * [JAGGERY-94] - session.get("x") is not printing the correct output when its value is concatenated with a string
    * [JAGGERY-100] - Getting the same output for response types "json", "xml", "text"
    * [JAGGERY-101] - Results given within "{"data" : "result", "xhr" : {}}" in post() and get() APIs
    * [JAGGERY-104] - Incorrect error validation in try-it
    * [JAGGERY-105] - Inconsistant validation in print()
    * [JAGGERY-106] - html tags are not allowed with <%=
    * [JAGGERY-108] - try-it editor - Returns a 'XML syntax error' when % used within html tags
    * [JAGGERY-110] - Where do the modules reside
    * [JAGGERY-115] - Correct the message that appear after a bulk upload of jaggery apps
    * [JAGGERY-116] - Mgt console styling does not with with IE
    * [JAGGERY-120] - Few comments on 'Setting up Jaggery' doc
    * [JAGGERY-122] - Overview given for 'Database' and 'Registry' are incorrect
    * [JAGGERY-124] - OutOfMemoryError appears when one app is continually hit while 100 jag apps are loaded
    * [JAGGERY-125] - OutOfMemoryError when shutting down the server after successfully sending 50000 messages
    * [JAGGERY-126] - .iml files in samples
    * [JAGGERY-127] - print/log serialization issue when an object is returned with NativeStrings
    * [JAGGERY-128] - Caching issue when the server machine and the webapp created machine are in different timezones
    * [JAGGERY-129] - Null pointer exception when a non-existing module is required
    * [JAGGERY-130] - Implement basic authentication support for XHR
    * [JAGGERY-131] - Gives an JdbcSQLException when try-it the code in Database API page
    * [JAGGERY-132] - Caching issue when *.jss names/directories contains "-"
    * [JAGGERY-141] - XML class does not work with text read from an XML file
    * [JAGGERY-143] - Documentation inconsistencies
    * [JAGGERY-144] - Doc sample > download button still says 'M3' in its label
    * [JAGGERY-145] - /products/jaggery/dl/jaggery-1.0.0-SNAPSHOT_M5.zip Not found
    * [JAGGERY-146] - Usability: Enable browser BACK button from "Say Hello to Jaggery" try-it page
    * [JAGGERY-147] - Gets an 'AuthenticationFailedException' even when correct credentials are used in the email sample
    * [JAGGERY-149] - Fix File hostobject to work according to the paths specified using "file URI scheme"
    * [JAGGERY-150] - A webapp session count doesn't change after you exit from the session
    * [JAGGERY-169] - Executing Database sample in TryIt fails
    * [JAGGERY-172] - JSON Parse example does not use the parse() method
    * [JAGGERY-174] - Can not access application uploaded as Jaggery Application Archive (.zip)
    * [JAGGERY-175] - Automatically uppercase the column name in the result set
    * [JAGGERY-176] - Jaggery is not starting when JAVA_HOME is point to JDK 1.7
    * [JAGGERY-180] - README - remove the mention about 'docs' folder withing the binary distro
    * [JAGGERY-181] - The 'sample' sample is returning an empty page
    * [JAGGERY-183] - spi samples "Go To URL" is not working out of the box
    * [JAGGERY-187] - API sample given for require() needs to be updated to reflect latest changes.
    * [JAGGERY-188] - Duplicate files inside /docs  sample
    * [JAGGERY-189] - try-it code for response object is different to the source given in the api
    * [JAGGERY-190] - Typos and few other corrections in jaggery API doc
    * [JAGGERY-191] - Session is still accessible after session.remove or session.invalidate
    * [JAGGERY-192] - put() api doesn't have a working example.
    * [JAGGERY-195] - API re-factoring properties vs operations
    * [JAGGERY-198] - Log logger name need to handle both jag and js files. and jaggery devs should be able to define the logger
    * [JAGGERY-199] - Display name appear blank in Jaggery Application Dashboard
    * [JAGGERY-200] - add entry for the display name in jaggery conf
    * [JAGGERY-201] - Clean up how to page


** Improvements
    * [JAGGERY-36] - Support uploading a jss file instead of a WAR
    * [JAGGERY-42] - Command line client needs to take a script as a command line param
    * [JAGGERY-50] - Provide means to deal with API elements in Try-it interface
    * [JAGGERY-84] - Standard folder structure for jaggery apps
    * [JAGGERY-85] - To have tryit-view as a tool and not only within the doc sample
    * [JAGGERY-96] - Can we support 'println'
    * [JAGGERY-97] - To support other request methods such as getPathInfo, getRemoteAddr,getRequestURI also in the API
    * [JAGGERY-151] - require() to facilitate .js file inclution
    * [JAGGERY-152] - log() need to be converted as a Log class with more options
    * [JAGGERY-153] - Change the label of HTTP and divide to to variables and HTTPClients
    * [JAGGERY-154] - Remove XSLT class from Docs
    * [JAGGERY-155] - Clean up Feed objects and have one API for Atom / RSS read and write
    * [JAGGERY-156] - File need to take FilePath relativly
    * [JAGGERY-157] - Database API cleanup and a sample
    * [JAGGERY-158] - Registry API cleanup
    * [JAGGERY-159] - Remove XMLList
    * [JAGGERY-164] - Rename file extension to .jag
    * [JAGGERY-165] - release-notes.html in SNAPSHOT_HOME refers to Carbon, not Jaggery
    * [JAGGERY-182] - Shall we change the name of the 'sample' sample
    * [JAGGERY-184] - Add help on how to use 'Add Query String' feature

** New Features
    * [JAGGERY-140] - Movie review sample
    * [JAGGERY-142] - Await page for jaggeryjs.org
    * [JAGGERY-202] - Jaggery.cof file to manage application configuration

** Sub-tasks
    * [JAGGERY-78] - Jaggery directory based deployment


------------

Hardware Requirements
-------------------
1. Minimum memory - 256MB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.6 (1.6.0_21 onwards)
2. Apache Ant - An Apache Ant version is required. Ant 1.7.0 version is recommended.
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

Known Issues
------------

All known issues have been recorded at https://wso2.org/jira/browse/JAGGERY

Carbon Binary Distribution Directory Structure
--------------------------------------------


    JAGGERY_HOME
        ├── apps
        │   ├── freshometer
        │   ├── ROOT
        │   ├── sample
        │   ├── shout
        │   └── taskmaster
        ├── bin
        │   ├── jaggery.bat
        │   ├── jaggery.sh
        │   ├── server.bat
        │   └── server.sh
        ├── carbon
        │   ├── bin
        │   ├── dbscripts
        │   ├── lib
        │   ├── repository
        │   ├── tmp
        │   └── wso2carbon.pid
        ├── etc
        │   └── modulemetafiles
        ├── INSTALL.txt
        ├── LICENSE.txt
        ├── modules
        │   └── modules.xml
        ├── README.txt
        └── release-notes.html

    - bin
      Contains the scripts needed to start the Jaggery server (server.sh) and Jaggery Shell (jaggeryshell.sh)

    - carbon
      Known as CARBON_HOME, which is the home directory of WSO2 carbon server, The carbon server act as the
      Jaggery container.

    - apps
      The directory that contains the jaggery applications

    - modules
      The modules directory, which contain jaggery modules configuration.

    - etc
      Contains configuration files

    - LICENSE.txt
      Apache License 2.0 under which Jaggery is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 Carbon

    - release-notes.html
      Release information for Jaggery

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Carbon, visit the WSO2 Oxygen Tank (http://wso2.org)

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

---------------------------------------------------------------------------
(c) Copyright 2011-2012 WSO2 Inc.
