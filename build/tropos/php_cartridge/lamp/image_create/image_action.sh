#!/bin/bash

RELEASE="2012-02-25"
SSHD_CONFIG="/etc/ssh/sshd_config"
CONFIG_FILE="conf/stratos_scripting.conf"
RESOURCES_DIR="resources"

action=""
image_id=""
image_user=""
image_password=""
ip1=""
ip2=""
work_dir=""
image_root=""
image_keys_file=""
image_template=""
svn_url=""
svn_dir=""

function image_validate {
    if [ -z $action ]; then
        echo "usage: image_action.sh action=<mount|create|unmount> <options>"
        exit 1
    fi

    if [[ !($action == "mount") && !($action == "create") && !($action == "unmount") ]]; then
        echo "usage: image_action.sh action=<mount|create|unmount> <options>"
        exit 1
    fi

    if [[ (-n $action) && ($action == "create") && (-z $image_id || -z $image_user || -z $image_password || -z $ip1 || -z $ip2 || -z $image_root || -z $image_keys_file || -z $image_template ) ]]; then
        echo "usage: image_action.sh action=create image-id=<image id> image-user=<image_user> image-password=<password> ip1=<ip1> ip2=<ip2> image-mask=<mask> image-root=<image_root> image-keys-file=<image keys file> template=<template> "
        echo "usage example: image_action.sh action=create image-id=12345678 image-user=yang image-password=yang ip1=10.100.1.20 ip2=192.168.254.2 image-root=/opt/lxc template=lamp "
        exit 1
    fi

    if [[ (-n $action) && ($action == "unmount") && (-z $image_id || -z $image_root) ]]; then
        echo "usage: image_action.sh action=unmount image-id=<image_id> image-root=<image_root>"
        echo "usage example: image_action.sh action=unmount image-id=12345678 image-root=/opt/lxc"
        exit 1
    fi
    
    if [[ (-n $action) && ($action == "mount") && (-z $image_id || -z $image_root || -z $image_image) ]]; then
        echo "usage: image_action.sh action=mount image-id=<image_id> image-root=<image_root> image-image=<image_image>"
        echo "usage example: image_action.sh action=mount image-id=12345678 image-root=/opt/lxc image-image=/opt/lxc/precise-server-cloudimg-amd64.img"
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

    if [ $key = "image-id" ]; then
        image_id="$value"
        echo "image id:" $image_id
    fi

    if [ $key = "image-user" ]; then
        image_user="$value"
        echo "user:" $image_user
    fi
    
    if [ $key = "image-password" ]; then
        image_password="$value"
    fi
    
    if [ $key = "ip1" ]; then
        ip1="$value"
        echo "ip1:" $ip1
    fi
    
    if [ $key = "ip2" ]; then
        ip2="$value"
        echo "ip2:" $ip2
    fi
    
    if [ $key = "image-root" ]; then
        image_root="$value"
        echo "image root:" $image_root
    fi
    
    if [ $key = "image-keys-file" ]; then
        image_keys_file="$value"
        echo "keys file:" $image_keys_file
    fi
    
    if [ $key = "template" ]; then
        image_template="$value"
        echo "template:" $image_template
    fi
    
    if [ $key = "image-image" ]; then
        image_image="$value"
        echo "image-image:" $image_image
    fi

done

image_validate
work_dir="$image_root/$image_id"
if [[ (-n $action) && ($action == "unmount") ]]; then
    echo "# unmount $image_id"
    if [ -d $work_dir/$image_id ]; then
        echo 'this action unmount image!'
        umount $work_dir/$image_id/dev
        umount $work_dir/$image_id/proc
        umount $work_dir/$image_id/sys
        umount $work_dir/$image_id
        losetup -d /dev/loop2
        #rm -rf $work_dir/$image_id
    fi
    #if [ -d "./$image_id" ]; then
    #    rm -rf ./$image_id
    #fi
    exit 0
fi

if [[ (-n $action) && ($action == "mount") ]]; then
    echo "# mount $image_id "
    if [ ! -d "$work_dir/$image_id" ]; then
        mkdir -p $work_dir/$image_id
    fi
    #cp -f $image_image $work_dir/
    losetup /dev/loop2 $image_image
    mount /dev/loop2 $work_dir/$image_id 
    mount -o bind /dev $work_dir/$image_id/dev
    mount -o bind /proc $work_dir/$image_id/proc
    mount -o bind /sys $work_dir/$image_id/sys
    exit 0
fi

#create image for the user
if [ -d "$image_id" ]; then
    echo "image setup directory already exists!!!"
else
    cp -rf ./image_setup_base "$image_id"
fi
cd "$image_id"

sed -i "s#temp_base#$work_dir#" ./lxc-ubuntu-x.conf
sed -i "s/temp_user/$image_user/" ./lxc-ubuntu-x.conf
sed -i "s/temp_password/$image_password/" ./lxc-ubuntu-x.conf
sed -i "s/temp_ip1/$ip1/" ./lxc-ubuntu-x.conf
sed -i "s#temp_keys#$image_keys_file#" ./lxc-ubuntu-x.conf

./lxc-ubuntu-x $image_id $work_dir/$image_id $image_template $ip1 $ip2
cd ..
echo
echo Release: $RELEASE
echo


exit

