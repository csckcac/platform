#!/bin/bash
image=$1
name=$2
if [ -z $image ]; then
    echo "Please pass an image id"
    exit 1
fi
nova boot --key_name=wso2 --flavor=1 --image=$image $name --user_data=/root/client/payload.zip
