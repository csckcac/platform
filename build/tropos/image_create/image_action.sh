#!/bin/bash

# Die on any error:
set -e

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
software=""

# Make sure the user is running as root.

if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi


function image_validate {
    if [ -z $action ]; then
        echo "usage: image_action.sh action=<mount|create|unmount> <options>"
        exit 1
    fi

    if [[ !($action == "mount") && !($action == "create") && !($action == "unmount") ]]; then
        echo "usage: image_action.sh action=<mount|create|unmount> <options>"
        exit 1
    fi

    if [[ (-n $action) && ($action == "create") && (-z $image_id || -z $image_root || -z $image_template ) ]]; then
        echo "usage: image_action.sh action=create image-id=<image id> image-root=<image_root> template=<template> "
        echo "usage example: image_action.sh action=create image-id=12345678 image-root=/opt/lxc template=lamp "
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
    
    if [ $key = "software" ]; then
        software="$value"
        echo "software:" $software
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
        if [ -d $work_dir/$image_id/dev ]; then
            umount -l $work_dir/$image_id
        fi
        losetup -d /dev/loop3
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
    losetup /dev/loop3 $image_image
    mount /dev/loop3 $work_dir/$image_id 
    mount -o bind /dev $work_dir/$image_id/dev
    mount -o bind /proc $work_dir/$image_id/proc
    mount -o bind /sys $work_dir/$image_id/sys
    exit 0
fi

# Where to create newly-created LXC container rootfs filesystems, fstabs, and confs:
export BASEDIR=$work_dir

# This user is added and given admin rights via sudo:
export INSTALL_USERNAME=$image_user

# This is the cleartext password for the above user.
export INSTALL_PASSWORD=$image_password

export LB_IP=$ip1

export HOST_MACHINE_IP=$ip2

# This file gets copied into /home/$INSTALL_USERNAME/.ssh/ in the new rootfs.
export AUTHORIZED_KEYS_TO_COPY=$image_keys_file

# This is a list of semi-colon separated files/directories that will be needed by templates.
export SOFTWARE="$software"

export HOST=$image_id
export ROOTFS=$work_dir/$image_id
export TEMPLATE=$image_template
export LB_IP=$ip1
export HM_IP=$ip2



if [ -z "$ROOTFS" ]; then
	ROOTFS="$BASEDIR/$HOST"
fi


if [ -z "$TEMPLATE" ]; then
	TEMPLATE="default"
fi

# to watch for the corner case that the $HOST and $TEMPLATE are the 
# same name (to prevent the new HOST from overwriting the TEMPLATE).
if [ "$HOST" == "$TEMPLATE" ]; then
	echo "ERROR: A new LXC host cannot be its own template; you have $TEMPLATE for both."
	exit 43
fi

./configure_software $HOST $ROOTFS $TEMPLATE $LB_IP $HM_IP
echo

exit

