#!/bin/bash
tenant=$1
controller_ip=$2
app_path=$3
docroot=$4

rm -rf $docroot/*
echo $controller_ip >> /tmp/y
echo $app_path >> /tmp/y
echo $tenant >> /tmp/y
echo $docroot >> /tmp/y
scp -i ./wso2-key root@$controller_ip:$app_path/$tenant/* $docroot/
pushd $docroot
for f in *.tar.gz; do tar -zxf $f;rm -f $f; done
popd

