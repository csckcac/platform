#!/usr/bin/env bash
export LOG=/var/log/wso2-openstack.log
export JAVA_HOME=/opt/jdk1.6.0_24/
instance_path=/var/lib/cloud/instance

# Variables taken from the payload passed to the instance
export REPO_PATH_S3=""
export CONF_PATH_S3=""
export REPO_PATH_EBS=""
export CONF_PATH_EBS=""
export PRODUCT_NAME=""
export CONTROLLER_IP=""


#Check whether if any java processes are running

if [ "$(pidof java)" ]; then
   # process was found
   echo "An already running java process is found. Exiting..." >> $LOG
   exit 0;
else
   # process not found
   echo "No java process found. Coninue script" >> $LOG
fi

echo ---------------------------- >> $LOG

# payload will be copied into ${instance_path}/user-data.txt file
# that file will be renamed as zip file and extract, 

# copy user-data.txt and rename as payload.zip file
cp ${instance_path}/user-data.txt ${instance_path}/payload.zip
unzip ${instance_path}/payload.zip -d ${instance_path}/

# if error code is 0, there was no error
if [ "$?" = "0" ]; then
	echo retrieved data >> $LOG
	rm -Rf ${instance_path}/payload
	unzip ${instance_path}/payload.zip -d ${instance_path}/
	# if unzip error code is 0, there was no error
	if [ "$?" = "0" ]; then
		echo Extracted payload >> $LOG
	else
		echo rc.local : payload.zip is corrupted >> $LOG
	fi

else
	echo rc.local : error retrieving user data >> $LOG
fi

chmod -R 0600 ${instance_path}/payload/wso2-key
cat ${instance_path}/payload/known_hosts >> ~/.ssh/known_hosts

#get the public and private ips
wget http://169.254.169.254/latest/meta-data/public-ipv4

files="`cat public-ipv4`"

echo wget the ipv4 >> $LOG
echo "$files" >> $LOG

for x in $files
do
    PUBLIC_IP="$x"
done

echo done_ public ip >> $LOG
echo $PUBLIC_IP >> $LOG

temp_private_ip=`ifconfig eth0 | grep inet | grep -v inet6 | cut -d ":" -f 2 | cut -d " " -f 1`
PRIVATE_IP=$temp_private_ip


for i in `/usr/bin/ruby /opt/scripts/get-launch-params.rb`
do
  export ${i}
  echo "export" ${i} >> /home/ubuntu/.bashrc
done

# Copying the product from the controller
scp -i ${instance_path}/payload/wso2-key root@${CONTROLLER_IP}:/opt/${PRODUCT_NAME}.zip /opt/

if [ "$ADMIN_USERNAME" = "" ]; then
	echo Launching with default admin username >> $LOG
else 
	cd /opt/${PRODUCT_NAME}/repository/conf
	find . -name "*user-mgt.xml" | xargs sed -i "s/<UserName>admin<\/UserName>/<UserName>$ADMIN_USERNAME<\/UserName>/g"
fi

if [ "$ADMIN_PASSWORD" = "" ]; then
	echo Launching with default admin password >> $LOG
else 
	cd /opt/${PRODUCT_NAME}/repository/conf
	find . -name "*user-mgt.xml" | xargs sed -i "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g"
fi

# Modifying axis2.xml file member
	cd /opt/${PRODUCT_NAME}/repository/conf/axis2

	find . -name "axis2.xml" | xargs sed -i "s/<hostName>member_host_name<\/hostName>/<hostName>$MEMBER_HOST<\/hostName>/g"
	find . -name "axis2.xml" | xargs sed -i "s/<port>member_port<\/port>/<port>$MEMBER_PORT<\/port>/g"

        find . -name "axis2.xml" | xargs sed -i "s/local_member_host<\/parameter>/$PUBLIC_IP<\/parameter>/g"
        find . -name "axis2.xml" | xargs sed -i "s/local_member_bind_address<\/parameter>/$PRIVATE_IP<\/parameter>/g"


nohup /opt/${PRODUCT_NAME}/bin/wso2server.sh & >> $LOG
