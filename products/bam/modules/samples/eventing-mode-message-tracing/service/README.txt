 This file explains how to trace a web service activity in eventing mode using WSO2 Business Activity Monitor.
================================================================================================================

This sample demonstrates how to trace activity of a web service using Business Activity Monitor.
Here servers communicate using an eventing(pushing) model.
This sample consist of following sections.

          1) Running business activity monitor 
          2) Running application server parallel to BAM
          3) Installing  "BAM Activity Service Data Publisher" feature to application server
          4) Adding a server to to BAM product
          5) Monitoring the server activity

Running Business Activity Monitor 
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

Installing  "BAM Activity Service Data Publisher" feature to application server
--------------------------------------------------------------------------------

1. Go to management console of the application server, and select Configure > Features from the side panel.
   Click "Add Repository" link

2. Enter a suitable name (eg: "wso2 release repo 3.2") and url "http://dist.wso2.org/p2/carbon/releases/3.2.0". 
   Click add. 

3. Search and install "BAM Activity Service Data Publisher" feature

4. Restart the application server

5. Go to management console of the application server, and select "Configure > Activity Service" from the side panel.
   Set "Message Dumping" option to "ON".

Adding a server to to BAM product
---------------------------------

1. Go to BAM management console and from the side panel and select configure > Monitored Servers.
   Then click "Add Server" link.

2. Enter application server's url (with the port eg: https://10.100.0.157:9558 ) to the "Server URL" text box

3. Select "Eventing" from the "Data Collection Method" radio buttons.
   Select "Message" from the "Type of Data" radio button.

4. Insert the same username and password used for logging in to the application server management console

5. Click "Add" button. if the server is successfully added it should appear in the "Monitored Servers" list.
   Click the verify button to check whether the application server is running and connected with BAM server successfully.

Monitoring the server activity
------------------------------

1. From the side panel select Dashboard > Main Dashboard.(this may take few seconds to load all the gadgets)

2. Invoke a web service of the application server.
   You can do this by invoking sample service that comes with the application server.
   Use following steps to invoke it using "Try It" tool :

              i)   Switch back to application server's management console.
              ii)  From the side panel select Web Services > List.
              iii) Click "Try this service" link for the "HelloService".
              iv)  Change ? to some name and click Invoke button
   You may have to do this few time to exceed the threshold (at least 3 times)

3. Observe the server statistics from the BAM dashboard's "Activity Data" tab.
	i).   Statistics relating to data published by activity publishers are present in the "Activity Details" gadget. In the configure tab of this gadget
	      select start and end times to cover the time span the events were published to BAM.
        ii).  Click "View Messages".
        iii). Now the message details are listed in a tabular format. Click on "View Message" link in the "Message" column of a message to view the message 
	      body.
	iv)   Additionally using "Activity Drill Down" gadget it is possible to drill down from server --> service --> operation to message level details.  
