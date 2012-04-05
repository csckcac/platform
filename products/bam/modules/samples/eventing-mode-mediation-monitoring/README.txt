 This file explains how to monitor mediation statistics in eventing mode using WSO2 Business Activity Monitor.
================================================================================================================

This sample demonstrates how to monitor an external server using WSO2 Business Activity Monitor. Here servers communicate using an eventing(pushing) model.
This sample consist of following sections.

          1) Running Business Activity Monitor 
          2) Running ESB parallel to BAM
          3) Running simple Axis2 server and deploying a sample service
          4) Installing  "BAM Mediation Statistics Data Publisher" feature to ESB server
          5) Adding a proxy service to ESB
          6) Adding a server to to BAM product
          7) Monitoring the mediator activity


Running Business Activity Monitor 
---------------------------------

1. Download (or build) and extract the BAM product. Extracted folder will be referred as BAM_HOME thought this document.
   Run BAM_HOME/wso2server.sh (on Linux/Unix) or BAM_HOME\wso2server.bat (on Windows)

2. Login to management console from the URL displayed in the console (eg: https://10.100.0.157:9443/carbon/)
   default username=admin and password=admin


Running ESB parallel to BAM
---------------------------

1. Download (or build) and extract the ESB product. extracted folder will be referred as ESB_HOME thorough out this document.

2. Open ESB_HOME/repository/conf/carbon.xml. Change the Offset value (under Ports) to 1. This will increment all the default port numbers by 1.
Ex: <!-- Ports offset. This entry will set the value of the ports defined below to
         the define value + Offset.
         e.g. Offset=2 and HTTPS port=9443 will set the effective HTTPS port to 9445
         -->
        <Offset>1</Offset>

3. Run ESB_HOME/wso2server.sh (on Linux/Unix) or ESB_HOME\wso2server.bat (on Windows)

4. Login to the management console of the ESB product (just like you logged to the management console of the BAM product)


Running simple Axis2 server and deploying a sample service
----------------------------------------------------------

1. Goto the ESB_HOME/samples/axis2Server/src/SimpleStockQuoteService

2. Run "ant build-service" (install ant if not installed http://ant.apache.org/manual/install.html)

3. Set environment variable AXIS2_HOME to ESB_HOME/samples/axis2Server (NOTE: there is no slash at the end)
Ex: For Linux : export AXIS2_HOME=ESB_HOME/samples/axis2Server
    For Windows : set AXIS2_HOME=ESB_HOME/samples/axis2Server

4. Run ESB_HOME/samples/axis2Server/axis2Server.sh

5. Browse to http://localhost:9000/services/ and verify whether SimpleStockQuoteService is running. The SimpleStockQuoteService will be shown on this page if it's running.


Installing  "BAM Mediation Statistics Data Publisher" feature to ESB server
---------------------------------------------------------------------------

1. Goto management console of the ESB management console, and select Configure > Features from the side panel.
   Click "Add Repository" link

2. Enter a suitable name (eg: "wso2 release repo 3.2") and url "http://dist.wso2.org/p2/carbon/releases/3.2.0". 
   Click add. 

3. Search and install "BAM Mediation Statistics Data Publisher" feature

4. Restart the ESB server

Adding a proxy service to WSO2 ESB 
---------------------------------

1. Goto ESB's management console and select Manage > Web Services > Add > Proxy Service from the side panel

2. Select WSDL Based Proxy 

3. Insert suitable Proxy Service Name (eg: SimpleStockQuoteServiceProxy)
         i)   http://localhost:9000/services/SimpleStockQuoteService?wsdl as the WSDL URI
         ii)  SimpleStockQuoteService as the WSDL Service
         iii) SimpleStockQuoteServiceHttpSoap11Endpoint as the WSDL Port

4. Click Create and select newly created proxy's name form the shown list to see the Service Dashboard of it.

5. Select "Enable Statistics" from "Specific Configuration" panel.


Adding a server to to BAM product
---------------------------------

1. Goto BAM management console and from the side panel and select configure > Monitored Servers.
   Then click "Add Server" link.

2. Enter ESB server's url (with the port eg: https://10.100.0.157:9558 ) to the "Server URL" text box

3. Select "Eventing" from the "Data Collection Method" radio buttons.
   Select "Mediation" from the "Type of Data" radio button.

4. Insert the same username and password used for logging in to the ESB management console

5. Click "Add" button. if the server is successfully added it should appear in the "Monitored Servers" list.
   Click the verify button to check whether the ESB is running and connected with BAM server successfully.


Monitoring the mediator activity
----------------------------------

1. From the side panel select dashboard > Main Dashboard.(this may take few seconds to load all the gadgets)

2. Goto ESB_HOME/samples/axis2Client and run "ant stockquote -Daddurl=<url> -Dmode=fullquote"
   Here <url> should be replaced by the Endpoint url shown in ESB's Service Dashboard(eg: https://localhost:8243/services/SimpleStockQuoteServiceProxy/)
   Run this few times to surpass the threshold (at least 3 times)
   (run "ant help" for more available modes)

3. Observe the server statistics from the BAM dashboard's "Mediation Data" tab.
