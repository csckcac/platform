#!/bin/bash
rm -rf /tmp/__upload

#./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.16.0.1 -x amd64 -y ubuntu -z lucid -w 10.04
#./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.16.0.1 -x amd64 -y ubuntu -z natty -w 11.04
./upload_ubuntu.sh -a admin -p openstack -t demo -C 172.16.0.1 -x amd64 -y ubuntu -z precise -w 12.04
