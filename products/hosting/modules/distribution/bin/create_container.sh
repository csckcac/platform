#/bin/bash
container_home=$1
id=$2
user=$3
host_ip=$4
ip=$5
bridge_ip=$6
template=$7

./container_action $container_home create $id $user g $host_ip $ip 255.255.255.0 $bridge_ip br-lxc "/mnt/lxc" ".ssh/authorized_keys" $template 512M 1G 1024 0-7 "" ""
