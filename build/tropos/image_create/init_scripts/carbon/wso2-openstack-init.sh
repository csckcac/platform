#!/usr/bin/env bash
export LOG=/var/log/wso2-openstack.log
export JAVA_HOME=/opt/java/
instance_path=/var/lib/cloud/instance

# Variables taken from the payload passed to the instance
export REPO_PATH_S3=""
export CONF_PATH_S3=""
export REPO_PATH_EBS=""
export CONF_PATH_EBS=""
export PRODUCT_NAME=""
export CONTROLLER_IP=""
CRON_DURATION=2
PUBLIC_IP=""


#Check whether if any java processes are running
if [[ "$(pidof java)" ]]; then
   # process was found
   echo "An already running java process is found. Exiting..." >> $LOG
    if [[ "$(pidof cron)" ]]; then
        crontab -r
    fi
   exit 0;
else
   # process not found
   echo "No java process found. Coninue script" >> $LOG
fi

echo ---------------------------- >> $LOG

if [ ! -d ${instance_path}/payload ]; then
    # payload will be copied into ${instance_path}/user-data.txt file
    # that file will be renamed as zip file and extract, 

    # copy user-data.txt and rename as payload.zip file
    cp ${instance_path}/user-data.txt ${instance_path}/payload.zip

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

    # Write a cronjob to execute wso2-openstack-init.sh periodically until public ip is assigned
    crontab -l > ./mycron
    echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    crontab ./mycron
    rm ./mycron

    for i in `/usr/bin/ruby /opt/get-launch-params.rb`
    do
        echo "export" ${i} >> /home/ubuntu/.bashrc
    done

fi


wget http://169.254.169.254/latest/meta-data/public-ipv4
files="`cat public-ipv4`"
if [[ -z ${files} ]]; then
    echo "getting public ip" >> $LOG
    for i in {1..30}
    do
      rm -f ./public-ipv4
      wget http://169.254.169.254/latest/meta-data/public-ipv4
      files="`cat public-ipv4`"
      if [ -z $files ]; then
          echo "Public ip is not yet assigned. Wait and continue for $i the time ..." >> $LOG
          sleep 1
      else
          echo "Public ip assigned" >> $LOG
          break
      fi
    done

    if [ -z $files ]; then
      echo "Public ip is not yet assigned. Exiting ..." >> $LOG
      exit 0
    fi
    for x in $files
    do
        PUBLIC_IP="$x"
    done

fi

echo wget the ipv4 >> $LOG
echo "$files" >> $LOG

echo done_ public ip >> $LOG
echo $PUBLIC_IP >> $LOG

temp_private_ip=`ifconfig eth0 | grep inet | grep -v inet6 | cut -d ":" -f 2 | cut -d " " -f 1`
PRIVATE_IP=$temp_private_ip

for i in `/usr/bin/ruby /opt/get-launch-params.rb`
do
    export ${i}
done

if [[ ! -d /opt/${PRODUCT_NAME} ]]; then
    if [[ "$(pidof scp)" || "$(pidof unzip)" ]]; then
        echo "Copying and unarchiving the products already begun. So exiting ..." >> $LOG
        exit 0
    fi
    if [[ ! -f /opt/${PRODUCT_NAME}.zip ]]; then
    	echo "Copying the product from the controller" >> $LOG
    	echo "scp -i ${instance_path}/payload/wso2-key root@${CONTROLLER_IP}:/opt/${PRODUCT_NAME}.zip /opt/" >> $LOG
    	scp -i ${instance_path}/payload/wso2-key root@${CONTROLLER_IP}:/opt/${PRODUCT_NAME}.zip /opt/
    	echo "Unarchiving the pack ..." >> $LOG
    	unzip /opt/${PRODUCT_NAME}.zip -d /opt/ 
        if [ "$?" = "0" ]; then
            echo Extracted product >> $LOG
        else
            echo product zip file is corrupted >> $LOG
            exit 0
        fi
    fi
fi

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

if [[ -d /opt/${PRODUCT_NAME} ]]; then
	echo "Starting carbon server ..." >> $LOG
	nohup /opt/${PRODUCT_NAME}/bin/wso2server.sh & >> $LOG
	sleep 1
	if [[ "$(pidof java)" ]]; then
	    echo "Carbon server started" >> $LOG
	    crontab -r
    	    rm -f /opt/${PRODUCT_NAME}.zip
	fi
else

    echo "Carbon server is not started yet" >> $LOG
fi


