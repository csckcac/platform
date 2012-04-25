---------------------------------------------------------------------------------
	Requirements
---------------------------------------------------------------------------------
1. WSO2 BAM-2.0.0 ALPHA2 
2. Apache Ant
3. Java Runtime Environment

---------------------------------------------------------------------------------
Configuring BAM
---------------------------------------------------------------------------------

1.Copy StatClient directory into BAM_HOME
2.Run "ant make_analyzer_jar" command. This will compile & copy API Stat analyzer into the repository/components/lib folder
3.Change port offset to 1 by editting the repository/conf/carbon.xml
4.Start WSO2 BAM server
5.Go to StatClient directory and run "ant initalize_column_family_datastore" command (This will take around 3 mins to complete. You need to have Internet connection)
6.Finally you can see the message - "BAM configured successfully for collecting API stats"
7.Following entry needs to be manually added to each API configuration element in 'repository/deployment/server/synapse-configs/default/synapse.xml'

<handler class="org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler" />

This will start pushing API usage statistics to an external BAM instance configured via repository/conf/amConfig.xml.
 

