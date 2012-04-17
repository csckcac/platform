---------------------------------------------------------------------------------
	Requirements
---------------------------------------------------------------------------------
 
	1. WSO2 BAM-2.0.0 ALPHA2 
	2. Ant build tool
	3. Java Runtime Environment

---------------------------------------------------------------------------------
Configure BAM
---------------------------------------------------------------------------------

	* First copy bamClient directory into BAM_HOME
        * Run "ant makejar" command and this will copy the class analyzer into the repository/components/lib folder
	* Change the port offset to 1 (You have to edit the repository/conf/carbon.xml)
	* Start WSO2 BAM server
	* Again go to bamClient directory and run "ant" command (It takes arround 3 min to complete the task)
	* Finally you can see the message - "BAM configured successfully for collecting API stats" 

