This file explains how to monitor a server in polling mode using WSO2 Business Activity Monitor.
================================================================================================

This sample demonstrates how to monitor an external server using WSO2 Business Activity Monitor.
This sample consist of following sections.
          1) Running business activity monitor 
          2) Running application server parallel to BAM
          3) Adding a server to to BAM product
          4) Monitoring the server activity


Running business activity monitor 
---------------------------------

1. Download (or build) and extract the BAM product. Extracted folder will be referred as BAM_HOME thought this document.
   Run BAM_HOME/wso2server.sh (on Linux/Unix) or BAM_HOME\wso2server.bat (on Windows)

2. Login to management console from the URL displayed in the console (eg: https://10.100.0.157:9443/carbon/)
   default username=admin and password=admin


Running application server parallel to BAM
------------------------------------------

1. Download (or build) and extract the AS product. extracted folder will be referred as AS_HOME thorough out this document.

2. Open AS_HOME/repository/conf/carbon.xml. Change the Offset value (under Ports) to 1. This will increment all the default port numbers by 1.
Ex: <!-- Ports offset. This entry will set the value of the ports defined below to
         the define value + Offset.
         e.g. Offset=2 and HTTPS port=9443 will set the effective HTTPS port to 9445
         -->
        <Offset>1</Offset>

3. Run AS_HOME/wso2server.sh (on Linux/Unix) or AS_HOME\wso2server.bat (on Windows)

4. Login to the management console of the AS product (just like you logged to the management console of the BAM product)


Adding a server to to BAM product
---------------------------------

1. Goto BAM management console and from the side panel and select configure > Monitored Servers.
   Then click "Add Server" link.

2. Enter Application Server's url (with the port eg: https://10.100.0.157:9558 ) to the "Server URL" text box

3. Select "Polling" from the "Data Collection Method" radio buttons

4. Insert the same username and password used for logging in to the application server management console

5. Click "Add" button. if the server is successfully added it should appear in the "Monitored Servers" list.
   Click the verify button to check whether the application server is running and connected with BAM server successfully.



Monitoring the server activity
------------------------------

1. From the side panel select dashboard > Main Dashboard.(this may take few seconds to load all the gadgets)

2. Invoke a web service of the application server.
   You can do this by invoking sample service that comes with the application server.
   Use following steps to invoke it using "Try It" tool :
               i)   Switch back to application server's management console.
               ii)  From the side panel select Web Services > List.
               iii) Click "Try this service" link for the "HelloService".
               iv)  Change ? to some name and click Invoke button

3. Observe the service statistics from the BAM dashboard.
