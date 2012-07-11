#!/bin/bash

#tenant="TENANT"
#controller_ip="CONTROLLERIP"
#app_path="APPPATH"
#docroot="DOCUMENTROOT"
#duration=DURATION

tenant="foo"
controller_ip="172.15.0.1"
app_path="/opt/wso2tropos-agent-1.0.0-SNAPSHOT/repository/deployment/server/phpapps"
docroot="/var/www"
duration=5

pushd /opt/payload
./deploy_apps.sh $tenant $controller_ip $app_path $docroot $duration 
popd

