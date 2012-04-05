This file explains how to use the Web service API of the registry to upload a file.
===================================================================================

This is useful when uploading large files, since atom based remote registry API is not that efficient in uploading large files.

Before getting the sample to work you have to run ant from GREG_HOME/bin and start the registry server.


Steps to get this sample to work:
--------------------------------

1. Run the ant build file. i.e.( ant upload)
 
2. Provide "Keystore path", which is provided with bin distribution "GREG_HOME/resources/security/client-truststore.jks".

3. Provide "Filepath", which you want to upload.

4. Provide "Path", where you need to upload your file system. 


Browsing to the resource
------------------------
Go to admin console "Resource Browser" menu. Where you can find that your resource will available with the name called "testpath".

Note:- You need "axis2_ant_plugin.jar(1.4/above)(http://ws.apache.org/axis2/tools/index.html)" to run this sample. Keep this jar inside the  "GREG_HOME/webapps/ROOT/WEB-INF/plugins/common"
	folder.
