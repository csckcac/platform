#!/bin/bash
#For testing purposes only(without cron)
pushd /opt
chmod -R 755 ./payload
chmod -R 0600 ./payload/wso2-key
cat ./payload/known_hosts >> ~/.ssh/known_hosts
/opt/payload/debian_cron_script.sh
popd
