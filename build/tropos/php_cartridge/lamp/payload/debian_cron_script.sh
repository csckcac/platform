#!/bin/bash
tenant="TENANT"
controller_ip="CONTROLLERIP"
app_path="APPPATH"
docroot="DOCUMENTROOT"

pushd /opt/payload
./deploy_apps.sh $tenant $controller_ip $app_path $docroot 
popd

