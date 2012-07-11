#!/bin/bash
rm -rf /tmp/__upload

./wso2imageupload.sh -a admin -p openstack -t demo -C 172.15.0.1 -x amd64 -y ubuntu -w 12.04 -z ubuntu-12.04-server-cloudimg-amd64.tar.gz
