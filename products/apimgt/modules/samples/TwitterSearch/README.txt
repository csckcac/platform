How to Deploy & Test TwitterSearch Sample?
===========================================

###########################################################################################
### IMPORTANT: Scripts being used for this sample will not work in Windows environment. ###
### Windows support will be added in near future.                                       ###
### We are working on reducing number of steps required to deploy this sample.          ###
### Any suggestions are welcome @ https://wso2.org/jira/browse/APISTORE .               ###
###########################################################################################

Prerequisites:
--------------
Following tools should be available on you environment.
- cURL (http://curl.haxx.se/)
- Apache Ant (http://ant.apache.org)

This sample downloads some resources from the Internet. Therefore you need to have an Internet
connection on your machine.

Steps:
------
** IMPORTANT: If you have already configured any other sample, start from Step 7 **

1. Extract wso2am-xxx.zip (eg: wso2am-1.0.0-SNAPSHOT.zip)
2. Go to wso2am-1.0.0-SNAPSHOT/bin folder & type 'ant'

3. Start WSO2AM by executing wso2am-1.0.0-SNAPSHOT/bin/wso2server.sh
This step will populate all master data required for WSO2AM server to start up. In next few steps, we are going to add
some more master data required for this sample.

4. Shutdown WSO2AM server. (IMPORTANT: This step is a MUST)

5. Run 'ant' inside wso2am-1.0.0-SNAPSHOT/samples/Data
You will see an output similar to following. This step adds two user accounts (provider1 & subscriber1) to WSO2AM's user base.

populate-user-database:
      [sql] Executing resource: .....  /wso2am-1.0.0-SNAPSHOT/samples/Data/UserPopulator.sql
      [sql] 10 of 10 SQL statements executed successfully
      
      
6. Start WSO2AM again & now you can login to API Publisher's console
URL - http://localhost:9763/publisher/
Username/password - provider1/provider1

Take a note of the fact that there are no APIs published. Next step adds an API & publishes it to API store.

7. Run wso2am-1.0.0-SNAPSHOT/samples/TwitterSearch/APIPopulator.sh  (eg: sh APIPopulator.sh)
You will see an output similar to following on the console. Refresh above page & you should be seeing the newly added TwitterSearch API.

{"error" : "false"}
{"error" : "false"}
{"error" : "false"}


8. Now let's try to access Twitter's search function through our newly deployed API. First you need to login
to the API Store and obtain an API key. Launch a web browser and enter the URL http://localhost:9763/apistore

9. Login as the user "subscriber1" with password "subscriber1". Click on the "Applications" tab at
the top of the page, and create a new application. Provide any name you prefer.

10. Now click on the "APIs" tab at the top of the page, select the "TwitterSearch" API and subscribe to
it using the newly created application. Go to the "My Subscriptions" tab. Click
on the "Generate" option in-front of the label "Production Key" and then select the "Show Key" option
to obtain an Application key.

11. Now we are ready to invoke the API. Copy and paste following into a new console window & execute it.

curl -k -d "q=wso2&count=5" -H "Authorization :Bearer 9nEQnijLZ0Gi0gZ6a3pZICktVUca" https://localhost:8243/twitter/1.0.0/search.atom

(** NOTE: Replace the string '9nEQnijLZ0Gi0gZ6a3pZICktVUca' with the Application key you generated earlier)

You should be able to see search results from Twitter on you console.
eg:
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:twitter="http://api.twitter.com/" xmlns:georss="http://www.georss.org/georss"
xmlns:google="http://base.google.com/ns/1.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/" xml:lang="en-US">
<id>tag:search.twitter.com,2005:search/wso2</id><link type="text/html" href="http://search.twitter.com/search?q=wso2"
rel="alternate" /><link type="application/atom+xm..............

12. Try executing the above command several times with different API keys. Note the authentication
failures returned by the API gateway when you pass invalid API keys.

13. After a few invocations, the throttling policy of the API will get activated and the API gateway
will start responding with 503 Service Unavailable response messages.
