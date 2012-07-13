---------------------------------------------------------------------------------
	Requirements
---------------------------------------------------------------------------------
1. WSO2 BAM-2.0.0 ALPHA3 
2. Apache Ant
3. Java Runtime Environment

---------------------------------------------------------------------------------
Configuring BAM
---------------------------------------------------------------------------------

1.Copy StatClient directory into BAM_HOME
2.Change port offset to 1 by editing the repository/conf/carbon.xml
3.Start WSO2 BAM server
4.Go to StatClient directory and run "ant initialize_column_family_datastore" command (You need to have an Internet connection.)
5.Finally you can see the message - "BAM configured successfully for collecting API stats"
6.To enable API statistics collection you need to configure the following properties in the api-manager.xml configuration file.
   
   <!--
	Enable/Disable the API usage tracker.
   -->
	<Enabled>true</Enabled>

   <!--
	JDBC URL to query remote JDBC database 
	E.g. jdbc:h2:/home/wso2/wso2bam-2.0.0-SNAPSHOT/repository/database/APIMGTSTATS_DB;AUTO_SERVER=TRUE
   -->
	<JDBCUrl>jdbc:h2:<!-- Full path to WSO2BAM home directory -->/repository/database/APIMGTSTATS_DB;AUTO_SERVER=TRUE</JDBCUrl>


7.Now start deploying samples & invoking them. Your invocation statistics should be visible under,
- APIs -> All Statistics
- My APIs -> Statistics 
- APIs -> All -> [YOUR API SAMPLE NAME] -> 'Versions' tab
- APIs -> All -> [YOUR API SAMPLE NAME] -> 'Users' tab
