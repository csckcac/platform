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
work=/opt/work
instance_path=/var/lib/cloud/instance

# Variables taken from the payload passed to the instance
export TENANT=""
export SVNPASS=""
export SVNURL=""
export APP=""
#export CONTROLLER_IP=""
export CRON_DURATION="1"
echo ---------------------------- >> $LOG
echo "sending the cluster join message" >> $LOG
curl -X POST -H "Content-Type: text/xml"   -d @request.xml "http://172.15.0.1:6060/axis2/services/CartridgeAgentService"  -v 2>> $LOG

if [ ! -d $work ]; then
    mkdir $work
    # Write a cronjob to execute wso2-openstack-init.sh periodically
    crontab -l > ./mycron
    echo "*/1 * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    crontab ./mycron
    rm ./mycron
fi


