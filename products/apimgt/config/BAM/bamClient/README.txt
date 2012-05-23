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
5.Go to StatClient directory and run "ant initalize_column_family_datastore" command (This will take around 10 mins to complete. You need to have Internet connection)
6.Finally you can see the message - "BAM configured successfully for collecting API stats"
7.Now start deploying samples & invoking them. Your invocation statistics should be visible under,
- Statistics -> My APIs
- APIs -> My APIs -> [YOUR API SAMPLE NAME] -> 'Versions' tab

