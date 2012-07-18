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
docroot=/var/www
work=/opt/work
instance_path=/var/lib/cloud/instance

# Variables taken from the payload passed to the instance
export TENANT=""
export APP_PATH=""
export CRON_DURATION=""
export CONTROLLER_IP=""

echo ---------------------------- >> $LOG

# payload will be copied into ${instance_path}/user-data.txt file
# that file will be renamed as zip file and extract, 

#  copy user-data.txt and rename as payload.zip file
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

# Export the variables passed from the payload
for i in `/usr/bin/ruby /opt/get-launch-params.rb`
do
  export ${i}
  echo "export" ${i} >> /home/ubuntu/.bashrc
done

# Write a cronjob to execute wso2-openstack-init.sh periodically
if [ ! -d $work ]; then
    crontab -l > ./mycron
    echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    crontab ./mycron
    rm ./mycron
	mkdir $work
fi

pushd $work
ssh -i ${instance_path}/wso2-key root@$CONTROLLER_IP "cd $APP_PATH/$TENANT; ls ./" | grep "zip" > ./app_list
if [ ! -e ./app_list.prev ]; then
    scp -i ${instance_path}/wso2-key root@$CONTROLLER_IP:$APP_PATH/$TENANT/*.zip ./
    for a in *.zip;
    do
        if [ -e $a ]; then
            unzip -q $a;rm -f $a;
        fi
    	b=${a%".zip"}
        if [ -d $docroot/$b ]; then
    	    rm -rf $docroot/$b
        fi
    	mv $b $docroot/
        echo "added $docroot/$b"
    done
else
    x=`cat ./app_list.prev`
    y=`cat ./app_list`
    z=0
    for f in $x;
    do
        for g in $y;
        do
            if [ $g == $f ]; then
                z=1; break;

            fi
        done
        if [ $z == 0 ]; then
            h=${f%".zip"}
            if [ -d $docroot/$h ]; then
                rm -rf $docroot/$h
                echo "deleted $docroot/$h"
            fi
        fi
        z=0
    done
fi

cp -f ./app_list ./app_list.prev

ssh -i ${instance_path}/payload/wso2-key root@$CONTROLLER_IP "cd $APP_PATH/$TENANT;find ./ -mmin $CRON_DURATION" | grep "zip" > ./updated_app_list
w=`cat ./updated_app_list`
for i in $w; 
do 
    scp -i ${instance_path}/payload/wso2-key root@$CONTROLLER_IP:$APP_PATH/$TENANT/$i ./
    unzip -q $i;rm -f $i;
    j=${i%".zip"}
    if [ -d $docroot/$j ]; then
    	rm -rf $docroot/$j
    fi
    mv $j $docroot/
    echo "added $docroot/$j"
done
popd

