How to deploy & test TwitterSearch sample?
===========================================

###########################################################################################
### IMPORTANT: Scripts being used for this sample will not work in Windows environment. ###
### Windows support will be added in near future.                                       ###
### We are working on reducing number of steps required to deploy this sample.          ###
### Any suggestions are welcome @ https://wso2.org/jira/browse/APISTORE .               ###
###########################################################################################

Prerequisites:
--------------
Following tools needs to be present on you environment.
- cURL
- Apache Ant

Steps:
------
1. Extract wso2am-xxx.zip (eg: wso2am-1.0.0-SNAPSHOT.zip)
2. Go to wso2am-1.0.0-SNAPSHOT/bin folder & type 'ant'

3. Start WSO2AM by executing wso2am-1.0.0-SNAPSHOT/bin/wso2server.sh
This step will populate all master data required for WSO2AM server to start up. In next few steps, we are going to add
some more master data required for this sample.

4. Shutdown WSOAM server. (IMPORTANT: This step is a MUST)

5. Run 'ant' inside wso2am-1.0.0-SNAPSHOT/samples/TwitterSearch
You will see an output similar to following. This step adds two user accounts (provider1 & subscriber1) to WSO2AM's user base.

populate-user-database:
      [sql] Executing resource: .....  /wso2am-1.0.0-SNAPSHOT/samples/Sample4/UserPopulator.sql
      [sql] 10 of 10 SQL statements executed successfully
      
      
6. Start WSO2AM again & now you can login to API Provider's console
URL - http://localhost:9763/apiprovider/
Username/password - provider1/provider1

Take a note of the fact that there are no APIs published. Next step adds an API & publish it to API store.

7. Run wso2am-1.0.0-SNAPSHOT/samples/TwitterSearch/APIPopulator.sh  (eg: sh APIPopulator.sh)
You will see an output similar to following on the console. Fresh above page & you should be seeing the newly added TwitterSearch API.

{"data" : {}, "message" : "Login successful for user provider1", "error" : "false"}
{"data" : {"cookie" : "JSESSIONID=054B59502342DE9B701832261222359C", "username" : "provider1"}, "message" : {}, "error" : "false"}
{"data" : {"message" : "success"}, "message" : {}, "error" : "false"}
{"data" : {"message" : "success"}, "message" : {}, "error" : "false"}

8. Now let's try to access ** Twitter's search API through our newly deployed API.
Copy, paste following into a new console window & execute it.

curl -k -d "q=wso2&count=5" -H "Authorization :Bearer 9nEQnijLZ0Gi0gZ6a3pZICktVUca" https://localhost:8243/twitter/1.0.0/search

(** NOTE: Authorization :Bearer 9nEQnijLZ0Gi0gZ6a3pZICktVUca is not being utilized in this sample. This sample assumes
an automatic subscription. If one wants, API Store can be accessed @ http://localhost:9763/apistore/ - subscriber1/subscriber1)

On your WSOAM's console, you should see something like following.

[2012-04-02 01:33:22,914]  INFO - LogMediator To: /twitter3/1.0.0/search, MessageID: urn:uuid:c5a39054-7230-40a6-8262-2bbd8709706a,
Direction: request, MESSAGE = Executing default 'fault' sequence, ERROR_CODE = 0, ERROR_MESSAGE = Null throttling
policy returned by Entry : conf:/throttle.policy.xml, Envelope: <?xml version='1.0' encoding='utf-8'?><soapenv:Envelope
xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"><soapenv:Body><xformValues><q>wso2</q><count>5</count>
</xformValues></soapenv:Body></soapenv:Envelope>

This is due to a limitation in this release pack. In following step we are going to remove throttling policy from configuration &
re-run the sample.

9. To remove throttling policy, go to wso2am-1.0.0-SNAPSHOT/repository/deployment/server/synapse-configs/default/api folder
10. Edit TwitterSearch_v1.0.0.xml
11. Comment out following section & save.

   <handlers>
      <handler class="org.wso2.carbon.api.handler.throttle.RestAPIThrottleHandler">
         <property name="id" value="A"/>
         <property name="policyKey" value="conf:/throttle.policy.xml"/>
      </handler>
   </handlers>

On you WSOAM console, messages similar to following will appear.

[2012-04-02 01:36:53,145]  INFO - API Initializing API: T3Search
[2012-04-02 01:36:53,148]  INFO - APIDeployer API: T3Search:v1.0.0 has been updated from the file: /Volumes/Data/projects/c/platform/trunk/products/apimgt/modules/distribution/product/target/wso2am-1.0.0-SNAPSHOT/repository/deployment/server/synapse-configs/default/api/T3Search_v1.0.0.xml
[2012-04-02 01:36:55,149]  INFO - API Destroying API: T3Search


12. Try cURL again.
curl -k -d "q=wso2&count=5" -H "Authorization :Bearer 9nEQnijLZ0Gi0gZ6a3pZICktVUca" https://localhost:8243/twitter/1.0.0/search

You should be able to see search results from Twitter on you console.
eg:
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:twitter="http://api.twitter.com/" xmlns:georss="http://www.georss.org/georss"
xmlns:google="http://base.google.com/ns/1.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/" xml:lang="en-US">
<id>tag:search.twitter.com,2005:search/wso2</id><link type="text/html" href="http://search.twitter.com/search?q=wso2"
rel="alternate" /><link type="application/atom+xm..............


13. Next release of WSO2 AM will bring a more functioning version of this Sample with Throttling policies & key validations.