#!/bin/bash

# ----------------------------------------------------------------------------
#  Copyright 2005-20012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
#!/usr/bin/env bash
export LOG=/var/log/wso2-openstack.log
webroot=/opt/jetty
work=/opt/work
instance_path=/var/lib/cloud/instance
product_name=jetty

# Variables taken from the payload passed to the instance
export TENANT=""
export SVNPASS=""
export SVNURL=""
export APP=""
#export CONTROLLER_IP=""
#export CRON_DURATION=""
echo ---------------------------- >> $LOG

# payload will be copied into ${instance_path}/user-data.txt file
# that file will be renamed as zip file and extract, 



#  copy user-data.txt and rename as payload.zip file
if [ ! -d $work ]; then
    mkdir $work
    cp ${instance_path}/user-data.txt ${instance_path}/payload.zip

	# if error code is 0, there was no error
	if [ "$?" = "0" ]; then
		echo "`date`[server] INFO:retrieved data" >> $LOG
		rm -Rf ${instance_path}/payload
		unzip ${instance_path}/payload.zip -d ${instance_path}/
		# if unzip error code is 0, there was no error
		echo "`date`[server] INFO:Extracted payload" >> $LOG

	else
		echo "`date`[server] ERROR:rc.local : error retrieving user data" >> $LOG
	fi

	chmod -R 0600 ${instance_path}/payload/wso2-key
	cat ${instance_path}/payload/known_hosts >> ~/.ssh/known_hosts
	# Export the variables passed from the payload
	for i in `/usr/bin/ruby /opt/get-launch-params.rb`
	do
            export ${i}
	    echo "export" ${i} >> /home/ubuntu/.bashrc
	done


    # Write a cronjob to execute wso2-openstack-init.sh periodically
    #crontab -l > ./mycron
    #echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    #crontab ./mycron
    #rm ./mycron

    if [[ -d /opt/${product_name} ]]; then
        echo "`date`[server] INFO:Starting jetty server ..." >> $LOG
        /opt/${product_name}/bin/jetty.sh start >> $LOG
        sleep 1
        if [[ "$(pidof java)" ]]; then
            echo "`date`[server] INFO:Jetty server started" >> $LOG
        else
            echo "`date`[server] ERROR:Could not start Jetty server" >> $LOG
            exit 1
        fi
    fi
    if [[ -f /opt/server ]]; then
        /opt/server &
    fi

else
	# Export the variables passed from the payload
	for i in `/usr/bin/ruby /opt/get-launch-params.rb`
	do
	    export ${i}
	done
fi

svn_exec_file=/opt/svn_client_y.sh
sed -i "s#user=.*#user=$TENANT#" $svn_exec_file
sed -i "s#passwd=.*#passwd=$SVNPASS#" $svn_exec_file
sed -i "s#url=.*#url=$SVNURL#" $svn_exec_file
sed -i "s#app=.*#app=$APP#" $svn_exec_file
sed -i "s#logfile=.*#logfile=$LOG#" $svn_exec_file

/opt/svn_client_y.sh
