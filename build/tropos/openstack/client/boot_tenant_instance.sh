#!/bin/bash
image=$1
tenant=$2
cartridge=$3
if [ -z $image ]; then
    echo "Please pass an image id"
    exit 1
fi
if [ -z $tenant ]; then
    echo "Please provide a tenant"
    exit 1
fi
if [ -z $cartridge ]; then
    echo "Please provide cartridge name"
    exit 1
fi
rm -rf ./payload
unzip payload.zip
sed -i "s#TENANT=[a-zA-Z0-9]*,#TENANT=$tenant,#" ./payload/launch-params
zip -rq ./payload.zip ./payload
nova boot --key_name=nova-key --flavor=1 --image=$image wso2-$cartridge-domain-$tenant-1693 --user_data=/root/client/payload.zip
