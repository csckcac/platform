#!/bin/bash

RELEASE="2012-02-25"
SSHD_CONFIG="/etc/ssh/sshd_config"
CONFIG_FILE="conf/stratos_scripting.conf"
RESOURCES_DIR="resources"

action=""
jail_user=""
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

    if [[ (-n $action) && ($action == "create") && (-z $jail_user || -z $jail_password || -z $jail_ip || -z $jail_mask || -z $jail_gateway || -z $jail_bridge || -z $jail_root || -z $jail_keys_file || -z $jail_template || -z $jail_memory || -z $jail_swap || -z $jail_cpu_shares || -z $jail_cpuset_cpus) ]]; then
        echo "usage: container_action.sh action=create jail-user=<jail_user> jail-password=<password> jail-ip=<ip> jail-mask=<mask> jail-gateway=<gateway> bridge=<bridge> jail-root=<jail_root> jail-keys-file=<jail keys file> template=<template> memory=<memory> swap=<swap> cpu-shares=<cpu shares> cpuset-cpus=<cpuset cpus>"
        echo "usage example: container_action.sh action=create jail-user=yang jail-password=yang jail-ip=192.168.254.2 jail-mask=255.255.255.0 jail-gateway=192.168.254.1 bridge=br-lxc jail-root=/mnt/lxc template=template-ubuntu-lucid-lamp memory=512M swap=1G cpu-shares=1024 cpuset-cpus=0-7"
        exit 1
    fi

    if [[ (-n $action) && ($action == "destroy") && (-z $jail_user || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=destroy jail-user=<jail_user>"
        echo "usage example: container_action.sh action=destroy jail-user=yang"
        exit 1
    fi
    
    if [[ (-n $action) && ($action == "start") && (-z $jail_user || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=start jail-user=<jail_user>"
        echo "usage example: container_action.sh action=start jail-user=yang"
        exit 1
    fi
    
    if [[ (-n $action) && ($action == "stop") && (-z $jail_user || -z $jail_root) ]]; then
        echo "usage: container_action.sh action=stop jail-user=<jail_user>"
        echo "usage example: container_action.sh action=stop jail-user=yang"
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

    if [ $key = "jail-user" ]; then
        jail_user="$value"
        echo "user:" $jail_user
    fi
    
    if [ $key = "jail-password" ]; then
        jail_password="$value"
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
    echo "# destroy $jail_user"
    lxc-destroy -n $jail_user
    unlink /etc/lxc/$jail_user.conf
    if [ -d $jail_root/$jail_user.rootfs ]; then
        echo 'this deletes all files of the container!'
        rm -rf $jail_root/$jail_user.*
    fi
    exit 0
fi

if [[ (-n $action) && ($action == "start") ]]; then
    echo "# start $jail_user container"
    lxc-start -n $jail_user -d -o $jail_root/$jail_user.start.log
    exit 0
fi

if [[ (-n $action) && ($action == "stop") ]]; then
    echo "# stop $jail_user container"
    lxc-stop -n $jail_user -o $jail_root/$jail_user.stop.log
    exit 0
fi

#create container for the user
if [ -d $jail_template_$jail_user ]; then
    echo "jail setup directory already exists!!!"
else
    cp -rf ./container_setup_base $jail_template_$jail_user
fi
cd $jail_template_$jail_user

sed -i "s#temp_base#$jail_root#" ./lxc-ubuntu-x.conf
sed -i "s/temp_user/$jail_user/" ./lxc-ubuntu-x.conf
sed -i "s/temp_password/$jail_password/" ./lxc-ubuntu-x.conf
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

./lxc-ubuntu-x $jail_user $jail_root/$jail_user.rootfs $jail_template
cd ..
echo
echo Release: $RELEASE
echo


exit

