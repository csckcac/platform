#!/bin/bash

RELEASE="2012-02-25"
SSHD_CONFIG="/etc/ssh/sshd_config"
CONFIG_FILE="conf/stratos_scripting.conf"
RESOURCES_DIR="resources"

action=""
jail_id=""
jail_user=""
jail_password=""
host_machine_ip=""
jail_ip=""
jail_mask=""
jail_gateway=""
jail_root=""
jail_keys_file=""
jail_template=""
jail_bridge=""
jail_memory=""
jail_swap=""
jail_cpu_shares=""
jail_cpuset_cpus=""
svn_url=""
svn_dir=""

function jail_validate {
    if [ -z $action ]; then
        echo "usage: container_action.sh action=<create|destroy|start|stop> <options>"
        exit 1
    fi

    if [[ !($action == "create") && !($action == "destroy") && !($action == "start") && !($action == "stop") ]]; then
        echo "usage: container_action.sh action=<create|destroy|start|stop> <options>"
        exit 1
    fi

    if [[ (-n $action) && ($action == "create") && (-z $jail_id || -z $jail_user || -z $jail_password || -z $host_machine_ip || -z $jail_ip || -z $jail_mask || -z $jail_gateway || -z $jail_bridge || -z $jail_root || -z $jail_keys_file || -z $jail_template || -z $jail_memory || -z $jail_swap || -z $jail_cpu_shares || -z $jail_cpuset_cpus) ]]; then
        echo "usage: container_action.sh action=create jail-id=<jail id> jail-user=<jail_user> jail-password=<password> host-machine-ip=<host machine ip> jail-ip=<ip> jail-mask=<mask> jail-gateway=<gateway> bridge=<bridge> jail-root=<jail_root> jail-keys-file=<jail keys file> template=<template> memory=<memory> swap=<swap> cpu-shares=<cpu shares> cpuset-cpus=<cpuset cpus>"
        echo "usage example: container_action.sh action=create jail-id=12345678 jail-user=yang jail-password=yang host-machine-ip=10.100.1.20 jail-ip=192.168.254.2 jail-mask=255.255.255.0 jail-gateway=192.168.254.1 bridge=br-lxc jail-root=/mnt/lxc template=template-ubuntu-lucid-lamp memory=512M swap=1G cpu-shares=1024 cpuset-cpus=0-7"
        exit 1
    fi

    if [[ (-n $action) && ($action == "destroy") && (-z $jail_id || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=destroy jail-id=<jail_id>"
        echo "usage example: container_action.sh action=destroy jail-id=12345678"
        exit 1
    fi
    
    if [[ (-n $action) && ($action == "start") && (-z $jail_id || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=start jail-id=<jail_id>"
        echo "usage example: container_action.sh action=start jail-id=12345678"
        exit 1
    fi
    
    if [[ (-n $action) && ($action == "stop") && (-z $jail_id || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=stop jail-id=<jail_id>"
        echo "usage example: container_action.sh action=stop jail-id=12345678"
        exit 1
    fi
}

for var in $@; do
    set -- `echo $var | tr '=' ' '`
    key=$1
    value=$2

    if [ $key = "action" ]; then
        action="$value"
        echo "action:" $action
    fi

    if [ $key = "jail-id" ]; then
        jail_id="$value"
        echo "jail id:" $jail_id
    fi

    if [ $key = "jail-user" ]; then
        jail_user="$value"
        echo "user:" $jail_user
    fi
    
    if [ $key = "jail-password" ]; then
        jail_password="$value"
    fi
    
    if [ $key = "host-machine-ip" ]; then
        host_machine_ip="$value"
        echo "host machine ip:" $host_machine_ip
    fi
    
    if [ $key = "jail-ip" ]; then
        jail_ip="$value"
        echo "ip:" $jail_ip
    fi
    
    if [ $key = "jail-mask" ]; then
        jail_mask="$value"
        echo "mask:" $jail_mask
    fi
    
    if [ $key = "jail-gateway" ]; then
        jail_gateway="$value"
        echo "gateway:" $jail_gateway
    fi
    
    if [ $key = "bridge" ]; then
        jail_bridge="$value"
        echo "bridge:" $jail_bridge
    fi

    if [ $key = "jail-root" ]; then
        jail_root="$value"
        echo "jail root:" $jail_root
    fi
    
    if [ $key = "jail-keys-file" ]; then
        jail_keys_file="$value"
        echo "keys file:" $jail_keys_file
    fi
    
    if [ $key = "template" ]; then
        jail_template="$value"
        echo "template:" $jail_template
    fi

    if [ $key = "memory" ]; then
        jail_memory="$value"
        echo "memory:" $jail_memory
    fi

    if [ $key = "swap" ]; then
        jail_swap="$value"
        echo "swap:" $jail_swap
    fi

    if [ $key = "cpu-shares" ]; then
        jail_cpu_shares="$value"
        echo "cpu shares:" $jail_cpu_shares
    fi

    if [ $key = "cpuset-cpus" ]; then
        jail_cpuset_cpus="$value"
        echo "cpuset cpus:" $jail_cpuset_cpus
    fi
    if [ $key = "svn-url" ]; then
        svn_url="$value"
        echo "svn_url:" $svn_url
    fi
    if [ $key = "svn-dir" ]; then
        svn_dir="$value"
        echo "svn_dir:" $svn_dir
    fi
done

jail_validate

if [[ (-n $action) && ($action == "destroy") ]]; then
    echo "# destroy $jail_id"
    lxc-destroy -n $jail_id
    unlink /etc/lxc/$jail_id.conf
    if [ -d $jail_root/$jail_id.rootfs ]; then
        echo 'this deletes all files of the container!'
        rm -rf $jail_root/$jail_id.*
        rm -rf ./$jail_id
    fi
    exit 0
fi

if [[ (-n $action) && ($action == "start") ]]; then
    echo "# start $jail_id container"
    lxc-start -n $jail_id -d -o $jail_root/$jail_id.start.log
    exit 0
fi

if [[ (-n $action) && ($action == "stop") ]]; then
    echo "# stop $jail_id container"
    lxc-stop -n $jail_id -o $jail_root/$jail_id.stop.log
    exit 0
fi

#create container for the user
if [ -d "$jail_id" ]; then
    echo "jail setup directory already exists!!!"
else
    cp -rf ./container_setup_base "$jail_id"
fi
cd "$jail_id"

sed -i "s#temp_base#$jail_root#" ./lxc-ubuntu-x.conf
sed -i "s/temp_user/$jail_user/" ./lxc-ubuntu-x.conf
sed -i "s/temp_password/$jail_password/" ./lxc-ubuntu-x.conf
sed -i "s/temp_host_machine_ip/$host_machine_ip/" ./lxc-ubuntu-x.conf
sed -i "s#temp_keys#$jail_keys_file#" ./lxc-ubuntu-x.conf

sed -i "s/temp_ip/$jail_ip/" ./hooks.d/configure_network
sed -i "s/temp_mask/$jail_mask/" ./hooks.d/configure_network
sed -i "s/temp_gateway/$jail_gateway/" ./hooks.d/configure_network

sed -i "s/temp_bridge/$jail_bridge/" ./hooks.d/configure_lxc
sed -i "s/temp_memory/$jail_memory/" ./hooks.d/configure_lxc
sed -i "s/temp_swap/$jail_swap/" ./hooks.d/configure_lxc
sed -i "s/temp_cpu_shares/$jail_cpu_shares/" ./hooks.d/configure_lxc
sed -i "s/temp_cpuset_cpus/$jail_cpuset_cpus/" ./hooks.d/configure_lxc
sed -i "s#temp_svn_url#$svn_url#" ./hooks.d/configure_lxc
sed -i "s#temp_svn_dir#$svn_dir#" ./hooks.d/configure_lxc

./lxc-ubuntu-x $jail_id $jail_root/$jail_id.rootfs $jail_template
cd ..
echo
echo Release: $RELEASE
echo


exit

