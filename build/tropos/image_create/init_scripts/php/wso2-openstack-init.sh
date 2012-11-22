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
RETRY_COUNT=30
SLEEP_DURATION=30

if [ ! -d ${instance_path}/payload ]; then

    echo "creating payload dir ... " >> $LOG
    mkdir ${instance_path}/payload
    echo "payload dir created ... " >> $LOG
    cp ${instance_path}/user-data.txt ${instance_path}/payload/user-data.zip
    echo "payload copied  ... "  >> $LOG
    unzip -d ${instance_path}/payload ${instance_path}/payload/user-data.zip
    echo "unzippeddd..." >> $LOG

    for i in `/usr/bin/ruby /opt/get-launch-params.rb`
    do
    echo "exporting to bashrc $i ... " >> $LOG
        echo "export" ${i} >> /home/ubuntu/.bashrc
    done
    source /home/ubuntu/.bashrc
    # Write a cronjob to execute wso2-openstack-init.sh periodically until public ip is assigned
    #crontab -l > ./mycron
    #echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    #crontab ./mycron
    #rm ./mycron

fi


echo ---------------------------- >> $LOG
echo "getting public ip from metadata service" >> $LOG

wget http://169.254.169.254/latest/meta-data/public-ipv4
files="`cat public-ipv4`"
if [[ -z ${files} ]]; then
    echo "getting public ip" >> $LOG
    for i in {1..$RETRY_COUNT}
    do
      rm -f ./public-ipv4
      wget http://169.254.169.254/latest/meta-data/public-ipv4
      files="`cat public-ipv4`"
      if [ -z $files ]; then
          echo "Public ip is not yet assigned. Wait and continue for $i the time ..." >> $LOG
          sleep $SLEEP_DURATION
      else
          echo "Public ip assigned" >> $LOG
          #crontab -r
          break
      fi
    done

    if [ -z $files ]; then
      echo "Public ip is not yet assigned. So shutdown the instance and exit" >> $LOG
      /sbin/shutdown -h now
      exit 0
    fi
    for x in $files
    do
        PUBLIC_IP="$x"
    done


else 
   PUBLIC_IP="$files"
   #crontab -r
fi


for i in `/usr/bin/ruby /opt/get-launch-params.rb`
do
    export ${i}
done


echo "Logging sys variables .. PUBLIC_IP:$PUBLIC_IP, HOST_NAME:$HOST_NAME, KEY:$KEY, PRIMARY_PORT:$PRIMARY_PORT, PROXY_PORT:$PROXY_PORT, TYPE:$TYPE " >> $LOG

echo "
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:agen="http://agent.cartridge.carbon.wso2.org">
  <soapenv:Header/>
  <soapenv:Body>
     <agen:register>
        <registrant>
           <hostName>${HOST_NAME}</hostName>
           <key>${KEY}</key>
          <maxInstanceCount>${MAX}</maxInstanceCount>
          <minInstanceCount>${MIN}</minInstanceCount>
           <portMappings>
              <primaryPort>${PRIMARY_PORT}</primaryPort>
              <proxyPort>${PROXY_PORT}</proxyPort>
              <type>${TYPE}</type>
           </portMappings>
        <remoteHost>${PUBLIC_IP}</remoteHost>
           <service>${SERVICE}</service>
           <tenantId>${TENANT_ID}</tenantId>
        </registrant>
     </agen:register>
  </soapenv:Body>
</soapenv:Envelope>
" > /etc/agent/conf/request.xml


echo "Private Key....Copying to .ssh  " >> $LOG

cp ${instance_path}/payload/id_rsa /root/.ssh/id_rsa
chmod 0600 /root/.ssh/id_rsa
echo "StrictHostKeyChecking no" >> /root/.ssh/config

echo "Sending register request to Cartridge agent service" >> $LOG

for i in {1..30}
do
    curl -X POST -H "Content-Type: text/xml" -d @/etc/agent/conf/request.xml --silent --output /dev/null "$CARTRIDGE_AGENT_EPR"
    ret=$?
    if [[ $ret -eq 2  ]]; then
        echo "[curl] Failed to initialize" >> $LOG
    elif [[ $ret -eq 5 || $ret -eq 6 || $ret -eq 7  ]]; then
        echo "[curl] Resolving host failed" >> $LOG
    elif [[ $ret -eq 28 ]]; then
        echo "[curl] Operation timeout" >> $LOG
    elif [[ $ret -eq 55 || $ret -eq 56 ]]; then
        echo "[curl] Failed sending/receiving network data" >> $LOG
    elif [[ $ret -eq 28 ]]; then
        echo "Operation timeout" >> $LOG
    else
        break
    fi
    sleep $SLEEP_DURATION
done
if [[ $ret -gt 0 ]]; then
    echo "Sending cluster join message failed. So shutdown instance and exit" >> $LOG
    /sbin/shutdown -h now
    exit 0
fi

echo "Git repo sync" >> $LOG

# If repo is available do a git pull, else clone
echo "#!/bin/bash
if [ -d \"$APP_PATH/.git\" ]; then
    cd ${APP_PATH}
    sudo git pull
else
    sudo git clone git@${GIT_HOST_NAME}:${GIT_REPO} ${APP_PATH}
    i=1
    while [ ! -d \"$APP_PATH/.git\" ]
        do
        sleep $SLEEP_DURATION
        sudo git clone git@${GIT_HOST_NAME}:${GIT_REPO} ${APP_PATH}
        if ( \$i=\"3\")
        then
                break              #Abandon the while lopp.
        fi
        (( i++ ))
    done


fi" > /opt/git.sh
echo "File created.." >> $LOG
chmod 755 /opt/git.sh
echo "Executing.." >> $LOG
/opt/git.sh
echo "Executed.." >> $LOG

# ========================== // End of script ===========================================================
