#!/bin/bash
rm -rf /tmp/__upload

#./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.16.0.1 -x amd64 -y ubuntu -w 10.04 -z 
#./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.16.0.1 -x amd64 -y ubuntu -w 11.04 -z 
./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.15.0.1 -x amd64 -y ubuntu -w 12.04 -z ubuntu-12.04-server-cloudimg-amd64.tar.gz
