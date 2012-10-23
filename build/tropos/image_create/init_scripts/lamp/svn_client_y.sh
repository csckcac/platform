#!/bin/bash

user=chan
passwd=g
url=http://192.168.1.2:80/svn
app=phpapps
logfile=/var/log/wso2-openstack.log

/opt/svn_client.sh $user $passwd $app $url $logfile
