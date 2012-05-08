#/bin/bash
container_home=$1
id=$2
user=$3
ip=$4
bridge_ip=$5

./container_action $container_home create $id $user g $ip 255.255.255.0 $bridge_ip br-lxc "/mnt/lxc" ".ssh/authorized_keys" template-wso2-carbon-server 512M 1G 1024 0-7 "" ""
