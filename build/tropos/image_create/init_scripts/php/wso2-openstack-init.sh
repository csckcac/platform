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
export LOG=/var/log/wso2-openstack.log
instance_path=/var/lib/cloud/instance
PUBLIC_IP=""
KEY=`uuidgen`
CRON_DURATION=1



if [ ! -d ${instance_path}/payload ]; then
    echo "creating payload dir ... " >> $LOG
    mkdir ${instance_path}/payload
    echo "payload dir created ... " >> $LOG
    # payload will be copied into ${instance_path}/payload/launch-params file
    cp ${instance_path}/user-data.txt ${instance_path}/payload/launch-params
    echo "payload copied  ... " >> $LOG
    for i in `/usr/bin/ruby /opt/get-launch-params.rb`
    do
    echo "exporting to bashrc $i ... " >> $LOG
        echo "export" ${i} >> /home/ubuntu/.bashrc
    done
    source /home/ubuntu/.bashrc
    # Write a cronjob to execute wso2-openstack-init.sh periodically until public ip is assigned
    crontab -l > ./mycron
    echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    crontab ./mycron
    rm ./mycron

fi

echo ---------------------------- >> $LOG
echo "getting public ip from metadata service" >> $LOG

wget http://169.254.169.254/latest/meta-data/public-ipv4
files="`cat public-ipv4`"
if [[ -z ${files} ]]; then
    echo "getting public ip. If fail retry 30 times" >> $LOG
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
          crontab -r
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


else 
    PUBLIC_IP="$files"
    crontab -r
fi


for i in `/usr/bin/ruby /opt/get-launch-params.rb`
do
    export ${i}
done



cd /etc/agent/conf

#source /home/ubuntu/.bashrc

#log variables..
### Temp hard-coding vairables..
#HOST_NAME="php.cloud-test.wso2.com"
#PRIMARY_PORT="80"
#PROXY_PORT="8280"
#TYPE="http"
#SERVICE="php"
#TENANT_ID="6"
#CARTRIDGE_AGENT_EPR="http://172.17.0.1:6060/axis2/services/CartridgeAgentService"

echo "Logging sys variables .. PUBLIC_IP:$PUBLIC_IP, HOST_NAME:$HOST_NAME, KEY:$KEY, PRIMARY_PORT:$PRIMARY_PORT, PROXY_PORT:$PROXY_PORT, TYPE:$TYPE " >> $LOG

find . -name "request.xml" | xargs sed -i "s/<remoteHost>remote_host<\/remoteHost>/<remoteHost>$PUBLIC_IP<\/remoteHost>/g"
find . -name "request.xml" | xargs sed -i "s/<hostName>host_name<\/hostName>/<hostName>$HOST_NAME<\/hostName>/g"
find . -name "request.xml" | xargs sed -i "s/<key>key<\/key>/<key>$KEY<\/key>/g"
find . -name "request.xml" | xargs sed -i "s/<primaryPort>primary_port<\/primaryPort>/<primaryPort>$PRIMARY_PORT<\/primaryPort>/g"
find . -name "request.xml" | xargs sed -i "s/<proxyPort>proxy_port<\/proxyPort>/<proxyPort>$PROXY_PORT<\/proxyPort>/g"
find . -name "request.xml" | xargs sed -i "s/<type>type<\/type>/<type>$TYPE<\/type>/g"
find . -name "request.xml" | xargs sed -i "s/<service>service<\/service>/<service>$SERVICE<\/service>/g"
find . -name "request.xml" | xargs sed -i "s/<tenantId>tenant_id<\/tenantId>/<tenantId>$TENANT_ID<\/tenantId>/g"


curl -X POST -H "Content-Type: text/xml"   -d @/etc/agent/conf/request.xml "$CARTRIDGE_AGENT_EPR"  -v 2>> $LOG
