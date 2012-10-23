#!/bin/bash
rm -rf /tmp/__upload

./imageupload.sh -a admin -p openstack -t demo -C 172.20.0.1 -x amd64 -y ubuntu -w 12.04 -z /opt/lxc/original_image/ubuntu-12.04-server-cloudimg-amd64.tar.gz -n ubuntu-12.04-server-jetty-cloudimg-amd64
